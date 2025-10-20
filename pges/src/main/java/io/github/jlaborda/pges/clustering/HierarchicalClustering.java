package io.github.jlaborda.pges.clustering;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Node;
import io.github.jlaborda.core.common.utils.Problem;

public class HierarchicalClustering extends HierarchicalNodeClustering implements EdgeClustering{

    //private final Object lock = new Object();
    //private Set<Edge> allEdges;
    //private double[][] simMatrix;
    //private boolean isParallel = false;
    //private boolean isJoint = false;
    //private final Map<Edge, Double> edgeScores = new ConcurrentHashMap<>();
    //private List<Set<Node>> clusters;
    //private final Map<Node, Set<Integer>> nodeClusterMap = new HashMap<>();

    public HierarchicalClustering(){

    }

    public HierarchicalClustering(Problem problem) {
        super(problem);
    }


    public HierarchicalClustering(Problem problem, boolean isParallel) {
        this(problem);
        this.isParallel = isParallel;
    }
    public HierarchicalClustering(boolean isParallel, boolean isJoint) {
        this.isParallel = isParallel;
        this.isJoint = isJoint;
    }
    
    @Override
    public List<Set<Edge>> generateEdgeDistribution(int numClusters) {
        // Generating node clusters
        if(super.clusters == null) {
            generateNodeClusters(numClusters);
        }
        // Generating edge distribution
        System.out.println("Generating edge distribution");
        List<Set<Edge>> edgeDistribution = clusters.stream()
            .map(cluster -> new HashSet<Edge>())
            .collect(Collectors.toList());

        // Generating the Inner and Outer edges
        System.out.println("Generating inner and outer edges");
        allEdges.forEach(edge -> {
            Node node1 = edge.getNode1();
            Node node2 = edge.getNode2();

            // Both sets indicate where each node is located in each cluster.
            Set<Integer> clusterIndexes1 = nodeClusterMap.get(node1);
            Set<Integer> clusterIndexes2 = nodeClusterMap.get(node2);

            // To calculate the inner edges, we need to check where the nodes repeat themselves in the clusters by means of an intersection.
            Set<Integer> innerClusterIndexes = new HashSet<>(clusterIndexes1);
            innerClusterIndexes.retainAll(clusterIndexes2);
            // To calculate the outer edges, we need to check where the nodes don't repeat themselves in the clusters by means of a difference.
            Set<Integer> outerClusterIndexes = new HashSet<>(clusterIndexes1);
            outerClusterIndexes.addAll(clusterIndexes2);
            outerClusterIndexes.removeAll(innerClusterIndexes);

            // Adding the edge to each cluster in the inner-cluster indexes to create inner edges:
            for (Integer innerClusterIndex : innerClusterIndexes) {
                edgeDistribution.get(innerClusterIndex).add(edge);
            }

            // This adds the edge as an outer edge to only one cluster.
            // If the edge is an outer edge, now we add it to the smallest edgeDistribution cluster.
            if(!outerClusterIndexes.isEmpty()) {
                int minSize = Integer.MAX_VALUE;
                int minIndex = -1;
                for (Integer outerClusterIndex : outerClusterIndexes) {
                    if (edgeDistribution.get(outerClusterIndex).size() < minSize) {
                        minSize = edgeDistribution.get(outerClusterIndex).size();
                        minIndex = outerClusterIndex;
                    }
                }
                edgeDistribution.get(minIndex).add(edge);
            }

        });
        System.out.println("Finished generating edge distribution");

        return edgeDistribution;
    }


}
