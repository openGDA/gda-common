/*-
 * Copyright © 2012 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.configuration.properties;

import static java.io.File.separator;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility singleton class which allows the getting of Java properties from a local source file or standard System properties.
 */
public final class LocalProperties {
	private static final Logger logger = LoggerFactory.getLogger(LocalProperties.class);

	private LocalProperties() {
		// Prevent instances
	}

	/**
	 * Along with {@link #GDA_GIT_LOC} replaces the gda.root variable.
	 * <p>
	 * The system property which defines the location of the some of the GDA installation. Within this folder should be the IDE's .metadata folder and the
	 * third-party plugin, plus any svn checkouts.
	 * <p>
	 * At Diamond, the folder structure is, by convention:
	 * </p>
	 *
	 * <pre>
	 * <folder named after GDA release version>/
	 *   |
	 *   |-> workspace/                      # GDA_WORKSPACE_LOC relates to this folder
	 *          |->tp                        # thirdparty plugin
	 *          |->plugins                   # checkout of plugin projects remaining in svn
	 *          |->features                  # checkout of feature projects remaining in svn
	 *   |
	 *   |->workspace_loc/                   # {@link #GDA_GIT_LOC} relates to this folder
	 *          |->gda-xas-core.git/         # folders of each git repository used in this installation at this level
	 *                  |->uk.ac.gda.core/   # each plugin project within this git repository at this level
	 * </pre>
	 * <p>
	 * It should not be assumed that the configuration files are relative to this location. This is defined by GDA_CONFIG
	 */
	public static final String GDA_WORKSPACE_LOC = "gda.install.workspace.loc";

	/**
	 * Along with {@link #GDA_WORKSPACE_LOC} replaces the gda.root variable.
	 * <p>
	 * The system property which defines the top-level folder holding the various git repositories which make up this gda installation.
	 * <p>
	 * It should not be assumed that the configuration files are relative to this location. This is defined by GDA_CONFIG
	 */
	public static final String GDA_GIT_LOC = "gda.install.git.loc";

	/**
	 * Property that sets the top-level directory where data is written to. The actual directory the data writers should use is defined by
	 * gda.data.scan.datawriter.datadir. That property may be dynamic and vary as the current visit varies, but the gda.data property should be static at
	 * runtime.
	 */
	public static final String GDA_DATA = "gda.data";

	/**
	 * Name of class in package gda.data.scan.datawriter that is called when ScanDataPoints are available to be written Must support interface DataWriter
	 */
	public static final String GDA_DATA_SCAN_DATAWRITER_DATAFORMAT = "gda.data.scan.datawriter.dataFormat";

	/**
	 * Property used to provide the path of the root of the visit
	 */
	public static final String GDA_VISIT_DIR = "gda.paths.visitdirectory";

	/**
	 * Property used to provide the 'default' property to gda.data.PathConstructor (in uk.ac.gda.core). This in turn is used by *many* classes to determine
	 * where scan files or images should be written.
	 */
	public static final String GDA_DATAWRITER_DIR = "gda.data.scan.datawriter.datadir";

	/**
	 * The directory in which to keep NumTracker files. (See NumTracker for alternative ways to specify this).
	 */
	public static final String GDA_DATA_NUMTRACKER = "gda.data.numtracker";

	/**
	 * Property that specifies the GDA configuration folder.
	 */
	public static final String GDA_CONFIG = "gda.config";

	/**
	 * The location of a global read-write directory for persistence of information which is continued to be used on the same beamline from version to version
	 * of GDA.
	 * <p>
	 * Has been config/var but the recommended location is outside of the configuration directory, at the same level that different GDA installations are
	 * located.
	 * <p>
	 * Directory in which gda.data.NumTracker files are stored.
	 */
	public static final String GDA_VAR_DIR = "gda.var";

	/**
	 * Property that specifies the folder into which all logs files should be placed.
	 */
	public static final String GDA_LOGS_DIR = "gda.logs.dir";

	/**
	 * Property that specifies a single GDA properties file.
	 */
	public static final String GDA_PROPERTIES_FILE = "gda.propertiesFile";

	/**
	 * Property that specifies the GDA factory name, e.g. "stnBase" or "i04-1".
	 */
	public static final String GDA_FACTORY_NAME = "gda.factory.factoryName";

	/**
	 * Boolean property that indicates whether GDA is using the dummy mode configuration.
	 */
	public static final String GDA_DUMMY_MODE_ENABLED = "gda.dummy.mode";

	/**
	 * Property that specifies the GDA running mode, e.g. "dummy", "live" or any other defined running mode
	 */
	public static final String GDA_MODE = "gda.mode";

	/**
	 * Boolean property that indicates whether GDA access control is enabled.
	 */
	public static final String GDA_ACCESS_CONTROL_ENABLED = "gda.accesscontrol.useAccessControl";

	/**
	 * Boolean property that indicates whether GDA baton management is enabled.
	 */
	public static final String GDA_BATON_MANAGEMENT_ENABLED = "gda.accesscontrol.useBatonControl";

	/**
	 * Boolean property that indicates that clients with the same user and visit can share the baton.
	 */
	private static final String GDA_BATON_SHARING_ENABLED = "gda.accesscontrol.sameUserVisitShareBaton";


	/**
	 * Boolean property that indicates that client should when started [if baton control is active
	 * AND the baton is held by another client on a different visit] use the reduced GUI start up
	 */

	private static final String GDA_GUI_REDUCED_ENABLED = "gda.accesscontrol.useReducedGUI";

	/**
	 * Property that specifies the server-side XML file.
	 */
	public static final String GDA_OBJECTSERVER_XML = "gda.objectserver.xml";

	/**
	 * Property that specifies the time in millis between isBusy polls in ScannableBase#waitWhileBusy().
	 */
	public static final String GDA_SCANNABLEBASE_POLLTIME = "gda.scannablebase.polltime.millis";

	/**
	 * Property that specifies the client-side XML file.
	 */
	public static final String GDA_GUI_XML = "gda.gui.xml";

	/**
	 * XML file used by the RCP client.
	 */
	public static final String GDA_GUI_BEANS_XML = "gda.gui.beans.xml";

	/**
	 * File containing beam centre and beam size values for each zoom level; read by gda.images.camera.BeamDataComponent in uk.ac.gda.px.
	 */
	public static final String GDA_IMAGES_DISPLAY_CONFIG_FILE = "gda.images.displayConfigFile";

	/**
	 * When reading {@link #GDA_IMAGES_DISPLAY_CONFIG_FILE}, if this is set to {@code true}, the first {@code
	 * crosshairX} and {@code crosshairY} values will be used for all zoom levels.
	 */
	public static final String GDA_IMAGES_SINGLE_BEAM_CENTRE = "gda.images.SingleBeamCenter";

	/**
	 * Beamline name, e.g. {@code "i02"}.
	 */
	public static final String GDA_BEAMLINE_NAME = "gda.beamline.name";

	/**
	 * The on-screen sample image shows the X axis from left to right, but the image can be flipped. This property indicates which edge of the image is the +ve
	 * side - {@code "left"} or {@code "right"}.
	 */
	public static final String GDA_IMAGES_HORIZONTAL_DIRECTION = "gda.images.horizontaldirection";

	/**
	 * Property that allows the beamline-specific orientation of the X/Y/Z axes to be specified. It should be a matrix, in the form
	 */
	public static final String GDA_PX_SAMPLE_CONTROL_AXIS_ORIENTATION = "gda.px.samplecontrol.axisorientation";

	/**
	 * Property that specifies the direction of a +ve omega rotation when viewed from behind the goniometer, with the beam going from left to right. Should be
	 * "clockwise" or "anticlockwise".
	 */
	public static final String GDA_PX_SAMPLE_CONTROL_OMEGA_DIRECTION = "gda.px.samplecontrol.omegadirection";

	/**
	 * Whether beam axis movements should be considered when moving the sample. Should be {@code true} or {@code false}.
	 */
	public static final String GDA_PX_SAMPLE_CONTROL_ALLOW_BEAM_AXIS_MOVEMENT = "gda.px.samplecontrol.allowbeamaxismovement";

	/**
	 * Default visit number if an ICAT system is not specified; or connection to ICAT fails; or user is a member of staff and has not other available visit ID
	 * in the ICAT system.
	 */
	public static final String GDA_DEF_VISIT = "gda.defVisit";

	/**
	 * The name of the instrument. This property should always be defined.
	 * <p>
	 * If <code>gda.beamline.name</code> is defined, as it should be on all Diamond beamlines, then this property
	 * should be considered a synonym for that one and therefore must always be set to the same value.
	 * The more general name is intended for when GDA is used outside outside of a beamline.
	 * <p>
	 */
	public static final String GDA_INSTRUMENT = "gda.instrument";

	/**
	 * An optional property giving the end station name, if different to the beamline name, e.g. PEEM or VXMi.
	 */
	public static final String GDA_END_STATION_NAME = "gda.endstation.name";

	public static final String GDA_FACILITY = "gda.facility";

	/**
	 * This is the default visit that will be used if no default visit is specified by the {@link #GDA_DEF_VISIT} property.
	 */
	public static final String DEFAULT_VISIT = "0-0";

	/**
	 * The visit which the current RCP application is running under. This should NOT be set in a java.properties file but set at runtime once the RCP process
	 * has identified which value it wishes to use.
	 * <p>
	 * For times when the metadata value is misleading to client-side objects.
	 */
	public static final String RCP_APP_VISIT = "gda.rcp.application.this.visit";

	/**
	 * The user name (federalid) which the current RCP application is running under. This should NOT be set in a java.properties file but set at runtime once
	 * the RCP process has identified which value it wishes to use.
	 * <p>
	 * For times when the metadata value is misleading to client-side objects.
	 */
	public static final String RCP_APP_USER = "gda.rcp.application.this.user";

	/**
	 * Extension to be used for NumTracker - to keep Nexus and SrsDataFile in step
	 */
	public static final String GDA_DATA_NUMTRACKER_EXTENSION = "gda.data.numtracker.extension";

	/**
	 * The number of ScanDataPoints that can be in a gda.scan.MultithreadedScanDataPointPipeline before it starts blocking new requests. i.e. the number of
	 * points 'behind' the collection completed points can get.
	 */
	public static final String GDA_SCAN_MULTITHREADED_SCANDATA_POINT_PIPElINE_LENGTH = "gda.scan.multithreadedScanDataPointPipeline.length";

	/**
	 * The number of ScanDataPoints that can be in a gda.scan.MultithreadedScanDataPointPipeline before it starts blocking new requests. i.e. the number of
	 * points 'behind' the collection completed points can get.
	 */
	public static final String GDA_SCAN_CONCURRENTSCAN_READOUT_CONCURRENTLY = "gda.scan.concurrentScan.readoutConcurrently";

	/**
	 * The number of threads used by a scan to convert position Callables from PositionCallableProviding Scannables to Object positions.
	 */
	public static final String GDA_SCAN_MULTITHREADED_SCANDATA_POINT_PIPElINE_POINTS_TO_COMPUTE_SIMULTANEOUSELY = "gda.scan.multithreadedScanDataPointPipeline.pointsToComputeSimultaneousely";

	/**
	 * Option to force application window to open with Intro / Welcome screen (default usually false)
	 */
	public static final String GDA_GUI_FORCE_INTRO = "gda.gui.window.force.intro";

	/**
	 * Option to save and restore the GUI state between sessions. Default 'true'. If 'true' the setting to force the Intro/Welcome Screen may have no effect
	 */
	public static final String GDA_GUI_SAVE_RESTORE = "gda.gui.save.restore";

	/**
	 * Starting width for the GDA application window
	 */
	public static final String GDA_GUI_START_WIDTH = "gda.gui.window.start.width";

	/**
	 * Starting height for the GDA application window
	 */
	public static final String GDA_GUI_START_HEIGHT = "gda.gui.window.start.height";

	/**
	 * Maximise the application window at startup
	 */
	public static final String GDA_GUI_START_MAXIMISE = "gda.gui.window.start.maximise";

	/**
	 * Prefix for the title of the GDA window
	 */
	public static final String GDA_GUI_TITLEBAR_PREFIX = "gda.gui.titlebar.prefix";
	public static final String GDA_GUI_TITLEBAR_SUFFIX = "gda.gui.titlebar.suffix";
	/**
	 * Option to display RCP Workbench default menus (default usually true)
	 */
	public static final String GDA_GUI_USE_ACTIONS_NEW = "gda.gui.useNewActions";
	public static final String GDA_GUI_USE_ACTIONS_SEARCH = "gda.gui.useSearchActions";
	public static final String GDA_GUI_USE_ACTIONS_RUN = "gda.gui.useRunActions";
	public static final String GDA_GUI_USE_ACTIONS_PERSPECTIVE_CUSTOM = "gda.gui.usePerspectiveCustomActions";
	public static final String GDA_GUI_USE_ACTIONS_NEW_EDITOR = "gda.gui.useNewEditorActions";
	public static final String GDA_GUI_USE_ACTIONS_NEW_WINDOW = "gda.gui.useNewWindowActions";
	public static final String GDA_GUI_USE_ACTIONS_EXPORT = "gda.gui.useExportActions";
	public static final String GDA_GUI_USE_ACTIONS_IMPORT = "gda.gui.useImportActions";

	/**
	 * Option to display the RCP Perspective bar
	 */
	public static final String GDA_GUI_USE_PERSPECTIVE_BAR = "gda.gui.usePerspectiveBar";

	/**
	 * Option to display the RCP main tool bar
	 */
	public static final String GDA_GUI_USE_TOOL_BAR = "gda.gui.useToolBar";

	/**
	 * Command to execute when "Stop all" button in status line is clicked
	 */
	public static final String GDA_GUI_STOP_ALL_COMMAND_ID = "gda.gui.stop.all.command.id";

	/**
	 * Client properties to be checked for locating the STOP ALL button on the status line
	 */
	public static final String GDA_GUI_FORCE_LEFT_STOP_ALL = "gda.gui.statusline.forceStopAllLeft";
	public static final String GDA_GUI_STATUS_HIDE_STOP_ALL = "gda.gui.statusline.hideStopAll";

	public static final String GDA_SCAN_SETS_SCANNUMBER = "gda.scan.sets.scannumber";

	public static final String GDA_ACTIVEMQ_BROKER_URI = "gda.activemq.broker.uri";

	/**
	 * Option to display visit name as data folder name in Data Project
	 */
	public static final String GDA_SHOW_VISIT_NAME_AS_DATA_FOLDER_NAME = "gda.show.visit.name.as.data.folder.name";

	/**
	 * Location of the server status port
	 */
	public static final String GDA_SERVER_HOST = "gda.server.host";

	/**
	 * Port number on which client can obtain status information from the GDA server
	 */
	public static final String GDA_SERVER_STATUS_PORT = "gda.server.statusPort";

	/**
	 * Default value for {@link #GDA_SERVER_STATUS_PORT}
	 */
	public static final int GDA_SERVER_STATUS_PORT_DEFAULT = 19999;

	public static String getActiveMQBrokerURI() {
		return get(GDA_ACTIVEMQ_BROKER_URI,
				String.format("failover:(tcp://%s:%d?daemon=true)?startupMaxReconnectAttempts=3", get(GDA_SERVER_HOST, "localhost"), 61616));
	}

	public static void setActiveMQBrokerURI(final String brokerURI) {
		set(GDA_ACTIVEMQ_BROKER_URI, brokerURI);
	}

	/**
	 * Use to undo {@link LocalProperties#forceActiveMQEmbeddedBroker()} between unit tests i.e. call from @org.junit.AfterClass annotated tearDownClass method.
	 */
	public static void unsetActiveMQBrokerURI() {
		setActiveMQBrokerURI(null);
		System.clearProperty("GDA/" + GDA_ACTIVEMQ_BROKER_URI);
	}

	/**
	 * For <a href="http://activemq.apache.org/how-to-unit-test-jms-code.html"/>unit tests</a> i.e. call from @org.junit.BeforeClass annotated setUpClass
	 * method. Undo with {@link LocalProperties#unsetActiveMQBrokerURI()} (or {@link LocalProperties#setActiveMQBrokerURI(String)}).
	 */
	public static void forceActiveMQEmbeddedBroker() {
		setActiveMQBrokerURI("vm://localhost?broker.persistent=false");
		// As ActiveMQSessionService cannot depend on this, must use System property.
		// Equivalent is done when reading from property config but not when setting config.
		System.setProperty("GDA/" + GDA_ACTIVEMQ_BROKER_URI, "vm://localhost?broker.persistent=false");
	}

	public static boolean isScanSetsScanNumber() {
		return LocalProperties.check(LocalProperties.GDA_SCAN_SETS_SCANNUMBER);
	}

	public static void setScanSetsScanNumber(boolean enable) {
		LocalProperties.set(LocalProperties.GDA_SCAN_SETS_SCANNUMBER, Boolean.toString(enable));
	}

	/**
	 * Property for setting where there is a dataserver running that can access the SWMR files and provide remote datasets
	 */
	public static final String GDA_DATASERVER_HOST = "gda.dataserver.host";

	/**
	 * Property for setting which port has a dataserver running that can access the SWMR files and provide remote datasets
	 */
	public static final String GDA_DATASERVER_PORT = "gda.dataserver.port";

	/**
	 * Property to set the initial length units for fields (xStart, fastAxisStep etc) in the Mapping GUI
	 */
	public static final String GDA_INITIAL_LENGTH_UNITS = "uk.ac.gda.client.defaultUnits";

	/**
	 * Control whether client should be closed automatically when user session expires
	 */
	public static final String GDA_CHECK_USER_VISIT_VALID = "uk.ac.gda.client.check.user.visit.valid";

	/**
	 * Property to choose whether to use the persistence service or file base persistence
	 */
	public static final String GDA_PERSISTENCE_SERVICE_ENABLED = "uk.ac.diamond.persistence.manager.enabled";

	// create Jakarta properties handler object
	// README - The JakartaPropertiesConfig class automatically picks up
	// system
	// properties on creation, so they are guaranteed to be present.
	private static PropertiesConfig propConfig = new JakartaPropertiesConfig();

	static {
		loadProperties();
	}

	private static void loadProperties() {
		// Try to get the location of the property file from a existing property (e.g. from system property)
		String propertiesFile = propConfig.getString(GDA_PROPERTIES_FILE, null);
		if (propertiesFile == null || propertiesFile.isEmpty()) {
			logger.warn("{} is not set. Trying to load properties from default location", GDA_PROPERTIES_FILE);
			// assume file is ${gda.config}/properties/java.properties
			propertiesFile = LocalProperties.getConfigDir() + separator + "properties" + separator + "java.properties";
		}
		File testExists = new File(propertiesFile);

		if (!testExists.exists()) {
			logger.error("Property file could not be found! - no properties are available");
		} else {
			try {
				propConfig.loadPropertyData(propertiesFile);
			} catch (ConfigurationException ex) {
				throw new IllegalArgumentException("Error loading " + propertiesFile, ex);
			}

			// We attempt to set all the properties loaded into System properties
			// This allows properties to be loaded from any bundle without making
			// a dependency on this bundle. However if this fails in any way then
			// it is a non-fatal error. This means that any bundle in any project may
			// check GDA properties without making dependencies. This is desirable for
			// instance with DAWN so that its bundles may contain specific code for
			// GDA configuration without making a hard dependency on LocalProperties
			try {
				for (Iterator<String> it = propConfig.getKeys(); it.hasNext();) {
					String key = it.next();
					String value = propConfig.getString(key, null);
					if (System.getProperty(key) == null && value != null) {
						System.setProperty("GDA/" + key, value);
					}
					// We preface with "GDA/" which should mean that no system
					// property is affected and also if System properties are dumped,
					// they can be filtered to remove GDA ones.
				}
			} catch (Exception ne) {
				logger.error("Cannot parse to system properties: {}", propertiesFile, ne);
			}
		}
	}

	/**
	 * <b>WARNING!</b> This method reloads ALL the properties. The reason of this method is double:
	 * <ol>
	 * <li>It solves the problem when different Junit tests use different properties files</li>
	 * <li>May allow, despite is untested, a hot reload of properties without restarting the server</li>
	 * </ol>
	 */
	public static void reloadAllProperties() {
		loadProperties();
	}

	/**
	 * Provide a more explicit means to force initialisation and loading of the Local Properties rather than just relying on, but in addition to, it happening
	 * when a specific property is loaded. This method doesn't need to do anything, the ability to call it alone will trigger the static initialiser block. By
	 * keeping the static initialiser block rather than making it into a static method, we keep the thread safety it guarantees without the need to add an
	 * initialised flag on which to synchronise it and all the access methods, which would need to check the flag.
	 */
	public static final void load() {
		// Just needs to exist to trigger the initialiser block when called
	}

	public static void dumpProperties() {
		propConfig.dumpProperties();
	}

	/**
	 * Get a string property value using a specified key string with windows path separator "\\" being replaced by "/".
	 *
	 * @param propertyName
	 *            the key specified to fetch the string value
	 * @return the property value to return to the caller
	 */
	public static String get(String propertyName) {
		String propertyValue = null;

		// README - must return null, instead of "", eg since ObjectServer
		// relies
		// on unsupplied mapping file path resulting in a null,
		// which causes mapping file to be fetched from gda.factory class path
		propertyValue = propConfig.getString(propertyName, null);

		// README - we're outlawing backslashes - since no distinction between
		// string properties and URL/URI/path properties in GDA code
		// so have to do this for all property strings.
		// It would be nice to move client code over to using getPath instead!
		if (propertyValue != null) {
			propertyValue = propertyValue.replace('\\', '/');
		}

		return propertyValue;
	}

	/**
	 * Get a boolean property value using a specified key string. No default is specified and "false" is returned if no key is found.
	 *
	 * @param propertyName
	 *            the key specified to fetch the boolean value
	 * @return the property value to return to the caller. Returns false if key is not found.
	 */
	public static boolean check(String propertyName) {
		return check(propertyName, false);
	}

	/**
	 * Get a boolean property value using a specified key string.
	 *
	 * @param propertyName
	 *            the key specified to fetch the boolean value
	 * @param defaultCheck
	 *            the default value to return if the key is not found
	 * @return the property value to return to the caller
	 */
	public static boolean check(String propertyName, boolean defaultCheck) {
		return propConfig.getBoolean(propertyName, defaultCheck);
	}

	/**
	 * Get an integer property value using a specified key string.
	 *
	 * @param propertyName
	 *            the key specified to fetch the integer value
	 * @param defaultValue
	 *            the default value to return if the key is not found
	 * @return the property value to return to the caller
	 */
	public static int getInt(String propertyName, int defaultValue) {
		return propConfig.getInteger(propertyName, defaultValue);
	}

	/**
	 * Get a double property value using a specified key string.
	 *
	 * @param propertyName
	 *            the key specified to fetch the double value
	 * @param defaultValue
	 *            the default value to return if the key is not found
	 * @return the property value to return to the caller
	 */
	public static double getDouble(String propertyName, double defaultValue) {
		return propConfig.getDouble(propertyName, defaultValue);
	}

	/**
	 * Get a string property value using a specified key string.
	 *
	 * @param propertyName
	 *            the key specified to fetch the string value
	 * @param defaultValue
	 *            the default value to return if the key is not found
	 * @return the property value to return to the caller
	 */
	public static String get(String propertyName, String defaultValue) {
		String value = propConfig.getString(propertyName, defaultValue);

		// README - we're outlawing backslashes - since no distinction between
		// string properties and URL/URI/path properties in GDA code
		// so have to do this for all property strings.
		// It would be nice to move client code over to using getPath instead!
		if (value != null) {
			value = value.replace('\\', '/');
		}

		return value;
	}

	/**
	 * Get a file path property value using a specified key string.
	 *
	 * @param name
	 *            the key specified to fetch the file path value
	 * @param defaultValue
	 *            the default value to return if the key is not found
	 * @return the property value to return to the caller
	 */
	public static String getPath(String name, String defaultValue) {
		// README - backslashes are replaced with forward slashes in here,
		// since we know its a path and its safe to do this.
		return propConfig.getPath(name, defaultValue);
	}

	/**
	 * Assign a string property value to a specified key string.
	 *
	 * @param propertyName
	 *            the key specified to assign to the value
	 * @param value
	 *            the string value to assign to the specified key
	 */
	public static void set(String propertyName, String value) {
		propConfig.setString(value, propertyName);
	}

	/**
	 * Assign a string property value to a specified key string.
	 *
	 * @param propertyName
	 *            the key specified to assign to the value
	 * @param value
	 *            the string value to assign to the specified key
	 */
	public static void set(String propertyName, boolean value) {
		propConfig.setBoolean(value, propertyName);
	}
	/**
	 * Determines whether GDA is using the dummy mode configuration.
	 *
	 * @return true if GDA is using the dummy mode configuration; false otherwise. False is default to make dummy mode the exception.
	 */
	public static boolean isDummyModeEnabled() {
		return check(LocalProperties.GDA_DUMMY_MODE_ENABLED, false);
	}

	/**
	 * Determines whether access control is enabled.
	 *
	 * @return true if access control is enabled; false otherwise. False is default to keep original behaviour.
	 */
	public static boolean isAccessControlEnabled() {
		return check(LocalProperties.GDA_ACCESS_CONTROL_ENABLED, false);
	}

	/**
	 * Determines whether baton management is enabled.
	 *
	 * @return true if baton management is enabled; false otherwise. False is default to keep original behaviour.
	 */
	public static boolean isBatonManagementEnabled() {
		return check(LocalProperties.GDA_BATON_MANAGEMENT_ENABLED, false);
	}

	/**
	 * @return true if with the same user and visit ID can share the baton. False by default.
	 */
	public static boolean canShareBaton() {
		return check(LocalProperties.GDA_BATON_SHARING_ENABLED, false);
	}

	/**
	 * @return true if the client should use the reduced gui when the baton control is enabled
	 * and another client on a different visit holds the baton.
	 * True by default.
	 */

	public static boolean useReducedGUI() {
		return check(LocalProperties.GDA_GUI_REDUCED_ENABLED, true);
	}

	/**
	 * Returns the location of the 'lib' folder in the 'core' plugin.
	 *
	 * @return the location of the core plugin's lib folder
	 */
	public static String getCoreLibraryDirectory() {
		return getParentGitDir() + "gda-core.git/uk.ac.gda.core/lib/";
	}

	/**
	 * {@link #GDA_WORKSPACE_LOC}
	 *
	 * @return String
	 */
	public static String getInstallationWorkspaceDir() {
		return appendSeparator(get(GDA_WORKSPACE_LOC));
	}

	/**
	 * {@link #GDA_GIT_LOC}
	 *
	 * @return String
	 */
	public static String getParentGitDir() {
		return appendSeparator(get(GDA_GIT_LOC));
	}

	/**
	 * {@link #GDA_CONFIG}
	 *
	 * @return String
	 */
	public static String getConfigDir() {
		return appendSeparator(get(GDA_CONFIG));
	}

	private static String appendSeparator(String file) {
		if (file == null || file.isEmpty()) {
			return file;
		}
		if (!file.endsWith(separator)) {
			return file + separator;
		}
		return file;
	}

	/**
	 * If the property gda.var is not defined, then it is assumed that there is a var dir inside the config directory (where var was previously recommended to
	 * be placed)
	 *
	 * @see #GDA_VAR_DIR
	 */
	public static String getVarDir() {
		String gda_var = appendSeparator(get(GDA_VAR_DIR));
		if (gda_var == null) {
			gda_var = getConfigDir() + "/var";
		}

		return appendSeparator(gda_var);
	}

	/**
	 * @return String
	 * @see #GDA_DATA
	 */
	public static String getBaseDataDir() {
		return appendSeparator(get(GDA_DATA));
	}

	/**
	 * @return if the persistence service is available
	 */
	public static boolean isPersistenceServiceAvailable() {
		return check(LocalProperties.GDA_PERSISTENCE_SERVICE_ENABLED, false);
	}

	/**
	 * @param s
	 * @return list of integers from a csv string e.g. 1,2 yields [1,2] returns null if property
	 */
	public static List<Integer> stringToIntList(String s) {
		if (s == null)
			return null;
		final List<Integer> ints = new ArrayList<>();
		String[] parts = s.split("[:, \t\r\n]");
		for (String part : parts) {
			if (!part.isEmpty())
				ints.add(Integer.valueOf(part));
		}
		return ints;
	}

	/**
	 * @param propertyName
	 * @return Value of a property as a list of integers e.g. a value of 1 2 3 returns [1,2,3]. DO NOT USE commas
	 */
	public static List<Integer> getAsIntList(String propertyName) {
		return stringToIntList(get(propertyName));
	}

	/**
	 * @param propertyName
	 * @param defaultValue
	 *            The list of default values to return if the propertyName does not exist
	 * @return Value of a property as a list of integers e.g. a value of 1 2 3 returns [1,2,3]. DO NOT USE commas
	 */
	public static List<Integer> getAsIntList(String propertyName, Integer[] defaultValue) {
		List<Integer> result = getAsIntList(propertyName);
		return result != null ? result : new ArrayList<>(Arrays.asList(defaultValue));
	}

	/**
	 * Get the value of the named property as an <code>int</code> value.
	 * <p>
	 * This method will throw NullPointerException if the property is undefined. To avoid this, call {@link #getAsInt(String, int)} instead.
	 *
	 * @param propertyName
	 *            the property to find
	 * @return the value of the named property, as an int
	 * @throws NullPointerException
	 *             if the named property is not defined
	 * @throws NumberFormatException
	 *             if the named property is defined but cannot be parsed as an int
	 */
	public static int getAsInt(String propertyName) {
		String s = get(propertyName);
		if (s != null) {
			return Integer.parseInt(s);
		}
		String message = "Property " + propertyName + " is not defined";
		logger.error(message);
		// This method used to return Integer, not int (despite the name!)
		// In all uses of the method, though, the result was assigned directly to an int without null checking, which
		// would throw a NullPointerException if the Integer value was null. Therefore, if the property is not defined,
		// we now throw an NPE to ensure behaviour remains the same.
		throw new NullPointerException(message);
	}

	/**
	 * Get the value of the named property as an <code>int</code> value. If the property is not defined, the given default value is returned.
	 *
	 * @param propertyName
	 *            the property to find
	 * @param defaultValue
	 *            the value to return if the propertyName is not defined
	 * @return the value of the named property, as an int
	 * @throws NumberFormatException
	 *             if the named property is defined but cannot be parsed as an int
	 */
	public static int getAsInt(String propertyName, int defaultValue) {
		String s = get(propertyName);
		return s != null ? Integer.parseInt(s) : defaultValue;
	}

	public static boolean contains(String propertyName) {
		return propConfig.containsKey(propertyName);
	}

	/**
	 * Remove a property from the configuration
	 *
	 * @param key
	 */
	public static void clearProperty(String key) {
		propConfig.clearProperty(key);
	}

	/**
	 * Properties that should not be set and the reason
	 */
	private static final Map<String, String> obsoletePropertyToReason = new HashMap<>();

	static {
		obsoletePropertyToReason.put("gda.objectDelimiter", "it is not used any more");
		obsoletePropertyToReason.put("gda.users", "this property was used ambiguously and should not be used any more");
		obsoletePropertyToReason.put("gda.jython.gdaScriptDir", "script paths are defined in the Spring configuration for the command_server");
		obsoletePropertyToReason.put("gda.jython.userScriptDir", "script paths are defined in the Spring configuration for the command_server");
		obsoletePropertyToReason.put("gda.device.scannable.ScannableMotor.isBusyThrowsExceptionWhenMotorGoesIntoFault", "it is not used any more");
		obsoletePropertyToReason.put("gda.jython.socket", "It was associated with telnet access which has been removed");
		obsoletePropertyToReason.put("gda.device.scannable.ScannableMotor.waitWhileBusyThrowsExceptionWhenMotorIsInFaultState", "it is not used any more");
		obsoletePropertyToReason.put("gda.epics.EpicsDeviceFactory", "it is not used any more: see DAQ-1156");
		obsoletePropertyToReason.put("gda.scan.endscan.neworder", "the new scan order is now the default and previous order is deprecated - See DAQ-1425");
		obsoletePropertyToReason.put("gda.epics.interface.schema",
				"this property is associated with use of an EPICS interface file, due to be deprecated in GDA 9.11");
		obsoletePropertyToReason.put("gda.epics.SimulatedEpicsDeviceFactory",
				"this property is associated with use of an EPICS interface file, due to be deprecated in GDA 9.11");
		obsoletePropertyToReason.put("gda.epics.interface.xml",
				"this property is associated with use of an EPICS interface file, due to be deprecated in GDA 9.11");
		obsoletePropertyToReason.put("gda.jython.GDAJythonInterpreter.useWriters",
				"This option was related to unicode in Jython output and was removed in 8.38");
		// Corba DAQ-1322
		obsoletePropertyToReason.put("gda.eventreceiver.purge", "Corba related removed in GDA 9.11 - see DAQ-1322");
		obsoletePropertyToReason.put("gda.ORBClass", "Corba related removed in GDA 9.11 - see DAQ-1322");
		obsoletePropertyToReason.put("gda.ORBSingletonClass", "Corba related removed in GDA 9.11 - see DAQ-1322");
		obsoletePropertyToReason.put("gda.eventChannelName", "Corba related removed in GDA 9.11 - see DAQ-1322");
		obsoletePropertyToReason.put("jacorb.config.dir", "Corba related removed in GDA 9.11 - see DAQ-1322");
		obsoletePropertyToReason.put("gda.remoting.disableCorba", "No longer needed Corba related removed in GDA 9.11 - see DAQ-1322");
		// Cairo
		obsoletePropertyToReason.put("org.eclipse.swt.internal.gtk.cairoGraphics",
				"MXGDA-3174 This issue has been fixed, and setting this now may result in flickering");
		obsoletePropertyToReason.put("org.eclipse.swt.internal.gtk.useCairo",
				"MXGDA-3174 This issue has been fixed, and setting this now may result in flickering");
	}

	public static void checkForObsoleteProperties() {
		for (Entry<String, String> property : obsoletePropertyToReason.entrySet()) {
			if (LocalProperties.contains(property.getKey())) {
				logger.warn("Please remove the '{}' property from your java.properties file - {}", property.getKey(), property.getValue());
			}
		}
	}

	public static String[] getStringArray(String propertyName) {
		return propConfig.getStringArray(propertyName);
	}

	/**
	 * Returns a list of property keys matching a regular expression
	 *
	 * @param regex
	 *            the regular expression to match
	 * @return a <code>List</code> of keys
	 */
	public static List<String> getKeysByRegexp(String regex) {
		return getStreamKeysByRegexp(regex).collect(toList());
	}

	/**
	 * Returns the first property key matching a regular expression
	 *
	 * @param regex
	 *            the regular expression to match
	 * @param defaultKey
	 *            the default value if no key is found
	 * @return a the first key that matches <code>String</code>, otherwise the <code>defaultKey</code>
	 */
	public static String getFirstKeyByRegexp(String regex, String defaultKey) {
		return getStreamKeysByRegexp(regex).findFirst().orElse(defaultKey);
	}

	private static Stream<String> getStreamKeysByRegexp(String regex) {
		Predicate<String> matches = s -> Pattern.compile(regex).matcher(s).matches();
		final Iterable<String> iterable = () -> propConfig.getKeys();
		return StreamSupport.stream(iterable.spliterator(), false).filter(matches);
	}
}
