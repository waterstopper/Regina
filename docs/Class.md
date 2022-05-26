# Class

## Structure

Class must have **a name**, and **a block of properties**:

An example of a class declaration:

``` kotlin
class name : superclass {
    variableName = variableValue
    parent.overrideName = overrideValue
    ...
}
```

There are special property classes. Some base classes have their own special property classes, such as 'points' array in
Polyline.

## Instantiation

Constructors have named arguments only. Requirements:

* unlike class properties, left side of argument is identifier only (it cannot be link)
* right side should be evaluated instantly, opposing the dynamic nature of class properties

## Export keyword

## Const keyword

## Inheritance

Inherited class has all the properties and functions of base class. However, if class shadows some properties

#### Resolve order for inherited class

Is stated in [compilation process](Compilation.md)

### Special property classes that each class has

#### Position

#### Color