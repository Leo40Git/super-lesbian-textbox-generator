/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.face;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class FaceSortHelper<T> {
    private final int size;
    private final Map<String, Entry> entryLookup;
    private int index;

    public FaceSortHelper(int size) {
        this.size = size;
        entryLookup = new HashMap<>(size);
        index = 0;
    }

    public void add(@NotNull String id, T data, @Nullable String before, @Nullable String after) {
        Entry thisEntry = entryLookup.get(id);
        if (thisEntry != null) {
            thisEntry.data = data;
            thisEntry.index = this.index++;
        } else {
            thisEntry = new Entry(data, index++);
            entryLookup.put(id, thisEntry);
        }

        if (before != null) {
            var prevEntry = entryLookup.get(before);
            if (prevEntry == null) {
                prevEntry = new Entry(null, -1);
                entryLookup.put(before, prevEntry);
            }
            prevEntry.link(thisEntry);
        }

        if (after != null) {
            var nextEntry = entryLookup.get(after);
            if (nextEntry == null) {
                nextEntry = new Entry(null, -1);
                entryLookup.put(after, nextEntry);
            }
            thisEntry.link(nextEntry);
        }
    }

    @Contract(" -> new")
    public @NotNull List<T> sort() {
        // FIRST KOSARAJU SCC VISIT
        var topoSort = new ArrayList<Entry>(size);
        for (var entry : entryLookup.values()) {
            forwardVisit(entry, null, topoSort);
        }
        clearStatus(topoSort);
        Collections.reverse(topoSort);

        // SECOND KOSARAJU SCC VISIT
        var entryToScc = new IdentityHashMap<Entry, EntryScc>();
        for (var entry : topoSort) {
            if (entry.visitStatus == VisitStatus.NOT_VISITED) {
                var sccEntries = new ArrayList<Entry>();
                backwardVisit(entry, sccEntries);
                sccEntries.sort(Comparator.comparing(e -> e.index));
                var scc = new EntryScc(sccEntries);
                for (var entryInScc : sccEntries) {
                    entryToScc.put(entryInScc, scc);
                }
            }
        }
        clearStatus(topoSort);

        // build SCC graph
        for (var scc : entryToScc.values()) {
            for (var entry : scc.entries) {
                if (entry.subsequentEntries != null) {
                    for (var subsequentEntry : entry.subsequentEntries) {
                        var subsequentScc = entryToScc.get(subsequentEntry);
                        if (scc != subsequentScc) {
                            scc.link(subsequentScc);
                            subsequentScc.inDegree++;
                        }
                    }
                }
            }
        }

        // order SCCs according to priorities
        // when there is a choice, use the SCC with the lowest index
        // the priority queue contains all SCCs that currently have 0 in-degree
        var pq = new PriorityQueue<EntryScc>(Comparator.comparing(scc -> scc.entries.get(0).index));
        var sorted = new ArrayList<T>(size);

        for (var scc : entryToScc.values()) {
            if (scc.inDegree == 0) {
                pq.add(scc);
                // prevent adding the same SCC multiple times
                scc.inDegree = -1;
            }
        }

        while (!pq.isEmpty()) {
            var scc = pq.poll();
            for (var entry : scc.entries) {
                if (entry.index < 0) {
                    // TODO warn about missing link
                    continue;
                }

                sorted.add(entry.data);
            }

            if (scc.subsequentSccs != null) {
                for (var subsequentScc : scc.subsequentSccs) {
                    subsequentScc.inDegree--;

                    if (subsequentScc.inDegree == 0) {
                        pq.add(subsequentScc);
                    }
                }
            }
        }

        return sorted;
    }

    private void forwardVisit(@NotNull Entry entry, @Nullable Entry parent, @NotNull List<Entry> topoSort) {
        if (entry.visitStatus == VisitStatus.NOT_VISITED) {
            entry.visitStatus = VisitStatus.VISITING;

            if (entry.subsequentEntries != null) {
                for (var next : entry.subsequentEntries) {
                    forwardVisit(next, entry, topoSort);
                }
            }
            topoSort.add(entry);

            entry.visitStatus = VisitStatus.VISITED;
        }

        // TODO if visitStatus is VISITING, warn about cycle
    }

    private void clearStatus(@NotNull List<Entry> entries) {
        for (var entry : entries) {
            entry.visitStatus = VisitStatus.NOT_VISITED;
        }
    }

    private void backwardVisit(@NotNull Entry entry, @NotNull List<Entry> sccPhases) {
        if (entry.visitStatus == VisitStatus.NOT_VISITED) {
            entry.visitStatus = VisitStatus.VISITING;

            sccPhases.add(entry);
            if (entry.previousEntries != null) {
                for (var prev : entry.previousEntries) {
                    backwardVisit(prev, sccPhases);
                }
            }

            //entry.visitStatus = VisitStatus.VISITED;
        }
    }

    private final class Entry {
        private T data;
        private int index;
        private @Nullable List<Entry> subsequentEntries, previousEntries;
        private @NotNull VisitStatus visitStatus;

        public Entry(T data, int index) {
            this.data = data;
            this.index = index;

            visitStatus = VisitStatus.NOT_VISITED;
        }

        public void link(@NotNull Entry subsequentEntry) {
            if (subsequentEntries == null) {
                subsequentEntries = new ArrayList<>();
            }

            if (subsequentEntry.previousEntries == null) {
                subsequentEntry.previousEntries = new ArrayList<>();
            }

            subsequentEntries.add(subsequentEntry);
            subsequentEntry.previousEntries.add(subsequentEntry);
        }
    }

    private enum VisitStatus {
        NOT_VISITED,
        VISITING,
        VISITED
    }

    private final class EntryScc {
        private final @NotNull List<Entry> entries;
        private @Nullable List<EntryScc> subsequentSccs;
        private int inDegree;

        public EntryScc(@NotNull List<Entry> entries) {
            this.entries = entries;
            inDegree = 0;
        }

        public void link(@NotNull EntryScc subsequentScc) {
            if (subsequentSccs == null) {
                subsequentSccs = new ArrayList<>();
            }

            subsequentSccs.add(subsequentScc);
        }
    }
}
