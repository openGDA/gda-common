/*-
 *******************************************************************************
 * Copyright (c) 2020 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.gda.common.activemq;

import javax.jms.JMSException;
import javax.jms.QueueSession;
import javax.jms.Session;

public interface ISessionService {

	public Session getSession(String brokerUri, boolean transacted, int acknowledgeMode) throws JMSException;

	public Session getSession() throws JMSException;

	public QueueSession getQueueSession(String brokerUri, boolean transacted, int acknowledgeMode) throws JMSException;

	public boolean defaultConnectionActive();

}
