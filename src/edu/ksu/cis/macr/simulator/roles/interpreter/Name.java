package edu.ksu.cis.macr.simulator.roles.interpreter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ParameterName defines a custom annotation type for method formal parameters
 * that allows them to retain parameter name information at runtime. This, in
 * turn, is used by the GoalCapabilityMap to map instance parameters to method
 * formal parameters.
 * 
 * @author Kyle Hill
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Name {
    /**
     * Returns the name of the associated parameter
     */
    String value();
}
