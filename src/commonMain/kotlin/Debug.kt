import properties.Type
import properties.Variable
import properties.primitive.*
import utils.Utils.NULL
import utils.Utils.toProperty

// used for js. Debug classes are classes without references
interface Debug

fun elementToDebug(element: Variable, references: References): Pair<String, Any> = if (element !is NestableDebug) {
    element.toDebugClass(references)
} else {
    val resolved = when (element) {
        is Type -> references.types[element.getDebugId().second] // TODO was .getDebugId().toString()
        is PList -> references.lists[element.getDebugId().second]
        is PDictionary -> references.dictionaries[element.getDebugId().second]
        else -> throw Exception("Unexpected type")
    } // returned resolved if != null
    if (resolved == null)
        references.queue[element.getDebugId()] = element
    element.getDebugId()
}

interface NestableDebug {
    fun getDebugId(): Pair<String, Any>
}

data class DebugType(val properties: Map<String, Pair<String, Any>>, val blankCopy: Type? = null) : Debug

data class DebugList(val properties: List<Pair<String, Any>>, val blankCopy: PList? = null) : Debug

data class DebugDictionary(
    val properties: MutableMap<Pair<String, Any>, Pair<String, Any>>,
    val blankCopy: PDictionary? = null
) : Debug

class References(
    val types: MutableMap<String, DebugType> = mutableMapOf(),
    val lists: MutableMap<Int, DebugList> = mutableMapOf(),
    val dictionaries: MutableMap<Int, DebugDictionary> = mutableMapOf(),
    val queue: MutableMap<Any, Variable> = mutableMapOf()
) {
    fun copy(v: Variable): Variable {
        val root = v.toDebugClass(this, copying = true)
        while (queue.isNotEmpty())
            queue.values.last().toDebugClass(this, copying = true)
        types.forEach { type ->
            type.value.properties.forEach {
                type.value.blankCopy!!.setProperty(
                    it.key, getReference(it.value).toProperty()
                )
            }
        }
        lists.forEach { list ->
            list.value.properties.forEach {
                list.value.blankCopy!!.getPValue().add(getReference(it))
            }
        }
        dictionaries.forEach { dict ->
            dict.value.properties.forEach {
                dict.value.blankCopy!!.getPValue()[getReference(it.key)] = getReference(it.value)
            }
        }
        return getReference(root)
    }

    private fun getReference(pair: Pair<String, Any>): Variable {
        return when (pair.first) {
            "Int" -> PInt(pair.second as Int)
            "Double" -> PDouble(pair.second as Double)
            "String" -> PString(pair.second as String)
            "List" -> lists[pair.second]!!.blankCopy!!
            "Dictionary" -> dictionaries[pair.second]!!.blankCopy!!
            "Null" -> NULL
            "Type", "Object" -> types[pair.second]!!.blankCopy!!
            else -> throw Exception("Unexpected reference name")
        }
    }
}