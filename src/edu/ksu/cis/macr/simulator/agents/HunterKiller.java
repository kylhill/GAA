package edu.ksu.cis.macr.simulator.agents;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import edu.ksu.cis.macr.agent.architecture.AssignmentTask;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.organization.model.Assignment;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import edu.ksu.cis.macr.simulator.capabilities.AdvancedBazooka;
import edu.ksu.cis.macr.simulator.capabilities.LocationData;
import edu.ksu.cis.macr.simulator.environment.Environment;
import edu.ksu.cis.macr.simulator.goals.GoalParameters;
import edu.ksu.cis.macr.simulator.goals.KillWumpi;
import edu.ksu.cis.macr.simulator.roles.HunterKillerRole;

/**
 * The HunterKiller agent implementation
 * 
 * @author Kyle Hill
 */
public class HunterKiller extends AbstractGaaAgent {
    /**
     * HunterKiller Constructor
     * 
     * @param environment
     *            the environment
     * @param identifier
     *            a unique agent identifier
     * @param x
     *            starting x location
     * @param y
     *            starting y location
     * @param organization
     *            the organization this agent belongs to
     */
    public HunterKiller(final Environment environment, final String identifier, final int x, final int y, final Element organization) {
        super(environment, identifier, x, y, organization);

        // Create and add capabilities
        bazooka = new AdvancedBazooka(this, environment);
        addCapability(bazooka);

        setupGoalCapabilityMap();
    }

    /**
     * Returns the bazooka capability of this class
     * 
     * @return the bazooka capability of this class
     */
    public final AdvancedBazooka getBazooka() {
        return bazooka;
    }

    @Override
    protected final void createNewAssignmentTasks() {
        super.createNewAssignmentTasks();

        if (getBazooka().hasAmmo()) {
            // Find all Wumpi locations on the map and generate goals for them
            for (final LocationData loc : getMap().findWumpi(getMover().getCurrentLocation())) {
                getMap().getTile(loc).setClaimed();

                final Map<UniqueIdentifier, Object> paramMap = new HashMap<UniqueIdentifier, Object>();
                paramMap.put(GoalParameters.LOCATION_DATA, loc);

                // Add a new assignment task to fetch the newly-found Wumpi
                final InstanceGoal<InstanceParameters> goal = killWumpi.getInstanceGoal(killWumpi, StringIdentifier.getIdentifier(killWumpi.getIdentifier().toString() + loc.toString()), new InstanceParameters(paramMap));
                addAssignmentTask(new AssignmentTask(new Assignment(getAgentIdentifier(), hunterKillerRole, goal)));
            }
        }
    }

    /**
     * The agent's bazooka capability
     */
    private final AdvancedBazooka bazooka;

    /**
     * This agent's GoldFetcherRole
     */
    private final HunterKillerRole hunterKillerRole = new HunterKillerRole();

    /**
     * The SpecificationGoal for fetching gold
     */
    private final KillWumpi killWumpi = new KillWumpi();
}
