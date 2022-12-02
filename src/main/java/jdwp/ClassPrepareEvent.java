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
import jdwp.model.ReferenceType;

public class ClassPrepareEvent extends MIEvent {

	int requestID;
	ReferenceType referenceType;
	byte suspendPolicy;


	public ClassPrepareEvent(int token, MIResult[] results, int requestID, byte suspendPolicy, ReferenceType referenceType) {
		super(token, results);
		this.requestID = requestID;
		this.suspendPolicy = suspendPolicy;
		this.referenceType = referenceType;
	}
}
