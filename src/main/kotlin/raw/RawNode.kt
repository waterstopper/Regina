package raw

import kotlinx.serialization.Serializable

@Serializable
sealed class RawNode(val name: String, val parent: String) {
    abstract fun calculateLinks()
}