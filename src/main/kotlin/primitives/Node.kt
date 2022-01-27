package primitives

sealed class Node() {
    abstract val parent: Node?
    var position: Pair<Float, Float> = Pair(0f, 0f)
    var color: List<Int> = listOf(0, 0, 0, 255)
    var scale: Pair<Float, Float> = Pair(1f, 1f)
    var rotation: Float = 0f
}