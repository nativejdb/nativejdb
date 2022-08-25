package jdwp.model;

import jdwp.JDWP;
import jdwp.PacketStream;
import jdwp.Translator;

import java.util.*;

public class ReferenceType {
    private final String className;

    private final Map<Long, Translator.MethodInfo> methods = new HashMap<>();

    private final Long uniqueID;

    private static Long counter = 0L;

    public ReferenceType(String className) {
        this.className = className;
        this.uniqueID = counter++;
    }

    public String getClassName() {
        return className;
    }

    public void addMethod(Translator.MethodInfo methodInfo) {
        methods.put(methodInfo.getUniqueID(), methodInfo);
    }

    public Collection<Translator.MethodInfo> getMethods() {
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

    private String getSignature() {
        return Translator.gdb2JNIType(className);
    }
}
