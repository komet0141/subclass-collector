package io.github.komet0141.subclassCollector;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionProcessor extends AbstractProcessor {
    private final String pkgNameBase = CollectionProcessor.class.getPackage().getName()+".loader";
    private final String className = "SubclassLoader";
    private boolean noError = true;
    private int numRound = 0;
    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    private JavaFileObject loaderFileObject;
    private final List<Element> subclasses = new ArrayList<>();
    private final List<collectingClass> collectingClasses = new ArrayList<>();
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Stream.of(CollectSubclass.class, CollectSubclass.Initializer.class)
                .map(Class::getName)
                .collect(Collectors.toSet());
    }
    
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        
        subclasses.clear();
        numRound++;
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        note("=========================starting round of annotation processing of CollectSubclass=========================");
        note("round: "+numRound);
        processCollectSubClass(roundEnv);
        processInitializer(roundEnv);
        if(noError && !subclasses.isEmpty()) try {generateCode();}
        catch (Exception e) {
            error("failed to create source file.");
            warn(e);
            Arrays.stream(e.getStackTrace()).forEach(this::warn);
        };
        note("==========================ending round of annotation processing of CollectSubclass==========================");
        return true;
    }
    
    private void generateCode() throws IOException {
        note("generating code of round: "+numRound);
        note("loading subclasses: %s",subclasses);
        note("collecting superclasses: %s",collectingClasses.stream().map(x->x.type).collect(Collectors.toList()));
        
        String sourceCode = null;
        try (Scanner s = new Scanner(loaderFileObject.openInputStream()).useDelimiter("^")) {
            sourceCode = s.next();
        }catch (Exception e){}
        
        
        String subpkgName = elementUtils
                .getPackageOf(subclasses.get(0))
                .getQualifiedName()
                .toString();
        String fullPackageName = pkgNameBase+"."+subpkgName;
        
        loaderFileObject = filer.createSourceFile(fullPackageName+"."+className);
        Writer out = loaderFileObject.openWriter();

        if(sourceCode == null)
            out.write("package "+ fullPackageName +";public class "+className+" {public static void load(){");
        else
            out.write(sourceCode.substring(0,sourceCode.length()-2));
        
        generateInitializer(out);
    }
    
    private void generateInitializer(Writer out) throws IOException {
        for (Element clazz : subclasses)
            for (collectingClass collClass : collectingClasses)
                if (typeUtils.isAssignable(clazz.asType(), collClass.type.asType()))
                    out.write(clazz + "." + collClass.initializer.getSimpleName() + "(" + clazz + ".class);");
        
        out.write("}}");
        out.close();
        subclasses.clear();
    }
    
    @SuppressWarnings("unchecked")
    private void processInitializer(RoundEnvironment roundEnv) {
        for(ExecutableElement exElm : (Set<ExecutableElement>)roundEnv.getElementsAnnotatedWith(CollectSubclass.Initializer.class)) {
            note("evaluating %s of %s",exElm, exElm.getEnclosingElement());
            if(isValidInitializer(exElm))
                collectingClasses.add(new collectingClass((TypeElement) exElm.getEnclosingElement(), exElm));
        }
    }
    
    private boolean isValidInitializer(ExecutableElement exElm) {
        Element enclosingElm = exElm.getEnclosingElement();
        
        if (!exElm.getModifiers().contains(Modifier.PUBLIC)) {
            error("%s has to be public to be used as initializer", exElm);
            return false;
        }
        
        if (!exElm.getModifiers().contains(Modifier.STATIC)) {
            error("%s has to be static to be used as initializer", exElm);
            return false;
        }
        
        if (!hasAnnotation(enclosingElm, CollectSubclass.class)) {
            error("%s annotation should be used in methods enclosed by class annotated with initializeSubclass annotation", CollectSubclass.Initializer.class);
            return false;
        }
        
        if (exElm.getReturnType().getKind() != TypeKind.VOID) {
            warn("return value of %s has no use", exElm);
        }
        
        List<? extends VariableElement> params = exElm.getParameters();
        if (params.size() != 1 || typeUtils.isSameType(params.get(0).asType(), enclosingElm.asType())) {
            error("%s should be (%s) -> void", exElm, enclosingElm);
            return false;
        }
        
        note("%s qualified as initializer", exElm);
        return true;
    }
    @SuppressWarnings("unchecked")
    private void processCollectSubClass(RoundEnvironment roundEnv) {
        for(TypeElement elm : (Set<TypeElement>)roundEnv.getElementsAnnotatedWith(CollectSubclass.class)) {
            final String elmName = elm.getQualifiedName().toString();
            note("processing: "+elmName);
            if(isValidCollectedClass(elm)) subclasses.add(elm);
        }
    }
    private boolean isValidCollectedClass(TypeElement typeElm) {
        
        if(!typeElm.getModifiers().contains(Modifier.PUBLIC)) {
            error("class annotated with %s needs to be public", CollectSubclass.class);
            return false;
        }
        
        if(typeElm.getKind() == ElementKind.INTERFACE) {
            warn("annotating %s with %s has no meaning because annotations of interfaces are not inherited", typeElm, CollectSubclass.class);
            return false;
        }
        if(hasAnnotation(typeElm, CollectSubclass.class)) {
            Element superclassElm = typeUtils.asElement(typeElm.getSuperclass());
            if(containAnnotation(superclassElm, CollectSubclass.class)) {
                error("subclass of %s cannot have %s annotation", superclassElm, CollectSubclass.class);
                return false;
            }
            
            if (!isValidSuperclass(typeElm)) return false;
        }
        
        if(typeElm.getModifiers().contains(Modifier.ABSTRACT)) {
            warn("skipping %s because %s is an abstract class", typeElm, typeElm);
            return false;
        }
        
        note("%s qualified as collected class",typeElm);
        return true;
    }
    boolean isValidSuperclass(TypeElement typeElm) {
        note("evaluating %s as collecting superclass", typeElm);
        int numInitializer = typeElm
                .getEnclosedElements()
                .stream()
                .map(t -> containAnnotation(t, CollectSubclass.Initializer.class))
                .mapToInt(bool -> bool ? 1 : 0)
                .sum();
        if (numInitializer > 1) {
            error("%s can have only 1 initializer", typeElm);
            return false;
        }
        if (numInitializer == 0) {
            error("%s needs to have an method annotated with %s", typeElm, CollectSubclass.Initializer.class);
            return false;
        }
        
        note("%s qualified as collecting superclass", typeElm);
        return true;
    }
    
    
    
    private void warn(Object msg) {
        messager.printMessage(Kind.WARNING, msg == null ? "null" : msg.toString());
    }
    private void warn(String str, Object... objs) {
        warn(String.format(str, objs));
    }
    private void note(Object msg) {
        messager.printMessage(Kind.NOTE, msg == null ? "null" : msg.toString());
    }
    private void note(String str, Object... objs) {
        note(String.format(str,objs));
    }
    private void error(Object msg) {
        messager.printMessage(Kind.ERROR, msg == null ? "null" : msg.toString());
        noError = false;
    }
    private void error(String str, Object... objs) {
        error(String.format(str,objs));
    }
    private boolean hasAnnotation(Element elm, Class<? extends Annotation> annotation) {
        return elm.getAnnotationMirrors()
                .stream()
                .map(Objects::toString)
                .map(x -> x.substring(1))
                .map(x -> x.equals(annotation.getName()))
                .reduce(false, (x, y) -> x || y);
    }
    private boolean containAnnotation(Element elm, Class<? extends Annotation> annotation) {
        return elm.getAnnotation(annotation) != null;
    }
}