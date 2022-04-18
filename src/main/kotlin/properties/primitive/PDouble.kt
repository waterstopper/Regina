package properties.primitive

import SymbolTable
import SymbolTable.Type

class PDouble(value: Double, parent: Type?) : Primitive("", value, parent){
    override val symbolTable: SymbolTable = SymbolTable()
}