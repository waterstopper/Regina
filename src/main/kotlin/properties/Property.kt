package properties

import kotlinx.serialization.Serializable
import properties.primitive.PInt

/**
 * used only as a class property. In everything else is similar to Variable
 */
//@Serializable
abstract class Property (parent: Type?) : Variable(parent) {
    protected fun getParentOrNull(): Property = parent ?: PInt(0, null)
}
