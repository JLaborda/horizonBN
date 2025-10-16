package io.github.jlaborda.core.common.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.benmanes.caffeine.cache.Cache;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import io.github.jlaborda.test.utils.Resources;



public class ProblemTest {

    /**
     * Path to the data file
     */
    static String path;

    /**
     * Dataset created from the data file
     */
    final DataSet dataset = Resources.CANCER_DATASET;


    @BeforeAll
    public static void setUp() throws IOException{
        path = Resources.getPathFromResource(Resources.CANCER_DATASET_PATH);
    }

    @Test
    public void constructorTest() throws IOException{
        //Act
        Problem p1 = new Problem(path);
        Problem p2 = new Problem(dataset);

        assertNotNull(p1);
        assertNotNull(p2);
    }

    @Test
    public void gettersTest(){
        Problem problem = new Problem(dataset);

        String [] varNames = problem.getVarNames();
        List<Node> variables = problem.getVariables();
        DataSet data = problem.getData();
        Cache<ParentSetKey,Double> cache = problem.getLocalScoreCache();
        // ConcurrentHashMap<ParentSetKey,Double> cache = problem.getLocalScoreCache();
        problem.setSamplePrior(20);
        problem.setStructurePrior(0.002);
        double samplePrior = problem.getSamplePrior();
        double structurePrior = problem.getStructurePrior();
        //int [][] cases = problem.getCases();
        HashMap<Node, Integer> index = problem.getHashIndices();
        //int [] nValues = problem.getnValues();


        // Checking names
        String [] cancerNames = {"Xray", "Dyspnoea", "Cancer", "Pollution", "Smoker"};
        boolean isCancerName;

        assertEquals(5, varNames.length);
        for (String name : varNames){
            isCancerName = false;
            for(String cName : cancerNames){
                if (name.equals(cName)){
                    isCancerName = true;
                    break;
                }
            }
            assertTrue(isCancerName);
        }

        //Checking Variables
        assertEquals(5, variables.size());
        for(Node n: variables){
            isCancerName = false;
            for(String cName: cancerNames){
                if(n.getName().equals(cName)){
                    isCancerName = true;
                }
            }
            assertTrue(isCancerName);
        }

        //Checking Data
        assertEquals(data, dataset);

        //Checking Cache
        assertNotNull(cache);

        //Checking index
        assertNotNull(index);

        //Checking nValues
        //assertEquals(5, nValues.length);
        //for(int n : nValues){
        //    assertEquals(2, n);
        //}

        //Checking cases
        //assertEquals(5000, cases.length);
        //System.out.println(cases.length);
        //for(int[] caseRow : cases){
        //    assertEquals(5, caseRow.length);
        //}

        //Checking samplePrior
        assertEquals(20.0, samplePrior, 0.0001);
        //Checking structurePrior

        assertEquals(0.002, structurePrior, 0.0001);

    }

    @Test
    public void equalsTest() throws IOException{
        String path1 = Resources.CANCER_DATASET_PATH;
        String path2 = Resources.EARTHQUAKE_DATASET_PATH;
        Problem p = new Problem(path1);
        Problem p2 = new Problem(path2);
        Problem p3 = p;
        Problem p4 = new Problem(path1);
        Object obj1 = new Object();
        assertEquals(p, p3);
        assertNotEquals(p, p2);
        assertNotEquals(null, p);
        assertEquals(p, p4);
        assertNotEquals(p, obj1);

    }

    @Test
    public void getNodeTest() throws IOException{
        Problem problem = new Problem(path);
        Node node = problem.getNode("Xray");
        assertNotNull(node);
        assertEquals("Xray", node.getName());
    }

    @Test
    public void nodeToIntegerListTest() throws IOException{
        Problem problem = new Problem(path);
        Node node = problem.getNode("Xray");
        List<Node> testNodes = new ArrayList<>();
        testNodes.add(node); 
        List<Integer> list = problem.nodeToIntegerList(testNodes);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(0, (int) list.get(0));
    }

    @Test
    public void getNodeIdTest() throws IOException{
        Problem problem = new Problem(path);
        Node node = problem.getNode("Xray");
        assertNotNull(node);
        assertEquals("Xray", node.getName());
    }

    @Test
    public void getNodeNotInProblemTest() throws IOException{
        Problem problem = new Problem(path);
        Node node = problem.getNode("NotInProblem");
        Node node2 = problem.getNode(100);
        assertNull(node);
        assertNull(node2);
    }
/*
    @Test
    public void testEvaluatePerformanceAndStability() {
        // Cambia esto si tu instancia de Problem requiere parámetros concretos
        String alarmPath = Resources.ALARM_DATASET_PATH;
        Problem problem = new Problem(alarmPath); 
        Problem.numTotalCalls = 0;
        Problem.numNonCachedCalls = 0;
        int numVariables = problem.getAllVariables().size(); // Por ejemplo: 37
        Random random = new Random(42);

        int numEvaluations = 1_000_000;

        System.gc(); // Sugerir GC antes de medir
        long memoryBefore = getUsedMemory();
        long startTime = System.nanoTime();

        for (int i = 0; i < numEvaluations; i++) {
            // Elige una variable x aleatoriamente
            int x = random.nextInt(numVariables);

            // Genera entre 0 y 4 padres aleatorios distintos de x
            int numParents = random.nextInt(5); // 0 a 4 padres
            int[] parents = random.ints(0, numVariables)
                                  .filter(p -> p != x)
                                  .distinct()
                                  .limit(numParents)
                                  .toArray();

            // Llama al método evaluate
            double score = problem.evaluate(x, parents);

            // Asegura que no devuelve NaN ni infinito
            assertFalse("Score returned NaN", Double.isNaN(score));
            assertFalse("Score returned Infinite", Double.isInfinite(score));

            // Si quieres mostrar progreso
            if (i % 100000 == 0) {
                System.out.println("Evaluations done: " + i);
            }
        }

        long endTime = System.nanoTime();
        System.gc(); // Sugerir GC después
        long memoryAfter = getUsedMemory();

        long durationMs = (endTime - startTime) / 1_000_000;
        long memoryUsed = memoryAfter - memoryBefore;

        System.out.println("Time elapsed: " + durationMs + " ms");
        System.out.println("Approx. memory used: " + (memoryUsed / (1024 * 1024)) + " MB");
        System.out.println("Cache size: " + problem.getLocalScoreCache().estimatedSize());
        System.out.println("Number of calls: " + Problem.numTotalCalls);
        System.out.println("Number of non-cached calls: " + Problem.numNonCachedCalls);
        System.out.println("Number of cached calls: " + (Problem.numTotalCalls - Problem.numNonCachedCalls));
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
*/

    @Test
    public void testEvaluateOrderIndependenceAndCacheUse() throws IOException {

        Problem problem = new Problem(Resources.ALARM_DATASET);

        int variableIndex = 3; // Cualquier variable del problema
        Set<Integer> parentsOriginal = new HashSet<>(Arrays.asList(0, 1, 2)); // Padres en un orden
        Set<Integer> parentsShuffled = new HashSet<>(Arrays.asList(2, 0, 1)); // Padres en un orden
        int[] parentsArray = {0, 1, 2}; // Padres en un orden
        int[] parentsArrayShuffled = {2, 0, 1}; // Padres en un orden

        // Asegura que la cache está vacía
        problem.getLocalScoreCache().invalidateAll();
        //problem.getLocalScoreCache().clear();
        Problem.numTotalCalls = 0;
        Problem.numNonCachedCalls = 0;

        // Primera llamada (debe calcular y guardar en cache)
        double score1 = problem.evaluate(variableIndex, parentsOriginal);
        int nonCachedCallsAfterFirst = Problem.numNonCachedCalls;
        int totalCallsAfterFirst = Problem.numTotalCalls;
        
        // Segunda llamada con mismo contenido pero orden distinto
        double score2 = problem.evaluate(variableIndex, parentsShuffled);
        int nonCachedCallsAfterSecond = Problem.numNonCachedCalls;
        int totalCallsAfterSecond = Problem.numTotalCalls;

        // Tercera llamada con el mismo contenido pero orden distinto
        double score3 = problem.evaluate(variableIndex, parentsArray);
        int nonCachedCallsAfterThird = Problem.numNonCachedCalls;
        int totalCallsAfterThird = Problem.numTotalCalls;

        // Cuarta llamada con el mismo contenido pero orden distinto
        double score4 = problem.evaluate(variableIndex, parentsArrayShuffled);
        int nonCachedCallsAfterFourth = Problem.numNonCachedCalls;
        int totalCallsAfterFourth = Problem.numTotalCalls;

        // Assert 1: las puntuaciones deben coincidir
        assertEquals(score1, score2, 1e-8); // La puntuación debe ser la misma sin importar el orden de los padres
        assertEquals(score1, score3, 1e-8); // La puntuación debe ser la misma sin importar el orden de los padres
        assertEquals(score1, score4, 1e-8); // La puntuación debe ser la misma sin importar el orden de los padres

        System.out.println("nonCachedCallsAfterFirst: " + nonCachedCallsAfterFirst);
        System.out.println("nonCachedCallsAfterSecond: " + nonCachedCallsAfterSecond);
        System.out.println("nonCachedCallsAfterThird: " + nonCachedCallsAfterThird);
        // Assert 2: a partir de la segunda llamada, el resultado debe venir de la cache (no incrementar nonCachedCalls)
        assertEquals(nonCachedCallsAfterFirst, nonCachedCallsAfterSecond); //La segunda llamada debe ser cacheada
        assertEquals(nonCachedCallsAfterFirst, nonCachedCallsAfterThird); // La tercera llamada debe ser cacheada
        assertEquals(nonCachedCallsAfterFirst, nonCachedCallsAfterFourth); // La cuarta llamada debe ser cacheada

        // Assert 3: la segunda llamada sí incrementa totalCalls
        assertEquals(totalCallsAfterFirst + 1, totalCallsAfterSecond); // La segunda llamada debe ser cacheada
        assertEquals(totalCallsAfterFirst + 2, totalCallsAfterThird); // La tercera llamada debe ser cacheada
        assertEquals(totalCallsAfterFirst + 3, totalCallsAfterFourth); // La cuarta llamada debe ser cacheada
        
        // Assert 4: el tamaño de la cache debe ser 1 después de las llamadas
        assertEquals(1, problem.getLocalScoreCache().asMap().size()); // La cache debe tener un tamaño de 1
        //assertEquals(1, problem.getLocalScoreCache().size()); // La cache debe tener un tamaño de 1
    }




}
