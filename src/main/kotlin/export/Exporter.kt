package export

import properties.Type
import properties.primitive.PArray
import properties.primitive.PDouble
import properties.primitive.PInt
import properties.primitive.PString

interface Exporter {
    fun visit(type: Type)
    fun visit(pArray: PArray)
    fun visit(pString: PString)
    fun visit(pDouble: PDouble)
    fun visit(pInt: PInt)
}