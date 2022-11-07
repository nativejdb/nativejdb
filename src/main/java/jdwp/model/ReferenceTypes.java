package jdwp.model;

import jdwp.JDWP;
import jdwp.Translator;

import java.nio.file.Path;
import java.util.*;

public class ReferenceTypes {
    private final TypeEnricher provider;
    private Map<Long, ReferenceType> idToTypes = new HashMap<>();
    private Map<ClassName, ReferenceType> classNameToTypes = new HashMap<>();

    public ReferenceTypes(Path src) {
        provider = TypeEnricherFactory.INSTANCE.getTypeEnricher(src);

    }

    public void addReferenceType(ReferenceType type) {
        idToTypes.put(type.getUniqueID(), type);
        classNameToTypes.put(type.getClassName(), type);
    }

    public Collection<ReferenceType> getTypes() {
        return idToTypes.values();
    }

    public Collection<ReferenceType> findBySignature(String signature) {
        var referenceType = classNameToTypes.get(ClassName.fromJNI(signature));
        return referenceType != null ? Collections.singletonList(referenceType) : Collections.emptyList();
    }

    public ReferenceType findbyId(long id) {
        return idToTypes.get(id);
    }

    public TypeEnricher getTypeEnricher() {
        return provider;
    }

    public Optional<MethodLocation> getLocation(String function, int line) {
        var classNameAndMethodName = Translator.getClassAndMethodName(function);
        var referenceType = classNameToTypes.get(ClassName.fromGDB(classNameAndMethodName[0]));
        if (referenceType != null) {
            return referenceType.findByNameAndLocation(classNameAndMethodName[1], line);
        }
        return Optional.empty();
    }

    public ReferenceType findByClassName(ClassName className) {
        var referenceType = classNameToTypes.get(className);
        if (referenceType == null && className != null && className.isArray()) {
            referenceType = new ReferenceType(this, className);
        }
        return referenceType;
    }
}
