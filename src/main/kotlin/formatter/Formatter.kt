package formatter

import lexer.Parser
import lexer.SemanticAnalyzer
import node.Node
import node.operator.Operator
import java.io.File

// 1. add spaces in arrays and calls after commas
// 2. add space before {
class Formatter(filePath: String) {
    private val result = mutableListOf<StringBuilder>()

    private val nodes: List<Node>
    private var identLevel = 0
    private var lineAdditions = 0
    private var line = 0

    init {
        nodes = createAST(filePath).map { it }
        for (token in nodes)
            traverse(token)
        placeIdents()
        result.forEach { println(it) }
    }

    private fun createAST(filePath: String): List<Node> {
        val text =
            StringBuilder(File(filePath).readText()).split('\n').map { it.trimStart { c -> c == ' ' || c == '\t' } }

        text.forEach { result.add(StringBuilder(it)) }

        val statements = Parser(text.joinToString(separator = "\n")).statements()
        return SemanticAnalyzer(filePath, statements).analyze()
    }

    private fun traverse(node: Node) {
        if (node.children.size != 0)
            traverse(node.left)
        write(node)
        if (node.children.size > 1)
            for (child in node.children.subList(1, node.children.size))
                traverse(child)
    }

    private fun write(node: Node) {
        if (node.position.second != line) {
            lineAdditions = 0
            line = node.position.second
        }
        if (node is Operator && node.children.size > 1) {
            if (result[node.position.second][node.position.first - 1 + lineAdditions] != ' ') {
                result[node.position.second].insert(node.position.first + lineAdditions, " ")
                lineAdditions++
            }
            if (result[node.position.second][node.position.first + node.value.length + lineAdditions] != ' ') {
                result[node.position.second].insert(
                    node.position.first + node.value.length + lineAdditions,
                    " "
                )
                lineAdditions++
            }
        }
    }

    private fun placeIdents() {
        for (line in result) {
            if (line.contains('}'))
                identLevel -= line.count { it == '}' }
            for (ident in 1..identLevel)
                line.insert(0, "\t")
            identLevel += line.count { it == '{' }
        }
    }

//    private fun preprocess(stringBuilder: StringBuilder): List<String> {
//        var start = 0
//        do {
//            var firstIndex = stringBuilder.substring(start + 1).withIndex()
//                .find { (start + it.index == 0 || stringBuilder[start + it.index] != '\\') && stringBuilder[start + 1 + it.index] == '"' }?.index
//                ?: break
//
//            firstIndex += start
//            val formatted = preprocessChunk(stringBuilder.substring(start, firstIndex))
//            stringBuilder.replace(start, firstIndex, formatted)
//            firstIndex += formatted.length - (firstIndex - start)
//            start = stringBuilder.substring(firstIndex + 1).withIndex()
//                .find { stringBuilder[it.index + firstIndex] != '\\' && stringBuilder[it.index + firstIndex + 1] == '"' }?.index
//                ?: throw PositionalException("unterminated string")
//            start += firstIndex + 1
//
//        } while (firstIndex != null)
//
//        return stringBuilder.split("\n")
//            .map { it.trimStart { c -> c == ' ' } }
//
//    }
//
//    private fun preprocessChunk(chunk: String): String =
//        chunk.replace(" +".toRegex(), " ")
//            .replace("\t+".toRegex(), "")
//            .replace("\\s*\\{\\s*".toRegex(), " \\{\n")
//            .replace("\\s*}".toRegex(), "\n}")
//            .replace("\\{\\s*}".toRegex(), "{}")
}

fun main() {
    Formatter("src/test/resources/testCode.redi")
}
