# Symbol Table

Symbol table is a common idea in language processing, it goes hand in hand with [scopes](../docs/Scopes.md).

Symbol table is a solution to three concerns:

1. Minding scopes,
2. Handling imports,
3. Recording variable names.

It is organized as four following maps:

1. static ```Map<String, Map<String, Type>>```
2. static ```Map<String, Map<String, Function>>```
3. static ```Map<String, Map<String, Object>>```
4. ```Map<String, Variable>```

## Variable names

Strings are keys for each map. Each string is a name for corresponding variable/type/function. It solves third problem
and makes sharing reference-type values between variables straightforward, unlike storing name in
a [Variable](../src/main/kotlin/properties/Variable.kt) class (how to store multiple names? When to remove a name? etc.)
.

## Scopes

Most of the evaluation functions have SymbolTable parameter for changing scopes. Each new block evaluation will copy
current symbol table so that new variable names are not stored in an original scope (this is applicable only for class
and function evaluation, "while" and conditional blocks pass variables further to the outside).

## Imports

Functions and classes can be imported from other files. The main issue is that functions from different files might have
same names. That's why three static maps have a "second map layer": second map's keys are file names in which a function
is declared.