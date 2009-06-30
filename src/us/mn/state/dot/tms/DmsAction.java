/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms;

import us.mn.state.dot.sonar.SonarObject;

/**
 * Action for sending a message to a DMS sign group triggered by an action plan.
 *
 * @author Douglas Lau
 */
public interface DmsAction extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "dms_action";

	/** Get the action plan */
	ActionPlan getActionPlan();

	/** Get the sign group */
	SignGroup getSignGroup();

	/** Set the "on deploy" trigger flag */
	void setOnDeploy(boolean od);

	/** Get the "on deploy" trigger flag */
	boolean getOnDeploy();

	/** Set the quick message */
	void setQuickMessage(QuickMessage qm);

	/** Get the quick message */
	QuickMessage getQuickMessage();

	/** Set the message priority.
	 * @param p Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	void setPriority(int p);

	/** Get the message priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	int getPriority();
}
