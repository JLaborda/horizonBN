package io.github.jlaborda.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;

/**
 * Esta clase de test verifica que la clase Resources carga correctamente
 * los datasets y las variables desde el classpath.
 */
class ResourcesTest {

    @Test
    void earthquakeDataset_shouldBeLoadedAndNotNull() {
        // 1. Arrange & Act: La carga se hace en el bloque static de la clase Resources.
        DataSet dataset = Resources.EARTHQUAKE_DATASET;

        // 2. Assert: Verificamos que el dataset no es nulo y tiene las dimensiones esperadas.
        assertNotNull(dataset, "El dataset EARTHQUAKE no debería ser nulo. Revisa la ruta y el fichero.");
        
        // Puedes añadir aserciones más específicas para estar seguro.
        assertEquals(5, dataset.getNumColumns(), "El dataset Earthquake debería tener 5 columnas.");
        //assertEquals(5, dataset.getNumVariables(), "El dataset Earthquake debería tener 5 variables.");
        // assertEquals(10000, dataset.getNumRows(), "El dataset Earthquake debería tener 10000 filas.");
    }

    @Test
    void cancerDataset_shouldBeLoadedAndNotNull() {
        // Arrange & Act
        DataSet dataset = Resources.CANCER_DATASET;

        // Assert
        assertNotNull(dataset, "El dataset CANCER no debería ser nulo.");
        assertEquals(5, dataset.getNumColumns(), "El dataset Cancer debería tener 5 columnas.");
    }

    @Test
    void earthquakeVariables_shouldBeInitializedCorrectly() {
        // Arrange & Act
        Node earthquakeNode = Resources.EARTHQUAKE;
        Node alarmNode = Resources.ALARM;

        // Assert
        assertNotNull(earthquakeNode, "La variable EARTHQUAKE no debería ser nula.");
        assertNotNull(alarmNode, "La variable ALARM no debería ser nula.");
        
        assertEquals("Earthquake", earthquakeNode.getName());
        assertEquals("Alarm", alarmNode.getName());
    }

    @Test
    void cancerVariables_shouldBeInitializedCorrectly() {
        // Arrange & Act
        Node cancerNode = Resources.CANCER;
        Node smokerNode = Resources.SMOKER;
        
        // Assert
        assertNotNull(cancerNode, "La variable CANCER no debería ser nula.");
        assertNotNull(smokerNode, "La variable SMOKER no debería ser nula.");

        assertEquals("Cancer", cancerNode.getName());
        assertEquals("Smoker", smokerNode.getName());
    }
}