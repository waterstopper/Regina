package properties.primitive

import table.SymbolTable.Type

class PInt(value: Int, parent: Type?) : Primitive(value, parent){
    override fun getPValue() = value as Int
}