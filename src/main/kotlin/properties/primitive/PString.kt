package properties.primitive

import properties.Variable
import table.SymbolTable.Type

class PString(value: String, parent: Type?) : Primitive(value, parent){
    override fun getPValue() = value as String
}