package jdwp.model;

public class MethodLocation {
    private final MethodInfo method;
    private final int line;

    public MethodLocation(MethodInfo method, int line) {
        this.method = method;
        this.line = line;
    }

    public MethodInfo getMethod() {
        return method;
    }

    public int getLine() {
        return line;
    }
}
