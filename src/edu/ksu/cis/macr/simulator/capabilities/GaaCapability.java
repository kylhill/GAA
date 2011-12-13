package edu.ksu.cis.macr.simulator.capabilities;

import edu.ksu.cis.macr.simulator.capability.CapabilityAction;

/**
 * Capability interface that indicates that implementing classes have annotated
 * each Method parameter in the public interface they wish to export to the
 * RoleInterpreter with @Name annotations.
 * 
 * @author Kyle Hill
 * 
 */
public interface GaaCapability extends CapabilityAction {
    // This interface exists to indicate that implementing classes have
    // annotated each Method parameter in the public interface they wish to
    // export to the RoleInterpreter with @Name annotations.
}
