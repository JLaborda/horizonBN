package io.github.jlaborda.core.ges.threads;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Node;
import java.util.Set;
import java.util.Objects;

public class EdgeSearchBP extends EdgeSearch{

    public EdgeSearchBP(double score, Set<Node> hSubSet, Edge edge) {
        super(score, hSubSet, edge);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof EdgeSearch) {
            EdgeSearch obj = (EdgeSearch) other;
            if (obj.edge.equals(this.edge) && obj.hSubset.equals(this.hSubset)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.edge, this.hSubset);
    }
}
