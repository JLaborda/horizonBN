package io.github.jlaborda.core.common.utils;

//import consensusBN.PairWiseConsensusBES;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.bayes.MlBayesIm.InitializationMethod;
import edu.cmu.tetrad.data.BoxDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.IntDataBox;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.pitt.dbmi.data.reader.Delimiter;
import edu.pitt.dbmi.data.reader.DiscreteData;
import edu.pitt.dbmi.data.reader.DiscreteDataColumn;
import edu.pitt.dbmi.data.reader.tabular.VerticalDiscreteTabularDatasetFileReader;
import es.uclm.i3a.simd.consensusBN.PairWiseConsensusBES;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.BIFReader;

public class Utils {


    private static Random random = new Random();
    private static long seed = System.currentTimeMillis();
    
    /**
     * Transforms a maximally directed pattern (PDAG) represented in graph
     * <code>g</code> into an arbitrary DAG by modifying <code>g</code> itself.
     * Based on the algorithm described in </p> Chickering (2002) "Optimal
     * structure identification with greedy search" Journal of Machine Learning
     * Research. </p> R. Silva, June 2004
     */
    public static void pdagToDag(Graph g) {
        Graph p = new EdgeListGraph(g);
        List<Edge> undirectedEdges = new ArrayList<>();

        for (Edge edge : g.getEdges()) {
            if (edge.getEndpoint1() == Endpoint.TAIL
                    && edge.getEndpoint2() == Endpoint.TAIL
                    && !undirectedEdges.contains(edge)) {
                undirectedEdges.add(edge);
            }
        }
        g.removeEdges(undirectedEdges);
        List<Node> pNodes = p.getNodes();

        do {
            Node x = null;

            for (Node pNode : pNodes) {
                x = pNode;

                if (!p.getChildren(x).isEmpty()) {
                    continue;
                }

                Set<Node> neighbors = new HashSet<>();

                for (Edge edge : p.getEdges()) {
                    if (edge.getNode1() == x || edge.getNode2() == x) {
                        if (edge.getEndpoint1() == Endpoint.TAIL
                                && edge.getEndpoint2() == Endpoint.TAIL) {
                            if (edge.getNode1() == x) {
                                neighbors.add(edge.getNode2());
                            } else {
                                neighbors.add(edge.getNode1());
                            }
                        }
                    }
                }
                if (!neighbors.isEmpty()) {
                    Collection<Node> parents = p.getParents(x);
                    Set<Node> all = new HashSet<>(neighbors);
                    all.addAll(parents);
                    if (!GraphUtils.isClique(all, p)) {
                        continue;
                    }
                }

                for (Node neighbor : neighbors) {
                    Node node1 = g.getNode(neighbor.getName());
                    Node node2 = g.getNode(x.getName());

                    g.addDirectedEdge(node1, node2);
                }
                p.removeNode(x);
                break;
            }
            pNodes.remove(x);
        } while (!pNodes.isEmpty());
    }

    public static void pdagToDag2(Graph graph){
        /*Algorithm From Chickering 2002: 
        "We first consider a simple implementation of PDAG-to-DAG due to Dor and Tarsi (1992).
         Let NX denote the neighbors of node X in a PDAG P.
         We first create a DAG G that contains all of the directed edges from P, and no other edges.
         We then repeat the following procedure: 
            First, select a node X in P such that: 
                (1) X has no out-going edges and 
                (2) if NX is non-empty, then NX PaX is a clique.
         If P admits a consistent extension, the node X is guaranteed to exist.
        Next, for each undirected edge Y X incident to X in P, insert a directed edge Y X to G.
        Finally, remove X and all incident edges from the P and continue with the next node.
        The algorithm terminates when all nodes have been deleted from P." */
    
        // First create a DAG G that contains all of the directed edges from the PDAG, and no other edges.
        Graph graphAux = new EdgeListGraph(graph);
        List<Edge> undirectedEdges = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            if(!edge.isDirected())
                undirectedEdges.add(edge);
            }
        graph.removeEdges(undirectedEdges);

        // We now repeat the following procedure: for each node in the pdag, check if it has no outgoing edges and if its neighbors form a clique.
        // If it does, we add the directed edges from the undirected edges to the graph. Y->X to G.
        List<Node> nodes = graphAux.getNodes();
        do{
            Node x = null; 
            for(Node node : nodes){
                x = node;
                //Checking if the node has no outgoing edges
                if(!graphAux.getChildren(node).isEmpty())
                    continue;
                //Checking if the neighbors form a clique
                if(!GraphUtils.isClique(graphAux.getAdjacentNodes(x), graphAux))
                    continue;
                // If these conditions are met, we add directed edges Y->X to G, where Y are nodes that are neighbors of X in the PDAG.
                for(Node neighbor : graphAux.getAdjacentNodes(x)){
                    if(!neighbor.equals(x)){
                        // Adding the directed edge from neighbor to node
                        graph.addDirectedEdge(neighbor, x);
                    }
                }
                // Finally, we remove the node and all incident edges from the PDAG and continue with the next node.
                graphAux.removeNode(node);
                break; // We break the loop to start again with the new graphAux without the removed node.
            }
            nodes.remove(x);
        }while(!nodes.isEmpty());
    }

    /**
     * Separates the set of possible arcs into as many subsets as threads we use to solve the problem.
     *
     * @param listOfArcs List of {@link Edge Edges} containing all the possible edges for the actual problem.
     * @param numSplits  The number of splits to do in the listOfArcs.
     * @return The subsets of the listOfArcs in an ArrayList of TupleNode.
     */
    public static <T> List<Set<T>> split(Set<T> set, int numSplits) {
        List<Set<T>> subSets = new ArrayList<>(numSplits);

        // Mezclamos los elementos del conjunto
        List<T> shuffledList = new ArrayList<>(set);
        Collections.shuffle(shuffledList, random);

        // Dividimos en subconjuntos
        int n = 0;
        for (int s = 0; s < numSplits - 1; s++) {
            Set<T> sub = new HashSet<>();
            for (int i = 0; i < Math.floorDiv(shuffledList.size(), numSplits); i++) {
                sub.add(shuffledList.get(n));
                n++;
            }
            subSets.add(sub);
        }

        // Agregar los elementos restantes al último subconjunto
        Set<T> sub = new HashSet<>();
        for (int i = n; i < shuffledList.size(); i++) {
            sub.add(shuffledList.get(i));
        }
        subSets.add(sub);

        return subSets;
    }

    public static void setSeed(long seed){
        Utils.seed = seed;
        random = new Random(seed);
    }

    public static long getSeed(){
        return seed;
    }

    public static Random getRandom(){
        return random;
    }

    /**
     * Calculates the amount of possible arcs between the variables of the dataset and stores it.
     *
     * @param data DataSet used to calculate the arcs between its columns (nodes).
     */
    public static Set<Edge> calculateArcs(DataSet data) {
        //0. Accumulator
        Set<Edge> setOfArcs = new HashSet<>(data.getNumColumns() * (data.getNumColumns() - 1));
        //1. Get edges (variables)
        List<Node> variables = data.getVariables();
        //int index = 0;
        //2. Iterate over variables and save pairs
        for (int i = 0; i < data.getNumColumns() - 1; i++) {
            for (int j = i + 1; j < data.getNumColumns(); j++) {
                // Getting pair of variables (Each variable is different)
                Node var_A = variables.get(i);
                Node var_B = variables.get(j);

                //3. Storing both pairs
                setOfArcs.add(Edges.directedEdge(var_A, var_B));
                setOfArcs.add(Edges.directedEdge(var_B, var_A));
                //index++;
                //this.setOfArcs[index] = new TupleNode(var_B,var_A);
                //index++;
            }
        }
        return setOfArcs;
    }
    
    /**
     * Calculates the amount of possible edges between the variables of the dataset and stores it.
     *
     * @param data DataSet used to calculate the edges between its columns (nodes).
     */
    public static Set<Edge> calculateEdges(DataSet data) {
        //0. Accumulator
        Set<Edge> setOfArcs = new HashSet<>(data.getNumColumns() * (data.getNumColumns() - 1));
        //1. Get edges (variables)
        List<Node> variables = data.getVariables();
        //2. Iterate over variables and save pairs
        for (int i = 0; i < data.getNumColumns() - 1; i++) {
            for (int j = i + 1; j < data.getNumColumns(); j++) {
                // Getting pair of variables (Each variable is different)
                Node var_A = variables.get(i);
                Node var_B = variables.get(j);

                //3. Storing both pairs
                setOfArcs.add(Edges.directedEdge(var_A, var_B));
            }
        }
        return setOfArcs;
    }


    /**
     * Stores the data from a csv as a DataSet object.
     * @param path
     * Path to the csv file.
     * @return DataSet containing the data from the csv file.
     * @throws IOException If path is incorrect, an error occurs while reading the file.
     */
    public static DataSet readData(String pathString) throws IOException{
        // Reading data with new data-reader library
        Path path = Paths.get(pathString);
        VerticalDiscreteTabularDatasetFileReader datasetReader = new VerticalDiscreteTabularDatasetFileReader(path, Delimiter.COMMA);
        datasetReader.setHasHeader(true);
        DiscreteData data = (DiscreteData) datasetReader.readInData();

        // Data to DataSet
        // Paso 3: crear variables discretas para Tetrad
        List<Node> variables = new ArrayList<>();
        for (DiscreteDataColumn column : data.getDataColumns()) {
            String varName = column.getDataColumn().getName();
            //System.out.println("Type of column: " + column.getDataColumn().getClass());
            //System.out.println("Name of column: " + varName);
            //System.out.println("Number of Categories: " + column.getCategories().size());
            List<String> categories = column.getCategories();//((TabularDataColumn) column.getDataColumn());
            variables.add(new DiscreteVariable(varName,categories));
        } //System.out.println("Size variables: " + variables.size());
        //System.out.println("Size data: " + data.getDataColumns().length);

        // Transposing the data matrix (rows to columns and columns to rows)
        int [][] dataMatrix = data.getData();
        int rows = dataMatrix[0].length;  // número de filas reales
        int cols = dataMatrix.length;     // número de columnas reales

        int[][] transposed = new int[rows][cols];

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                transposed[j][i] = dataMatrix[i][j];
            }
        }

        IntDataBox dataBox = new IntDataBox(transposed);

        //System.out.println("Number of columns in dataBox: " + dataBox.numCols());

        return new BoxDataSet(dataBox, variables);

        
        // Reading data with old data-reader library
        // // Initial Configuration
        // DataReader reader = new DataReader();
        // reader.setDelimiter(DelimiterType.COMMA);
        // reader.setMaxIntegralDiscrete(100);
        // DataSet dataSet = null;
        // // Reading data
        // try {
        //     dataSet = reader.parseTabular(new File(path));
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }

        // return dataSet;
    }


    public static Node getNodeByName(List<Node> nodes, String name){
        for(Node n : nodes){
            if (n.getName().equals(name)){
                return n;
            }
        }
        return null;
    }

    public static int getIndexOfNodeByName(List<Node> nodes, String name){
        for(int i = 0; i < nodes.size(); i++){
            Node n = nodes.get(i);
            if(n.getName().equals(name)){
                return i;
            }
        }
        return -1;
    }

    private static void ensureVariables(ArrayList<Dag> setofbns){

        List<Node> nodes = setofbns.get(0).getNodes();
        //System.out.println("Nodes: " + nodes);
        for(int i = 1 ; i< setofbns.size(); i++) {
            Dag oldDag = setofbns.get(i);
            Set<Edge> oldEdges = oldDag.getEdges();
            Dag newdag = new Dag(nodes);
            for(Edge e: oldEdges){
                /*
                System.out.println("Node1");
                System.out.println(e.getNode1());
                System.out.println("Node2");
                System.out.println(e.getNode2());
                */
                //int tailIndex = nodes.indexOf(e.getNode1());
                //int headIndex = nodes.indexOf(e.getNode2());

                int tailIndex = getIndexOfNodeByName(nodes, e.getNode1().getName());
                int headIndex = getIndexOfNodeByName(nodes, e.getNode2().getName());

                //System.out.println("tail: " + tailIndex);
                //System.out.println("head: "  + headIndex);
                Edge newEdge = new Edge(nodes.get(tailIndex),nodes.get(headIndex), Endpoint.TAIL, Endpoint.ARROW);
                newdag.addEdge(newEdge);
            }
            setofbns.remove(i);
            setofbns.add(i, newdag);
        }
    }

    public static int compare(Dag bn1, Dag bn2){
        ArrayList<Dag> dags = new ArrayList<>();
        dags.add(bn1);
        dags.add(bn2);
        ensureVariables(dags);
        PairWiseConsensusBES kl = new PairWiseConsensusBES(dags.get(0), dags.get(1));
        kl.fusion();
        int hmd =  kl.calculateHammingDistance();
        return hmd;
    }

    public static int SHD (Dag bn1, Dag bn2) {

        ArrayList<Dag> dags = new ArrayList<>();
        dags.add(bn1);
        dags.add(bn2);
        ensureVariables(dags);

        Graph g1 = new EdgeListGraph(dags.get(0));
        Graph g2 = new EdgeListGraph(dags.get(1));

        for(Node n: dags.get(0).getNodes()) {
            List<Node> p = dags.get(0).getParents(n);
            for (int i=0; i<p.size()-1;i++)
                for(int j=i+1; j<p.size();j++) {
                    Edge e1 = g1.getEdge(p.get(i), p.get(j));
                    Edge e2 = g1.getEdge(p.get(j), p.get(i));
                    if(e1==null && e2 == null) {
                        Edge e = new Edge(p.get(i),p.get(j),Endpoint.TAIL,Endpoint.TAIL);
                        g1.addEdge(e);
                    }
                }
        }

        for(Node n: dags.get(1).getNodes()) {
            List<Node> p = dags.get(1).getParents(n);
            for (int i=0; i<p.size()-1;i++)
                for(int j=i+1; j<p.size();j++) {
                    Edge e1 = g2.getEdge(p.get(i), p.get(j));
                    Edge e2 = g2.getEdge(p.get(j), p.get(i));
                    if(e1==null && e2 == null) {
                        Edge e = new Edge(p.get(i),p.get(j),Endpoint.TAIL,Endpoint.TAIL);
                        g2.addEdge(e);
                    }
                }
        }

        int sum = 0;
        for(Edge e: g1.getEdges()) {
            Edge e2 = g2.getEdge(e.getNode1(), e.getNode2());
            Edge e3 = g2.getEdge(e.getNode2(), e.getNode1());
            if(e2 == null && e3 == null) sum++;
        }

        for(Edge e: g2.getEdges()) {
            Edge e2 = g1.getEdge(e.getNode1(), e.getNode2());
            Edge e3 = g1.getEdge(e.getNode2(), e.getNode1());
            if(e2 == null && e3 == null) sum++;
        }
        return sum;
    }



    public static List<Node> getMarkovBlanket(Dag bn, Node n){
        List<Node> mb = new ArrayList<>();

        // Adding children and parents to the Markov's Blanket of this node
        List<Node> children = bn.getChildren(n);
        List<Node> parents = bn.getParents(n);

        mb.addAll(children);
        mb.addAll(parents);

        for(Node child : children){
            for(Node father : bn.getParents(child)){
                if (!father.equals(n)){
                    mb.add(father);
                }
            }
        }

        return mb;
    }

    /**
     * Gives back the percentages of markov's blanquet difference with the original bayesian network. It gives back the
     * percentage of difference with the blanquet of the original bayesian network, the percentage of extra nodes added
     * to the blanquet and the percentage of missing nodes in the blanquet compared with the original.
     * @param original
     * @param created
     * @return
     */
    public static double [] avgMarkovBlanquetdif(Dag original, Dag created) {

        if (original.getNodes().size() != created.getNodes().size())
            return null;

        for (String originalNodeName : original.getNodeNames()) {
            if (!created.getNodeNames().contains(originalNodeName))
                return null;
        }

        // First number is the average dfMB, the second one is the amount of more variables in each MB, the last number is the the amount of missing variables in each MB
        double[] result = new double[3];
        double differenceNodes = 0;
        double plusNodes = 0;
        double minusNodes = 0;


        for (Node e1 : original.getNodes()) {
            Node e2 = created.getNode(e1.getName());

            // Creating Markov's Blanket
            List<Node> mb1 = getMarkovBlanket(original, e1);
            List<Node> mb2 = getMarkovBlanket(created, e2);


            ArrayList<String> names1 = new ArrayList<>();
            ArrayList<String> names2 = new ArrayList<>();
            // Nodos de más en el manto creado
            for (Node n1 : mb1) {
                String name1 = n1.getName();
                names1.add(name1);
            }
            for (Node n2 : mb2) {
                String name2 = n2.getName();
                names2.add(name2);
            }

            //Variables de más
            for(String s2: names2) {
                if(!names1.contains(s2)) {
                    differenceNodes++;
                    plusNodes++;
                }
            }
            // Variables de menos
            for(String s1: names1) {
                if(!names2.contains(s1)) {
                    differenceNodes++;
                    minusNodes++;
                }
            }
        }

        // Differences of MM

        result[0] = differenceNodes;
        result[1] = plusNodes;
        result[2] = minusNodes;

        return result;

    }

    /**
     * Transforms a graph to a DAG, and removes any possible inconsistency found throughout its structure.
     * @param g Graph to be transformed.
     * @return Resulting DAG of the inserted graph.
     */
    public static Dag removeInconsistencies(Graph g){
        // Transforming the current graph into a DAG
        pdagToDag2(g);
        //System.out.println("Graph after pdagToDag: " + g);

        // Checking Consistency
        Node nodeT, nodeH;
        for (Edge e : g.getEdges()){
            if(!e.isDirected()) continue;
            //System.out.println("Undirected Edge: " + e);
            nodeH = Edges.getDirectedEdgeHead(e);
            nodeT = Edges.getDirectedEdgeTail(e);


            if(g.paths().existsDirectedPath(nodeH, nodeT)){
                //System.out.println("Directed path from " + nodeH + " to " + nodeT +"\t Deleting Edge because of cycle...");
                g.removeEdge(e);
            }
        }
        // Adding graph from each thread to the graphs array
        return new Dag(g);

    }

    public static double LL(BayesIm bn, DataSet data) {

        BayesIm bayesIm;

        int[][][] observedCounts;

        Graph graph = bn.getDag();
        Node[] nodes = new Node[graph.getNumNodes()];

        observedCounts = new int[nodes.length][][];

        int[][] observedCountsRowSum = new int[nodes.length][];

        bayesIm = new MlBayesIm(bn);

        for (int i = 0; i < nodes.length; i++) {

            int numRows = bayesIm.getNumRows(i);
            observedCounts[i] = new int[numRows][];

            observedCountsRowSum[i] = new int[numRows];

            for (int j = 0; j < numRows; j++) {

                observedCountsRowSum[i][j] = 0;

                int numCols = bayesIm.getNumColumns(i);
                observedCounts[i][j] = new int[numCols];
            }
        }

        //At this point set values in observedCounts

        for (int j = 0; j < data.getNumColumns(); j++) {
            DiscreteVariable var = (DiscreteVariable) data.getVariables().get(j);
            String varName = var.getName();
            Node varNode = bn.getDag().getNode(varName);
            int varIndex = bayesIm.getNodeIndex(varNode);

            int[] parentVarIndices = bayesIm.getParents(varIndex);

            if (parentVarIndices.length == 0) {
                //System.out.println("No parents");
                for (int col = 0; col < var.getNumCategories(); col++) {
                    observedCounts[varIndex][0][col] = 0;
                }

                for (int i = 0; i < data.getNumRows(); i++) {

                    observedCounts[varIndex][0][data.getInt(i, j)] += 1.0;


                }

            }
            else {    //For variables with parents:
                int numRows = bayesIm.getNumRows(varIndex);

                for (int row = 0; row < numRows; row++) {
                    int[] parValues = bayesIm.getParentValues(varIndex, row);

                    for (int col = 0; col < var.getNumCategories(); col++) {
                        try{
                            observedCounts[varIndex][row][col] = 0;
                        }catch(Exception ex) {}
                    }

                    for (int i = 0; i < data.getNumRows(); i++) {
                        //for a case where the parent values = parValues increment the estCount

                        boolean parentMatch = true;

                        for (int p = 0; p < parentVarIndices.length; p++) {
                            if (parValues[p] != data.getInt(i, parentVarIndices[p])) {
                                parentMatch = false;
                                break;
                            }
                        }

                        if (!parentMatch) {
                            continue;  //Not a matching case; go to next.
                        }

                        observedCounts[varIndex][row][data.getInt(i, j)] += 1;
                    }

                }

            }


        }


        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < bayesIm.getNumRows(i); j++) {
                for (int k = 0; k < bayesIm.getNumColumns(i); k++) {
                    observedCountsRowSum[i][j] += observedCounts[i][j][k];
                }
            }
        }

        double sum = 0.0;

        int n = nodes.length;

        for (int i = 0; i < n; i++) {
            int qi = bayesIm.getNumRows(i);
            for (int j = 0; j < qi; j++) {
                double p2 = observedCountsRowSum[i][j];
                // If p2 is 0, all p1 will be 0 too, so we skip the rest of the operations
                if(p2 == 0.0) 
                    continue;
                int ri = bayesIm.getNumColumns(i);
                for (int k = 0; k < ri; k++) {
                    double p1 = observedCounts[i][j][k];
                    // If p1 is 0, the log will be undefined, but since p1 is multiplied later, we can skip the rest of the operations
                    if(p1 == 0.0) 
                        continue;
                    
                    double p3 = Math.log(p1/p2);
                    sum += p1 * p3;
                }

            }

        }

        return sum / data.getNumRows() / data.getNumColumns();
    }

    public static double LL(Dag g, DataSet data) {
        BayesPm bnaux = new BayesPm(g);
        MlBayesIm bnOut = new MlBayesIm(bnaux, InitializationMethod.MANUAL);
        return LL(bnOut, data);
    }

    /**
     * Transforms a BayesNet read from a xbif file into a BayesPm object for tetrad
     *
     * @param wekabn BayesNet read from an xbif file
     * @return The BayesPm of the BayesNet
     */
    public static BayesPm transformBayesNetToBayesPm(BayesNet wekabn) {
        Dag graph = new Dag();

        // Getting nodes from weka network and adding them to a GraphNode
        for (int indexNode = 0; indexNode < wekabn.getNrOfNodes(); indexNode++) {
            GraphNode node = new GraphNode(wekabn.getNodeName(indexNode));
            graph.addNode(node);
        }
        // Adding all of the edges from the wekabn into the new Graph
        for (int indexNode = 0; indexNode < wekabn.getNrOfNodes(); indexNode++) {
            int nParent = wekabn.getNrOfParents(indexNode);
            for (int np = 0; np < nParent; np++) {
                int indexp = wekabn.getParent(indexNode, np);
                Edge ed = new Edge(graph.getNode(wekabn.getNodeName(indexp)), graph.getNode(wekabn.getNodeName(indexNode)), Endpoint.TAIL, Endpoint.ARROW);
                graph.addEdge(ed);
            }
        }
        //System.out.println(graph);
        return new BayesPm(graph);

    }

    public static String getDatabaseNameFromPattern(String databasePath){
        // Matching the end of the csv file to get the name of the database
        Pattern pattern = Pattern.compile(".*/(.*).csv");
        Matcher matcher = pattern.matcher(databasePath);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String[] readParameters(String paramsFilePath, int index) throws Exception {
        String[] parameterStrings = null;
        try (BufferedReader br = new BufferedReader(new FileReader(paramsFilePath))) {
            String line;
            for (int i = 0; i < index; i++)
                br.readLine();
            line = br.readLine();
            parameterStrings = line.split(" ");
        }
        catch(FileNotFoundException e){
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
        return parameterStrings;
    }

    public static MlBayesIm readOriginalBayesianNetwork(String netPath) throws Exception {
        BIFReader bayesianReader = new BIFReader();
        bayesianReader.processFile(netPath);
        BayesNet bayesianNet = bayesianReader;
        //System.out.println("Numero de variables: " + bayesianNet.getNrOfNodes());

        //Transforming the BayesNet into a BayesPm
        BayesPm bayesPm = Utils.transformBayesNetToBayesPm(bayesianNet);
        MlBayesIm bn2 = new MlBayesIm(bayesPm);

        return bn2;
    }

    public static Dag createOriginalDAG(String netPath) {
        MlBayesIm controlBayesianNetwork;
        try {
            controlBayesianNetwork = Utils.readOriginalBayesianNetwork(netPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Dag(controlBayesianNetwork.getDag());
    }

    public static BigInteger binomial(int n, int k) {
        if (k < 0 || k > n) return BigInteger.ZERO;
        k = Math.min(k, n - k);
        BigInteger result = BigInteger.ONE;
        for (int i = 1; i <= k; i++) {
            result = result.multiply(BigInteger.valueOf(n - k + i))
                           .divide(BigInteger.valueOf(i));
        }
        return result;
    }

    // Suma de C(n-1, i) para i = 0..k
    public static BigInteger sumCombinations(int n, int k) {
        BigInteger sum = BigInteger.ZERO;
        for (int i = 0; i <= k; i++) {
            sum = sum.add(binomial(n - 1, i));
        }
        return sum;
    }

        /**
     * Calcula el tamaño máximo de la cache.
     *
     * @param nVariables Número de variables en el problema
     * @param maxParents Número máximo de padres permitidos (k)
     * @param ramGB RAM total disponible en GB
     * @param percent Porcentaje (ej. 0.1 para 10%)
     * @return Tamaño máximo recomendado de la cache
     */
    public static long computeCacheSize(int nVariables, int maxParents, long ramGB, double percent, int estimatedBytesPerEntry) {
        // 1. Número total de posibles parent sets (N_total)
        BigInteger totalParentSets = BigInteger.ZERO;
        for (int j = 0; j <= maxParents; j++) {
            totalParentSets = totalParentSets.add(binomial(nVariables - 1, j));
        }
        BigInteger nTotal = totalParentSets.multiply(BigInteger.valueOf(nVariables));

        // 2. Entradas según porcentaje
        BigInteger byPercent = new BigInteger(String.valueOf((long)(percent * 1e6))); // factor para evitar dobles
        BigInteger nPercent = nTotal.multiply(byPercent).divide(BigInteger.valueOf(1_000_000));

        // 3. Límite por memoria (aprox. 220 bytes por entrada)
        long bytesAvailable = (long)(ramGB * 1L << 30) / 3; // solo 1/3 de la RAM para cache
        long maxEntriesByRAM = bytesAvailable / estimatedBytesPerEntry;

        // 4. Tomar el mínimo
        BigInteger limitByRAM = BigInteger.valueOf(maxEntriesByRAM);
        BigInteger chosen = nPercent.min(limitByRAM);

        return chosen.min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }

/*     public static List<Node> getTopologicalOrder(Graph graph){
        List<Node> randomNodes = new ArrayList<>(graph.getNodes());
        Random random = new Random();
        Collections.shuffle(randomNodes, random);

        return graph.paths().getValidOrder(randomNodes, false);
    }
*/
    //public static ArrayList<Node> getTopologicalOrder(){
    
    //     ArrayList<Node> L = new ArrayList<>();
    //     ArrayList<Node> S = new ArrayList<>();
        
    //     Graph graphCopy = new Dag_n(this);
        
    //     // S -> Nodos raíz, sin padres
    //     for (Node node : graphCopy.getNodes()) {
    //         if (graphCopy.getParents(node).isEmpty()) {
    //             S.add(node);
    //         }
    //     }

    //     while (!S.isEmpty()) {
    //         Node node = S.remove(random.nextInt(S.size()));
    //         L.add(node);
            
    //         // Borramos el nodo, y los enlaces a los hijos. 
    //         // Si ahora los hijos no tienen padres, los añadimos a S
    //         for (Node children : graphCopy.getAdjacentNodes(node)) {
    //             graphCopy.removeEdgesNRRDPath(node, children);
    //             if (graphCopy.getParents(children).isEmpty()) {
    //                 S.add(children);
    //             }
    //         }
    //     }
        
    //     return L;
    // }

}
