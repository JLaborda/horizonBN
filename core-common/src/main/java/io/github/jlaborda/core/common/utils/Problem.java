package io.github.jlaborda.core.common.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.score.BdeuScore;
import edu.cmu.tetrad.search.score.DiscreteScore;
import edu.cmu.tetrad.search.score.Score;
import edu.cmu.tetrad.util.Matrix;
import edu.cmu.tetrad.util.Vector;
import static io.github.jlaborda.core.common.utils.Utils.pdagToDag;

public class Problem {

    /**
     * Data of the problem
     */
    private DataSet data;

    /**
     * Array of variable names from the data set, in order.
     */
    private String[] varNames;

    /**
     * List of variables in the data set, in order.
     */
    private List<Node> variables;

    /**
     * For discrete data scoring, the structure prior.
     */
    protected double structurePrior;

    /**
     * For discrete data scoring, the sample prior.
     */
    protected double samplePrior;

    /**
     * Cases for each variable of the problem.
     */
    //protected int[][] cases;

    /**
     * Number of values a variable can take.
     */
    //protected int[] nValues;

    /**
     * Map from variables to their column indices in the data set.
     */
    protected  HashMap<Node, Integer> hashIndices = null;

    /**
     * Caches scores for discrete search.
     */
    private final Cache<ParentSetKey, Double> localScoreCache; 
    //private final ConcurrentHashMap<ParentSetKey,Double> localScoreCache = new ConcurrentHashMap<>();
    //protected LocalScoreCacheConcurrent localScoreCache = new LocalScoreCacheConcurrent();
    
    /**
     * Maximum number of parents for a variable.
     */
    public static int MAX_PARENTS =  5; //Integer.MAX_VALUE;
    
    /**
     * Total calls done
     */
    public static int numTotalCalls=0;

    /**
     * Total calls done to non-cached information
     */
    public static int numNonCachedCalls=0;


    /**
     * BDeu Score.
     */
    protected DiscreteScore bdeu;
    //protected BdeuScoreOptimized bdeu;

    public static double emptyGraphScore;

    public static int nInstances;
    
    //public AtomicInteger counter;
    //public AtomicInteger counterSinDict;

    private double [][] mutualInformationMatrix = null;


    public Problem(DataSet dataSet){
        System.out.println("Creating problem");
        System.out.println("DataSet: " + dataSet.getName());
        System.out.println("Num rows: " + dataSet.getNumRows());
        System.out.println("Num columns: " + dataSet.getNumColumns());
        //System.out.println("Printing dataset:");
        //System.out.println(dataSet.toString());

        //this.counter = new AtomicInteger();
        //this.counterSinDict = new AtomicInteger();
        
        //Setting dataset
        List<String> _varNames = dataSet.getVariableNames();

        this.data = dataSet;
        this.varNames = _varNames.toArray(String[]::new);
        this.variables = dataSet.getVariables();

        //Initializing Structure and Sample Prior
        structurePrior = 0.001;
        samplePrior = 10.0;

        //building index
        Graph graph = new EdgeListGraph(new LinkedList<>(this.variables));
        buildIndexing(graph);
        
        bdeu = new BdeuScore(data);
        bdeu.setSamplePrior(samplePrior);
        bdeu.setStructurePrior(structurePrior);

        //Initializing cache
        long ramGB = (long)((double)Runtime.getRuntime().maxMemory() / Math.pow(1024,3));
        long maxCacheSize = Utils.computeCacheSize(this.getVariables().size(), MAX_PARENTS, ramGB, 0.1, 220);//Utils.sumCombinations(this.getVariables().size(), Problem.MAX_PARENTS);
        localScoreCache = Caffeine.newBuilder()
            .maximumSize(maxCacheSize)
            .build();
        
        //bdeu = new BdeuScoreOptimized(data);
        nInstances = dataSet.getNumRows();
        initializeEmptyGraphScore(graph);
    }

    /**
     * Initializes the emptyGraphScore after construction to avoid leaking 'this'.
     */
    private void initializeEmptyGraphScore(Graph graph) {
        emptyGraphScore = scoreGraph(graph, this);
    }


    public Problem(String resourceFile) throws IOException{
        this(Utils.readDataFromResource(resourceFile));
    }


    /**
     * Builds the indexing structure for the Graph passed as an argument.
     * @param g Graph being indexed.
     */
    private void buildIndexing(Graph g) {
        Graph graph = new EdgeListGraph(g);
        this.hashIndices = new HashMap<>();
        for (Node next : graph.getNodes()) {
            for (int i = 0; i < varNames.length; i++) {
                if (varNames[i].equals(next.getName())) {
                    hashIndices.put(next, i);
                    break;
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (obj instanceof Problem p){
            return data.equals(p.getData());
        }
        return false;

    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    public DataSet getData() {
        return data;
    }

    public double getSamplePrior() {
        return samplePrior;
    }

    public void setSamplePrior(double samplePrior){
        this.samplePrior = samplePrior;
        this.bdeu.setSamplePrior(samplePrior);
    }


    public double getStructurePrior() {
        return structurePrior;
    }

    public void setStructurePrior(double structurePrior) {
        this.structurePrior = structurePrior;
        this.bdeu.setStructurePrior(structurePrior);
    }
/* 
    public int[] getnValues() {
        return nValues;
    }

    public int[][] getCases() {
        return cases;
    }
*/
    public final List<Node> getVariables() {
        return variables;
    }

    public String[] getVarNames() {
        return varNames;
    }

    public HashMap<Node, Integer> getHashIndices() {
        return hashIndices;
    }

    /*public LocalScoreCacheConcurrent getLocalScoreCache() {
        return localScoreCache;
    }*/
    
    public Cache<ParentSetKey, Double> getLocalScoreCache() {
        return localScoreCache;
    }

    // public ConcurrentHashMap<ParentSetKey, Double> getLocalScoreCache() {
    //     return localScoreCache;
    // }
    
    public Score getScoreEvaluator() {
        return bdeu;
    }

    public Node getNode(String name){
        for (Node node: variables) {
            if(node.getName().equals(name))
                return node;
        }
        return null;
    }
    
    public Node getNode(int id){
        for (Node node: variables) {
            if(hashIndices.get(node) == id)
                return node;
        }
        return null;
    }

    public ArrayList<Integer> nodeToIntegerList(List<Node> nodes){
        ArrayList<Integer> integers = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            integers.add(nodeToInteger(node));
        }
        return integers;
    }

    public int nodeToInteger(Node node){
        return this.hashIndices.get(node);
    }

    public double evaluate(Integer x, Set<Integer> parents){
        // Creating key for the cache
        numTotalCalls++;
        ParentSetKey key = new ParentSetKey(x, parents);
    
        // Check if the evaluation is already in the cache
        Double cachedScore = localScoreCache.getIfPresent(key);
        // Double cachedScore = localScoreCache.getOrDefault(key,null );
        if (cachedScore != null) {
            return cachedScore;
        }

        // If the number of parents exceeds the maximum, return negative infinity
        if(parents.size() >= MAX_PARENTS){
            localScoreCache.put(key, Double.NEGATIVE_INFINITY);
            return Double.NEGATIVE_INFINITY;
        }
        numNonCachedCalls++;
    
        // Convertimos a array para el evaluador
        int[] parentArray = new int[parents.size()];
        int i = 0;
        for (int p : parents) {
            parentArray[i++] = p;
        }
    
        double score = bdeu.localScore(x, parentArray);
        localScoreCache.put(key, score);
    
        return score;
    }

    public double evaluate(Integer x, List<Integer> parents){
        // Convertimos la lista de padres a Set
        Set<Integer> parentSet = new HashSet<>(parents);
        return evaluate(x, parentSet);
    }

    public double evaluate(Integer x, int[] parents){
        // Convertimos el array de padres a Set
        Set<Integer> parentSet = new HashSet<>();
        for (int p : parents) {
            parentSet.add(p);
        }
        return evaluate(x, parentSet);
    }

        /**
     * Scores a DAG using the BDeu score function
     * @param graph DAG graph being evaluated
     * @return score of the graph.
     */
    public double scoreGraph(Graph graph) {

        if (graph == null){
            return Double.NEGATIVE_INFINITY;
        }

//        Graph dag = SearchGraphUtils.dagFromPattern(graph);
        Graph dag = new EdgeListGraph(graph);
        Utils.pdagToDag(dag);
        double score = 0.;

        for (Node next : dag.getNodes()) {
            Set<Node> parents = new HashSet<>(dag.getParents(next));
            int nextIndex = -1;
            for (int i = 0; i < this.getVariables().size(); i++) {
                if (varNames[i].equals(next.getName())) {
                    nextIndex = i;
                    break;
                }
            }
            int[] parentIndices = new int[parents.size()];
            Iterator<Node> pi = parents.iterator();
            int count = 0;
            while (pi.hasNext()) {
                Node nextParent = pi.next();
                for (int i = 0; i < this.getVariables().size(); i++) {
                    if (varNames[i].equals(nextParent.getName())) {
                        parentIndices[count++] = i;
                        break;
                    }
                }
            }
                score += localBdeuScore(nextIndex, parentIndices);
            }
        return score;
    }

    private double localBdeuScore(int node, int[] parents) {
        numTotalCalls++;
        // Check if the evaluation is already in the cache
        ParentSetKey key = new ParentSetKey(node, parents);
        Double cachedScore = localScoreCache.getIfPresent(key);
        if (cachedScore != null) {
            return cachedScore;
        }
       
        // If not, calculate the score and store it in the cache
        numNonCachedCalls++;
        double score = bdeu.localScore(node, parents);
        localScoreCache.put(key, score);
        
        return score;
    }

    public double mutualInformation(Node x, Node y){
        // Check if x and y are in the dataset
        Integer xIndex = this.getHashIndices().get(x);
        Integer yIndex = this.getHashIndices().get(y);
        if (xIndex == null || yIndex == null) {
            return Double.NaN; // or throw an exception
        }
        // Checking if the matrix is initialized
        if(this.mutualInformationMatrix == null){
            this.mutualInformationMatrix = new double[this.getVariables().size()][this.getVariables().size()];
            for(int i = 0; i < this.getVariables().size(); i++){
                for(int j = 0; j < this.getVariables().size(); j++){
                    this.mutualInformationMatrix[i][j] = Double.NaN;
                }
            }
        }
        // Checking if the mutual information has already been calculated
        if(this.mutualInformationMatrix[xIndex][yIndex] != Double.NaN){
            return this.mutualInformationMatrix[xIndex][yIndex];
        }



        // Check if x and y are discrete variables
        if (! (x instanceof DiscreteVariable) || ! (y instanceof DiscreteVariable)) {
            return Double.NaN; // or throw an exception
        }

        // Initialize variables
        int[] colX = this.getColumnValues(xIndex);
        int[] colY = this.getColumnValues(yIndex);
        Map<Integer, Integer> xCounts = new HashMap<>();
        Map<Integer, Integer> yCounts = new HashMap<>();
        Map<String, Integer> jointCounts = new HashMap<>();
        int totalCount = this.getDataSetNumberOfRows();

        // Count occurrences of each value in x and y and jointCounts.
        for (int i = 0; i < totalCount; i++) { 
            xCounts.put(colX[i], xCounts.getOrDefault(colX[i], 0) + 1);
            yCounts.put(colY[i], yCounts.getOrDefault(colY[i], 0) + 1);
            String key = colX[i] + "," + colY[i];
            jointCounts.put(key, jointCounts.getOrDefault(key, 0) + 1);
        }

        // Calculate the mutual information score
        double mi = 0.0;
        for (Map.Entry<String, Integer> entry : jointCounts.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int xi = Integer.parseInt(parts[0]);
            int yi = Integer.parseInt(parts[1]);
            double pXY = (double) entry.getValue() / totalCount;
            double pX = (double) xCounts.get(xi) / totalCount;
            double pY = (double) yCounts.get(yi) / totalCount;
            mi += pXY * Math.log(pXY / (pX * pY)) / Math.log(2);
        }
        // Store the mutual information in the matrix
        this.mutualInformationMatrix[xIndex][yIndex] = mi;
        return mi;
    }

    public int[] getColumnValues(int colIndex) {
        Matrix dataBox =  this.data.getDoubleData();
        Vector columnVector = dataBox.getColumn(colIndex);
        double[] aux = columnVector.toArray();
        int[] column = new int[aux.length];
        for (int i = 0; i < aux.length; i++) {
            column[i] = (int) aux[i];
        }
        return column;
        //int[][] cases = dataBox.getData();
        //int[] column = new int[cases.length];
        //for (int i = 0; i < cases.length; i++) {
        //    column[i] = cases[i][colIndex];
        //}
        //return column;
    }


    public String getVariableName(int varIndex){
        if(varIndex < 0 || varIndex >= varNames.length)
            return null;
        return varNames[varIndex];
    }

    public Integer getVariable(String varName){
        varName = varName.toLowerCase();
        for (int i = 0; i < varNames.length; i++) {
            if(varNames[i].toLowerCase().equals(varName))
                return i;
        }
        return -1;
    }

    public List<Integer> getAllVariables(){
        return this.nodeToIntegerList(this.getVariables());
    }

    public int getDataSetNumberOfRows() {
        return this.data.getNumRows();
    }

    public void setBdeu(DiscreteScore bdeu) {
        this.bdeu = bdeu;
    }

    public Dag createDummyDag() {
        // DAG vacío con los nodos del dataset
        Dag dag = new Dag(this.variables);

        // Añadir arcos simples en cadena
        for (int i = 0; i < this.variables.size() - 1; i++) {
            Node from = this.variables.get(i);
            Node to = this.variables.get(i + 1);
            dag.addDirectedEdge(from, to);
        }

        return dag;
    }

    /**
     * Scores a DAG using the BDeu score function
     * @param graph DAG graph being evaluated
     * @return score of the graph.
     */
    public static double scoreGraph(Graph graph, Problem problem) {

        if (graph == null){
            return Double.NEGATIVE_INFINITY;
        }

//        Graph dag = SearchGraphUtils.dagFromPattern(graph);
        Graph dag = new EdgeListGraph(graph);
        pdagToDag(dag);
        double score = 0.;

        for (Node next : dag.getNodes()) {
            Set<Node> parents = new HashSet<>(dag.getParents(next));
            int nextIndex = -1;
            for (int i = 0; i < problem.getVariables().size(); i++) {
                String[] varNames = problem.getVarNames();
                if (varNames[i].equals(next.getName())) {
                    nextIndex = i;
                    break;
                }
            }
            int[] parentIndexes = new int[parents.size()];
            Iterator<Node> pi = parents.iterator();
            int count = 0;
            while (pi.hasNext()) {
                Node nextParent = pi.next();
                for (int i = 0; i < problem.getVariables().size(); i++) {
                    String[] varNames = problem.getVarNames();
                    if (varNames[i].equals(nextParent.getName())) {
                        parentIndexes[count++] = i;
                        break;
                    }
                }
            }
            score += problem.evaluate(nextIndex, parentIndexes);
        }
        return score;
    }
    
    public double scoreDag(Graph graph) {
        
        if (graph == null){
            return Double.NEGATIVE_INFINITY;
        }
        
        Graph dag = new EdgeListGraph(graph);
        pdagToDag(dag);        
        double _score = 0;

        for (Node node : getVariables()) {
            List<Node> x = dag.getParents(node);

            int[] parentIndices = new int[x.size()];

            int count = 0;
            for (Node parent : x) {
                parentIndices[count++] = hashIndices.get(parent);
            }

            //final double nodeScore = problem.getScoreEvaluator().localScore(hashIndices.get(node), parentIndices);
            final double nodeScore = this.evaluate(hashIndices.get(node), parentIndices);
            _score += nodeScore;
        }
        return _score;
    }

    /**
     * Score difference of the node y when it is associated as child with one set of parents and when it is associated with another one.
     * @param y {@link Node Node} being considered for the score.
     * @param parents1 Set of {@link Node Node} of the first set of parents.
     * @param parents2 Set of {@link Node Node} of the second set of parents.
     * @return Score difference between both possibilities.
     */
    public static double scoreGraphChange(Node y, Set<Node> parents1,
                                   Set<Node> parents2, Graph graph, Problem problem) {
        // Getting indexing
        HashMap<Node, Integer> index = problem.getHashIndices();

        // Getting indexes
        int yIndex = index.get(y);
        int[] parentIndices1 = new int[parents1.size()];

        int count = 0;
        for (Node aParents1 : parents1) {
            parentIndices1[count++] = (index.get(aParents1));
        }

        int[] parentIndices2 = new int[parents2.size()];

        int count2 = 0;
        for (Node aParents2 : parents2) {
            parentIndices2[count2++] = (index.get(aParents2));
        }

        // Calculating the scores of both possibilities and returning the difference
        double score1 = problem.evaluate(yIndex, parentIndices1);
        double score2 = problem.evaluate(yIndex, parentIndices2);
        return score1 - score2;
    }

    public static void main(String[] args) {
        System.out.println("RAM(B): " + Runtime.getRuntime().maxMemory());
        System.out.println("RAM(GB): " + (double)Runtime.getRuntime().maxMemory()/Math.pow(1024,3));
        System.out.println("Cache size: " + Utils.computeCacheSize(1041, 5, (long)((double)Runtime.getRuntime().maxMemory() / Math.pow(1024,3)), 0.1, 220));
    }

}
