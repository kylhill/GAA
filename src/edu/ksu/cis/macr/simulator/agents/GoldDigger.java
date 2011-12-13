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
import edu.ksu.cis.macr.simulator.capabilities.AdvancedGoldGrabber;
import edu.ksu.cis.macr.simulator.capabilities.LocationData;
import edu.ksu.cis.macr.simulator.environment.Environment;
import edu.ksu.cis.macr.simulator.goals.FetchGold;
import edu.ksu.cis.macr.simulator.goals.GoalParameters;
import edu.ksu.cis.macr.simulator.roles.GoldFetcherRole;

/**
 * The GoldDigger agent implementation
 * 
 * @author Kyle Hill
 */
public class GoldDigger extends AbstractGaaAgent {
    /**
     * GoldDigger Constructor
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
    public GoldDigger(final Environment environment, final String identifier, final int x, final int y, final Element organization) {
        super(environment, identifier, x, y, organization);

        // Create and add capabilities
        grabber = new AdvancedGoldGrabber(this, environment);
        addCapability(grabber);

        setupGoalCapabilityMap();
    }

    /**
     * Returns the Gold Grabber capability of this agent
     * 
     * @return the Gold Grabber capability of this agent
     */
    public final AdvancedGoldGrabber getGoldGrabber() {
        return grabber;
    }

    @Override
    protected final void createNewAssignmentTasks() {
        super.createNewAssignmentTasks();

        // Find all gold locations on the map and generate goals for them
        for (final LocationData loc : getMap().findGold(getMover().getCurrentLocation())) {
            getMap().getTile(loc).setClaimed();

            // Add a new assignment task to fetch the newly-found gold
            final Map<UniqueIdentifier, Object> paramMap = new HashMap<UniqueIdentifier, Object>();
            paramMap.put(GoalParameters.LOCATION_DATA, loc);

            final InstanceGoal<InstanceParameters> goal = fetchGold.getInstanceGoal(fetchGold, StringIdentifier.getIdentifier(fetchGold.getIdentifier().toString() + loc.toString()), new InstanceParameters(paramMap));
            addAssignmentTask(new AssignmentTask(new Assignment(getAgentIdentifier(), goldFetcherRole, goal)));
        }
    }

    /**
     * The SpecificationGoal for fetching gold
     */
    private final FetchGold fetchGold = new FetchGold();

    /**
     * This agent's GoldFetcherRole
     */
    private final GoldFetcherRole goldFetcherRole = new GoldFetcherRole();

    /**
     * The agent's gold grabber capability
     */
    private final AdvancedGoldGrabber grabber;
}
