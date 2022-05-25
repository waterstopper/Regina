# Exporting

Initially I wanted to create a no-code web application for creating generators. After 3 months of rare thinking I came up with a simple idea of containers and primitives. Primitives would fully resemble SVG classes, such as line, rect, ellipse etc. And there were two types of containers:
1. Variant — contains n types of elements, randomly becomes one of them.
2. Iterative — contains n containers of its child type (this is where the idea of dynamic recursive instantiation came from).

Also, I thought about making SVG DSL, where SVG markup language would have cycles in it, something like:
```svg
<group for i in 5 <rect></group>
```

It creates problems, which can be resolved by writing **code**. But writing any code is complicated, so I decided: why not create an entire language? I already had some related knowledge from my first semester so that was the way to go. In brief periods of insight I regret not creating a small Javascript library but vanity got the best of me. This language is a byproduct of it.

exporting is now not embedded in language and that will make bitmap generation possible. It was a great (but obvious) idea which I hapily implemented.

# Export expressions
There were other ideas to implement export. But none of them satisfied all of the requirements:
* Should not fill much memory
* It should br possible to export a function result
* It should be possible to export one property into two different  exports or two properties into one

Ideas:
1. `exportArgs["attributes"] = {attributeName:value}` - if value is a big class, it consumes memory
2. ```kotlin
    @attribute(attributeName)
    propertyName = propertyValue
it is not possible to export into two exports