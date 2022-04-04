package deprecated.connections
@Deprecated("")
interface Connection {
    fun getRelativeToCenter(): Pair<Float, Float> = Pair(0f, 0f)

}