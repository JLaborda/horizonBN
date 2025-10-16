package io.github.jlaborda.core.ges.threads;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import io.github.jlaborda.core.common.utils.Problem;
import io.github.jlaborda.core.common.utils.Utils;
import io.github.jlaborda.core.ges.framework.BackwardStage;
import io.github.jlaborda.core.ges.framework.ForwardStage;
import io.github.jlaborda.test.utils.Resources;

/**
 * Tests that the ThBES class works as expected.
 */
public class BESThreadTest {

    /**
     * Dataset created from the data file
     */
    final DataSet dataset = Resources.CANCER_DATASET;
    /**
     * Variable X-Ray
     */
    final Node xray = dataset.getVariable("Xray");
    /**
     * Variable Dysponea
     */
    final Node dyspnoea = dataset.getVariable("Dyspnoea");
    /**
     * Variabe Cancer
     */
    final Node cancer = dataset.getVariable("Cancer");
    /**
     * Variable Pollution
     */
    final Node pollution = dataset.getVariable("Pollution");
    /**
     * Variable Smoker
     */
    final Node smoker = dataset.getVariable("Smoker");

    /**
     * Subset1 of pairs of nodes or variables.
     */
    final Set<Edge> subset1 = new HashSet<>();
    /**
     * Subset2 of pairs of nodes or variables.
     */
    final Set<Edge> subset2 = new HashSet<>();

    Problem problem;


    /**
     * This method initializes the subsets, splitting the nodes in what is expected to happen when the seed is 42
     */
    public BESThreadTest(){
        //GESThread.setProblem(dataset);
        problem = new Problem(dataset);
        initializeSubsets();
    }

    @BeforeEach
    public void restartMeans(){
        BackwardStage.meanTimeTotal = 0;
        ForwardStage.meanTimeTotal = 0;
    }


    /**
     * This method initializes the subsets, splitting the nodes in what is expected to happen when the seed is 42
     */
    private void initializeSubsets(){
        // Seed used for arc split is 42

        // Subset 1:
        subset1.add(Edges.directedEdge(dyspnoea, cancer));
        subset1.add(Edges.directedEdge(cancer, dyspnoea));
        subset1.add(Edges.directedEdge(dyspnoea, smoker));
        subset1.add(Edges.directedEdge(smoker, dyspnoea));
        subset1.add(Edges.directedEdge(xray, pollution));
        subset1.add(Edges.directedEdge(pollution, xray));
        subset1.add(Edges.directedEdge(xray , cancer));
        subset1.add(Edges.directedEdge(cancer, xray));
        subset1.add(Edges.directedEdge(cancer, pollution));
        subset1.add(Edges.directedEdge(pollution, cancer));

        //Subset 2:
        subset2.add(Edges.directedEdge(pollution, smoker));
        subset2.add(Edges.directedEdge(smoker, pollution));
        subset2.add(Edges.directedEdge(cancer, smoker));
        subset2.add(Edges.directedEdge(smoker, cancer));
        subset2.add(Edges.directedEdge(dyspnoea, pollution));
        subset2.add(Edges.directedEdge(pollution, dyspnoea));
        subset2.add(Edges.directedEdge(xray, smoker));
        subset2.add(Edges.directedEdge(smoker, xray));
        subset2.add(Edges.directedEdge(xray, dyspnoea));
        subset2.add(Edges.directedEdge(dyspnoea, xray));

    }


    /**
     * Checks that the constructor works perfectly
     * @result  Both constructors create a ThBES object.
     * @throws InterruptedException Exception caused by thread interruption
     */
    @Test
    public void constructorTest() throws InterruptedException{
        // Arrange
        FESThread thread1 = new FESThread(problem, subset1, 15, false);
        thread1.run();
        Graph graph = thread1.getCurrentGraph();
        // Act
        BESThread thread2 = new BESThread(problem, graph, subset1);
        // Arrange
        assertNotNull(thread1);
        assertNotNull(thread2);
    }

    /**
     * Checks that the BES stage works as expected
     * @result All edges are in the expected result.
     * @throws InterruptedException Interruption caused by external forces.
     */
    @Test
    public void searchTwoThreadsTest() throws InterruptedException {
        //Arrange

        List<Node> nodes = Arrays.asList(cancer, xray, dyspnoea, pollution, smoker);
        Graph fusionGraph = new EdgeListGraph(nodes);
        fusionGraph.addDirectedEdge(cancer, dyspnoea);
        fusionGraph.addDirectedEdge(cancer, xray);
        fusionGraph.addDirectedEdge(pollution, cancer);
        fusionGraph.addDirectedEdge(smoker, cancer);
        fusionGraph.addDirectedEdge(xray, dyspnoea);
        fusionGraph.addDirectedEdge(pollution, smoker);

        
        BESThread thread1 = new BESThread(problem, fusionGraph, subset1);

/*
        List<Edge> expected = new ArrayList<>();
        expected.add(new Edge(cancer,xray,Endpoint.TAIL, Endpoint.ARROW));
        expected.add(new Edge(pollution,cancer,Endpoint.TAIL, Endpoint.ARROW));
        expected.add(new Edge(smoker,cancer,Endpoint.TAIL, Endpoint.ARROW));
        expected.add(new Edge(smoker,pollution,Endpoint.TAIL, Endpoint.ARROW));
        expected.add(new Edge(xray,dyspnoea,Endpoint.TAIL, Endpoint.ARROW));
*/
        // Act
        thread1.run();
        Graph g1 = thread1.getCurrentGraph();

        // Getting dag
        Dag gdag1 = Utils.removeInconsistencies(g1);

        //System.out.println("ThBES");
        //System.out.println(gdag1);

        double scoreFusionGraph = problem.scoreGraph(Utils.removeInconsistencies(fusionGraph));
        double scoreBES = problem.scoreGraph(gdag1);

        System.out.println("Score BES: " + scoreBES);
        System.out.println("Score Fusion Graph: " + scoreFusionGraph);


        // Asserting
       assertNotNull(gdag1);
       assertEquals(gdag1.getNodes().size(),5);
       assertTrue(scoreBES >= scoreFusionGraph);

    }








}
