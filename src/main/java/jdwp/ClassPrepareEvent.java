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
