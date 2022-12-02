package jdwp.model;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TypeEnricherFactory {

    public  static TypeEnricherFactory INSTANCE = new TypeEnricherFactory();

    private Map<Path, TypeEnricher> enrichers = new HashMap<>();

    public TypeEnricher getTypeEnricher(Path path) {
        return enrichers.computeIfAbsent(path.toAbsolutePath(), p -> new TypeEnricher(p));

    }
}
