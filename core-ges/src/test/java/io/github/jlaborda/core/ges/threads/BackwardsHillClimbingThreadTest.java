package io.github.jlaborda.core.ges.threads;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import io.github.jlaborda.core.common.utils.Problem;
import io.github.jlaborda.test.utils.Resources;

public class BackwardsHillClimbingThreadTest {


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

    private final Problem problem;


    /**
     * Constructor of the test. It initializes the subsets.
     */
    public BackwardsHillClimbingThreadTest(){
        problem = new Problem(dataset);
        initializeSubsets();
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


    @Test
    public void constructorTest() throws InterruptedException{
        // Arrange
        BackwardsHillClimbingThread thread1 = new BackwardsHillClimbingThread(problem, subset1);
        thread1.run();
        Graph graph = thread1.getCurrentGraph();
        // Act
        BackwardsHillClimbingThread thread2 = new BackwardsHillClimbingThread(problem, graph, subset1);
        // Arrange
        assertNotNull(thread1);
        assertNotNull(thread2);
    }

    /**
     * Checks the first iteration of the Cancer problem for the BHC stage
     * @result Each expected node is in the resulting graph after executing the first iteration of FES stage
     * @throws InterruptedException Exception caused by thread interruption
     */
    // @Test
    // public void searchTwoThreadsTest() throws InterruptedException {

    //     List<Node> nodes = Arrays.asList(cancer, xray, dyspnoea, pollution, smoker);
    //     Graph fusionGraph = new EdgeListGraph(nodes);
    //     fusionGraph.addDirectedEdge(cancer, dyspnoea);
    //     fusionGraph.addDirectedEdge(cancer, xray);
    //     fusionGraph.addDirectedEdge(pollution, cancer);
    //     fusionGraph.addDirectedEdge(smoker, cancer);
    //     fusionGraph.addDirectedEdge(xray, dyspnoea);
    //     fusionGraph.addDirectedEdge(pollution, smoker);


    //     //System.out.println("Initial Graph");
    //     //System.out.println(fusionGraph);

    //     // Threads objects
    //     BackwardsHillClimbingThread thread1 = new BackwardsHillClimbingThread(problem, fusionGraph, subset1);

    //     // Expectation
    //     List<Edge> expected = new ArrayList<>();
    //     expected.add(new Edge(cancer, xray, Endpoint.TAIL, Endpoint.ARROW));
    //     expected.add(new Edge(pollution, cancer, Endpoint.TAIL, Endpoint.ARROW));
    //     expected.add(new Edge(pollution, smoker, Endpoint.TAIL, Endpoint.ARROW));
    //     expected.add(new Edge(smoker, cancer, Endpoint.TAIL, Endpoint.ARROW));
    //     expected.add(new Edge(xray, dyspnoea, Endpoint.TAIL, Endpoint.ARROW));



    //     //Act
    //     thread1.run();
    //     Graph g1 = thread1.getCurrentGraph();

    //     System.out.println(g1);

    //     // Getting dags
    //     Dag gdag1 = Utils.removeInconsistencies(g1);


    //     for(Edge edge : expected){
    //         assertTrue(gdag1.getEdges().contains(edge));
    //     }


//  }






}
