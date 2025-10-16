package io.github.jlaborda.core.ges.algorithms;

import java.io.IOException;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import io.github.jlaborda.core.common.utils.Utils;
import io.github.jlaborda.core.ges.framework.BNBuilder;
import io.github.jlaborda.core.ges.threads.BackwardsHillClimbingThread;
import io.github.jlaborda.core.ges.threads.ForwardHillClimbingThread;

public class HillClimbingSearch extends BNBuilder {


    public HillClimbingSearch(String path, int maxIterations, int nItInterleaving) throws IOException {
        super(path, 0, maxIterations, nItInterleaving);
    }

    public HillClimbingSearch(DataSet data, int maxIterations, int nItInterleaving) {
        super(data, 0, maxIterations, nItInterleaving);
    }

    public HillClimbingSearch(DataSet data) {
        super(data, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public HillClimbingSearch(String path) throws IOException {
        super(path, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public HillClimbingSearch(Graph initialGraph, String path) throws IOException {
        super(initialGraph, path, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public HillClimbingSearch(Graph initialGraph, DataSet data){
        super(initialGraph, data, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }



    @Override
    protected boolean convergence() {
        return false;
    }

    @Override
    protected void initialConfig() {
    }

    @Override
    protected void repartition() {
    }

    @Override
    protected void forwardStage() throws InterruptedException {
        Graph g = getCurrentGraph();
        ForwardHillClimbingThread fhc;
        if (g == null){
            fhc = new ForwardHillClimbingThread(getProblem(), getSetOfArcs(), getItInterleaving());
        }
        else {
            fhc = new ForwardHillClimbingThread(getProblem(), getCurrentGraph(), getSetOfArcs(), getItInterleaving());
        }
        fhc.run();
        Graph graph = fhc.getCurrentGraph();
        currentGraph = Utils.removeInconsistencies(graph);
    }

    @Override
    protected void forwardFusion() throws InterruptedException {

    }

    @Override
    protected void backwardStage() throws InterruptedException {
        BackwardsHillClimbingThread bhc = new BackwardsHillClimbingThread(getProblem(), getCurrentGraph(), getSetOfArcs());
        bhc.run();
        Graph g = bhc.getCurrentGraph();
        currentGraph = Utils.removeInconsistencies(g);
    }

    @Override
    protected void backwardFusion() throws InterruptedException {

    }

    @Override
    public Graph search(){
        try {
            forwardStage();
            backwardStage();
        }catch(InterruptedException e){
            System.err.println("Interrupted Exception");
            System.out.println(e.getMessage());
        }
        return this.currentGraph;
    }
}
