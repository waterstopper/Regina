import lexer.Parser
import lexer.PositionalException
//import lexer.SemanticAnalyzer
import node.Node
import utils.Utils.treeView
import java.io.File
import java.io.FileNotFoundException

fun main() {
//    val other = Parser("a[1](a,b,c)[2]").statements()
//    println(other.treeView())
    val statements = Parser(
        """
        if(cond)
        {
            doThis()
        }
        /** err
        treteefw
        **/
        else
        {
            doThat()
        }
        while(asew)
            doo()
    """
    ).statements()
//    println(Parser("A !is B").statements().treeView())
    // println(Parser("(v-a).b.c[1][2][3].s.r").statements().treeView())
//    val s = readFile("src/test/resources/testCode.redi")
//    SemanticAnalyzer.initializeSuperTypes()
//    SymbolTable.initializeObjects()
//    println(Evaluation.globalTable)
//    evaluate(s, "testCode.redi")
 //   val s = readFile("src/test/resources/testCode.redi")
   // SemanticAnalyzer.initializeSuperTypes()
    // println(Evaluation.globalTable)
}

//fun readFile(path: String = "", nodePath: Node = Node()): List<Node> {
//    val file = File(if (path == "") nodePath.value else if (path.contains(".")) path else "$path.redi")
//    val text: String
//    try {
//        text = file.readText()
//    } catch (_: FileNotFoundException) {
//        throw PositionalException("no import `${file.name}` found", nodePath)
//    }
//    val statements = Parser(text).statements()
//    // println(statements.treeView())
//   // return SemanticAnalyzer(parseFilePath(path), statements.map { it.toNode() }).analyze()
//}

fun parseFilePath(path: String): String = path.split("/").last()

// fun createJsonFromResources() {
//    File("src/main/resources/info.json").createNewFile()
//    val res = File("src/main/resources/info.json").bufferedWriter()
//    res.append('{')
//    File("src/main/resources").walk().forEach {
//        res.append("\"${it.name}\":")
//        if(it.isFile)
//            res.append("\"${it.readText()}\",")
//        else res.append("\"{}\",")
//        println(it.path)
//    }
//    res.append('}')
//    res.flush()
// }
