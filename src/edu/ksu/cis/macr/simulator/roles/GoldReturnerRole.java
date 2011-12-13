package edu.ksu.cis.macr.simulator.roles;

import java.io.File;

import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import edu.ksu.cis.macr.simulator.roles.interpreter.RoleLevelGoalModel;
import edu.ksu.cis.macr.simulator.roles.interpreter.RoleLevelGoalModelImpl;

/**
 * Gold Returner Role definition
 * 
 * @author Kyle Hill
 * 
 */
public class GoldReturnerRole extends AbstractGaaRole {
    /**
     * Constructor
     */
    public GoldReturnerRole() {
        super(StringIdentifier.getIdentifier("GoldReturnerRole"));
    }

    @Override
    public final int getPriority() {
        return 40;
    }

    @Override
    public final RoleLevelGoalModel getRoleLevelGoalModel(final UniqueIdentifier rootGoal) {
        if (rlgm == null) {
            rlgm = new RoleLevelGoalModelImpl(new File("models/GoldReturnerRole.goal"), rootGoal.toString());
        }
        return rlgm;
    }

    /**
     * This Role's RLGM
     */
    private RoleLevelGoalModel rlgm = null;
}
