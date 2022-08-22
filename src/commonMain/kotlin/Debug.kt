// used for js. Debug classes are classes without references
interface Debug

data class DebugType(val properties: Map<String, Any>) : Debug

// @Array-number
data class DebugArray(val properties: List<Any>) : Debug {}

// @Dictionary-number
data class DebugDictionary(val properties: MutableMap<Any, Any>) : Debug {

}

class References(
    val types: MutableMap<String, DebugType>,
    val arrays: MutableMap<Int, DebugArray>,
    val dictionaries: MutableMap<Int, DebugDictionary>
) {

}