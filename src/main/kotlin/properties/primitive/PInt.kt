package properties.primitive

import SymbolTable
import SymbolTable.Type

class PInt(value: Int, parent: Type?) : Primitive("", value, parent) {
    override val symbolTable: SymbolTable = SymbolTable()
}