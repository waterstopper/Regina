package rand

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Rand")
sealed class Rand {
   abstract fun evaluate():Any
}