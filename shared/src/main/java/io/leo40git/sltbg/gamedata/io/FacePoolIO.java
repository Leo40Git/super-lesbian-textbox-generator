/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.io;

import java.io.IOException;
import java.nio.file.Path;

import io.leo40git.sltbg.gamedata.FacePool;
import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.JsonWriteUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class FacePoolIO {
	private FacePoolIO() {
		throw new UnsupportedOperationException("FacePoolIO only contains static declarations.");
	}

	@Contract("_ -> new")
	public static @NotNull FacePool read(@NotNull JsonReader reader) throws IOException {
		var pool = new FacePool();
		JsonReadUtils.readSimpleMap(reader, FaceCategoryIO::read, pool::add);
		return pool;
	}

	public static void write(@NotNull JsonWriter writer, @NotNull FacePool pool, @NotNull Path rootDir) throws IOException {
		pool.sortIfNeeded();
		JsonWriteUtils.writeObject(writer, (writerx, value) -> FaceCategoryIO.write(writerx, value, rootDir), pool.getCategories().values());
	}
}
