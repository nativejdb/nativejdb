package jdwp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ReferenceType {

	public static Map<Long, ReferenceType> refsById = new HashMap<>();
	public static Map<String, ReferenceType> refsBySignature = new HashMap<>();

	long uniqueId = 0;

	String source;
	long superclass;
	byte tag;
	String signature;
	String genericSignature;
	byte status;

	Map<Long, jdwp.Method> methods = new HashMap<>();

	private long getUniqueId() {
		return uniqueId++;
	}

	public ReferenceType(String source, long superclass, byte tag, long uniqueId, String signature, String genericSignature, byte status) {
		//this.uniqueId = getUniqueId();
		this.source = source;
		this.superclass = superclass;
		this.uniqueId = uniqueId;
		this.tag = tag;
		this.signature = signature;
		if (!genericSignature.equals("<null>")) {
			this.genericSignature = genericSignature;
		} else {
			this.genericSignature = null;
		}
		this.status = status;
		refsById.put(uniqueId, this);
		refsBySignature.put(signature, this);
	}

	public String source() {
		return source;
	}

	public long superclass() { return superclass; }

	public byte tag() {
		return tag;
	}

	public long uniqueId() {
		return uniqueId;
	}

	public String signature() {
		return signature;
	}

	public String genericSignature() {
		return genericSignature;
	}

	public byte status() {
		return status;
	}

	public Collection<Method> methods() {
		return methods.values();
	}

	public void addMethod(Method method) {
		methods.put(method.uniqueId(), method);
	}

	public static void loadClasses() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("/jdwp/apps/classes.txt")));
			String s;
			while ((s = reader.readLine()) != null) {
				String source = s;
				if (source.equals("Bits.java")) {
					int i = 0;
				}
				s = reader.readLine();
				long superclass = Long.parseLong(s);
				s = reader.readLine();
				byte tag = Byte.parseByte(s);
				s = reader.readLine();
				long uniqueId = Long.parseLong(s);
				s = reader.readLine();
				String signature = s;
				s = reader.readLine();
				String genericSignature = s;
				s = reader.readLine(); // status, ignore
				byte status = Byte.parseByte(s);
				s = reader.readLine(); // newline, ignore
				new ReferenceType(source, superclass, tag, uniqueId, signature, genericSignature, status);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
