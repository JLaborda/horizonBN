package io.github.jlaborda.core.ges.framework;

import es.uclm.i3a.simd.consensusBN.ConsensusUnion;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Graph;
import io.github.jlaborda.core.ges.threads.ForwardHillClimbingThread;
import io.github.jlaborda.core.ges.threads.GESThread;
import io.github.jlaborda.core.common.utils.Problem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FHCFusion extends FusionStage{

    public FHCFusion(Problem problem, Graph currentGraph, ArrayList<Dag> graphs) {
        super(problem, currentGraph, graphs);
    }

    @Override
    protected Dag fusion() throws InterruptedException {
        // Applying ConsensusUnion fusion
        ConsensusUnion fusion = new ConsensusUnion(this.graphs);
        Graph fusionGraph = fusion.union();

        // Getting Scores
        double fusionScore = GESThread.scoreGraph(fusionGraph, problem);
        double currentScore = GESThread.scoreGraph(this.currentGraph, problem);

        System.out.println("Fusion Score: " + fusionScore);
        System.out.println("Current Score: " + currentScore);



        // Checking if the score has improved
        if (fusionScore > currentScore) {
            this.currentGraph = fusionGraph;
            return (Dag) this.currentGraph;
        }

        System.out.println("FHC to obtain the fusion: ");


        Set<Edge> candidates = new HashSet<>();


        for (Edge e: fusionGraph.getEdges()){
            if(this.currentGraph.getEdge(e.getNode1(), e.getNode2())!=null || this.currentGraph.getEdge(e.getNode2(),e.getNode1())!=null ) continue;
            candidates.add(Edges.directedEdge(e.getNode1(),e.getNode2()));
            candidates.add(Edges.directedEdge(e.getNode2(),e.getNode1()));
        }


        //FESThread fuse = new FESThread(this.problem,this.currentGraph,candidates,candidates.size());
        ForwardHillClimbingThread fuse = new ForwardHillClimbingThread(problem, this.currentGraph, candidates, candidates.size());

        fuse.run();


        this.currentGraph = fuse.getCurrentGraph();
        System.out.println("Score Fusion: "+ ForwardHillClimbingThread.scoreGraph(this.currentGraph, problem));
        //this.currentGraph = Utils.removeInconsistencies(this.currentGraph);
        //System.out.println("Score Fusion sin inconsistencias: "+ ForwardHillClimbingThread.scoreGraph(this.currentGraph, problem));


        return new Dag(this.currentGraph);
    }
}
