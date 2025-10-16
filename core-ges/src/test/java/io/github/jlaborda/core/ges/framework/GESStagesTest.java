package io.github.jlaborda.core.ges.framework;

import java.io.IOException;
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
import edu.cmu.tetrad.graph.Graph;
import io.github.jlaborda.core.common.utils.Problem;
import io.github.jlaborda.core.common.utils.Utils;
import io.github.jlaborda.core.ges.threads.GESThread;
import io.github.jlaborda.test.utils.Resources;


public class GESStagesTest {

    @BeforeEach
    public void restartMeans(){
        BackwardStage.meanTimeTotal = 0;
        ForwardStage.meanTimeTotal = 0;
    }

    @Test
    public void runTest() throws InterruptedException, IOException{
        //Arrange
        DataSet ds = Resources.CANCER_DATASET;
        Problem problem = new Problem(ds);
        int nThreads = 2;
        int itInterleaving = 5;
        List<Set<Edge>> subsets = Utils.split(Utils.calculateArcs(problem.getData()), nThreads);
        FESStage fesStage = new FESStage(problem, nThreads, itInterleaving, subsets, false);

        // TESTING FESStage
        // Act
        fesStage.run();
        double fesStageScore = (GESThread.scoreGraph(fesStage.getGraphs().get(0), problem) + GESThread.scoreGraph(fesStage.getGraphs().get(1), problem)) / 2;
        //Assert
        //assertTrue(flag);
        assertEquals(nThreads, fesStage.getGraphs().size());
        assertNotNull(fesStage.getGraphs().get(0));
        assertNotNull(fesStage.getGraphs().get(1));

        //TESTING FESFusion
        Stage fesFusion = new FESFusion(problem, fesStage.getCurrentGraph(), fesStage.getGraphs());
        fesFusion.run();
        Graph g = fesFusion.getCurrentGraph();
        double fesFusionScore = GESThread.scoreGraph(g, problem);

        //assertTrue(flag);
        assertNotNull(g);
        assertTrue(g instanceof Dag);
        assertEquals(g.getNumNodes(), problem.getVariables().size());
        assertTrue(fesFusionScore >= fesStageScore);

        //TESTING BESStage
        BESStage besStage = new BESStage(problem,
                g,
                nThreads,
                itInterleaving,
                subsets
        );
        // No edge is deleted
        besStage.run();
        double besStageScore = (GESThread.scoreGraph(besStage.getGraphs().get(0), problem) + GESThread.scoreGraph(besStage.getGraphs().get(1), problem)) / 2;

        //assertFalse(flag);
        assertEquals(nThreads, besStage.getGraphs().size());
        assertNotNull(besStage.getGraphs().get(0));
        assertNotNull(besStage.getGraphs().get(1));
        assertTrue(besStageScore >= fesFusionScore);

        //TESTING BESFusion
        Stage besFusion = new BESFusion(problem, besStage.getCurrentGraph(), besStage.getGraphs(), besStage);
        besFusion.run();
        Graph g2 = besFusion.getCurrentGraph();
        double besFusionScore = GESThread.scoreGraph(g2, problem);

        //assertTrue(flag);
        assertNotNull(g2);
        assertTrue(g2 instanceof Dag);
        assertEquals(g2.getNumNodes(), problem.getVariables().size());
        assertTrue(besFusionScore >= besStageScore);

        //SECOND ITERATION
        Stage fesStage2 = new FESStage(problem,
                g2,
                nThreads,
                itInterleaving,
                subsets, false);
        fesStage2.run();
        double fesStageScore2 = (GESThread.scoreGraph(fesStage2.getGraphs().get(0), problem) + GESThread.scoreGraph(fesStage2.getGraphs().get(1), problem)) / 2;
        //Assert
        // No new edges added
        //assertFalse(flag);
        assertEquals(nThreads, fesStage2.getGraphs().size());
        assertNotNull(fesStage2.getGraphs().get(0));
        assertNotNull(fesStage2.getGraphs().get(1));
        assertTrue(fesStageScore2 >= besFusionScore);


    }

}
