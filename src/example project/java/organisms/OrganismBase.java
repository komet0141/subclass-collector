package organisms;

import io.github.komet0141.subclassCollector.CollectSubclass;
import io.github.komet0141.subclassCollector.utils.InstanceHolder;

@CollectSubclass
public abstract class OrganismBase {
    public final static InstanceHolder<OrganismBase> INSTANCES = new InstanceHolder<>();
    @CollectSubclass.Initializer
    public static void initialize(Class<? extends OrganismBase> clazz) {
        INSTANCES.put(clazz);
    }
}