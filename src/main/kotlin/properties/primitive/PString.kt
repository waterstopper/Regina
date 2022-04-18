package properties.primitive

import SymbolTable
import SymbolTable.Type

class PString(value: String, parent: Type?, override val symbolTable: SymbolTable = SymbolTable()) : Primitive("", value, parent)