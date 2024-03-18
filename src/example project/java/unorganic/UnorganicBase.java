package unorganic;

import io.github.komet0141.subclassCollector.CollectSubclass;
import io.github.komet0141.subclassCollector.utils.InstanceHolder;

@CollectSubclass
public abstract class UnorganicBase {
    public static InstanceHolder<UnorganicBase> INSTANCES = new InstanceHolder<>();
    
    @CollectSubclass.Initializer
    public static void init(Class<? extends UnorganicBase>clazz, Object[] args){
        INSTANCES.put(clazz);
    }
}
