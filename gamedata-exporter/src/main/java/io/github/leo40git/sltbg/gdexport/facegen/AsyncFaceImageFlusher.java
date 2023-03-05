package io.github.leo40git.sltbg.gdexport.facegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;

import io.leo40git.sltbg.gamedata.Face;
import io.leo40git.sltbg.gamedata.io.FaceIO;
import io.leo40git.sltbg.gamedata.io.FaceIOException;
import io.leo40git.sltbg.swing.util.ImageUtils;
import io.leo40git.sltbg.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AsyncFaceImageFlusher {
    private final @NotNull Path destDir;
    private final @NotNull ImageTypeSpecifier imageType;
    private final @NotNull ConcurrentLinkedDeque<Face> pendingFaces;
    private final @NotNull ConcurrentLinkedQueue<Exception> exceptions;
    private final @NotNull AtomicBoolean doFinishingTouches;

    private @Nullable WorkerThread workerThread;

    public AsyncFaceImageFlusher(@NotNull Path destDir, @NotNull ImageTypeSpecifier imageType) {
        this.destDir = destDir;
        this.imageType = imageType;
        pendingFaces = new ConcurrentLinkedDeque<>();
        exceptions = new ConcurrentLinkedQueue<>();
        doFinishingTouches = new AtomicBoolean(false);
    }

    public void start() {
        if (isRunning()) {
            throw new IllegalStateException("Already started!");
        }

        pendingFaces.clear();
        exceptions.clear();
        doFinishingTouches.set(false);

        workerThread = new WorkerThread();
        workerThread.start();
    }

    public boolean queue(@NotNull Face face) {
        if (!isRunning()) {
            throw new IllegalStateException("Not running!");
        }

        if (!face.hasImage()) {
            throw new IllegalArgumentException("face doesn't have an image");
        }

        if (!imageType.equals(ImageTypeSpecifier.createFromRenderedImage(face.getImage()))) {
            throw new IllegalArgumentException("face's image has a different type than expected!");
        }

        if (!exceptions.isEmpty()) {
            return false;
        }

        pendingFaces.add(face);
        return true;
    }

    public boolean isRunning() {
        return workerThread != null && workerThread.isAlive();
    }

    public void abort() {
        if (workerThread == null) {
            return;
        }

        doFinishingTouches.set(false);
        workerThread.interrupt();
        try {
            workerThread.join();
        } catch (InterruptedException ignored) { }
        workerThread = null;

        pendingFaces.clear();
    }

    public void finish() {
        if (workerThread == null) {
            return;
        }

        doFinishingTouches.set(true);
        workerThread.interrupt();
        try {
            workerThread.join();
        } catch (InterruptedException ignored) { }
        workerThread = null;

        if (!pendingFaces.isEmpty()) {
            exceptions.add(new Exception("Failed to write all faces"));
        }
        pendingFaces.clear();
    }

    public @NotNull ConcurrentLinkedQueue<Exception> getExceptions() {
        return exceptions;
    }

    private final class WorkerThread extends Thread {
        private final @NotNull HashMap<String, ImageWriter> imageWritersBySuffix;

        public WorkerThread() {
            super("AsyncFaceImageFlusher");
            setDaemon(true);

            imageWritersBySuffix = new HashMap<>();
        }

        private void performLoop(@NotNull Path tempDir) {
            var face = pendingFaces.pollFirst();
            if (face == null) {
                return;
            }

            final var imagePath = tempDir.resolve(face.getImagePath());

            String fileSuffix = FileUtils.getFileSuffix(face.getImagePath());
            var writer = imageWritersBySuffix.get(fileSuffix);
            if (writer == null) {
                var it = ImageUtils.getImageWritersByTypeAndFileSuffix(imageType, fileSuffix);
                if (!it.hasNext()) {
                    var innerE = new IOException("Can't write image to \"" + imagePath + "\": couldn't find ImageWriter for file extension \"." + fileSuffix + "\"\n"
                            + "(or image type cannot be encoded with this extension)");
                    exceptions.add(new FaceIOException(face, "Failed to write face image to \"" + imagePath + "\"", innerE));
                    return;
                }
                writer = it.next();
                imageWritersBySuffix.put(fileSuffix, writer);
            }

            try {
                FaceIO.createImageDirectories(face, tempDir);
            } catch (FaceIOException e) {
                exceptions.add(e);
                return;
            }

            try (var os = Files.newOutputStream(imagePath);
                 var out = ImageIO.createImageOutputStream(os)) {
                writer.setOutput(out);
                writer.write(face.getImage());
                face.clearImage();
            } catch (IOException e) {
                exceptions.add(new FaceIOException(face, "Failed to write face image to \"" + imagePath + "\"", e));
            }
        }

        @Override
        public void run() {
            final Path tempDir;
            try {
                tempDir = Files.createTempDirectory(destDir.getRoot(),
                        destDir.getFileName().toString() + ".partial");
            } catch (IOException e) {
                exceptions.add(new IOException("Failed to create temporary partial directory", e));
                return;
            }

            while (!Thread.interrupted()) {
                performLoop(tempDir);
            }

            if (doFinishingTouches.get()) {
                while (!pendingFaces.isEmpty()) {
                    performLoop(tempDir);
                }

                if (exceptions.isEmpty()) {
                    try {
                        Files.move(tempDir, destDir);
                        return;
                    } catch (IOException e) {
                        exceptions.add(new IOException("Failed to rename temporary directory to destination directory", e));
                    }
                }
            }

            try {
                FileUtils.deleteDirectory(tempDir);
            } catch (IOException e) {
                exceptions.add(new IOException("Failed to remove temporary partial directory", e));
            }

            for (var writer : imageWritersBySuffix.values()) {
                try {
                    writer.dispose();
                } catch (Exception ignored) {}
            }
            imageWritersBySuffix.clear();
        }
    }
}
