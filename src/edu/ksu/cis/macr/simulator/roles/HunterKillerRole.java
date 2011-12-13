package edu.ksu.cis.macr.simulator.roles;

import java.io.File;

import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import edu.ksu.cis.macr.simulator.roles.interpreter.RoleLevelGoalModel;
import edu.ksu.cis.macr.simulator.roles.interpreter.RoleLevelGoalModelImpl;

/**
 * The Hunter Killer role definition
 * 
 * @author Kyle Hill
 * 
 */
public class HunterKillerRole extends AbstractGaaRole {
    /**
     * Constructor
     */
    public HunterKillerRole() {
        super(StringIdentifier.getIdentifier("HunterKillerRole"));
    }

    @Override
    public final int getPriority() {
        return 20;
    }

    @Override
    public final RoleLevelGoalModel getRoleLevelGoalModel(final UniqueIdentifier rootGoal) {
        if (rlgm == null) {
            rlgm = new RoleLevelGoalModelImpl(new File("models/HunterKillerRole.goal"), rootGoal.toString());
        }
        return rlgm;
    }

    /**
     * This Role's RLGM
     */
    private RoleLevelGoalModel rlgm = null;
}
