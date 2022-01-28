package rand

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Deprecated("use RandNum")
@Serializable
@SerialName("RandInt")
class RandInt(
    val from: Int = 0,
    val until: Int = 1,
    val step: Int = 1
) {//: RandNum() {


}