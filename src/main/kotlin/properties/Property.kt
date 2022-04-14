package properties


import structure.SymbolTable.Type
/**
 * used only as a class property. In everything else is similar to Variable
 */
abstract class Property(name: String, parent: Type?) : Variable(name, parent)