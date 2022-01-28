package raw

class RawRect(
    override val name: String,
    override var color: String,
    override var scale: String,
    override var rotation: String,
    override var position: String,
    //override val parent: String
) : RawNode()