package rand

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RandNum")
sealed class RandNum : Rand() {
    @Contextual
    abstract val from: Number
    @Contextual
    abstract val until: Number
    @Contextual
    abstract val step: Number

    abstract override fun evaluate(): Number
}