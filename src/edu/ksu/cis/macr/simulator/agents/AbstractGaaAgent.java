package edu.ksu.cis.macr.simulator.agents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.w3c.dom.Element;

import edu.ksu.cis.macr.agent.architecture.AssignmentTask;
import edu.ksu.cis.macr.agent.architecture.ExecutionPlan;
import edu.ksu.cis.macr.agent.architecture.RolePlan;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.organization.model.AgentImpl;
import edu.ksu.cis.macr.organization.model.Assignment;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import edu.ksu.cis.macr.simulator.GaaLauncher;
import edu.ksu.cis.macr.simulator.agent.AbstractAgent;
import edu.ksu.cis.macr.simulator.capabilities.AdvancedCommunication;
import edu.ksu.cis.macr.simulator.capabilities.AdvancedMovement;
import edu.ksu.cis.macr.simulator.capabilities.AdvancedSensors;
import edu.ksu.cis.macr.simulator.capabilities.CommunicationImpl;
import edu.ksu.cis.macr.simulator.capabilities.LocationData;
import edu.ksu.cis.macr.simulator.capabilities.OmacsInterface;
import edu.ksu.cis.macr.simulator.capabilities.map.LocationComparator;
import edu.ksu.cis.macr.simulator.capabilities.map.Map;
import edu.ksu.cis.macr.simulator.capabilities.map.MapUtils;
import edu.ksu.cis.macr.simulator.capabilities.map.Tile;
import edu.ksu.cis.macr.simulator.capability.Failure;
import edu.ksu.cis.macr.simulator.display.DisplayInformation;
import edu.ksu.cis.macr.simulator.environment.Environment;
import edu.ksu.cis.macr.simulator.goals.GoalParameters;
import edu.ksu.cis.macr.simulator.goals.SearchArea;
import edu.ksu.cis.macr.simulator.roles.AbstractGaaRole;
import edu.ksu.cis.macr.simulator.roles.AreaSearcherRole;
import edu.ksu.cis.macr.simulator.roles.interpreter.GoalCapabilityMap;
import edu.ksu.cis.macr.simulator.roles.interpreter.GoalCapabilityMapFactory;
import edu.ksu.cis.macr.simulator.roles.interpreter.RoleInterpreter;

/**
 * Common base class for user agent implementation
 * 
 * @author Kyle Hill
 */
public abstract class AbstractGaaAgent extends AbstractAgent implements GaaAgent {
    /**
     * Compares one AssignmentTask with another to impose a total ordering.
     * 
     * @author Kyle Hill
     * 
     */
    private static final class AssignmentTaskComparator implements Comparator<AssignmentTask> {
        /**
         * Compares one AssignmentTask with another to impose a total ordering
         * 
         * @param loc
         *            the location to compare all other locations within
         *            AssignmentTasks to
         */
        public AssignmentTaskComparator(final LocationData loc) {
            locationComparator = new LocationComparator(loc);
        }

        @Override
        public int compare(final AssignmentTask a1, final AssignmentTask a2) {
            final AbstractGaaRole r1 = (AbstractGaaRole) a1.getAssignment().getRole();
            final AbstractGaaRole r2 = (AbstractGaaRole) a2.getAssignment().getRole();

            // Sort by priority first: The highest numerical value is the
            // highest priority
            int delta = r2.getPriority() - r1.getPriority();

            // If the priority is equal, compare based on the goal's distance to
            // our current location, if possible.
            if (delta == 0) {
                final LocationData l1 = getLocationFromAssignmentTask(a1);
                final LocationData l2 = getLocationFromAssignmentTask(a2);

                if ((l1 != null) && (l2 != null)) {
                    delta = locationComparator.compare(l1, l2);
                }
            }
            return delta;
        }

        /**
         * The location to compare our goal locations to
         */
        private final LocationComparator locationComparator;
    }

    /**
     * CommonAgent Constructor
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
    public AbstractGaaAgent(final Environment environment, final String identifier, final int x, final int y, final Element organization) {
        super(environment, identifier, x, y, organization);

        // Construct the map
        map = new Map();

        // Create common capabilities
        mover = new AdvancedMovement(this, environment);
        sensors = new AdvancedSensors(this, environment);
        omacsInterface = new OmacsInterface(this, environment);
        communication = new AdvancedCommunication(this, environment, CommunicationImpl.MAX_RANGE, Failure.MIN_FAILURE, Failure.MIN_FAILURE);

        // Add common capabilities
        addCapability(mover);
        addCapability(sensors);
        addCapability(omacsInterface);
        setCommunicationCapability(communication);

        // Setup GUI
        final JFrame frame = new JFrame(getUniqueIdentifier().toString());
        frame.setLayout(new BorderLayout());

        final JPanel panel = new JPanel(new GridLayout(Map.MAX_Y, Map.MAX_X));
        final Dimension size = new Dimension(13, 13);
        for (int pY = Map.MAX_Y - 1; pY >= 0; pY--) {
            for (int pX = 1; pX < Map.MAX_X; pX++) {
                panels[pX][pY] = new JButton();
                panels[pX][pY].setEnabled(false);
                panels[pX][pY].setBackground(Color.lightGray);
                panels[pX][pY].setPreferredSize(size);
                panel.add(panels[pX][pY]);
            }
        }
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public final void broadcast(final String channelID, final Object content) {
        super.broadcast(channelID, content);
    }

    /**
     * Returns this agent's identifier
     * 
     * @return The agent identifier
     */
    public final AgentImpl<String> getAgentIdentifier() {
        return agentIdentifier;
    }

    @Override
    public final GoalCapabilityMap getGoalCapabilityMap() {
        // Extending classes must call setupGoalCapabilityMap() once all
        // capabilities have been added!
        assert gcm != null;

        return gcm;
    }

    /**
     * Get this agent's map
     * 
     * @return this agent's map
     */
    public final Map getMap() {
        return map;
    }

    /**
     * Get this agent's movement capability
     * 
     * @return this agent's movement capability
     */
    public final AdvancedMovement getMover() {
        return mover;
    }

    /**
     * Get this agent's OmacsInterface capability
     * 
     * @return the OmacsInterface capability
     */
    public final OmacsInterface getOmacsInterface() {
        return omacsInterface;
    }

    /**
     * Get this agent's sensors capability
     * 
     * @return the sensors capability
     */
    public final AdvancedSensors getSensors() {
        return sensors;
    }

    @Override
    public final Object receive() {
        return super.receive();
    }

    @Override
    public final void robotCode() {
        // Get some initial sensor data
        getSensors().readSensors();

        while (isAlive() && !isDone) {
            // Get new maps from other agents (free, no capabilities used)
            getSensors().readOthersMaps();

            // Remove any old, obsolete assignments
            cleanupOldAssignmentTasks();

            // Instantiate any new assignments
            createNewAssignmentTasks();

            // Get the highest priority assignment
            final AssignmentTask task = getBestAssignmentTask();
            if (task != null) {
                if (!task.equals(currentTask)) {
                    // If our current task has been interrupted, reset its plan
                    // to null so that a new RLGM will be constructed the next
                    // time it is executed
                    if (currentTask != null) {
                        currentTask.setPlan(null);
                    }
                    currentTask = task;
                }

                // If this AssignmentTask's plan is null, set a plan for it
                ExecutionPlan plan = task.getPlan();
                if (plan == null) {
                    final Assignment assignment = task.getAssignment();
                    plan = ((RolePlan) assignment.getRole()).getPlan(assignment.getInstanceGoal());
                    task.setPlan(plan);
                }

                if (!plan.isDone()) {
                    // Redraw the GUI
                    updateGui();

                    // Execute this assignment task's plan
                    plan.execute(this, task.getAssignment().getInstanceGoal());
                } else {
                    // Set the assignment's status now that we're done
                    task.setStatus(((RoleInterpreter) plan).getGoalStatus());
                }
            } else {
                isDone = true;

                // Redraw the GUI
                updateGui();
            }
        }

        // We cannot die!
        assert isAlive();
    }

    @Override
    public final DisplayInformation toDisplayInformation() {
        final DisplayInformation displayObject = super.toDisplayInformation();
        try {
            displayObject.image = ImageIO.read(new File("resources/Robot.png"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return displayObject;
    }

    /**
     * Generates new assignments for this agent based on current map data.
     */
    protected void createNewAssignmentTasks() {
        // Find the nearest unsearched location
        final LocationData location = map.findNearestUnsearchedLocation(mover.getCurrentLocation());

        if (location != null) {
            LocationData targetLocation = null;

            // Search through our current assignments for an existing
            // AreaSearcherRole
            Iterator<AssignmentTask> i = assignmentTasks.iterator();
            while (i.hasNext() && (targetLocation == null)) {
                final AssignmentTask task = i.next();
                if (task.getAssignment().getRole() instanceof AreaSearcherRole) {
                    // Try to extract the target location of the role
                    targetLocation = getLocationFromAssignmentTask(task);
                }
            }

            // If we found an existing AreaSearcher role, figure out how far it
            // is from us
            int distToTarget = Integer.MAX_VALUE;
            if (targetLocation != null) {
                distToTarget = MapUtils.getManhattanDistance(getMover().getCurrentLocation(), targetLocation);
            } else {
                i = null;
            }

            // If we're closer to another location than our current search
            // target, drop our current AreaSearcher assignment and create a new
            // one for the closer location
            if (MapUtils.getManhattanDistance(getMover().getCurrentLocation(), location) < distToTarget) {
                if (i != null) {
                    i.remove();
                }

                // Add a new assignment task to search dest
                final java.util.Map<UniqueIdentifier, Object> paramMap = new HashMap<UniqueIdentifier, Object>();
                paramMap.put(GoalParameters.LOCATION_DATA, location);

                final InstanceGoal<InstanceParameters> goal = searchArea.getInstanceGoal(searchArea, StringIdentifier.getIdentifier(searchArea.getIdentifier().toString() + location.toString()), new InstanceParameters(paramMap));
                addAssignmentTask(new AssignmentTask(new Assignment(getAgentIdentifier(), areaSearcherRole, goal)));
            }
        }
    }

    /**
     * Sets this Agent's GCM to the given parameter
     */
    protected final void setupGoalCapabilityMap() {
        final Collection<Object> capabilities = new ArrayList<Object>();
        capabilities.addAll(getCapabilities());
        gcm = GoalCapabilityMapFactory.parseMap(GaaLauncher.getGoalCapabilityMapFile(), capabilities);
    }

    /**
     * Removes any old, obsolete assignments from this agent
     */
    private final void cleanupOldAssignmentTasks() {
        // Iterate through all assignment tasks
        for (final Iterator<AssignmentTask> i = assignmentTasks.iterator(); i.hasNext();) {
            // Remove any tasks that have been finished
            final AssignmentTask task = i.next();
            if ((task.getStatus() != AssignmentTask.Status.IN_PROGRESS)) {
                i.remove();
            }
        }
    }

    /**
     * Gets the best assignment task for this agent
     * 
     * @return the best assignment task for this agent
     */
    private final AssignmentTask getBestAssignmentTask() {
        // Add all assignments to local list
        while (assignmentTasks() > 0) {
            assignmentTasks.add(pollAssignmentTask());
        }

        // Sort tasks by priority and proximity to the agent
        Collections.sort(assignmentTasks, new AssignmentTaskComparator(getMover().getCurrentLocation()));

        // Return the first (best) task
        return assignmentTasks.peek();
    }

    /**
     * Update the GUI to reflect what this agent knows about its surroundings
     */
    private final void updateGui() {
        for (int x = 1; x < Map.MAX_X; x++) {
            for (int y = 1; y < Map.MAX_Y; y++) {
                final Tile tile = map.getTile(x, y);
                if (tile.isClaimed()) {
                    panels[x][y].setBackground(Color.green);
                } else if (tile.hasObstruction()) {
                    if (tile.isWumpi()) {
                        panels[x][y].setBackground(Color.red);
                    } else if (tile.isWall()) {
                        panels[x][y].setBackground(Color.black);
                    } else {
                        panels[x][y].setBackground(Color.darkGray);
                    }
                } else {
                    if (tile.hasSmell()) {
                        panels[x][y].setBackground(Color.orange.darker());
                    } else if (tile.hasBreeze()) {
                        panels[x][y].setBackground(Color.orange);
                    } else if (tile.hasGlitter()) {
                        panels[x][y].setBackground(Color.yellow);
                    } else if (tile.hasSearched()) {
                        panels[x][y].setBackground(Color.white);
                    } else {
                        panels[x][y].setBackground(Color.lightGray);
                    }
                }
            }
        }

        // Color our own tile
        final LocationData location = mover.getCurrentLocation();
        if (isDone) {
            panels[location.getX()][location.getY()].setBackground(Color.magenta);
        } else {
            panels[location.getX()][location.getY()].setBackground(Color.blue);
        }
    }

    /**
     * Gets the LocationData associated with the given task, if any
     * 
     * @param task
     *            the task to extract LocationData from
     * @return the extracted LocationData, or null if none found
     */
    protected static final LocationData getLocationFromAssignmentTask(final AssignmentTask task) {
        LocationData location = null;

        final Object o = task.getAssignment().getInstanceGoal().getParameter();
        if (o instanceof InstanceParameters) {
            final Object p = ((InstanceParameters) o).getValue(GoalParameters.LOCATION_DATA);
            if (p instanceof LocationData) {
                location = (LocationData) p;
            }
        }
        return location;
    }

    /**
     * An agent identifier for assignment purposes
     */
    private final AgentImpl<String> agentIdentifier = new AgentImpl<String>(getUniqueIdentifier());

    /**
     * This agent's AreaSearcherRole
     */
    private final AreaSearcherRole areaSearcherRole = new AreaSearcherRole();

    /**
     * Set of assignmentTasks for this agent
     */
    private final LinkedList<AssignmentTask> assignmentTasks = new LinkedList<AssignmentTask>();

    /**
     * The agent's advanced communication capability
     */
    private final AdvancedCommunication communication;

    /**
     * The current assignment task for this agent
     */
    private AssignmentTask currentTask = null;

    /**
     * This agent's mappings of goals to capability methods
     */
    private GoalCapabilityMap gcm = null;

    /**
     * true when this agent no longer has any uncompleted AssignmentTasks
     */
    private boolean isDone = false;

    /**
     * The agent's map of the world
     */
    private final Map map;

    /**
     * The agent's advanced movement capability
     */
    private final AdvancedMovement mover;

    /**
     * The agent's goal modification capability
     */
    private final OmacsInterface omacsInterface;

    /**
     * UI panels for visible display
     */
    private final JButton[][] panels = new JButton[Map.MAX_X][Map.MAX_Y];

    /**
     * The specification goal for searching an area
     */
    private final SearchArea searchArea = new SearchArea();

    /**
     * The agent's advanced sensors capability
     */
    private final AdvancedSensors sensors;
}
