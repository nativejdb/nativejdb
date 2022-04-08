package jdwp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Method {
	long typeId;
	long methodId;
	String name;
	String signature;
	int modifiers;
	String normalizedName;

	LineTable linetable;

	public static Map<String, Method> methods = new HashMap<>();

	public Method(long typeId, long methodId, String name, String signature, int modifiers) {
		this.typeId = typeId;
		this.methodId = methodId;
		this.name = name;
		this.signature = signature;
		this.modifiers = modifiers;
		ReferenceType ref = ReferenceType.refsById.get(typeId);
		if (ref != null) {
			ref.addMethod(this);
		}
		normalizedName = getNormalizedName();
		methods.put(normalizedName, this);
	}

	private String getNormalizedName() {
		jdwp.ReferenceType refType = ReferenceType.refsById.get(typeId);
		String ref = refType.signature();
		if (ref.startsWith("L")) {
			ref = ref.substring(1); // remove the "L"
		}
		if (ref.endsWith(";")) {
			ref = ref.substring(0, ref.length() - 1);
		}
		String sig = signature;
		int index = sig.indexOf(")");
		if (index > 0) {
			sig = sig.substring(0, index + 1);
		}
		return  ref + "::" + name + sig;
	}


	public long uniqueId() {
		return methodId;
	}

	public String name() {
		return name;
	}

	public String signature() {
		return signature;
	}

	public String genericSignature() {
		return null;
	}

	public int modifiers() {
		return modifiers;
	}

	public LineTable lineTable() {
		return linetable;
	}

	public void setLineTable(LineTable table) {
		linetable = table;
	}

	public String normalizedName() {
		return normalizedName;
	}


	public static void loadMethods() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("/jdwp/apps/methods.txt")));
			String s;
			while ((s = reader.readLine()) != null) {
				long typeId = Long.parseLong(s);
				s = reader.readLine();
				int methodsNum = Integer.parseInt(s);
				for(int i=0; i < methodsNum; i++) {
					s = reader.readLine();
					long methodId = Long.parseLong(s);
					s = reader.readLine();
					String name = s;
					s = reader.readLine();
					String signature = s;
					s = reader.readLine();
					int modifiers = Integer.parseInt(s);
					new Method(typeId, methodId, name, signature, modifiers);
					s = reader.readLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void loadLineTables() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("/jdwp/apps/linetables.txt")));
			String s;
			while ((s = reader.readLine()) != null) {
				long typeId = Long.parseLong(s);
				s = reader.readLine();
				long methodId = Long.parseLong(s);
				s = reader.readLine();
				int start = Integer.parseInt(s);
				s = reader.readLine();
				int end = Integer.parseInt(s);
				s = reader.readLine();
				int size = Integer.parseInt(s);
				LineTable table = new LineTable(typeId, methodId, start, end, size);
				for (int i = 0; i < size; i++) {
					s = reader.readLine(); // index - line
					String[] lines = s.split(" - ");
					int codeindex = Integer.parseInt(lines[0]);
					int line = Integer.parseInt(lines[1]);
					table.addLine(codeindex, line);
				}
				s = reader.readLine();

			}
		} catch (IOException e) {
		e.printStackTrace();
		}
	}

}
