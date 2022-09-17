package jdwp.model;

import jdwp.Translator;

import java.nio.file.Path;
import java.util.*;

public class ReferenceTypes {
    private final TypeEnricher provider;
    private Map<Long, ReferenceType> idToTypes = new HashMap<>();
    private Map<String, ReferenceType> signatureToTypes = new HashMap<>();

    private Map<String, ReferenceType> classNameToTypes = new HashMap<>();

    public ReferenceTypes(Path src) {
        provider = TypeEnricherFactory.INSTANCE.getTypeEnricher(src);

    }

    public void addReferenceType(ReferenceType type) {
        idToTypes.put(type.getUniqueID(), type);
        signatureToTypes.put(type.getSignature(), type);
        classNameToTypes.put(type.getClassName(), type);
    }

    public Collection<ReferenceType> getTypes() {
        return idToTypes.values();
    }

    public Collection<ReferenceType> findBySignature(String signature) {
        var referenceType = signatureToTypes.get(signature);
        return referenceType != null ? Collections.singletonList(referenceType) : Collections.emptyList();
    }

    public ReferenceType findbyId(long id) {
        return idToTypes.get(id);
    }

    public TypeEnricher getTypeEnricher() {
        return provider;
    }

    public MethodLocation getLocation(String function, int line) {
        MethodLocation result = null;
        var classNameAndMethodAndParameters = Translator.getClassFunctionNameAndParameters(function);
        var referenceType = classNameToTypes.get(classNameAndMethodAndParameters[0]);
        if (referenceType != null) {
            var method = referenceType.findMethodByNameAndParameters(classNameAndMethodAndParameters[1] + classNameAndMethodAndParameters[2]);
            if (method != null) {
                result = new MethodLocation(method, line);
            }
        }
        return result;
    }

    public ReferenceType findByClassName(String className) {
        return classNameToTypes.get(className);
    }
}
