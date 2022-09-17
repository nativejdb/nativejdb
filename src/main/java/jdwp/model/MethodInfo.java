package jdwp.model;

import jdwp.PacketStream;
import jdwp.Translator;

import java.lang.reflect.Modifier;
import java.util.*;

public class MethodInfo {
    private ReferenceType referenceType;

    private String methodName;

    private final String nameAndParameters;

    private String returnType;

    private List<String> argumentTypes = new ArrayList<>();

    private int modifier = Modifier.STATIC | Modifier.PUBLIC;

    private final Long uniqueID;

    private static Long counter = 0L;
    private Optional<NavigableSet<Integer>> lines = Optional.empty();

    private List<VariableInfo> variables = Collections.emptyList();

    public MethodInfo(ReferenceType referenceType, String nameAndParameters, String name) {
        this.referenceType = referenceType;
        this.nameAndParameters = nameAndParameters;
        this.methodName = name;
        this.uniqueID = counter++;
        referenceType.addMethod(this);
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public void addArgumentType(String paramType) {
        argumentTypes.add(paramType);
    }

    public String getJNISignature() {
        StringBuilder builder = new StringBuilder("(");
        for (String argType : argumentTypes) {
            builder.append(Translator.gdb2JNIType(argType));
        }
        builder.append(')');
        builder.append(Translator.gdb2JNIType(returnType));
        return builder.toString();
    }

    public void addModifier(int modifier) {
        this.modifier |= modifier;
    }

    public void removeModifier(int modifier) {
        this.modifier &= ~modifier;
    }

    public int getModifier() {
        return modifier;
    }

    public Long getUniqueID() {
        return uniqueID;
    }

    public void write(PacketStream answer, boolean generic) {
        answer.writeObjectRef(uniqueID);
        answer.writeString(methodName);
        answer.writeString(getJNISignature());
        if (generic) {
            answer.writeString("");
        }
        answer.writeInt(modifier);
    }

    public Optional<NavigableSet<Integer>> getLines() {
        getReferenceType().ensureEnriched();
        return lines;
    }

    public void setLines(NavigableSet<Integer> lines) {
        this.lines = Optional.of(lines);
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public String getNameAndParameters() {
        return nameAndParameters;
    }

    public List<VariableInfo> getVariables() {
        getReferenceType().ensureEnriched();
        return variables;
    }

    public void setVariables(List<VariableInfo> variables) {
        this.variables = variables;
    }

    public int getArgumentCount() {
        return argumentTypes.size();
    }

    public VariableInfo findVariableBySlot(int slot) {
        return getVariables().stream().filter(v -> v.getIndex() == slot).findFirst().orElse(null);
    }
}
