package io.github.jlaborda.core.common.utils;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ParentSetKeyTest {

    @Test
    public void testEquivalenceBetweenConstructors() {
        int variable = 3;

        // Entrada como Set
        Set<Integer> parentSet = new HashSet<>(Arrays.asList(1, 2, 0));

        // Entrada como array (orden diferente)
        int[] parentArray = new int[] {2, 1, 0};

        ParentSetKey keyFromSet = new ParentSetKey(variable, parentSet);
        ParentSetKey keyFromArray = new ParentSetKey(variable, parentArray);

        // Comprobamos que son iguales
        assertEquals(keyFromSet, keyFromArray,"Both keys must be equals");
        assertEquals(keyFromSet.hashCode(), keyFromArray.hashCode(),"Both keys must have the same hashcode");
    }
}
