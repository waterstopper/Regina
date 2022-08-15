package lexer

import node.Link
import node.Node
import java.io.File

object PathBuilder {
    fun getNodes(fileName: String): List<Node> {
        val code = File(fileName).readText()
        return Parser(code).statements().map { it.toNode() }
    }

    fun getFullPath(importName: Node, roots: List<String>): String {
        val path = when (importName) {
            is Link -> {
                val res = StringBuilder()
                for (child in importName.children)
                    res.append("${child.value}/")
                res.deleteAt(res.lastIndex).append(".rgn").toString()
            }
            else -> importName.value + ".rgn" // an Identifier, checked in [RegistryFactory]
        }
        val candidates = checkFiles(path, roots)
        if (candidates.isEmpty())
            throw PositionalException("File not found", importName)
        if (candidates.size > 1)
            throw PositionalException(
                "Impossible to import $importName. Found ${candidates.size} files: " +
                        candidates.joinToString(separator = ", "), importName
            )
        return candidates.first()
    }

    private fun checkFiles(path: String, roots: List<String>): List<String> {
        val candidates = mutableListOf<String>()
        for (root in roots) {
            val file = File("$root$path")
            if (file.exists())
                candidates.add("$root$path")
        }
        return candidates
    }
}