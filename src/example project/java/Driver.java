import io.github.komet0141.subclassCollector.CollectSubclass;
import io.github.komet0141.subclassCollector.loader.exampleProject.SubclassLoader;
import organisms.OrganismBase;
import unorganic.UnorganicBase;

@CollectSubclass.OutputPackage(Driver.packageName)
public class Driver {
    static final String packageName = "exampleProject";
    public static void main(String[] args) {
        System.out.println(OrganismBase.INSTANCES.values());
        System.out.println(UnorganicBase.INSTANCES.values());
        SubclassLoader.load();SubclassLoader.load();SubclassLoader.load();SubclassLoader.load();
        System.out.println(OrganismBase.INSTANCES.values());
        System.out.println(UnorganicBase.INSTANCES.values());
    }
}