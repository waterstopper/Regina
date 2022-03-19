# Function
Similar to classes, functions can have assignments, which are visible only during function execution. Unlike classes, functions execute in a strict top-down order. So each undefined variable will provide a compile error

## Embedded functions
Even though language is built to be as flexible as possible, there are functions that cannot be reassigned.

```kotlin
class Array {
    fun add(a) // adds a to the end of array
    fun remove(a) // removes element a if found and returns it (even if not found)
    fun removeIndex(i) // removes element by index
    fun has(a) // returns 1 if array contains is a, else returns 0
}

class String {
    fun int() // returns int from this string
    fun double() // returns double from this string
    fun str() // returns itself
}
```