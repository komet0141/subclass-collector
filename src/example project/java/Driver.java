import organisms.OrganismBase;
import io.github.komet0141.subclassCollector.loader.SubclassLoader;
import unorganic.UnorganicBase;

public class Driver {
    public static void main(String[] args) {
        System.out.println(OrganismBase.INSTANCES);
        System.out.println(UnorganicBase.INSTANCES);
        SubclassLoader.load();
        System.out.println(OrganismBase.INSTANCES);
        System.out.println(UnorganicBase.INSTANCES);
    }
}