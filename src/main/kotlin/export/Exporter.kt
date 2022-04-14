package export

import properties.primitive.PArray
import properties.primitive.PDouble
import properties.primitive.PInt
import properties.primitive.PString
import SymbolTable

interface Exporter {
    fun visit(type: SymbolTable.Type)
    fun visit(pArray: PArray)
    fun visit(pString: PString)
    fun visit(pDouble: PDouble)
    fun visit(pInt: PInt)
}