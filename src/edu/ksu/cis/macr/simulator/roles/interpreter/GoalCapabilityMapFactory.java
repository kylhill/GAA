package edu.ksu.cis.macr.simulator.roles.interpreter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A factory for the creation of new GoalCapabilityMaps.
 * 
 * @author Kyle Hill
 * 
 */
public final class GoalCapabilityMapFactory {
    /**
     * Prevents Instantiation
     */
    private GoalCapabilityMapFactory() {
        // Prevent Instantiation
    }

    /**
     * Parse the given file and create a new GoalCapabilityMap to the given
     * capabilities.
     * 
     * @param file
     *            the XML file to parse
     * @param capabilities
     *            the capabilities to map
     * @return the resulting GoalCapabilitymap
     */
    public static GoalCapabilityMap parseMap(final File file, final Collection<Object> capabilities) {
        if ((file == null) || (capabilities == null)) {
            throw new IllegalArgumentException();
        }

        final GoalCapabilityMap gcm = new GoalCapabilityMapImpl();

        try {
            final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);

            // Parse GoalCapabilityMap
            final NodeList capabilityList = document.getElementsByTagName(CAPABILITY);
            for (int i = 0; i < capabilityList.getLength(); i++) {
                final Node capabilityNode = capabilityList.item(i);

                // Get class, its methods and the method's parameters
                final NamedNodeMap attributes = capabilityNode.getAttributes();
                if (attributes != null) {
                    final Class<?> capabilityClass = getClass(attributes.getNamedItem(CLASS), attributes.getNamedItem(PACKAGE));
                    if (capabilityClass != null) {
                        final Object capability = getObject(capabilityClass, capabilities);
                        if (capability != null) {
                            // Add entries to the GCM
                            addEntries(gcm, capability, capabilityNode);
                        }
                    }
                }
            }
        } catch (final SAXException e) {
            e.printStackTrace();
            assert false;
        } catch (final IOException e) {
            e.printStackTrace();
            assert false;
        } catch (final ParserConfigurationException e) {
            e.printStackTrace();
            assert false;
        }

        return gcm;
    }

    /**
     * Add new entries to the GoalCapabilityMap for the given capability and its
     * child Method nodes
     * 
     * @param gcm
     *            the map to add entries to
     * @param capability
     *            the capability the methods belong to
     * @param capabilityNode
     *            the capability node that the methd mappings belong to
     */
    private static void addEntries(final GoalCapabilityMap gcm, final Object capability, final Node capabilityNode) {
        final NodeList methodList = capabilityNode.getChildNodes();
        for (int i = 0; i < methodList.getLength(); i++) {
            final Node methodNode = methodList.item(i);
            if (methodNode.getNodeName().equals(METHOD)) {
                boolean entryAdded = false;

                final NamedNodeMap attributes = methodNode.getAttributes();
                if (attributes != null) {
                    final Node nameNode = attributes.getNamedItem(GOAL_NAME);
                    if (nameNode != null) {
                        final Method method = getMethod(capability.getClass(), methodNode);
                        if (method != null) {
                            // Add the mapping
                            gcm.addMapping(nameNode.getNodeValue(), method, capability);
                            entryAdded = true;
                        }
                    }
                }
                assert entryAdded;
            }
        }
    }

    /**
     * Get a class object for the given classNode and packageNodes. Returns null
     * if none can be found or loaded.
     * 
     * @param classNode
     *            the class node
     * @param packageNode
     *            the package node
     * @return the class object, or null if none can be found or loaded
     */
    private static Class<?> getClass(final Node classNode, final Node packageNode) {
        Class<?> newClass = null;

        if ((classNode != null) && (packageNode != null)) {
            try {
                newClass = Class.forName(packageNode.getNodeValue() + "." + classNode.getNodeValue());
            } catch (final ClassNotFoundException e) {
                // Unable to load class
                e.printStackTrace();
            }
        }

        assert newClass != null;
        return newClass;
    }

    /**
     * Returns the desired Method from the given class. Returns null if no
     * matching Method can be found.
     * 
     * @param capabilityClass
     *            the class to look in
     * @param methodNode
     *            the Method node, containing parameter information
     * @return the desired method, or null if no matching Method can be found
     */
    private static Method getMethod(final Class<?> capabilityClass, final Node methodNode) {
        Method method = null;

        final NamedNodeMap attributes = methodNode.getAttributes();
        if (attributes != null) {
            final Node nameNode = attributes.getNamedItem(DECLARED_NAME);
            if (nameNode != null) {
                try {
                    method = capabilityClass.getMethod(nameNode.getNodeValue(), getParameters(methodNode));
                } catch (final NoSuchMethodException e) {
                    // Unable to find method
                    e.printStackTrace();
                }
            }
        }

        assert method != null;
        return method;
    }

    /**
     * Get the object that corresponds to the given class from the given
     * collection. Returns null if no matching object is found.
     * 
     * @param toFind
     *            the object type to find
     * @param objects
     *            the collection to look in
     * @return the matching object, or null if none can be found.
     */
    private static Object getObject(final Class<?> toFind, final Collection<Object> objects) {
        for (final Object o : objects) {
            if (o.getClass().equals(toFind)) {
                return o;
            }
        }
        return null;
    }

    /**
     * Gets the list of parameters from the given method node
     * 
     * @param methodNode
     *            the XML node to search for parameters within
     * @return the set of parameters
     */
    private static Class<?>[] getParameters(final Node methodNode) {
        final List<Class<?>> parameters = new ArrayList<Class<?>>();

        final NodeList children = methodNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child.getNodeName().equals(PARAMETER)) {
                final NamedNodeMap attributes = child.getAttributes();
                if (attributes != null) {
                    final Class<?> newClass = getClass(attributes.getNamedItem(CLASS), attributes.getNamedItem(PACKAGE));
                    if (newClass != null) {
                        parameters.add(newClass);
                    }
                }
                assert !parameters.isEmpty();
            }
        }
        return parameters.toArray(new Class<?>[parameters.size()]);
    }

    /**
     * The capability element literal
     */
    private static final String CAPABILITY = "Capability";

    /**
     * The class attribute literal
     */
    private static final String CLASS = "class";

    /**
     * The declared name attribute literal
     */
    private static final String DECLARED_NAME = "declared_name";

    /**
     * The goal name attribute literal
     */
    private static final String GOAL_NAME = "goal_name";

    /**
     * The method element literal
     */
    private static final String METHOD = "Method";

    /**
     * The package attribute literal
     */
    private static final String PACKAGE = "package";

    /**
     * The parameter element literal
     */
    private static final String PARAMETER = "Parameter";
}
