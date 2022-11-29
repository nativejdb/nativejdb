package jdwp.model;

import jdwp.JDWP;
import jdwp.PacketStream;
import jdwp.Translator;

import java.util.*;

public class ReferenceType {
    private final ClassName className;

    private final Map<Long, MethodInfo> methods = new HashMap<>();
    private final Map<MethodSignature, MethodInfo> signatureToMethod = new HashMap<>();

    private Map<Long, FieldInfo> fields = new HashMap<>();

    private final Long uniqueID;

    private static Long counter = 0L;

    private final ReferenceTypes types;

    private final String sourceFile;

    private ClassName superClassName;

    private boolean enriched;

    private byte type = JDWP.TypeTag.CLASS;

    public ReferenceType(ReferenceTypes types, String sourceFile, ClassName className) {
        this.types = types;
        this.sourceFile = sourceFile;
        this.className = className;
        this.uniqueID = counter++;
        types.addReferenceType(this);
    }

    public ReferenceType(ReferenceTypes types, ClassName className) {
        this(types, null, className);
        this.type = JDWP.TypeTag.ARRAY;
        System.out.println("className" + className);
    }

    public ClassName getClassName() {
        return className;
    }

    public void addMethod(MethodInfo methodInfo) {
        methods.put(methodInfo.getUniqueID(), methodInfo);
        signatureToMethod.put(methodInfo.getSignature(), methodInfo);
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
        answer.writeString(getClassName().getJNI());
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

    public MethodInfo findBySignature(MethodSignature signature) {
        return signatureToMethod.get(signature);
    }

    public Optional<MethodLocation> findByNameAndLocation(String methodName, int line) {
        ensureEnriched();
        return methods.values().stream().filter(info -> methodName.equals(info.getSignature().getName())).
                filter(info -> info.getLines().isPresent()).
                filter(info -> info.getLines().get().contains(line)).
                findFirst().map(info -> new MethodLocation(info, line));
    }

    public void setSuperClassName(ClassName className) {
        this.superClassName = className;
    }

    public ReferenceType getSuperReferenceType() {
        ensureEnriched();
        return getReferenceTypes().findByClassName(superClassName);
    }

    public void setEnriched(boolean enriched) {
        this.enriched = enriched;
    }

    public void ensureEnriched() {
        if (!enriched) {
            getReferenceTypes().getTypeEnricher().parse(this);
        }
    }

    public byte getType() {
        return type;
    }

    public void addField(FieldInfo field) {
        fields.put(field.getUniqueID(), field);
    }

    public Collection<FieldInfo> getFields() {
        return fields.values();
    }

    public FieldInfo findFieldByID(long id) {
        return fields.get(id);
    }
}
