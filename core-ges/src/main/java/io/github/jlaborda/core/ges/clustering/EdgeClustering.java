package io.github.jlaborda.core.ges.clustering;

import java.util.List;
import java.util.Set;

import edu.cmu.tetrad.graph.Edge;
import io.github.jlaborda.core.common.utils.Problem;

public interface EdgeClustering {
    public List<Set<Edge>> generateEdgeDistribution(int numClusters);
    public void setProblem(Problem problem);
}
