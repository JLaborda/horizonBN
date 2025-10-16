package io.github.jlaborda.core.ges.threads;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Node;

import java.util.Objects;
import java.util.Set;

public class EdgeSearch implements Comparable<EdgeSearch> {

    public double score;
    public Set<Node> hSubset;
    public Edge edge;

    public EdgeSearch(double score, Set<Node> hSubSet, Edge edge) {
        this.score = score;
        this.hSubset = hSubSet;
        this.edge = edge;
    }

    @Override
    public int compareTo(EdgeSearch o) {
        return Double.compare(this.score, (o).score);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof EdgeSearch) {
            EdgeSearch obj = (EdgeSearch) other;
            if (obj.edge.equals(this.edge)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.edge);
    }

    public double getScore() {
        return this.score;
    }

    public Set<Node> gethSubset() {
        return this.hSubset;
    }

    public Edge getEdge() {
        return this.edge;
    }
}
