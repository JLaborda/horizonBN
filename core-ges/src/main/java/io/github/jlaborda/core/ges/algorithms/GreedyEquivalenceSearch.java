package io.github.jlaborda.core.ges.algorithms;

import java.io.IOException;
import java.util.Set;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import io.github.jlaborda.core.common.utils.Problem;
import io.github.jlaborda.core.common.utils.Utils;
import io.github.jlaborda.core.ges.framework.BNBuilder;
import io.github.jlaborda.core.ges.framework.BackwardStage;
import io.github.jlaborda.core.ges.framework.ForwardStage;
import io.github.jlaborda.core.ges.threads.BESThread;
import io.github.jlaborda.core.ges.threads.FESThread;

public class GreedyEquivalenceSearch extends BNBuilder {

    private boolean fesFlag = false;

    private boolean besFlag = false;
    
    private final boolean speedUp;

    public GreedyEquivalenceSearch(DataSet data, boolean speedUp) {
        super(data, 1, -1, -1);
        this.speedUp = speedUp;
    }

    public GreedyEquivalenceSearch(String path, boolean speedUp) throws IOException {
        super(path, 1, -1, -1);
        this.speedUp = speedUp;
    }

    public GreedyEquivalenceSearch(Graph initialDag, DataSet data, boolean speedUp) {
        super(initialDag, data, 1, -1,-1);
        this.speedUp = speedUp;
        this.currentGraph = new EdgeListGraph(initialDag);
    }

    public GreedyEquivalenceSearch(Graph initialDag, String path, boolean speedUp) throws IOException {
        super(initialDag, path, 1, -1,-1);
        this.speedUp = speedUp;
        this.currentGraph = new EdgeListGraph(initialDag);
    }

    public GreedyEquivalenceSearch(Graph initialDag, Problem problem, Set<Edge> subsetEdges, boolean speedUp) {
        super(initialDag, problem, 1, -1,-1);
        super.setOfArcs = subsetEdges;
        this.speedUp = speedUp;
    }

    @Override
    public boolean convergence() {
        // No changes in either fes or bes stages
        return !(fesFlag || besFlag);
    }

    @Override
    protected void initialConfig() {
    }

    @Override
    protected void repartition() {

    }

    @Override
    protected void forwardStage() throws InterruptedException {
        ForwardStage.meanTimeTotal = 0;
        FESThread fes = new FESThread(problem, super.getInitialGraph(), setOfArcs, Integer.MAX_VALUE, speedUp);
        fes.run();
        currentGraph = fes.getCurrentGraph();
        fesFlag = fes.getFlag();
        score = fes.getScoreBDeu();
    }

    @Override
    protected void forwardFusion() throws InterruptedException {

    }

    @Override
    protected void backwardStage() throws InterruptedException {
        BackwardStage.meanTimeTotal = 0;
        BESThread bes = new BESThread(problem, currentGraph, setOfArcs);
        bes.run();
        currentGraph = bes.getCurrentGraph();
        besFlag = bes.getFlag();
        score = bes.getScoreBDeu();
        currentGraph = Utils.removeInconsistencies(currentGraph);
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
