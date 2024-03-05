package organisms;

import io.github.komet0141.subclassCollector.CollectSubclass;

@CollectSubclass
public class Doge3 {
    @CollectSubclass.Initializer
    void bark(){};
    
    @CollectSubclass.Initializer
    void barkbark(){};
}
