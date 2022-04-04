package deprecated.connections
@Deprecated("")
class BoundingBoxConnection(width:Float, height:Float, val position:Pair<Float,Float>) : Connection {
    override fun getRelativeToCenter(): Pair<Float,Float> = position
}