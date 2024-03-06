package organisms.Mammals;

import organisms.OrganismBase;

public class Doge extends OrganismBase {
    public static void initialize(Class<? extends OrganismBase>cls){
        OrganismBase.initialize(cls);
        System.out.println("initializing from overriden initializer");
    }
}
