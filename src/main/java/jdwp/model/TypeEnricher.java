package jdwp.model;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.nodeTypes.NodeWithStatements;
import com.github.javaparser.resolution.declarations.AssociableToAST;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import jdwp.Translator;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TypeEnricher {
    private final Path path;

    private final JavaParser parser;

    private JavaParser createParser(Path path) {
        var parser = new JavaParser();
        var solver = new CombinedTypeSolver();
        var reflectionSolver = new ReflectionTypeSolver();
        var sourceSolver = new JavaParserTypeSolver(path);
        solver.add(reflectionSolver);
        solver.add(sourceSolver);
        parser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(solver));
        return parser;
    }

    public TypeEnricher(Path path) {
        this.path = path;
        parser = createParser(path);
    }


    public void parse(ReferenceType referenceType) {
        var queue = new ArrayDeque<Path>();
        queue.add(path.resolve(referenceType.getSourceFile()));
        while (!queue.isEmpty()) {
            try {
                var parseResult = parser.parse(queue.remove());
                if (parseResult.isSuccessful()) {
                    for(var type : parseResult.getResult().get().getTypes()) {
                        processType(referenceType.getReferenceTypes(), type, queue);
                    }

                }
            } catch (IOException e) {
            }
        }
    }

    private void processType(ReferenceTypes types, TypeDeclaration<?> type, Queue<Path> queue) {
        ReferenceType referenceType = types.findByClassName(type.getFullyQualifiedName().get());
        if (referenceType == null) {
            var path = type.findCompilationUnit().flatMap(CompilationUnit::getStorage).map(CompilationUnit.Storage::getPath);
            referenceType = new ReferenceType(types, path.isPresent()?path.get().toString():null, type.getFullyQualifiedName().get());
        }
        var parent = type.resolve().getAncestors(true).stream().filter(rtype -> rtype.getTypeDeclaration().isPresent() && rtype.getTypeDeclaration().get().isClass()).findFirst();
        ReferenceType finalReferenceType1 = referenceType;
        parent.ifPresent(typ -> {
            finalReferenceType1.setSuperClassName(typ.getQualifiedName());
            typ.getTypeDeclaration().ifPresent(typeDecl -> {
                if (typeDecl instanceof AssociableToAST) {
                    ((AssociableToAST<?>) typeDecl).toAst().ifPresent(node -> node.findCompilationUnit().
                            ifPresent(cu -> cu.getStorage().ifPresent(st -> queue.add(st.getPath()))));
                }

            });
        });
        for(var method : type.findAll(MethodDeclaration.class)) {
            processMethod(referenceType, method);
        }
    }

    private void processMethod(ReferenceType referenceType, MethodDeclaration method) {
        var resolvedMethod = method.resolve();
        var variables = new ArrayList<VariableInfo>();
        AtomicInteger variableIndex = new AtomicInteger();
        ReferenceType finalReferenceType = referenceType;
        method.getBody().ifPresent(blockStmt -> {
            String methodSignature = getSignature(resolvedMethod);
            blockStmt.getRange().ifPresent(r -> {
                var methodInfo = finalReferenceType.findMethodByNameAndParameters(methodSignature);
                if (methodInfo != null) {
                    if ((methodInfo.getModifier() & Modifier.STATIC) == 0) {
                        variables.add(new VariableInfo("this", referenceType.getSignature(),
                                r.begin.line, r.end.line,
                                variableIndex.getAndIncrement()));
                    }
                    method.getParameters().forEach(p -> {
                        variables.add(new VariableInfo(p.getNameAsString(), getJNIType(p.resolve().getType()),
                                r.begin.line, r.end.line,
                                variableIndex.getAndIncrement()));
                    });
                    blockStmt.findAll(VariableDeclarator.class).forEach(variableDeclarator -> {
                        variables.add(new VariableInfo(variableDeclarator.getNameAsString(),
                                getJNIType(variableDeclarator.resolve().getType()),
                                variableDeclarator.getRange().get().begin.line,
                                getEndLine(variableDeclarator, blockStmt),
                                variableIndex.getAndIncrement()));
                    });
                    methodInfo.setLines(toSet(r));
                    methodInfo.setVariables(variables);
                }
            });
        });
    }

    private static int getEndLine(VariableDeclarator variableDeclarator, Node parent) {
        return variableDeclarator.findFirst(Node.TreeTraversal.PARENTS, n -> n instanceof NodeWithBody ?
                Optional.of(n) : Optional.empty()).flatMap(Node::getRange).orElseGet(() -> parent.getRange().get()).end.line;
    }

    private NavigableSet<Integer> toSet(Range range) {
        var result = new TreeSet<Integer>();
        for(int i=range.begin.line;i <= range.end.line;i++) {
            result.add(i);
        }
        return result;
    }

    private String getSignature(ResolvedMethodDeclaration resolvedMethod) {
        StringBuilder builder = new StringBuilder(resolvedMethod.getName());
        builder.append('(');
        for(int i=0; i < resolvedMethod.getNumberOfParams();++i) {
            var type = resolvedMethod.getParam(i).getType().erasure();
            if (type instanceof ResolvedReferenceType) {
                type = ((ResolvedReferenceType) type).toRawType();
            }
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(type.describe());
            /* GDB add this for non primitive parameters */
            if (!type.isPrimitive()) {
                builder.append(" *");
            }
        }
        builder.append(')');
        return builder.toString();
    }

    private void getJNIType(ResolvedType type, StringBuilder builder) {
        if (type.isArray()) {
            for(int i=0; i < type.arrayLevel();++i) {
                builder.append('[');
            }
            getJNIType(type.asArrayType().getComponentType(), builder);
        } else if (type.isPrimitive()) {
            builder.append(Translator.typeSignature.get(type.asPrimitive().describe()));
        } else if (type.isVoid()) {
            builder.append(Translator.typeSignature.get("void"));
        } else if (type.isReferenceType()) {
            builder.append('L');
            builder.append(type.asReferenceType().getQualifiedName().replace('.', '/'));
            builder.append(';');
        } else if (type.isTypeVariable()) {
            getJNIType(type.asTypeVariable().erasure(), builder);
        }
    }

    private String getJNIType(ResolvedType type) {
        var builder = new StringBuilder();
        getJNIType(type, builder);
        return builder.toString();
    }
}
