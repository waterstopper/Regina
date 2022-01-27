import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import primitives.Node
import primitives.Rect

class Deserialize {
//    @OptIn(ExperimentalSerializationApi::class)
//    val format = Json { explicitNulls = false }


    fun decode(json: String) {
        val root = Rect(null)
        val rect = Rect(root)
        val str = Json.encodeToString(rect)
        val obj = Json.decodeFromString<Node>(str)
    }
}