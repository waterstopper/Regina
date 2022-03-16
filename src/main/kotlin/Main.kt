import lexer.Token
import lexer.Parser
import java.io.File

fun main(args: Array<String>) {
//    val rnd:Rand = RandNum(5.0,7.0,2.0)
//    val str = Json.encodeToString(rnd)
//    println(str)
//    val obj = Json.decodeFromString<Rand>(str)
//    println(obj)
//    println(obj.evaluate())

    //Painter(Container("", null, mutableMapOf())).export()
    //println(Painter(Container("", null, mutableMapOf())).defaultLine())

    //println(Formula("-{@sin,angle} * 10 + x").getAllWords())
//    createDefs()
//    val t = TreeBuilder()
//    t.resolveTree()
//    val p = Painter(t.root)
//    p.export()
    val text = File("example.txt").readText()
    val s = Parser(text).statements()
    println(s.treeView())
    print("")
}

fun List<Token>.treeView(): String {
    val res = StringBuilder()
    for (t in this) {
        res.append(t.toTreeString(0))
        res.append('\n')
    }
    return res.toString()
}

private fun createDefs() {
    val root = Container("Root", null, mutableMapOf())
    //root.children.add(Property("type", root, "Line"))
    root.declarations["type"] = Formula("@Line")
    root.declarations["child"] = Formula("@Segment")
    //root.declarations["child2"] = Formula("@Segment")
    root.declarations["iter"] = Formula("0")
    root.declarations["x"] = Formula("20")
    root.declarations["y"] = Formula("0")
    root.declarations["x2"] = Formula("20")
    root.declarations["y2"] = Formula("0")

    //root.declarations["rotation"] = Formula()

    val segment = Container("Segment", root, mutableMapOf())
    //segment.children.add(Property("type", segment, "Line"))
    segment.declarations["type"] = Formula("@Line")
    segment.declarations["next"] = Formula("iter < 10 ? ({@randNum,0,1}>0.3 ? @Segment : @DoubleSegment) : @Nothing")
    segment.declarations["iter"] = Formula("parent.iter + 1")
    segment.declarations["x"] = Formula("parent.x2")
    segment.declarations["y"] = Formula("parent.y2")
    segment.declarations["x2"] = Formula("-{@sin,angle} * 10 + x")
    segment.declarations["y2"] = Formula("{@cos,angle} * 10 + y")
    segment.declarations["angle"] = Formula("{@randNum,-0.7,0.7}")

    val doubleSegment = Container("DoubleSegment", null, mutableMapOf())
    doubleSegment.declarations["child"] = Formula("@Segment")
    doubleSegment.declarations["child2"] = Formula("@Segment")
    doubleSegment.declarations["x"] = Formula("parent.x2")
    doubleSegment.declarations["y"] = Formula("parent.y2")
    doubleSegment.declarations["x2"] = Formula("parent.x2")
    doubleSegment.declarations["y2"] = Formula("parent.y2")
    doubleSegment.declarations["iter"] = Formula("parent.iter + 1")

    //segment.declarations["rotation"] = Formula("{@randNum,-10,10}")

    val nothing = Container("Nothing", root, mutableMapOf())

    TreeBuilder.definitions.addAll(mutableListOf(root, segment, nothing, doubleSegment))
}