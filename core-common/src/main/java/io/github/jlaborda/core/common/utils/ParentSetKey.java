package io.github.jlaborda.core.common.utils;

import java.util.BitSet;
import java.util.Set;

public class ParentSetKey {
    private final int variable;
    private final BitSet parentMask;

    public ParentSetKey(int variable, Set<Integer> parents) {
        this.variable = variable;
        this.parentMask = new BitSet();
        for (Integer p : parents) {
            parentMask.set(p);
        }
    }

    public ParentSetKey(int variable, int[] parents){
        this.variable = variable;
        this.parentMask = new BitSet();
        for (int p : parents) {
            parentMask.set(p);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParentSetKey)) return false;
        ParentSetKey other = (ParentSetKey) o;
        return this.variable == other.variable && this.parentMask.equals(other.parentMask);
    }

    @Override
    public int hashCode() {
        return 31 * Integer.hashCode(variable) + parentMask.hashCode();
    }

}