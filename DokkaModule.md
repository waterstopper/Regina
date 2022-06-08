# Module Regina-interpreter

The module is a library for Regina programming language

# Package lexer

Contains classes that:

* transform plain text of .rng files into AST tree
* perform simple semantic analysis
* create symbol tables for later use.

Parsing algorithm was taken and rewritten
from [here](https://www.cristiandima.com/top-down-operator-precedence-parsing-in-go).
This [article](https://www.crockford.com/javascript/tdop/tdop.html) also helped.

Many thanks to Cristian Dima and Douglas Crockford.

# Package evaluation

Contains API to interact with the system.

# Package table

Contains decomposed symbol table.