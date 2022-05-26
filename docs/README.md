# GRⱯPL

/ˈgræpl/, sounds like grapple. Generative random visual programming language. Not
a [VPL](https://en.wikipedia.org/wiki/Visual_programming_language#:~:text=In%20computing%2C%20a%20visual%20programming,than%20by%20specifying%20them%20textually.)

# PLOV

Programming Language Oriented on Visuals

## Conventions

Variables start with a letter and contain letters (a-z, A-Z), digits (0-9) and underscores (_)

Operators are 1-2 chars long. They cannot contain letters, digits and these characters: @ _ ( ) { } , ;

It is impossible to name classes and properties with same words because the program wouldn't know which data to pick

List of keywords (variables and containers cannot be named like that):

* x, y
* tX, tY
* rotation
* width
* height
* scaleWidth
* scaleHeight
* text
* type
* Root

## Properties

Description of what certain properties do:

* **group** - groups all children of this container and applies all transformations of such container to the group. By
  default, all containers are on the <svg> level,
* **fill** - color of space inside container. Opaque by default.
* **outline** - color of outline. Black by default
* **opacity** - opacity
* **x** - position

### Functions

functions are

#### Rand

There are two types of random: numeric and list

#### Math

* root(arg,power)
* pow(arg, power)
* sin(arg)
* cos(arg)