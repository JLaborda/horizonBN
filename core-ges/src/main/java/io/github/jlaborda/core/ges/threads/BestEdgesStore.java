package io.github.jlaborda.core.ges.threads;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadLocalRandom;

public class BestEdgesStore {
    private static final int NUMBER_SELECTION = 10; // Número de mejores elementos a mantener
    private final ConcurrentSkipListSet<EdgeSearch> bestEdges;

    public BestEdgesStore() {
        this.bestEdges = new ConcurrentSkipListSet<>(Comparator.reverseOrder()); // Orden inverso, mejor score primero
    }

    public synchronized void addEdge(EdgeSearch edge) {
        bestEdges.add(edge);
        if (bestEdges.size() > NUMBER_SELECTION) {
            bestEdges.pollLast(); // Elimina el peor elemento (último en orden inverso)
        }
    }

    public EdgeSearch getRandomBestEdge() {
        if (bestEdges.isEmpty()) return null;
        int index = ThreadLocalRandom.current().nextInt(bestEdges.size());
        return bestEdges.stream().skip(index).findFirst().orElse(null);
    }

    public EdgeSearch getBestEdgeSearch() {
        return bestEdges.first();
    }

    public List<EdgeSearch> getBestEdges() {
        return new ArrayList<>(bestEdges);
    }

    public void clear(){
        this.bestEdges.clear();
    }
}

