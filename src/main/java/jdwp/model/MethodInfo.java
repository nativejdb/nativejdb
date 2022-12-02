package jdwp.model;

import jdwp.PacketStream;
import jdwp.Translator;

import java.lang.reflect.Modifier;
import java.util.*;

public class MethodInfo {
    private ReferenceType referenceType;

    private final MethodSignature signature;

    private int modifier = Modifier.STATIC | Modifier.PUBLIC;

    private final Long uniqueID;

    private static Long counter = 0L;
    private Optional<NavigableSet<Integer>> lines = Optional.empty();

    private List<VariableInfo> variables = Collections.emptyList();

    public MethodInfo(ReferenceType referenceType, MethodSignature signature) {
        this.referenceType = referenceType;
        this.signature = signature;
        this.uniqueID = counter++;
        referenceType.addMethod(this);
        if (signature.isInstanceMethod()) {
            removeModifier(Modifier.STATIC);
        }
    }

    public String getJNISignature() {
        StringBuilder builder = new StringBuilder("(");
        for (String argType : signature.getParameterTypes()) {
            builder.append(Translator.gdb2JNI(argType));
        }
        builder.append(')');
        builder.append(Translator.gdb2JNI(signature.getReturnType()));
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
        answer.writeString(signature.getName());
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

    public MethodSignature getSignature() {
        return signature;
    }

    public List<VariableInfo> getVariables() {
        getReferenceType().ensureEnriched();
        return variables;
    }

    public void setVariables(List<VariableInfo> variables) {
        this.variables = variables;
    }

    public int getArgumentCount() {
        return signature.getParameterTypes().size() + (modifier & Modifier.STATIC) == 0 ? 1 : 0;
    }

    public VariableInfo findVariableBySlot(int slot) {
        return getVariables().stream().filter(v -> v.getIndex() == slot).findFirst().orElse(null);
    }
}
