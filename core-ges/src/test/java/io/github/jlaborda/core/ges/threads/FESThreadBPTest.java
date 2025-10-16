package io.github.jlaborda.core.ges.threads;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import io.github.jlaborda.core.common.utils.Problem;
import io.github.jlaborda.test.utils.Resources;


public class FESThreadBPTest {

    private static Problem problem;
    private static Set<Edge> subset;
    private static FESThreadBP fesThreadBP;

    @BeforeAll
    public static void setUp() throws IOException {
        // Mock or load a problem instance
        assertNotNull(Resources.CANCER_DATASET, "Cancer dataset should not be null");
        problem = new Problem(Resources.CANCER_DATASET);

        // Create a subset of edges for testing
        subset = new HashSet<>();
        Node cancerNode = problem.getNode("Cancer");
        Node xrayNode = problem.getNode("Xray");

        assertNotNull(cancerNode, "Cancer node should not be null");
        assertNotNull(xrayNode, "Xray node should not be null");
        subset.add(Edges.directedEdge(cancerNode, xrayNode));

        // Initialize FESThreadBP
        fesThreadBP = new FESThreadBP(problem, subset, 10, false);
        assertNotNull(fesThreadBP, "FESThreadBP could not be instantiated");
    }


    @Test
    void constructor_shouldInitializeObjectWithCorrectState() {
        assertEquals(10, fesThreadBP.getMaxIt());
        assertFalse(fesThreadBP.isAggressivelyPreventCycles());
    }

    @Test
    void run_shouldProduceValidGraph() throws InterruptedException {
        // Act
        fesThreadBP.run();
        Graph result = fesThreadBP.getCurrentGraph();

        // Assert
        assertNotNull(result, "El grafo resultante no debería ser nulo después de ejecutar run().");
        assertTrue(result.getNodes().containsAll(problem.getVariables()), "El grafo resultante debe contener todas las variables del problema.");
    }

    @Test
    void setAndGetMaxIt_shouldUpdateTheValue() {
        // Getting the original value to restore later
        int originalMaxIt = fesThreadBP.getMaxIt();
        
        // Act
        fesThreadBP.setMaxIt(20);
        
        // Assert
        assertEquals(20, fesThreadBP.getMaxIt());
        
        // Restore the original value
        fesThreadBP.setMaxIt(originalMaxIt);
    }

    @Test
    public void testEdgeEvaluation() throws Exception {
        fesThreadBP.run();
        Graph result = fesThreadBP.getCurrentGraph();
        assertNotNull(result);

        // Ensure edges in the subset are evaluated and potentially added
        for (Edge edge : subset) {
            assertTrue(result.getEdges().contains(edge) || result.getEdges().contains(edge.reverse()));
        }
    }

    @Test
    void setAndGetAggressivelyPreventCycles_shouldUpdateTheValue() {
        // Act
        fesThreadBP.setMeekPreventCycles(true);
        
        // Assert
        assertTrue(fesThreadBP.isAggressivelyPreventCycles());
        
        // Restore the original value
        fesThreadBP.setMeekPreventCycles(false);
    }
}
