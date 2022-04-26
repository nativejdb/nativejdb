/*
 * Copyright (C) 2022 IBM Corporation
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License v2 with Classpath Exception.
 * The text of the license is available in the file LICENSE.TXT.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See LICENSE.TXT for more details.
 */

package jdwp;

import gdb.mi.service.command.events.MIEvent;
import gdb.mi.service.command.output.MIResult;
import jdwp.jdi.ReferenceTypeImpl;

public class ClassPrepareEvent extends MIEvent {

	int requestID;
	ReferenceTypeImpl referenceType;
	byte suspendPolicy;


	public ClassPrepareEvent(int token, MIResult[] results, int requestID, byte suspendPolicy, ReferenceTypeImpl referenceType) {
		super(token, results);
		this.requestID = requestID;
		this.suspendPolicy = suspendPolicy;
		this.referenceType = referenceType;
	}
}
