package organisms.Creature;

import java.util.Arrays;

public class CreatureAlpha extends CreatureBaseA{
    public static void creatureInitA(Class<?extends CreatureBaseA> cls, Object[] args) {
        System.out.println("hello world from initializer! argument was: "+ Arrays.asList(args));
    }
}
