package io.github.jlaborda.pges;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Node;
import io.github.jlaborda.core.common.utils.Utils;
import io.github.jlaborda.core.ges.clustering.EdgeClustering;
import io.github.jlaborda.core.ges.clustering.RandomClustering;
import io.github.jlaborda.core.ges.framework.BackwardStage;
import io.github.jlaborda.core.ges.framework.ForwardStage;
import io.github.jlaborda.test.utils.Resources;



public class ParallelGreedyEquivalenceSearchTest {

    DataSet dataSet = Resources.CANCER_DATASET; 
    String path = Resources.CANCER_DATASET_PATH;
    EdgeClustering clustering = new RandomClustering();

    @BeforeEach
    public void restartMeans(){
        BackwardStage.meanTimeTotal = 0;
        ForwardStage.meanTimeTotal = 0;
    }


    @Test
    public void testConstructor() throws IOException{
        ParallelGreedyEquivalenceSearch alg1 = new ParallelGreedyEquivalenceSearch(dataSet, clustering, 2, 100, 5, false);
        ParallelGreedyEquivalenceSearch alg2 = new ParallelGreedyEquivalenceSearch(path,clustering, 2, 100, 5, false);

        List<Node> nodes = Arrays.asList(Resources.CANCER, Resources.DYSPNOEA, Resources.POLLUTION, Resources.XRAY, Resources.SMOKER);
        Dag initialGraph = new Dag(nodes);
        initialGraph.addDirectedEdge(Resources.CANCER, Resources.DYSPNOEA);
        initialGraph.addDirectedEdge(Resources.CANCER, Resources.XRAY);

        ParallelGreedyEquivalenceSearch alg3 = new ParallelGreedyEquivalenceSearch(initialGraph, path, clustering, 2, 100, 5, false);
        ParallelGreedyEquivalenceSearch alg4 = new ParallelGreedyEquivalenceSearch(initialGraph, dataSet, clustering, 2, 100, 5, false);


        assertNotNull(alg1);
        assertNotNull(alg2);
        assertNotNull(alg3);
        assertNotNull(alg4);

        assertEquals(2, alg1.getnThreads());
        assertEquals(2, alg2.getnThreads());
        assertEquals(2, alg3.getnThreads());
        assertEquals(2, alg4.getnThreads());
        assertEquals(100, alg1.getMaxIterations());
        assertEquals(100, alg2.getMaxIterations());
        assertEquals(100, alg3.getMaxIterations());
        assertEquals(100, alg4.getMaxIterations());
        assertEquals(5, alg1.getItInterleaving());
        assertEquals(5, alg2.getItInterleaving());
        assertEquals(5, alg3.getItInterleaving());
        assertEquals(5, alg4.getItInterleaving());
        assertNull(alg1.getCurrentGraph());
        assertNull(alg2.getCurrentGraph());
        assertNotNull(alg3.getCurrentGraph());
        assertNotNull(alg4.getCurrentGraph());

    }

    @Test
    public void searchTest(){
        System.out.println("ParallelGreedyEquivalenceSearchTest: searchTest");
        ParallelGreedyEquivalenceSearch alg1 = new ParallelGreedyEquivalenceSearch(Resources.CANCER_DATASET, clustering, 2, 100, 5, false);
        Utils.setSeed(42);
        List<Node> nodes = new ArrayList<>();
        nodes.add(Resources.CANCER);
        nodes.add(Resources.DYSPNOEA);
        nodes.add(Resources.XRAY);
        nodes.add(Resources.POLLUTION);
        nodes.add(Resources.SMOKER);
        Dag expected = new Dag(nodes);
        expected.addDirectedEdge(Resources.CANCER, Resources.DYSPNOEA);
        expected.addDirectedEdge(Resources.CANCER, Resources.XRAY);
        expected.addDirectedEdge(Resources.CANCER, Resources.POLLUTION);
        expected.addDirectedEdge(Resources.SMOKER, Resources.CANCER);

        alg1.search();

        System.out.println((alg1.getCurrentGraph()));

        assertNotNull(alg1.getCurrentGraph());
        assertTrue(alg1.getCurrentGraph() instanceof Dag);

    }


    @Test
    public void convergenceTest() throws IOException{
        ParallelGreedyEquivalenceSearch alg = new ParallelGreedyEquivalenceSearch(dataSet,
                clustering,
                2,
                1,
                5, false
        );
        alg.search();

        assertNotNull(alg.getCurrentGraph());
        assertTrue(alg.getCurrentGraph() instanceof Dag);
        assertEquals(1,alg.getIterations());

    }

    @Test
    public void testSearchWithInitialGraph() throws IOException {
        List<Node> nodes = Arrays.asList(Resources.XRAY, Resources.DYSPNOEA, Resources.CANCER, Resources.POLLUTION, Resources.SMOKER);
        Dag initialGraph = new Dag(nodes);
        initialGraph.addDirectedEdge(Resources.CANCER, Resources.DYSPNOEA);
        initialGraph.addDirectedEdge(Resources.CANCER, Resources.XRAY);

        EdgeClustering clusteringTest = new RandomClustering(42);
        ParallelGreedyEquivalenceSearch pges = new ParallelGreedyEquivalenceSearch(initialGraph, path, clusteringTest, 2, 100, 5, false);

        Dag result = pges.getCurrentDag();
        // Equals is never gonna work. Because tetrad doesn't have a proper equals
        assertEquals(initialGraph.getNodes(), result.getNodes());

        //Asserting that the edges from the initial Graph inserted and the current graph are the same
        for (Edge edgeInitial : initialGraph.getEdges()) {
            Node x = edgeInitial.getNode1();
            Node y = edgeInitial.getNode2();
            boolean found = false;
            for (Edge edgeCurrent : result.getEdges()) {
                if (edgeCurrent.getNode1().equals(x) && edgeCurrent.getNode2().equals(y)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        pges.search();

        //Asserting that there is a resulting graph from pges.
        assertNotNull(pges.getCurrentGraph());
        assertNotNull(pges.getCurrentDag());

        //Asserting that there has been a modification in the graph by using the initial graph in pges.
        assertNotEquals(initialGraph, pges.getCurrentDag());

    }


}
