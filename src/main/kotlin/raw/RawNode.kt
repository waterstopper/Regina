package raw

import kotlinx.serialization.Serializable

@Serializable
class RawNode(val name:String, val parent:String) {
}