package edu.ksu.cis.macr.simulator.roles.interpreter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;

/**
 * A Goal Capability Map
 * 
 * @author Kyle Hill
 * 
 */
public class GoalCapabilityMapImpl implements GoalCapabilityMap {
    /**
     * A class to represent the mappings used in the GoalCapabilityMap
     * 
     * @author Kyle Hill
     * 
     */
    private static final class CapabilityMapEntry {
        /**
         * Constructs a new CapabilityMapEntry.
         * 
         * @param c
         *            the capability
         * @param m
         *            the method
         */
        public CapabilityMapEntry(final Method m, final Object c) {
            if ((m == null) || (c == null)) {
                throw new IllegalArgumentException();
            }
            method = m;
            capability = c;
        }

        /**
         * Returns this entry's capability.
         * 
         * @return this entry's capability.
         */
        public Object getCapability() {
            return capability;
        }

        /**
         * Returns this entry's method.
         * 
         * @return this entry's method.
         */
        public Method getMethod() {
            return method;
        }

        /**
         * This entry's capability.
         */
        private final Object capability;

        /**
         * This entry's method.
         */
        private final Method method;
    }

    @Override
    public final void addMapping(final String goalName, final Method method, final Object capability) {
        if ((goalName == null) || (goalName.length() < 1) || (method == null) || (capability == null)) {
            throw new IllegalArgumentException();
        }
        map.put(goalName, new CapabilityMapEntry(method, capability));
    }

    @Override
    public final Object invoke(final InstanceGoal<InstanceParameters> goal) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final CapabilityMapEntry entry = map.get(goal.getSpecificationIdentifier().toString());
        if (entry != null) {
            // Get method formal parameters
            final Method method = entry.getMethod();

            // Try to match the given instance parameters with the
            // appropriate formal parameter
            final List<Object> actualParameters = new ArrayList<Object>();
            for (final String parameterName : getParameterNames(method)) {
                boolean paramMapped = false;

                final InstanceParameters instanceParameters = goal.getParameter();
                if (instanceParameters != null) {
                    final Object actualParameter = instanceParameters.getValue(StringIdentifier.getIdentifier(parameterName));
                    if (actualParameter != null) {
                        actualParameters.add(actualParameter);
                        paramMapped = true;
                    }
                }

                if (!paramMapped) {
                    // We were unable to find an suitable argument for the
                    // formal parameter in the set of instance parameters
                    throw new IllegalArgumentException();
                }
            }

            // Invoke the method call
            return method.invoke(entry.getCapability(), actualParameters.toArray());
        }
        throw new NoSuchMethodException();
    }

    /**
     * Get the list of parameter names associated with the given method
     * 
     * @param method
     *            the method to get the parameter names of
     * @return the names, in declaration order, of the given method's formal
     *         parameters
     * @throws IllegalArgumentException
     *             if no @Name annotation is found all of the given method's
     *             parameters
     */
    private static final List<String> getParameterNames(final Method method) throws IllegalArgumentException {
        final List<String> parameterNames = new ArrayList<String>();

        for (final Annotation[] annotations : method.getParameterAnnotations()) {
            boolean annotationFound = false;
            for (final Annotation annotation : annotations) {
                if (annotation instanceof Name) {
                    parameterNames.add(((Name) annotation).value());
                    annotationFound = true;
                    break;
                }
            }

            if (!annotationFound) {
                // All capability methods that we wish to execute
                // through an RLGM must be annotated with @Name
                throw new IllegalArgumentException();
            }
        }

        if (parameterNames.size() != method.getParameterTypes().length) {
            // The number of annotations did not match the number of formal
            // method parameters
            throw new IllegalArgumentException();
        }
        return parameterNames;
    }

    /**
     * A mapping of strings to Objects that contain those strings as methods
     */
    private final Map<String, CapabilityMapEntry> map = new HashMap<String, CapabilityMapEntry>();
}
