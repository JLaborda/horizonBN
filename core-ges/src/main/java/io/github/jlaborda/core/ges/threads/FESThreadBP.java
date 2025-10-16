package io.github.jlaborda.core.ges.threads;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.jlaborda.core.common.utils.Problem;
import static io.github.jlaborda.core.common.utils.Utils.pdagToDag;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.utils.GraphSearchUtils;
import edu.cmu.tetrad.search.utils.MeekRules;

@SuppressWarnings("DuplicatedCode")
public class FESThreadBP extends GESThread {

    private static int threadCounter = 1;
    
    private final boolean speedUp;

    private BestEdgesStore bestEdgesStore = new BestEdgesStore();

    private enum SelectionMode {
        RANDOM, GREEDY
    }
    public final static SelectionMode selectionMode = SelectionMode.RANDOM;

    /**
     * Constructor of FESThread with an initial DAG
     *
     * @param problem object containing all the information of the problem
     * @param initialDag initial DAG with which the FES stage starts with, if
     * it's null, use the other constructor.
     * @param subset subset of edges the fes stage will try to add to the
     * resulting graph
     * @param maxIt maximum number of iterations allowed in the fes stage
     * @param speedUp
     */
    public FESThreadBP(Problem problem, Graph initialDag, Set<Edge> subset, int maxIt, boolean speedUp) {
        this(problem, subset, maxIt, speedUp);
        this.initialDag = initialDag;
    }

    /**
     * Constructor of FESThread with an initial DataSet 
     *
     * @param problem object containing information of the problem such as data
     * or variables.
     * @param subset subset of edges the fes stage will try to add to the
     * resulting graph
     * @param maxIt maximum number of iterations allowed in the fes stage
     * @param speedUp
     */
    public FESThreadBP(Problem problem, Set<Edge> subset, int maxIt, boolean speedUp) {
        this.problem = problem;
        this.initialDag = new EdgeListGraph(new LinkedList<>(getVariables()));
        setSubSetSearch(subset);
        setMaxIt(maxIt);
        this.id = threadCounter;
        threadCounter++;
        this.isForwards = true;
        this.speedUp = speedUp;
    }

    //==========================PUBLIC METHODS==========================//
    @Override
    /*
      Run method from {@link Thread Thread} interface. The method executes the {@link #search()} search} method to add
      edges to the initial graph.
     */
    public void run() {
        this.currentGraph = search();
        pdagToDag(this.currentGraph);
    }

    //===========================PRIVATE METHODS========================//
    /**
     * Greedy equivalence search: Start from the empty graph, add edges till
     * model is significant. Then start deleting edges till a minimum is
     * achieved.
     *
     * @return the resulting Pattern.
     */
    private Graph search() {
        if (!S.isEmpty()) {
            startTime = System.currentTimeMillis();
            numTotalCalls = 0;
            numNonCachedCalls = 0;
            //localScoreCache.clear();

            Graph graph = new EdgeListGraph(this.initialDag);
            //buildIndexing(graph);

            // Method 1-- original.
            double scoreInitial = problem.scoreGraph(graph);

            // Do forward search.
            fes(graph, scoreInitial);

            long endTime = System.currentTimeMillis();
            this.elapsedTime = endTime - startTime;

            double newScore = problem.scoreGraph(graph);
            
            // UNCOMMENT THIS TO PRINT THE SCORE
            //System.out.println(" [" + getId() + "] FES New Score: " + newScore + ", Initial Score: " + scoreInitial);
            
            
            // If we improve the score, return the new graph
            if (newScore > scoreInitial) {
                this.modelBDeu = newScore;
                this.flag = true;
                return graph;
            } else {
                //System.out.println("   [" + getId() + "] ELSE");
                this.modelBDeu = scoreInitial;
                this.flag = false;
                return this.initialDag;
            }
        }
        return this.initialDag;
    }

    /**
     * Forward equivalence search.
     *
     * @param graph The graph in the state prior to the forward equivalence
     * search.
     * @param score The score in the state prior to the forward equivalence
     * search
     * @return the score in the state after the FES method. Note that the graph
     * is changed as a side-effect to its state after the forward equivalence
     * search.
     */
    private double fes(Graph graph, double score) {

        // Defining initial variables
        double bestScore = score;
        double bestInsert;
        iterations = 0;
        edgesCandidates = S;
        bestInsert = fs(graph);
        while ((x_i != null) && (iterations < this.maxIt)) {
            // Changing best score because x_i, and therefore, y_i is not null
            bestScore = bestInsert;

            // Inserting edge
            //System.out.println("Thread " + getId() + " inserting: (" + x_i + ", " + y_i + ", " + t_0 + "), score: " + bestScore);
            insert(x_i, y_i, t_0, graph);

            // Checking cycles?
            //boolean cycles = graph.existsDirectedCycle();

            //PDAGtoCPDAG
            //rebuildPattern(graph);
            updateEdges(graph);
            
            // Printing score
            /*if (!t_0.isEmpty()) {
                System.out.println("[" + getId() + "] Score: " + nf.format(bestScore) + " (+" + nf.format(bestInsert - score) + ")\tOperator: " + graph.getEdge(x_i, y_i) + " " + t_0);
            } else {
                System.out.println("[" + getId() + "] Score: " + nf.format(bestScore) + " (+" + nf.format(bestInsert - score) + ")\tOperator: " + graph.getEdge(x_i, y_i));
            }*/
            bestScore = bestInsert;

            // Checking that the maximum number of edges has not been reached
            if (getMaxNumEdges() != -1 && graph.getNumEdges() >= getMaxNumEdges()) {
                //System.out.println("Maximum edges reached");
                break;
            }

            // Executing FS function to calculate the best edge to be added
            bestInsert = fs(graph);

            // Indicating that the thread has added an edge to the graph
            this.flag = true;
            iterations++;
        }
        return bestScore;

    }

    /**
     * Forward search. Finds the best possible edge to be added into the current
     * graph and returns its score.
     *
     * @param graph The graph in the state prior to the forward equivalence
     * search.
     * @return the score in the state after the forward equivalence search. Note
     * that the graph is changed as a side-effect to its state after the forward
     * equivalence search.
     */
    private double fs(Graph graph) {
        // Initial Setup
        //PowerSetFabric.setMode(PowerSetFabric.MODE_FES);
        x_i = y_i = null;
        t_0 = null;
        
        // Calculating best edges and storing them in bestEdgesStore
        edgesCandidates.parallelStream().forEach(e -> scoreEdge(graph, e));

        // Choosing best edge from the storage (Randomly or Greedy)
        EdgeSearch max = null;
        switch (FESThreadBP.selectionMode) {
            case GREEDY:
                max = this.bestEdgesStore.getBestEdgeSearch();
                break;
            case RANDOM:
                max = this.bestEdgesStore.getRandomBestEdge();
                break;
            default:
                break;
        }

        // If no edge is found, return Double.MIN_VALUE
        if (max == null) {
            x_i = null;
            y_i = null;
            t_0 = null;
            this.bestEdgesStore.clear();
            return Double.MIN_VALUE;
        }
        // Seeting x_i, y_i and t_0
        x_i = max.edge.getNode1();
        y_i = max.edge.getNode2();
        t_0 = max.hSubset;

        // Deleting the selected edge from edgesCandidates and clearing the queue
        edgesCandidates.remove(max.edge);
        this.bestEdgesStore.clear();

        return max.score;


        // Calculating 
        /* 
        Set<EdgeSearch> newScores = edgesCandidates.parallelStream()
                .map(e -> scoreEdge(graph, e))
                .collect(Collectors.toSet());
        
                
        HashSet<EdgeSearch> temp = new HashSet<>();
        temp.addAll(newScores);
        temp.addAll(this.scores);
        this.scores = temp;
        
        EdgeSearch max = Collections.max(this.scores);

        if (max.score > 0) {
            //Assigning values to x_i, y_i and t_0
            x_i = max.edge.getNode1();
            y_i = max.edge.getNode2();
            t_0 = max.hSubset;

            // Deleting the selected edge from edgesCandidates
            edgesCandidates.remove(max.edge);
            this.scores.remove(max);
        }

        return max.score;

        */
    }

    private void updateEdges(Graph graph){
        // Modo normal
        if (!speedUp) {
            // Getting the common adjacents of x_i and y_i
            Set<Node> process = revertToCPDAG(graph);
            removeEdgesNotNeighbors(graph, process);
        }
        // Modo heurístico. No comprobamos los enlaces invertidos en revertToCPDAG
        else {
            // Getting the common adjacents of x_i and y_i
            Set<Node> process = new HashSet<>();
            removeEdgesNotNeighbors(graph, process);
        }
    }

    private void removeEdgesNotNeighbors(Graph graph, Set<Node> process) {
        process.add(x_i);
        process.add(y_i);

        process.addAll(graph.getAdjacentNodes(x_i));
        process.addAll(graph.getAdjacentNodes(y_i));

        edgesCandidates = new HashSet<>(S);
        edgesCandidates.removeIf(edge -> {
            Node x = edge.getNode1();
            Node y = edge.getNode2();
            return !process.contains(x) && !process.contains(y);
        });
        //System.out.println("TAMAÑO DE enlaces: " + enlaces.size() + ", S: " + S.size() + ". \t Process: " + process.size()  + ", revert: " + tam);
    }

    private Set<Node> revertToCPDAG(Graph graph) {
        GraphSearchUtils.basicCpdag(graph);
        MeekRules rules = new MeekRules();
        rules.setMeekPreventCycles(true);
        return rules.orientImplied(graph);
    }

    private void scoreEdge(Graph graph, Edge edge) {

        Node _x = edge.getNode1();//Edges.getDirectedEdgeTail(edge);
        Node _y = edge.getNode2();//Edges.getDirectedEdgeHead(edge);

        if (!graph.isAdjacentTo(_x, _y)) {
            List<Node> tNeighbors = getSubsetOfNeighbors(_x, _y, graph);

            Set<Node> tSubset = new HashSet<>();
            double insertEval = insertEval(_x, _y, tSubset, graph, problem);
            //System.out.println("InsertEval: " + insertEval);
            if (insertEval > 0) {
                List<Node> naYXT = new LinkedList<>(tSubset);
                List<Node> naYX = findNaYX(_x, _y, graph);
                naYXT.addAll(naYX);

                boolean passTests = evaluateTestConditions(graph, _x, _y, naYXT);

                if (passTests) {
                    double greedyScore = insertEval;
                    int bestNodeIndex;
                    Node bestNode = null;

                    do {
                        bestNodeIndex = -1;
                        for (int k = 0; k < tNeighbors.size(); k++) {
                            Node node = tNeighbors.get(k);
                            Set<Node> newT = new HashSet<>(tSubset);
                            newT.add(node);
                            insertEval = insertEval(_x, _y, newT, graph, problem);

                            if (insertEval <= greedyScore) {
                                continue;
                            }

                            naYXT = new LinkedList<>(newT);
                            naYXT.addAll(naYX);
                            if(!isClique(naYXT, graph) || !isSemiDirectedBlocked(_x, _y, naYXT, graph, new HashSet<>())){ 
                                continue;
                            }



                            bestNodeIndex = k;
                            bestNode = node;
                            greedyScore = insertEval;
                        }
                        if (bestNodeIndex != -1) {
                            tSubset.add(bestNode);
                            tNeighbors.remove(bestNodeIndex);
                        }

                    } while ((bestNodeIndex != -1) && (tSubset.size() <= 1));

                    if (greedyScore > insertEval) {
                        insertEval = greedyScore;
                    }
                    //System.out.println("InsertEval: " + insertEval);
                    //return new EdgeSearchBP(insertEval, tSubset, edge);
                    this.bestEdgesStore.addEdge(new EdgeSearchBP(insertEval, tSubset, edge));

                }
            }
        }
        // This shouldn't happen, because x and y should be adjacent
        //return new EdgeSearchBP(0, new SubSet(), edge);
        //this.bestEdgesStore.addEdge(new EdgeSearchBP(0, new SubSet(), edge));


    }

    private boolean evaluateTestConditions(Graph graph, Node _x, Node _y, List<Node> naYXT) {
        return !(!isClique(naYXT, graph) || !isSemiDirectedBlocked(_x, _y, naYXT, graph, new HashSet<>()));
    }

    public int getMaxIt() {
        return maxIt;
    }
}


