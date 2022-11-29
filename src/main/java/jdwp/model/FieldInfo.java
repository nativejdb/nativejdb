package jdwp.model;

public class FieldInfo {
    private final String name;
    private final String jni;
    private final int modifier;

    private final long uniqueID;

    private static long counter = 0L;

    public FieldInfo(String name, String jni, int modifier) {
        this.name = name;
        this.jni = jni;
        this.modifier = modifier;
        this.uniqueID = counter++;
    }

    public String getName() {
        return name;
    }

    public String getJni() {
        return jni;
    }

    public int getModifier() {
        return modifier;
    }

    public long getUniqueID() {
        return uniqueID;
    }
}
