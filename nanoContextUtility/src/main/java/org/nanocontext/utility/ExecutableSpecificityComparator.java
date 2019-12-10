package org.nanocontext.utility;

import org.nanocontext.utility.Utility;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * Implements a Comparator ordering methods such that more specific parameter lists
 * precede less specific parameter lists.
 * This may be used to compare parameter lists for Method or Constructor instances
 */
public class ExecutableSpecificityComparator implements Comparator<Executable> {
    @Override
    public int compare(final Executable executable1, final Executable executable2) {
        return Utility.compareParameterTypes(executable1.getParameterTypes(), executable2.getParameterTypes());
    }
}
