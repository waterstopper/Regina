# Scopes
## Global
In global scope classes and functions are declared.
## Class
In class scope assignments and functions are declared
## Function
Functions can have variable assignments and blocks. Functions are not changing its arguments, arguments are values, not references. But everything else is changeable by referencing from function body.
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
## Blocks
There are two types of blocks: while cycle and if-else. Both of them change already defined values. Variables defined inside of blocks are not visible outside from it.
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