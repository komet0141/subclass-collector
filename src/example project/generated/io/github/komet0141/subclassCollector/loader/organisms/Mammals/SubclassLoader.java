package io.github.komet0141.subclassCollector.loader.organisms.Mammals;

public class SubclassLoader {
    public static void load() {
    }
    
    static {
        organisms.Mammals.Camel.initialize(organisms.Mammals.Camel.class);
        organisms.Mammals.Cat.initialize(organisms.Mammals.Cat.class);
        organisms.Mammals.Dog.initialize(organisms.Mammals.Dog.class);
        organisms.Mammals.Doge.initialize(organisms.Mammals.Doge.class);
        organisms.Mammals.Duck.initialize(organisms.Mammals.Duck.class);
        unorganic.rocks.Gravel.init(unorganic.rocks.Gravel.class);
        unorganic.rocks.Marble.init(unorganic.rocks.Marble.class);
    }
}