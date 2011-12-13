package edu.ksu.cis.macr.simulator.goals;

import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;

/**
 * Goal Parameter definitions
 * 
 * @author Kyle Hill
 * 
 */
public final class GoalParameters {
    /**
     * Constructor
     */
    private GoalParameters() {
        // Prevent Instantiation
    }

    /**
     * Goals parameterized with LOCATION_DATA contain information about the
     * locations that are relevant to the goal being pursued.
     */
    public static final UniqueIdentifier LOCATION_DATA = StringIdentifier.getIdentifier("loc");
}
