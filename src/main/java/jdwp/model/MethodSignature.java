package jdwp.model;

import java.util.List;
import java.util.Objects;

public class MethodSignature {
    private String name;
    private String returnType;
    private List<String> parameterTypes;
    private final boolean instanceMethod;

    public MethodSignature(String name, String returnType, List<String> parameterTypes, boolean instanceMethod) {
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.instanceMethod = instanceMethod;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public boolean isInstanceMethod() {
        return instanceMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodSignature that = (MethodSignature) o;
        return instanceMethod == that.instanceMethod && name.equals(that.name) && returnType.equals(that.returnType) && parameterTypes.equals(that.parameterTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, returnType, parameterTypes, instanceMethod);
    }

}
