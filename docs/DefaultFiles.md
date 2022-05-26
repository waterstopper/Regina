# Default files

There are three default files that make a basic library for GRVPL. All of them can be rewritten for each program.

## Exports file

Contain deprecated.export classes and classes needed for them. If concise naming is preferred, classes can be rewritten
like that:

```kotlin
class Line deprecated.export line {
    pos.x deprecated.export x
    pos.y deprecated.export y
    
    pos = Position
    pts = [pos,Position(x=1,y=0)]
}

class Rect : Line deprecated.export rect {
    
}
```

Export classes can be defined everywhere as every other class and function (considering [Scopes](Scopes.md))

## Constants file

Contain useful const classes ```Colors``` and ```Math```.