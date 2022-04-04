# Object

Sometimes it is important to have a global variable shared between different instances. It is possible to make such
thing by creating ```class Constants``` but each time a constant is needed compiler would have to create new instance of
that class: ```Constants().someConstant``` which is time-consuming.

That's why objects were added. They represent static classes and evaluated before ```fun main()```. There is only one
instance of each object per programme.

## Constraints

**Objects cannot be instantiated**. All references are called without constructor
invocation ```Constants.someConstant```.

**Constants cannot have property-instances**. If they could, whole util class idea is ruined, because utils store data,
not structure.