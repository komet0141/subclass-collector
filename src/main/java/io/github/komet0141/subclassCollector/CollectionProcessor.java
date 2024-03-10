package io.github.komet0141.subclassCollector;

import io.github.komet0141.subclassCollector.utils.SubclassLoader;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionProcessor extends AbstractProcessor {
    private String subpkgName;
    private int numRound = 0;
    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    private final Map<String, String> initializers = new HashMap<>();
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
        
        numRound++;
    }
    
    
    
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        note("=========================starting round "+numRound+" of annotation processing of CollectSubclass=========================");
        try {
            note("---performing validation of round %s---", numRound);
            validateSuperclasses(roundEnv);
            validateInitializers(roundEnv);
            note("-----ending validation of round %s-----", numRound);
            mapInitializers(roundEnv);
            if(!initializers.isEmpty()) {
                validatePackageName(roundEnv);
                generateCode();
            }
        }
        catch (Exception e) {
            initializers.clear();
            error("failed to generate loader.");
            error(e.toString());
            Arrays.stream(e.getStackTrace()).forEach(this::error);
        }
        note("==========================ending round "+numRound+" of annotation processing of CollectSubclass==========================");
        return true;
    }
    private void validatePackageName(RoundEnvironment roundEnv) throws Exception {
        Set<? extends Element> elms = roundEnv.getElementsAnnotatedWith(CollectSubclass.OutputPackage.class);
        if(elms.isEmpty()) throw new Exception(format("there needs to be %s somewhere in code to specify package name of %s", CollectSubclass.OutputPackage.class,SubclassLoader.class.getSimpleName()));
        if(elms.size() > 1) throw new Exception(format("there can be only one %s in your code", CollectSubclass.OutputPackage.class));
        
        elms
                .iterator()
                .next()
                .getAnnotationMirrors()
                .get(0)
                .getElementValues()
                .values()
                .forEach(x->{subpkgName = (String) x.getValue();});
    }
    private void validateInitializers(RoundEnvironment roundEnv) throws Exception {
        for(Element elm : roundEnv.getElementsAnnotatedWith(CollectSubclass.Initializer.class)) {
            ExecutableElement exElm = (ExecutableElement) elm;
            
            note("evaluating as initializer: %s.%s",exElm.getEnclosingElement(),exElm.getSimpleName());
            TypeElement enclosingElm = (TypeElement) exElm.getEnclosingElement();
            
            if (!exElm.getModifiers().contains(Modifier.PUBLIC))
                throw new Exception(format("%s has to be public to be used as initializer", exElm));
            
            if (!exElm.getModifiers().contains(Modifier.STATIC))
                throw new Exception(format("%s has to be static to be used as initializer", exElm));
            
            if (!isValidSuperclassAnnotation(enclosingElm))
                throw new Exception(format("%s annotation should be used in methods enclosed by class annotated with %s annotation, or subclass annotated with %s",
                        CollectSubclass.Initializer.class, CollectSubclass.class, CollectSubclass.Abstract.class));
            
            if (exElm.getReturnType().getKind() != TypeKind.VOID)
                warn("return value of %s has no use", exElm);
            
            List<? extends VariableElement> params = exElm.getParameters();
            TypeElement classE = elementUtils.getTypeElement(Class.class.getCanonicalName());
            TypeMirror defaultParamType = typeUtils.getDeclaredType(classE, typeUtils.getWildcardType(enclosingElm.asType(),null) );
            if (params.size() != 1 || !typeUtils.isAssignable(defaultParamType, params.get(0).asType()))
                throw new Exception(format("%s should be (%s) -> void", exElm, defaultParamType));
            
            note(" qualified as initializer: %s", exElm.getSimpleName());
            initializers.put(exElm.getEnclosingElement().toString(), exElm.getSimpleName().toString());
        }
    }
    private void validateSuperclasses(RoundEnvironment roundEnv) throws Exception{
        for (Element elm : getSuperclassElements(roundEnv)) {
            note("evaluating as collecting superclass: %s", elm);
            TypeElement typeElm = (TypeElement) elm;
            
            isValidSubclass(typeElm);
            
            Element superclassElm = typeUtils.asElement(typeElm.getSuperclass());
            if(containAnnotation(superclassElm, CollectSubclass.class))
                throw new Exception(format("subclass of %s cannot have %s annotation(%s)", superclassElm, CollectSubclass.class, typeElm));
            
            int numInitializer = getInitializers(typeElm).size();
            
            if (numInitializer > 1)
                throw new Exception(format("%s can have only 1 initializer", typeElm));
            
            if (numInitializer == 0)
                throw new Exception(format("%s needs to have an method annotated with %s", typeElm, CollectSubclass.Initializer.class));
            
            note(" qualified as collecting superclass: %s\n", typeElm);
        }
    }
    private Set<Element> getSuperclassElements(RoundEnvironment roundEnv) {
        return new HashSet<Element>(){{
            roundEnv.getElementsAnnotatedWith(CollectSubclass.class)
                    .stream()
                    .filter(CollectionProcessor.this::isValidSuperclassAnnotation)
                    .forEach(this::add);
            roundEnv.getElementsAnnotatedWith(CollectSubclass.Abstract.class)
                    .stream()
                    .filter(CollectionProcessor.this::isValidSuperclassAnnotation)
                    .forEach(this::add);
        }};
    }
    private Set<Element> getCollectingSubclasses(RoundEnvironment roundEnv) {
        return new HashSet<Element>(){{
            this.addAll(roundEnv.getElementsAnnotatedWith(CollectSubclass.class));
            roundEnv.getElementsAnnotatedWith(CollectSubclass.Abstract.class)
                    .stream()
                    .filter(x->!hasAnnotation(x, CollectSubclass.Abstract.class))
                    .forEach(this::add);
        }};
    }
    private boolean isValidSuperclassAnnotation(Element elm) {
        boolean result = hasAnnotation(elm, CollectSubclass.class) ||
               hasAnnotation(typeUtils.asElement(((TypeElement) elm).getSuperclass()),CollectSubclass.Abstract.class);
        return result;
    }
    private void isValidSubclass(TypeElement typeElm) throws Exception{
        note("evaluating as collected class: %s",typeElm);
        
        if(!typeElm.getModifiers().contains(Modifier.PUBLIC))
            throw new Exception(format("class annotated with %s needs to be public", CollectSubclass.class));
        
        if(typeElm.getKind() == ElementKind.INTERFACE)
            throw new Exception(format("annotating %s with %s has no meaning because annotations of interfaces are not inherited", typeElm, CollectSubclass.class));

        
        note(" qualified as collected class: %s",typeElm);
    }
    private void mapInitializers(RoundEnvironment roundEnv) {
        note("---mapping initializers to subclasses (round %s)---",numRound);
        for(Element elm : getCollectingSubclasses(roundEnv)) {
            note("mapping initializer of: %s",elm);
            List<String> path = new ArrayList<>();
            for(TypeElement cls = (TypeElement) elm;isCollectedSubclass(cls);cls = (TypeElement) typeUtils.asElement(cls.getSuperclass())){
                path.add(cls.toString());
            }
            note("final path: %s", path);
            String initializerName = getInitializers(elementUtils.getTypeElement(path.get(path.size()-1)))
                    .get(0)
                    .getSimpleName()
                    .toString();
            note("mapping initializer to: %s.%s", elm,initializerName);
            path.forEach(className -> initializers.put(className, initializerName));
        }
        note("----finished mapping of initializers (round %s)----",numRound);
    }
    private void generateCode() throws Exception {
        note("loading subclasses: %s (round %s)",initializers.keySet(),numRound);
        String fullClassName = SubclassLoader.fullClassName(subpkgName);
        note("generating "+fullClassName);
        
        JavaFileObject loaderFileObject = filer.createSourceFile(fullClassName);
        Writer out = loaderFileObject.openWriter();
        out.write("package "+ SubclassLoader.fullPackageName(subpkgName) +";public class "+SubclassLoader.class.getSimpleName()+" {public static void load(){}static{");

        generateInitializer(out);
    }
    private void generateInitializer(Writer out) throws Exception {
        for (Map.Entry<String, String> entry : initializers.entrySet()) {
            String key = entry.getKey();
            String initName = entry.getValue();
            boolean isAbstract = elementUtils
                    .getTypeElement(key)
                    .getModifiers()
                    .contains(Modifier.ABSTRACT);
            if (!isAbstract) out.write(format("%s.%s(%s.class);", key, initName, key));
        }
        
        out.write("}}");
        out.close();
        initializers.clear();
    }
    private boolean isCollectedSubclass(TypeElement typeE) {
        return containAnnotation(typeE, CollectSubclass.class) ||
                (
                        !hasAnnotation(typeE, CollectSubclass.Abstract.class) &&
                        containAnnotation(typeE, CollectSubclass.Abstract.class)
                );
    }
    
    
    private List<ExecutableElement> getInitializers(TypeElement typeElm) {
        return typeElm
                .getEnclosedElements()
                .stream()
                .filter(elm -> containAnnotation(elm, CollectSubclass.Initializer.class))
                .map(x->(ExecutableElement)x)
                .collect(Collectors.toList());
    }
    private void warn(Object msg, Object... objs) {
        messager.printMessage(Kind.WARNING, format(msg, objs));
    }
    private void note(Object msg, Object... objs) {
        messager.printMessage(Kind.NOTE, format(msg, objs));
    }
    private void error(Object msg, Object... objs) {
        messager.printMessage(Kind.ERROR, format(msg, objs));
    }
    private String format(Object msg, Object... objs) {
        if(msg instanceof String) {
            return String.format((String) msg, objs);
        } else
            return msg +", "+String.join(", ",Arrays.stream(objs).map(String::valueOf).toArray(String[]::new));
    }
    private static boolean hasAnnotation(Element elm, Class<? extends Annotation> annotation) {
        return elm.getAnnotationMirrors()
                .stream()
                .map(Objects::toString)
                .map(x -> x.substring(1))
                .map(x -> x.equals(annotation.getCanonicalName()))
                .reduce(false, (x, y) -> x || y);
    }
    private boolean containAnnotation(Element elm, Class<? extends Annotation> annotation) {
        return elm.getAnnotation(annotation) != null;
    }
}