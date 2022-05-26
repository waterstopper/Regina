# Syntax

ReGIna's syntax is an amalgamation of kotlin and python.

## Declarations

### Class

Class declaration should satisfy following format:

```kotlin 
class ClassName : SuperClassName import circle {
    ...
}
```

where `: SuperClassName` and `import ...` are optional.

### Object

```kotlin
object ObjectName import circle {
    ...
}
```

Similar to type, although it cannot be inherited.

### Function

```kotlin
fun functionName(arg0, arg1, ...) {
    ...
}
```

### Property/variable assignment

```kotlin
variableName = ...
className.PropertyName = ...
```

Variables and properties are dynamic, meaning they are type independent. `a` can be String and in the next line it can
be Int or class instance.

## References

References are expressions of form `a.b.c`. They serve 3 purposes:

1. accessing class/object properties
2. accessing primitive/class/object and functions
3. specifying package declarations
4. reducing expressions

### 1. Accessing properties

# Code conventions

Camel case is used.

Names of [class](Syntax.md/###Class), [object](Syntax.md/###Object) start with capital.

Variables, functions start with lowercase letter.