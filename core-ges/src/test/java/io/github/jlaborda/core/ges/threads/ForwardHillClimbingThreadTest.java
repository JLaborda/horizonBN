package io.github.jlaborda.core.ges.threads;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import io.github.jlaborda.core.common.utils.Problem;
import io.github.jlaborda.core.common.utils.Utils;
import io.github.jlaborda.core.ges.framework.BackwardStage;
import io.github.jlaborda.core.ges.framework.ForwardStage;
import io.github.jlaborda.test.utils.Resources;



public class ForwardHillClimbingThreadTest {


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


    @BeforeEach
    public void restartMeans(){
        BackwardStage.meanTimeTotal = 0;
        ForwardStage.meanTimeTotal = 0;
    }


    /**
     * Constructor of the test. It initializes the subsets.
     */
    public ForwardHillClimbingThreadTest(){
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
        ForwardHillClimbingThread thread1 = new ForwardHillClimbingThread (problem, subset1, 15);
        thread1.run();
        Graph graph = thread1.getCurrentGraph();
        // Act
        ForwardHillClimbingThread thread2 = new ForwardHillClimbingThread(problem, graph, subset1, 15);
        // Arrange
        assertNotNull(thread1);
        assertNotNull(thread2);
    }

    /**
     * Checks the first iteration of the Cancer problem for the FES stage
     * @result Each expected node is in the resulting graph after executing the first iteration of FES stage
     * @throws InterruptedException Exception caused by thread interruption
     */
    @Test
    public void searchTwoThreadsTest() throws InterruptedException {

        // ThFES objects
        ForwardHillClimbingThread thread1 = new ForwardHillClimbingThread(problem, subset1, 15);
        ForwardHillClimbingThread thread2 = new ForwardHillClimbingThread(problem, subset2, 15);

        // Expectation
        List<Edge> expected1 = new ArrayList<>();
        expected1.add(new Edge(xray, cancer, Endpoint.TAIL, Endpoint.ARROW));

        List<Edge> expected2 = new ArrayList<>();
        expected2.add(new Edge(cancer, smoker, Endpoint.TAIL, Endpoint.ARROW));


        //Act
        thread1.run();
        thread2.run();
        Graph g1 = thread1.getCurrentGraph();
        Graph g2 = thread2.getCurrentGraph();

        // Getting dags
        Dag gdag1 = Utils.removeInconsistencies(g1);
        Dag gdag2 = Utils.removeInconsistencies(g2);

        assertTrue(!gdag1.getEdges().isEmpty());
        assertTrue(!gdag2.getEdges().isEmpty());

        for(Edge edge : expected1){
            assertTrue(gdag1.getEdges().contains(edge));
        }

        for(Edge edge : expected2){
            assertTrue(gdag2.getEdges().contains(edge));
        }

    }


    /**
     * Checking that FHC stops when there are no more edges to be added.
     * @result The number of iterations is less than the maximum iterations set
     */
    @Test
    public void noMoreEdgesToAddInFESTest(){

        // ThFES objects
        ForwardHillClimbingThread thread1 = new ForwardHillClimbingThread(problem, subset1, 1000);

        //Act
        thread1.run();

        //Assert
        assertNotEquals(thread1.getIterations(), 1000);
    }


    /**
     * Testing that fhc stops when the maximum number of edges is reached.
     * @result The resulting graph has the same number of edges as the set maximum number of edges.
     * @throws InterruptedException Caused by an external interruption.
     */
    @Test
    public void maximumNumberOfEdgesReachedTest() throws InterruptedException {
        // ThFES objects
        FESThread thread1 = new FESThread(problem, subset1, 1000, false);
        thread1.setMaxNumEdges(2);

        //Act
        thread1.run();
        Graph result = thread1.getCurrentGraph();
        //Assert
        assertTrue(result.getEdges().size()<=2);

    }

    /**
     * Tests that the algorithm works correct with the Alarm network.
     *
     * @throws InterruptedException Caused by an external interruption.
     * @throws IOException 
     * @result The resulting graph has the same number of edges as the set maximum number of edges.
     */
    @Test
    public void cancerExecutionTest() throws InterruptedException, IOException {
        // ThFES objects
        DataSet cancerDataSet = Resources.CANCER_DATASET;
        Set<Edge> setOfArcs = Utils.calculateArcs(cancerDataSet);
        Utils.setSeed(42);
        List<Set<Edge>> subsets = Utils.split(setOfArcs, 2);
        Set<Edge> subsetAux1 = subsets.get(0);
        Set<Edge> subsetAux2 = subsets.get(1);

        Problem pCancer = new Problem(cancerDataSet);
        ForwardHillClimbingThread thread1 = new ForwardHillClimbingThread(pCancer, subsetAux1, 100);
        ForwardHillClimbingThread thread2 = new ForwardHillClimbingThread(pCancer, subsetAux2, 100);


        //Act
        thread1.run();
        thread2.run();
        Graph result1 = thread1.getCurrentGraph();
        Graph result2 = thread2.getCurrentGraph();
        //Assert
        assertNotNull(result1);
        assertNotNull(result2);

    }



}
