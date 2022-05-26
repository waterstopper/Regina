# Scopes

Scopes define visibility of classes, functions. In GRVPL everything is public.

## Global

In global scope classes and functions are declared.

## Class

In class scope properties are declared. Functions cannot be declared in class to increase language usability.

Classes cannot be reassigned. ```ClassName = something``` will create a variable or property with same name and it will
shadow that class for its scope, making it impossible to use ```class ClassName``` in scope.

## Function

Functions can have variable assignments and blocks. Functions are not changing its arguments, arguments are values, not
references. But everything else is changeable by referencing from function body.

```kotlin
const class Colors {
    BLACK = "000000"
}

fun changeClassProperties(arg) {
    arg = 5 // arg will change only for function scope
    Colors.BLACK = "111111" // Colors.BLACK is changed
    arg.parent.property = 3 // property will change outside of function scope
} 
```

Variables are visible from anywhere, because assignment is same to declaration

```kotlin
fun scope() { 
    i=0
    while(i < 5) {
        a = 1
        i = i + 1
    }
    return a // here a is visible and equals 1
}
```

*This behaviour differs from most of the languages, consequently it might change in future.*

## Blocks

There are two types of blocks: while cycle and if-else. Both of them change already defined values. Variables defined
inside of blocks are not visible outside from it.

### While cycle

```kotlin
fun someFunction() {
    a = 5
    // a == 5
    while (condition) {
        a = 3
        b = 2
        // a == 3
    }
    // a == 3
    // b is not defined
    a = b // will not compile
}
```

### If-else

Unlike ternary conditional operator, else block can be omitted.