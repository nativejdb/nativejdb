package jdwp.model;

public class VariableInfo {
    private String name;
    private String jniSignature;
    private int startLine;
    private int endLine;
    private int index;

    public VariableInfo(String name, String jniSignature, int startLine, int endLine, int index) {
        this.name = name;
        this.jniSignature = jniSignature;
        this.startLine = startLine;
        this.endLine = endLine;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public String getJNISignature() {
        return jniSignature;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getIndex() {
        return index;
    }
}
