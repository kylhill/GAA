package edu.ksu.cis.macr.simulator.roles;

import java.io.File;

import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import edu.ksu.cis.macr.simulator.roles.interpreter.RoleLevelGoalModel;
import edu.ksu.cis.macr.simulator.roles.interpreter.RoleLevelGoalModelImpl;

/**
 * The Area Searcher Role definition
 * 
 * @author Kyle Hill
 * 
 */
public class AreaSearcherRole extends AbstractGaaRole {
    /**
     * Constructor
     */
    public AreaSearcherRole() {
        super(StringIdentifier.getIdentifier("AreaSearcherRole"));
    }

    @Override
    public final int getPriority() {
        return 10;
    }

    @Override
    public final RoleLevelGoalModel getRoleLevelGoalModel(final UniqueIdentifier rootGoal) {
        if (rlgm == null) {
            rlgm = new RoleLevelGoalModelImpl(new File("models/AreaSearcherRole.goal"), rootGoal.toString());
        }
        return rlgm;
    }

    /**
     * This Role's RLGM
     */
    private RoleLevelGoalModel rlgm = null;
}
