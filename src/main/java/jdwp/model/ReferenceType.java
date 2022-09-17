package jdwp.model;

import jdwp.JDWP;
import jdwp.PacketStream;
import jdwp.Translator;

import java.util.*;

public class ReferenceType {
    private final String className;

    private final Map<Long, MethodInfo> methods = new HashMap<>();
    private final Map<String, MethodInfo> nameAndParametersToMethod = new HashMap<>();

    private final Long uniqueID;

    private static Long counter = 0L;

    private final ReferenceTypes types;

    private final String sourceFile;

    private String superClassName;

    private boolean enriched;

    public ReferenceType(ReferenceTypes types, String sourceFile, String className) {
        this.types = types;
        this.sourceFile = sourceFile;
        this.className = className;
        this.uniqueID = counter++;
        types.addReferenceType(this);
    }

    public String getClassName() {
        return className;
    }

    public void addMethod(MethodInfo methodInfo) {
        methods.put(methodInfo.getUniqueID(), methodInfo);
        nameAndParametersToMethod.put(methodInfo.getNameAndParameters(), methodInfo);
    }

    public Collection<MethodInfo> getMethods() {
        return methods.values();
    }

    public Long getUniqueID() {
        return uniqueID;
    }

    public void write(PacketStream answer, boolean generic) {
        answer.writeByte(JDWP.TypeTag.CLASS); //TODO
        answer.writeObjectRef(uniqueID);
        answer.writeString(getSignature());
        if (generic) {
            answer.writeString("");
        }
        answer.writeInt(JDWP.ClassStatus.INITIALIZED | JDWP.ClassStatus.PREPARED | JDWP.ClassStatus.VERIFIED);
    }

    public void writeReference(PacketStream answer) {
        answer.writeByte(JDWP.TypeTag.CLASS); //TODO
        answer.writeObjectRef(uniqueID);
        answer.writeInt(JDWP.ClassStatus.INITIALIZED | JDWP.ClassStatus.PREPARED | JDWP.ClassStatus.VERIFIED);
    }

    public String getSignature() {
        return Translator.gdb2JNIType(className);
    }

    public ReferenceTypes getReferenceTypes() {
        return types;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getBaseSourceFile() {
        String sourceFile = getSourceFile();
        return sourceFile.substring(sourceFile.lastIndexOf('/') + 1);
    }

    public MethodInfo findMethodById(long methodID) {
        return methods.get(methodID);
    }

    public MethodInfo findMethodByNameAndParameters(String nameAndParameters) {
        return nameAndParametersToMethod.get(nameAndParameters);
    }

    public void setSuperClassName(String className) {
        this.superClassName = className;
    }

    public ReferenceType getSuperReferenceType() {
        ensureEnriched();
        return getReferenceTypes().findByClassName(superClassName);
    }

    public void ensureEnriched() {
        if (!enriched) {
            getReferenceTypes().getTypeEnricher().parse(this);
            enriched = true;
        }
    }
}
