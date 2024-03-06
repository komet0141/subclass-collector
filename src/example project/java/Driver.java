import io.github.komet0141.subclassCollector.loader.organisms.Mammals.SubclassLoader;
import organisms.OrganismBase;
import unorganic.UnorganicBase;

public class Driver {
    public static void main(String[] args) {
        System.out.println(OrganismBase.INSTANCES.values());
        System.out.println(UnorganicBase.INSTANCES.values());
        SubclassLoader.load();SubclassLoader.load();SubclassLoader.load();SubclassLoader.load();
        System.out.println(OrganismBase.INSTANCES.values());
        System.out.println(UnorganicBase.INSTANCES.values());
    }
}