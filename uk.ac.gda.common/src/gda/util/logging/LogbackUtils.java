/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.util.logging;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.net.SocketAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.Duration;
import gda.configuration.properties.LocalProperties;

/**
 * Utility methods for Logback.
 */
public class LogbackUtils {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LogbackUtils.class);

	public static final String SOURCE_PROPERTY_NAME = "GDA_SOURCE";

	/**
	 * Returns the default Logback logger context.
	 *
	 * <p>This method can be used instead of calling {@link LoggerFactory#getILoggerFactory()} directly and casting the
	 * result to a Logback {@link LoggerContext}. It assumes that Logback is being used, so that the singleton SLF4J
	 * logger factory can be cast to a Logback {@link LoggerContext}. If a {@link ClassCastException} occurs, a more
	 * useful exception will be thrown instead.
	 *
	 * @return the Logback logger context
	 */
	public static LoggerContext getLoggerContext() {
		try {
			return (LoggerContext) LoggerFactory.getILoggerFactory();
		} catch (ClassCastException e) {
			final String msg = "Couldn't cast the logger factory to a Logback LoggerContext. Perhaps you aren't using Logback?";
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Resets the specified Logback logger context. This causes the following to happen:
	 *
	 * <ul>
	 * <li>All appenders are removed from any loggers that have been created. (Loggers are created when they are
	 * configured or used from code.)</li>
	 * <li>Existing loggers are retained, but their levels are cleared.</li>
	 * </ul>
	 */
	public static void resetLogging(LoggerContext loggerContext) {
		loggerContext.reset();
	}

	/**
	 * Resets the default Logback logger context. This causes the following to happen:
	 *
	 * <ul>
	 * <li>All appenders are removed from any loggers that have been created. (Loggers are created when they are
	 * configured or used from code.)</li>
	 * <li>Existing loggers are retained, but their levels are cleared.</li>
	 * </ul>
	 */
	public static void resetLogging() {
		resetLogging(getLoggerContext());
	}

	/**
	 * Configures the default Logback logger context using the specified configuration file.
	 *
	 * <p>Appenders defined in the file are <b>added</b> to loggers. Repeatedly configuring using the same configuration
	 * file will result in duplicate appenders.
	 *
	 * @param filename the Logback configuration file
	 */

	public static void configureLogging(LoggerContext loggerContext, String filename) throws JoranException {
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(loggerContext);
		configurator.doConfigure(filename);
	}

	/**
	 * Configures the default Logback logger context using the specified configuration file.
	 *
	 * <p>Appenders defined in the file are <b>added</b> to loggers. Repeatedly configuring using the same configuration
	 * file will result in duplicate appenders.
	 *
	 * @param url the Logback configuration file
	 */
	public static void configureLogging(LoggerContext loggerContext, URL url) throws JoranException {
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(loggerContext);
		configurator.doConfigure(url);
	}

	/**
	 * Configures the default Logback logger context using the specified configuration file.
	 *
	 * <p>Appenders defined in the file are <b>added</b> to loggers. Repeatedly configuring using the same configuration
	 * file will result in duplicate appenders.
	 *
	 * @param filename the Logback configuration file
	 */
	public static void configureLogging(String filename) throws JoranException {
		configureLogging(getLoggerContext(), filename);
	}

	/**
	 * Returns a list of all appenders for the specified logger.
	 *
	 * @param logger a Logback {@link Logger}
	 *
	 * @return a list of the logger's appenders
	 */
	public static List<Appender<ILoggingEvent>> getAppendersForLogger(Logger logger) {
		List<Appender<ILoggingEvent>> appenders = new LinkedList<>();
		Iterator<Appender<ILoggingEvent>> iterator = logger.iteratorForAppenders();
		while (iterator.hasNext()) {
			appenders.add(iterator.next());
		}
		return appenders;
	}

	/**
	 * For the specified Logback logger context, dumps a list of all loggers, their levels, and their appenders.
	 */
	public static void dumpLoggers(LoggerContext loggerContext) {
		System.out.println("Loggers:");
		List<Logger> loggers = loggerContext.getLoggerList();
		for (Logger logger : loggers) {
			System.out.printf("    %s level=%s effective=%s\n", logger, logger.getLevel(), logger.getEffectiveLevel());
			Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders();
			while (it.hasNext()) {
				Appender<ILoggingEvent> appender = it.next();
				System.out.println("        " + appender);
			}
		}
	}

	/**
	 * For the default Logback logger context, dumps a list of all loggers, their levels, and their appenders.
	 */
	public static void dumpLoggers() {
		dumpLoggers(getLoggerContext());
	}

	/**
	 * Name of property that specifies the logging configuration file used for server-side processes (channel server,
	 * object servers).
	 */
	public static final String GDA_SERVER_LOGGING_XML = "gda.server.logging.xml";

	/**
	 * Configures Logback for a server-side process.
	 *
	 * @param processName       the name of the process for which logging is being configured
	 * @param configFilename    the server logging config file to be used
	 */
	public static void configureLoggingForServerProcess(String processName, String configFilename) {
		configureLoggingForProcess(processName, configFilename);
	}

	/**
	 * Name of property that specifies the logging configuration file used for client-side processes.
	 */
	public static final String GDA_CLIENT_LOGGING_XML = "gda.client.logging.xml";

	/**
	 * Configures Logback for a client-side process.
	 *
	 * @param processName the name of the process for which logging is being configured
	 */
	public static void configureLoggingForClientProcess(String processName) {

		// Look for the property
		String configFilename = LocalProperties.get(GDA_CLIENT_LOGGING_XML);

		// If the property isn't found, log an error. Treat this as non-fatal, because Logback will still
		// be in its default state (so log messages will still be displayed on the console).
		if (configFilename == null) {
			logger.error("Please set the {} property, to specify the logging configuration file", GDA_CLIENT_LOGGING_XML);
			return;
		}

		configureLoggingForProcess(processName, configFilename);
	}

	/**
	 * Name of property that specifies the hostname/IP address of the log server.
	 */
	public static final String GDA_LOGSERVER_HOST = "gda.logserver.host";

	public static final String GDA_LOGSERVER_HOST_DEFAULT = "localhost";

	/**
	 * Name of property that specifies the port on which the log server appends logging events.
	 */
	public static final String GDA_LOGSERVER_OUT_PORT = "gda.logserver.out.port";

	public static final int GDA_LOGSERVER_OUT_PORT_DEFAULT = 6750;

	/**
	 * Configures Logback for either a server- or client-side process, using a specified configuration file.
	 *
	 * @param processName           The name of the process for which logging is being configured
	 * @param configFilename        The name of the custom logging configuration file to use
	 */
	protected static void configureLoggingForProcess(String processName, String configFilename) {

		LoggerContext context = getLoggerContext();

		// If no config filename is specified, log an error. Treat this as non-fatal, because Logback will still
		// be in its default state (so log messages will still be displayed on the console).
		if (configFilename == null) {
			logger.error("Unable to configure using null or empty logging filename.");
			return;
		}

		// Reset logging.
		resetLogging(context);

		// If anything goes wrong from here onwards, we should throw an exception. It's not worth trying to log the
		// error, since there may be no appenders.

		// Set source property early so that it can be used in the xml config files
		addSourcePropertyAndListener(context, processName);

		// Capture java.util.logging calls and handle with slf4j
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		// Configure using the specified logging configuration.
		try {
			//Use stdout as use of logger is no good if the logging configuration is wrong
			System.out.println("Configure logging using file: " + configFilename);
			configureLogging(context, configFilename);
		} catch (JoranException e) {
			final String msg = String.format("Unable to configure logging using %s", configFilename);
			throw new RuntimeException(msg, e);
		}

		setEventDelayToZeroInAllSocketAppenders(context);

		addShutdownHook();
	}

	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() ->  {
			logger.info("Shutting down logging");
			// See https://logback.qos.ch/manual/configuration.html#stopContext
			getLoggerContext().stop();
		}));
	}

	public static void addSourcePropertyAndListener(LoggerContext context, final String processName) {

		// Add a listener that will restore the source property when the context is reset
		context.addListener(new LoggerContextAdapter() {

			@Override
			public boolean isResetResistant() {
				// Must return true so that this listener isn't removed from the context
				// when the context is reset (otherwise the property will only get added
				// to the context once).
				return true;
			}

			@Override
			public void onReset(LoggerContext context) {
				addSourcePropertyToContext(context, processName);
			}
		});

		// Set the property initially
		addSourcePropertyToContext(context, processName);
	}

	private static void addSourcePropertyToContext(LoggerContext context, String processName) {
		context.putProperty(SOURCE_PROPERTY_NAME, processName);
	}

	public static void setEventDelayToZeroInAllSocketAppenders(LoggerContext context) {
		// Force event delay to zero for all SocketAppenders.
		// Prevents 100 ms delay per log event when a SocketAppender's queue fills up
		// (this happens if the SocketAppender can't connect to the remote host)
		for (Logger logger : context.getLoggerList()) {
			final Iterator<Appender<ILoggingEvent>> appenderIterator = logger.iteratorForAppenders();
			while (appenderIterator.hasNext()) {
				final Appender<ILoggingEvent> appender = appenderIterator.next();
				if (appender instanceof SocketAppender) {
					final SocketAppender sockAppender = (SocketAppender) appender;
					sockAppender.setEventDelayLimit(Duration.buildByMilliseconds(0));
				}
			}
		}
	}

	/**
	 * Logback uses a {@link ScheduledThreadPoolExecutor} for {@code ServerSocket[Receiver,Appender]} connections which uses a thread
	 * to listen for clients and also a thread for each client. As documented in the Javadoc, {@link ScheduledThreadPoolExecutor}
	 * behaves as a fixed thread pool. Logback has coded this to 8 threads (see {@link CoreConstants#SCHEDULED_EXECUTOR_POOL_SIZE}).
	 * When more than 6 clients/log panels are connected a major issue arises as a backlog of tasks is created - each of which
	 * has ownership of a socket stuck in {@code CLOSE_WAIT} state. As clients time out and attempt to reconnect the queue increases
	 * and more sockets are used until the process reaches its quota for max open files.
	 * <p>
	 * This method can be scheduled to run regularly as a workaround which monitors the queue and adjusts the thread count
	 * to match demand, scaling it both up and down.
	 * <p>
	 * This technique was taken from GDA's {@code Async} class.
	 */
	public static void monitorAndAdjustLogbackExecutor() {
		LoggerContext context = getLoggerContext();
		ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) context.getScheduledExecutorService();
		// SCHEDULER stats
		int scheduleThreadCount = executor.getActiveCount();
		int schedulerPoolSize = executor.getCorePoolSize();
		int scheduleQueueSize = executor.getQueue().size();
		if (scheduleThreadCount >= schedulerPoolSize) {
			logger.warn("Logback scheduled thread pool using {}/{} threads. Queue size: {}", scheduleThreadCount, schedulerPoolSize, scheduleQueueSize);
			int newThreadPoolSize = schedulerPoolSize + 4; // Ramp up quickly to combat rising sockets
			logger.info("Increasing Logback scheduler pool size to {}", newThreadPoolSize);
			executor.setCorePoolSize(newThreadPoolSize);
		} else {
			logger.trace("Logback scheduled pool thread using {}/{} threads. Queue size: {}", scheduleThreadCount, schedulerPoolSize, scheduleQueueSize);
			if (schedulerPoolSize > scheduleThreadCount + 2 && schedulerPoolSize > CoreConstants.SCHEDULED_EXECUTOR_POOL_SIZE) {
				/*
				 * Reducing the core pool size while all threads are active will not kill the threads It only kills them when they become idle so in the case
				 * where new tasks have been added since the threads were counted, no processes will be affected
				 */
				int newSize = schedulerPoolSize - 1;
				logger.info("Reducing the Logback scheduler pool size to {}", newSize);
				executor.setCorePoolSize(newSize);
			}
		}
	}

}
