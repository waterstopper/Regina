# Recursive instantiating and assigning

Core idea of the improg is recursive instantiating. Classes can have references to other classes inside them as
properties. These property-classes are evaluated dynamically. Take a look at this example:

```kotlin
class Node export rect {
    // export = false - can deny export
    iter = if (parent) parent.iter + 1 else 0

    childNode = if (iter == 5) Nothing() else Node()

    position = if (childNode is Node) \
    Position(childNode.position.x + 1, childNode.position.y) \
    else Position(0, 0)
}

fun main() {
    Node()
}
```

This will create following svg:

```svg

<svg>
    <rect x="4" y="0"/>
    <rect x="3" y="0"/>
    <rect x="2" y="0"/>
    <rect x="1" y="0"/>
    <rect x="0" y="0"/>
</svg>
```

First algorithm creates empty Node() (we'll call it *Node0*) from main(). Then, starting from top to bottom:

1. algorithm assigns **iter** of *Node0* to 0 because parent returns 0 as an equivalent of null.
2. After that, **childNode** is assigned a new Node (*Node1*).
3. **position** cannot be assigned because **childNode.position** is not yet assigned. Algorithm goes to
   childNode.position, that is *Node1*.position and tries to assign it. However, childNode is required, so we go to *
   Node1*.childNode, which needs iter.

*It's not important to consider while assigning values, but it shows why there cannot be any cyclic dependencies for
properties*.

## Implied decision

Also, it is the reason why class functions are impossible. Imagine this case:

```kotlin
class FunctionOveruse {
    prop = make()

    fun make() {
        FunctionOveruse()
        return prop
    }
}
```

Both lines in ```make()``` will execute forever.

To make functions as expressive as possible, it is important to allow class instantiating inside them. If

We either make internal class functions (which is purely decompositional thing) or make instantiating inside functions
possible (and ```fun main()``` as an entry point)