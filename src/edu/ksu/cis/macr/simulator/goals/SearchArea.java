package edu.ksu.cis.macr.simulator.goals;

import edu.ksu.cis.macr.goal.model.ParameterizedSpecificationGoal;
import edu.ksu.cis.macr.goal.model.SpecificationParameters;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;

/**
 * Map Exploration Goal
 * 
 * @author Kyle Hill
 */
public final class SearchArea extends ParameterizedSpecificationGoal {
    /**
     * Constructs a new instance of <code>ExploreMap</code>.
     */
    public SearchArea() {
        super(StringIdentifier.getIdentifier("SearchArea"), new SpecificationParameters(GoalParameters.LOCATION_DATA));
    }
}
