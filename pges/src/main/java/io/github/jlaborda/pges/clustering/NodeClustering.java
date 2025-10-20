package io.github.jlaborda.pges.clustering;

import java.util.List;
import java.util.Set;

import edu.cmu.tetrad.graph.Node;
import io.github.jlaborda.core.common.utils.Problem;

public abstract class NodeClustering {
    protected Problem problem;
    protected boolean isParallel = false;
    protected boolean isJoint = false;

    public NodeClustering(){

    }

    public NodeClustering(Problem problem){
        this.problem = problem;
    }

    //public abstract List<Set<Edge>> generateEdgeDistribution(int numClusters);
    public abstract List<Set<Node>> generateNodeClusters(int numClusters);

    public void setProblem(Problem problem) {
        this.problem = problem;
    }
}
