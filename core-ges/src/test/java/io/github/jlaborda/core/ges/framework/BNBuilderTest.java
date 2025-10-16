package io.github.jlaborda.core.ges.framework;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Edge;
import io.github.jlaborda.core.common.utils.Problem;
import io.github.jlaborda.core.common.utils.Utils;
import io.github.jlaborda.core.ges.algorithms.HillClimbingSearch;
import io.github.jlaborda.test.utils.Resources;

public class BNBuilderTest {

    @BeforeEach
    public void restartMeans(){
        BackwardStage.meanTimeTotal = 0;
        ForwardStage.meanTimeTotal = 0;
    }

    @Test
    public void settersAndGettersTest() throws IOException{
        DataSet ds = Resources.CANCER_DATASET;
        BNBuilder algorithm = new HillClimbingSearch(ds, 15, 5);
        Problem problem = algorithm.getProblem();
        Set<Edge> arcs = Utils.calculateArcs(problem.getData());

        algorithm.setSeed(30);
        algorithm.setMaxIterations(30);
        algorithm.setnItInterleaving(20);


        assertEquals(30, algorithm.getSeed());
        assertEquals(arcs, algorithm.getSetOfArcs());
        assertTrue(algorithm.getSubSets().isEmpty());
        assertEquals(problem.getData(), algorithm.getData());
        assertEquals(30, algorithm.getMaxIterations());
        assertNull(algorithm.getGraphs());
        assertEquals(20,algorithm.getItInterleaving());
        assertNull(algorithm.getCurrentGraph());
        assertEquals(1, algorithm.getIterations());
        assertEquals(problem, algorithm.getProblem());
        assertEquals(0, algorithm.getnThreads());


    }
}
