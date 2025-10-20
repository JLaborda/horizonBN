package io.github.jlaborda.pges.clustering;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Node;
import io.github.jlaborda.core.common.utils.Problem;
import io.github.jlaborda.core.common.utils.Utils;

public class RandomClustering extends NodeClustering implements EdgeClustering{

    public RandomClustering(Problem problem){
        super(problem);
        Utils.setSeed(42);
    }

    public RandomClustering(Problem problem,long seed){
        this(problem);
        Utils.setSeed(seed);
    }

    public RandomClustering(long seed){
        super();
        Utils.setSeed(seed);
    }

    public RandomClustering(){
        super();
        Utils.setSeed(42);
    }

    @Override
    public List<Set<Edge>> generateEdgeDistribution(int numClusters) {
        return Utils.split(Utils.calculateArcs(problem.getData()), numClusters);
    }

    @Override
    public List<Set<Node>> generateNodeClusters(int numClusters) {
        // 1. Get variables from problem
        Set<Node> nodes = new HashSet<>(problem.getVariables());
        // 2. Split the variables into numClusters
        return Utils.split(nodes, numClusters);   
    }
    
}
