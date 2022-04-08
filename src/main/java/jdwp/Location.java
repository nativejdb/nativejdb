package jdwp;

public class Location {
	byte tag;
	long typeId;
	long methodId;
	long codeIndex;

	public Location(byte tag, long typeId, long methodId, long codeIndex) {
		this.tag = tag;
		this.typeId = typeId;
		this.methodId = methodId;
		this.codeIndex = codeIndex;
	}

	public byte tag() {
		return tag;
	}

	public long typeId() {
		return typeId;
	}

	public long methodId() {
		return methodId;
	}

	public long codeIndex() {
		return codeIndex;
	}

	public int getLine(long codeIndex) {
		return getReferenceType().methods.get(methodId).lineTable().getLineForCodeindex(codeIndex);
	}

	public ReferenceType getReferenceType() {
		return ReferenceType.refsById.get(typeId);
	}



}
