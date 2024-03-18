import io.github.komet0141.subclassCollector.CollectSubclass;
import organisms.OrganismBase;
import io.github.komet0141.subclassCollector.utils.SubclassLoader;
import unorganic.UnorganicBase;

@CollectSubclass.OutputPackage(Driver.packageName)
public class Driver {
    static final String packageName = "exampleProject";
    public static void main(String[] args) {
        System.out.println(OrganismBase.INSTANCES.values());
        System.out.println(UnorganicBase.INSTANCES.values());
        SubclassLoader.load(packageName,
                "argument",
                "foo");
        System.out.println(OrganismBase.INSTANCES.values());
        System.out.println(UnorganicBase.INSTANCES.values());
    }
}