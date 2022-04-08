package jdwp;

import java.util.HashMap;
import java.util.Map;

public class LineTable {
	long typeId;
	long methodId;
	int start;
	int end;
	int size;

	Map<Long, Integer> lines = new HashMap<>();

	public LineTable(long typeId, long methodId, int start, int end, int size) {
		this.typeId = typeId;
		this.methodId = methodId;
		this.start = start;
		this.end = end;
		this.size = size;

		ReferenceType refType = ReferenceType.refsById.get(typeId);
		Method method = refType.methods.get(methodId);
		method.setLineTable(this);
	}

	public void addLine(long codeindex, int line) {
		lines.put(codeindex, line);
	}

	public long typeId() {
		return typeId;
	}

	public long methodId() {
		return methodId;
	}

	public int start() {
		return start;
	}

	public int end() {
		return end;
	}

	public int size() {
		return size;
	}

	public int getLineForCodeindex(long codeindex) {
		return lines.get(codeindex);
	}

	public Location getLocation(int line) {
		jdwp.ReferenceType refType = ReferenceType.refsById.get(typeId);
		for (long codeindex: lines.keySet()) {
			if (lines.get(codeindex) == line) {
				return new Location(refType.tag(), typeId, methodId, codeindex);
			}
		}
		return null;
	}

	public Map<Long, Integer> lines() {
		return lines;
	}

}
