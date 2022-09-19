package jdwp.model;

public class FrameInfo {
    private final MethodLocation location;

    private final int frameID;

    public FrameInfo(MethodLocation location, int frameID) {
        this.location = location;
        this.frameID = frameID;
    }

    public MethodLocation getLocation() {
        return location;
    }

    public int getFrameID() {
        return frameID;
    }
}
