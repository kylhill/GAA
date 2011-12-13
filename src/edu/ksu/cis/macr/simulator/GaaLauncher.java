package edu.ksu.cis.macr.simulator;

import java.io.File;

import edu.ksu.cis.macr.simulator.display.WumpiWorldDisplay;
import edu.ksu.cis.macr.simulator.environment.Environment;
import edu.ksu.cis.macr.simulator.utils.XMLUtils;

/**
 * Launcher class for the Wumpi World application.
 * 
 * @author Kyle Hill
 */
public final class GaaLauncher {
    /**
     * Constructor
     */
    private GaaLauncher() {
        // Prevent instantiation
    }

    /**
     * Get the Wumpi World display
     * 
     * @return the display
     */
    public static WumpiWorldDisplay getDisplay() {
        return display;
    }

    /**
     * Get the GoalCapabilityMap associated with this instance
     * 
     * @return The GoalCapabilityMap file
     */
    public static File getGoalCapabilityMapFile() {
        return gcmFile;
    }

    /**
     * Starts up the Wumpi World application.
     * 
     * @param args
     *            the xml configuration to load the world.
     */
    public static void main(final String[] args) {
        final Environment e = Environment.getEnvironment();

        if (args.length > 1) {
            gcmFile = new File(args[1]);
            if (!gcmFile.canRead()) {
                System.err.println("Unable to read goal capability map file: " + gcmFile.getPath());
                System.exit(1);
            }

            final String configString = args[0];
            final File configFile = new File(args[0]);
            if (configFile.canRead()) {
                XMLUtils.loadFile(configString, e);
                display = new WumpiWorldDisplay();
            } else {
                System.err.println("Unable to read environment configuration file: " + configFile.getPath());
            }
        } else {
            System.err.println("Environment configuration file name and GoalCapabilityMap file name must be provided");
        }
    }

    /**
     * The Wumpi World Display
     */
    private static WumpiWorldDisplay display;

    /**
     * The GoalCapabilityMap file
     */
    private static File gcmFile = null;
}
