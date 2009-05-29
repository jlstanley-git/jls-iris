/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

/**
 * Ntcip DmsLongPowerRecoveryMessage object
 *
 * @author Douglas Lau
 */
public class DmsLongPowerRecoveryMessage extends MessageIDCode {

	/** Create a new DMS long power recovery object
	 * @param m memory type
	 * @param n message number
	 * @param c message CRC */
	public DmsLongPowerRecoveryMessage(int m, int n, int c) {
		memory = m;
		number = n;
		crc = c;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.signControl.createOID(new int[] {9, 0});
	}
}
