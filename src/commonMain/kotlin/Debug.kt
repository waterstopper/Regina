import properties.Type
import properties.Variable
import properties.primitive.PDictionary
import properties.primitive.PList

// used for js. Debug classes are classes without references
interface Debug

fun elementToDebug(element: Variable, references: References) = if (element !is NestableDebug) {
    element.toDebugClass(references)
} else {
    val resolved = when (element) {
        is Type -> references.types[element.getDebugId().toString()]
        is PList -> references.lists[element.getDebugId().second]
        is PDictionary -> references.dictionaries[element.getDebugId().second]
        else -> throw Exception("Unexpected type")
    }
    if (resolved != null) {
        resolved
    } else {
        references.queue[element.getDebugId()] = element
        element.getDebugId()
    }
}

interface NestableDebug {
    fun getDebugId(): Pair<String, Any>
}

data class DebugType(val properties: Map<String, Any>) : Debug

data class DebugList(val properties: List<Any>) : Debug

data class DebugDictionary(val properties: MutableMap<Any, Any>) : Debug

class References(
    val types: MutableMap<String, DebugType> = mutableMapOf(),
    val lists: MutableMap<Int, DebugList> = mutableMapOf(),
    val dictionaries: MutableMap<Int, DebugDictionary> = mutableMapOf(),
    val queue: MutableMap<Any, Variable> = mutableMapOf()
)
