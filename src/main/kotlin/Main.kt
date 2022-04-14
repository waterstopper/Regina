import token.Token
import lexer.Parser
import lexer.PositionalException
import lexer.SemanticAnalyzer
import java.io.File
import java.io.FileNotFoundException

fun main() {
    val statements = readFile("constants")
    println(statements.treeView())
    // (statements, "constants")
}

fun readFile(path: String = "", tokenPath: Token = Token()): List<Token> {
    val file = File(if (path == "") tokenPath.value else path)
    val text: String
    try {
        text = file.readText()
    } catch (e: FileNotFoundException) {
        throw PositionalException("no import ${file.name} found", tokenPath)
    }

    val statements = Parser(text).statements()
    return SemanticAnalyzer(path, statements).analyze()
}

fun List<Token>.treeView(): String {
    val res = StringBuilder()
    for (t in this) {
        res.append(t.toTreeString(0))
        res.append('\n')
    }
    return res.toString()
}

//private fun createDefs() {
//    val root = OldContainer("Root", null, mutableMapOf())
//    //root.children.add(Property("type", root, "Line"))
//    root.declarations["type"] = Formula("@Line")
//    root.declarations["child"] = Formula("@Segment")
//    //root.declarations["child2"] = deprecated.Formula("@Segment")
//    root.declarations["iter"] = Formula("0")
//    root.declarations["x"] = Formula("20")
//    root.declarations["y"] = Formula("0")
//    root.declarations["x2"] = Formula("20")
//    root.declarations["y2"] = Formula("0")
//
//    //root.declarations["rotation"] = deprecated.Formula()
//
//    val segment = OldContainer("Segment", root, mutableMapOf())
//    //segment.children.add(Property("type", segment, "Line"))
//    segment.declarations["type"] = Formula("@Line")
//    segment.declarations["next"] = Formula("iter < 10 ? ({@randNum,0,1}>0.3 ? @Segment : @DoubleSegment) : @Nothing")
//    segment.declarations["iter"] = Formula("parent.iter + 1")
//    segment.declarations["x"] = Formula("parent.x2")
//    segment.declarations["y"] = Formula("parent.y2")
//    segment.declarations["x2"] = Formula("-{@sin,angle} * 10 + x")
//    segment.declarations["y2"] = Formula("{@cos,angle} * 10 + y")
//    segment.declarations["angle"] = Formula("{@randNum,-0.7,0.7}")
//
//    val doubleSegment = OldContainer("DoubleSegment", null, mutableMapOf())
//    doubleSegment.declarations["child"] = Formula("@Segment")
//    doubleSegment.declarations["child2"] = Formula("@Segment")
//    doubleSegment.declarations["x"] = Formula("parent.x2")
//    doubleSegment.declarations["y"] = Formula("parent.y2")
//    doubleSegment.declarations["x2"] = Formula("parent.x2")
//    doubleSegment.declarations["y2"] = Formula("parent.y2")
//    doubleSegment.declarations["iter"] = Formula("parent.iter + 1")
//
//    //segment.declarations["rotation"] = deprecated.Formula("{@randNum,-10,10}")
//
//    val nothing = OldContainer("Nothing", root, mutableMapOf())
//
//    TreeBuilder.definitions.addAll(mutableListOf(root, segment, nothing, doubleSegment))
//}