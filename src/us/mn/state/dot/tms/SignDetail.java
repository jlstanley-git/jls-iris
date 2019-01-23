/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
 * Sign detail defines detailed parameters of a sign.
 *
 * @author Douglas Lau
 */
public interface SignDetail extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "sign_detail";

	/** Get DMS type */
	int getDmsType();

	/** Get portable flag */
	boolean getPortable();

	/** Get sign technology description */
	String getTechnology();

	/** Get sign access description */
	String getSignAccess();

	/** Get sign legend */
	String getLegend();

	/** Get beacon type description */
	String getBeaconType();

	/** Get monochrome scheme foreground color (24-bit). */
	int getMonochromeForeground();

	/** Get monochrome scheme background color (24-bit). */
	int getMonochromeBackground();

	/** Get the hardware make */
	String getHardwareMake();

	/** Get the hardware model */
	String getHardwareModel();

	/** Get the software make */
	String getSoftwareMake();

	/** Get the software model */
	String getSoftwareModel();
}
