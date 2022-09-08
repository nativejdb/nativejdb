package jdwp.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ReferenceTypes {
    private Map<Long, ReferenceType> idToTypes = new HashMap<>();
    private Map<String, ReferenceType> signatureToTypes = new HashMap<>();

    public void addReferenceType(ReferenceType type) {
        idToTypes.put(type.getUniqueID(), type);
        signatureToTypes.put(type.getSignature(), type);
    }

    public Collection<ReferenceType> getTypes() {
        return idToTypes.values();
    }
}
