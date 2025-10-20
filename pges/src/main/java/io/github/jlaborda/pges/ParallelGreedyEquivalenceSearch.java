package io.github.jlaborda.pges;

import java.io.IOException;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import io.github.jlaborda.core.common.utils.Problem;
import io.github.jlaborda.core.common.utils.Utils;
import io.github.jlaborda.core.ges.framework.BESFusion;
import io.github.jlaborda.core.ges.framework.BESStage;
import io.github.jlaborda.core.ges.framework.BNBuilder;
import io.github.jlaborda.core.ges.framework.FESFusion;
import io.github.jlaborda.core.ges.framework.FESStage;
import io.github.jlaborda.core.ges.threads.GESThread;
import io.github.jlaborda.pges.clustering.EdgeClustering;
import io.github.jlaborda.pges.clustering.HierarchicalClustering;

public class ParallelGreedyEquivalenceSearch extends BNBuilder {

    //private boolean fesFlag = false;
    //private boolean besFlag = false;
    
    private FESStage fesStage;
    private BESStage besStage;
    
    private final boolean speedUp;

    private final EdgeClustering clustering;

    public ParallelGreedyEquivalenceSearch(DataSet data, EdgeClustering clustering, int nThreads, int maxIterations, int nItInterleaving, boolean speedUp) {

        super(data, nThreads, maxIterations, nItInterleaving);
        this.clustering = clustering;
        this.clustering.setProblem(super.getProblem());
        this.speedUp = speedUp;
    }
    
    public ParallelGreedyEquivalenceSearch(Problem problem, EdgeClustering clustering, int nThreads, int maxIterations, int nItInterleaving, boolean speedUp) {

        super(problem.getData(), nThreads, maxIterations, nItInterleaving);
        this.clustering = clustering;
        this.clustering.setProblem(super.getProblem());
        this.speedUp = speedUp;
    }

    public ParallelGreedyEquivalenceSearch(String path, EdgeClustering clustering, int nThreads, int maxIterations, int nItInterleaving, boolean speedUp) throws IOException {
        super(path, nThreads, maxIterations, nItInterleaving);
        this.clustering = clustering;
        this.clustering.setProblem(super.getProblem());
        this.speedUp = speedUp;
    }

    public ParallelGreedyEquivalenceSearch(Graph initialGraph, String path, EdgeClustering clustering, int nThreads, int maxIterations, int nItInterleaving, boolean speedUp) throws IOException {
        super(initialGraph, path, nThreads, maxIterations, nItInterleaving);
        this.clustering = clustering;
        this.clustering.setProblem(super.getProblem());
        this.speedUp = speedUp;
    }

    public ParallelGreedyEquivalenceSearch(Graph initialGraph, DataSet data, EdgeClustering clustering, int nThreads, int maxIterations, int nItInterleaving, boolean speedUp) {
        super(initialGraph, data, nThreads, maxIterations, nItInterleaving);
        this.clustering = clustering;
        this.clustering.setProblem(super.getProblem());
        this.speedUp = speedUp;
    }

    @Override
    protected boolean convergence() {
        // Checking Iterations
        if (it >= this.maxIterations)
            return true;

        it++;
        System.out.println("\n\nIterations: " + it);

        // Checking working status
        /*if(!fesFlag && !besFlag){
            return true;
        }*/
        double currentScore = GESThread.scoreGraph(this.currentGraph, this.problem);

        System.out.println("Current: " + currentScore + ", prev: "+ prevScore);
        if(currentScore > prevScore){

            prevScore = currentScore;
            return false;
        }
        else{
            return true;
        }
    }

    @Override
    protected void initialConfig() {

    }

    @Override
    protected void repartition() {
        this.subSets = clustering.generateEdgeDistribution(nThreads);
    }

    @Override
    protected void forwardStage(){
        fesStage = new FESStage(problem, currentGraph,nThreads,nItInterleaving, subSets, speedUp);
        fesStage.run();
        graphs = fesStage.getGraphs();
    }

    @Override
    protected void forwardFusion() throws InterruptedException {
        FESFusion fesFusion = new FESFusion(problem, currentGraph, graphs);
        fesFusion.run();
        currentGraph = fesFusion.getCurrentGraph();
    }

    @Override
    protected void backwardStage(){
        besStage = new BESStage(problem, currentGraph, nThreads, nItInterleaving, subSets);
        besStage.run();
        graphs = besStage.getGraphs();
    }

    @Override
    protected void backwardFusion() throws InterruptedException {
        BESFusion besFusion = new BESFusion(problem, currentGraph, graphs, besStage);
        besFusion.run();
        currentGraph = besFusion.getCurrentGraph();
    }

    public static void main(String[] args) throws IOException{
        // 1. Read Data
        String path = "/Users/jdls/developer/projects/horizonBN/res/datasets/diabetes/diabetes1.csv";
        DataSet ds = Utils.readData(path);
        Problem problem = new Problem(ds);

        // 2. Configuring algorithm
        //public PGESwithStages(Problem problem, EdgeClustering clustering, int nThreads, int maxIterations, int nItInterleaving, boolean speedUp) {

        ParallelGreedyEquivalenceSearch pges= new ParallelGreedyEquivalenceSearch(problem, new HierarchicalClustering(problem), Runtime.getRuntime().availableProcessors(), 100, Integer.MAX_VALUE, false);

        // 3. Running Algorithm
        pges.search();

        // 4. Printing out the results
        System.out.println("Number of Iterations: " + pges.getIterations());
        System.out.println("Resulting Graph: " + pges.getCurrentGraph());
        System.out.println("Resulting Score: " + pges.getProblem().scoreGraph(pges.getCurrentGraph()));//GESThread.scoreGraph(pGESv2.getCurrentGraph(), pGESv2.getProblem()));

    }
 
}
