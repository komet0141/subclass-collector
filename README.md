# Subclass Collector

## Overview
A repo that aims to simulate inherited static initialize block. Expected use case in reducing boilerplate code in Minecraft modding.

## Description

### main feature
The main feature of this package is the annotation processor which would generate a `SubclassLoader` class with `load` method. This `load` method does the following:
- iterate through classes that has a superclass annotated with`@CollectSubclass`.
- for each of those class, initializer method specified with the `@CollectSubclass.Initializer` would be called.
  - consider the following code:
    ```
      @CollectSubclass
      class ParentClass {
          @CollectSubclass.Initializer
          public static void init(Class<? extends ParentClass> cls) {
            ...
          }
          ...
      }
    ```
    in this sample code, `SUBCLASS.init` would be called for every subclass of `ParentClass` when the `load` is called.
  - you might have realized, but if you have `init` with the same signature as the `init` of the superclass, then `load` would run the subclass's `init`, making the initializer simulate the inheritance and override.