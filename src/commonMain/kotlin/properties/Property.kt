package properties

import utils.Utils.NULL

/**
 * used only as a class property. In everything else is similar to Variable
 */
//@Serializable
abstract class Property(parent: Type?) : Variable(parent) {
    protected fun getParentOrNull(): Property = parent ?: NULL
}
