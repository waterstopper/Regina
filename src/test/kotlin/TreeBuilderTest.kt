import org.junit.Test

internal class TreeBuilderTest {

    // TODO
    // 1. move root to defs, no need to frame it specially DONE
    // 2. think about changing children in Container to map because have to somehow preserve node type
    // (maybe in 'type' definition?) DONE
    // 3. remove @ in links PARTIALLY DONE
    @Test
    fun resolveTree() {
        createDefs()

        //println(evaluation.Global.evaluate("1 < 10 ? Segment : Nothing"))

        TreeBuilder().resolveTree()
    }

    private fun createDefs() {
        val root = Container("Root", null, mutableMapOf())
        root.declarations["child"] = Formula("Segment")
        root.declarations["iter"] = Formula("0")
        root.declarations["x"] = Formula("0")
        root.declarations["y"] = Formula("0")
        root.declarations["x2"] = Formula("0")
        root.declarations["y2"] = Formula("0")
        root.declarations["type"] = Formula("Line")

        val segment = Container("Segment", root, mutableMapOf())
        segment.declarations["next"] = Formula("iter < 10 ? Segment : Nothing")
        segment.declarations["iter"] = Formula("parent.iter + 1")
        segment.declarations["x"] = Formula("parent.x2")
        segment.declarations["y"] = Formula("parent.y2")
        segment.declarations["x2"] = Formula("x")
        segment.declarations["y2"] = Formula("y + 10")
        segment.declarations["type"] = Formula("Line")

        val nothing = Container("Nothing",root, mutableMapOf())

        TreeBuilder.definitions.addAll(mutableListOf(root, segment, nothing))
    }
}