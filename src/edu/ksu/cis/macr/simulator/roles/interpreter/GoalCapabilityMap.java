package edu.ksu.cis.macr.simulator.roles.interpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.organization.model.InstanceGoal;

/**
 * The generic interface for a GoalCapabilityMap
 * 
 * @author Kyle Hill
 * 
 */
public interface GoalCapabilityMap {
    /**
     * Adds the given goalName -> capability entry mapping to the map.
     * 
     * @param goalName
     *            the goal name (capability method name) key to add to the
     *            mapping
     * @param method
     *            the method value to add to the mapping
     * @param capability
     *            the capability instance value to add to the mapping
     */
    void addMapping(final String goalName, final Method method, final Object capability);

    /**
     * Invokes the given goal (capability method name) on the object that it
     * maps to the goal in the GoalCapabilityMap with the goal's instance
     * parameters.
     * 
     * @param goal
     *            the capability to invoke
     * @return the return value of the method
     * @throws NoSuchMethodException
     *             if no method is mapped to the given goal
     * @throws IllegalArgumentException
     *             if the given goal's instance parameters do not match up with
     *             the formal parameters of the method that it maps to
     * @throws IllegalAccessException
     *             if the requested method cannot be invoked due to Java
     *             visibility restrictions
     * @throws InvocationTargetException
     *             if an exception is thrown or an assertion is hit while
     *             invoking the target method
     */
    Object invoke(final InstanceGoal<InstanceParameters> goal) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException;
}
