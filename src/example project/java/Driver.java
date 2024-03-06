import organisms.OrganismBase;
import io.github.komet0141.subclassCollector.loader.SubclassLoader;
import unorganic.UnorganicBase;

public class Driver {
    public static void main(String[] args) {
        System.out.println(OrganismBase.INSTANCES.values());
        System.out.println(UnorganicBase.INSTANCES.values());
        SubclassLoader.load();
        System.out.println(OrganismBase.INSTANCES.values());
        System.out.println(UnorganicBase.INSTANCES.values());
    }
}