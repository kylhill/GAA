package edu.ksu.cis.macr.simulator.goals;

import edu.ksu.cis.macr.goal.model.ParameterizedSpecificationGoal;
import edu.ksu.cis.macr.goal.model.SpecificationParameters;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;

/**
 * Kill Wumpi Goal
 * 
 * @author Kyle Hill
 */
public final class KillWumpi extends ParameterizedSpecificationGoal {
    /**
     * Constructs a new instance of <code>KillWumpi</code>.
     */
    public KillWumpi() {
        super(StringIdentifier.getIdentifier("KillWumpi"), new SpecificationParameters(GoalParameters.LOCATION_DATA));
    }
}
