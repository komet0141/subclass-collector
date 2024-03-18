package organisms.Creature;

import io.github.komet0141.subclassCollector.CollectSubclass;

public abstract class CreatureBaseA extends CreatureBase{
    @CollectSubclass.Initializer
    public static void creatureInitA(Class<?extends CreatureBaseA> cls, Object[] args) {
        System.out.println("init from creature base A");
    }
}
