package io.github.jlaborda.test.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.cmu.tetrad.data.BoxDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.IntDataBox;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Node;
import edu.pitt.dbmi.data.reader.Delimiter;
import edu.pitt.dbmi.data.reader.DiscreteData;
import edu.pitt.dbmi.data.reader.DiscreteDataColumn;
import edu.pitt.dbmi.data.reader.tabular.VerticalDiscreteTabularDatasetFileReader;

public class Resources {
    // EARTHQUAKE
    public static final String EARTHQUAKE_DATASET_PATH = "/datasets/earthquake.xbif_.csv";
    public static final String EARTHQUAKE_NET_PATH = "/networks/earthquake.xbif";
    public static final String EARTHQUAKE_TEST_PATH = "/datasets/tests/earthquake_test.csv";
    public static final DataSet EARTHQUAKE_DATASET = loadDatasetOrThrow(EARTHQUAKE_DATASET_PATH);

    // Variables of Earthquake's dataset
    public static final Node ALARM = EARTHQUAKE_DATASET.getVariable("Alarm");
    public static final Node MARYCALLS = EARTHQUAKE_DATASET.getVariable("MaryCalls");
    public static final Node BURGLARY = EARTHQUAKE_DATASET.getVariable("Burglary");
    public static final Node EARTHQUAKE = EARTHQUAKE_DATASET.getVariable("Earthquake");
    public static final Node JOHNCALLS = EARTHQUAKE_DATASET.getVariable("JohnCalls");

    // CANCER
    public static final String CANCER_DATASET_PATH = "/datasets/cancer.xbif_.csv";
    public static final String CANCER_NET_PATH = "/networks/cancer.xbif";
    public static final String CANCER_TEST_PATH = "/datasets/tests/cancer_test.csv";
    public static final DataSet CANCER_DATASET = loadDatasetOrThrow(CANCER_DATASET_PATH);
    

    // Variables of Cancer's dataset
    public static final Node XRAY = CANCER_DATASET.getVariable("Xray");
    public static final Node DYSPNOEA = CANCER_DATASET.getVariable("Dyspnoea");
    public static final Node CANCER = CANCER_DATASET.getVariable("Cancer");
    public static final Node POLLUTION = CANCER_DATASET.getVariable("Pollution");
    public static final Node SMOKER = CANCER_DATASET.getVariable("Smoker");

    // ALARM
    public static final String ALARM_DATASET_PATH = "/datasets/alarm.xbif_.csv";
    public static final String ALARM_NET_PATH = "/networks/alarm.xbif";
    public static final String ALARM_TEST_PATH = "/datasets/tests/alarm_test.csv";
    public static final DataSet ALARM_DATASET = loadDatasetOrThrow(ALARM_DATASET_PATH);

    public static boolean equalsEdges(Set<Edge> expected, Set<Edge> result) {
        boolean assertion = false;
        //NodeEqualityMode.setEqualityMode(NodeEqualityMode.Type.NAME);
        for (Edge resEdge : result) {
            for (Edge expEdge : expected) {
                if (expEdge.equals(resEdge)) {
                    assertion = true;
                    break;
                }
            }
            if (!assertion)
                return false;
        }
        return assertion;
    }
    

/**
     * Stores the data from a csv as a DataSet object.
     * @param path
     * Path to the csv file.
     * @return DataSet containing the data from the csv file.
     * @throws IOException If path is incorrect, an error occurs while reading the file.
     */
    private static DataSet readData(Path path) throws IOException{
        // Reading data with new data-reader library
        VerticalDiscreteTabularDatasetFileReader datasetReader = new VerticalDiscreteTabularDatasetFileReader(path, Delimiter.COMMA);
        datasetReader.setHasHeader(true);
        DiscreteData data = (DiscreteData) datasetReader.readInData();

        // Data to DataSet
        List<Node> variables = new ArrayList<>();
        for (DiscreteDataColumn column : data.getDataColumns()) {
            String varName = column.getDataColumn().getName();
            List<String> categories = column.getCategories();//((TabularDataColumn) column.getDataColumn());
            variables.add(new DiscreteVariable(varName,categories));
        } 

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

        // Creating DataSet
        IntDataBox dataBox = new IntDataBox(transposed);
        return new BoxDataSet(dataBox, variables);
    }


    /**
     * Método auxiliar para cargar un dataset. Si falla, lanza un error claro.
     * Esto evita los NullPointerException silenciosos.
     */
    private static DataSet loadDatasetOrThrow(String resourcePath) {
        try {
            return loadDataSetFromResource(resourcePath);
        } catch (IOException e) {
            // Lanza un error de inicialización que detendrá los tests y te dirá exactamente qué falló.
            throw new ExceptionInInitializerError("Failed to load resource: " + resourcePath + ". Error: " + e.getMessage());
        }
    }

    /**
     * Loads a DataSet from a resource located in the classpath.
     * It handles the creation and cleanup of a temporary file.
     *
     * @param resourcePath The path to the resource (e.g., "/datasets/earthquake.csv").
    * @return The loaded DataSet.
    * @throws IOException If the resource cannot be found or read.
    */
    public static DataSet loadDataSetFromResource(String resourcePath) throws IOException {
        Path tempFile = null;
        // Usamos try-with-resources para asegurar que el InputStream se cierre
        try (InputStream inputStream = Resources.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            // 1. Crear un fichero temporal con un prefijo y sufijo
            tempFile = Files.createTempFile("horizonBN-dataset-", ".csv");

            // 2. Copiar el contenido del recurso al fichero temporal
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 3. Llamar a tu método original con el Path del fichero temporal
            return readData(tempFile);

        } finally {
            // 4. (CRUCIAL) Asegurarse de borrar el fichero temporal
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
            }
        }
    }
}
