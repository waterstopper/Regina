package lexer

import FileSystem
import node.Link
import node.Node

object PathBuilder {
    fun getNodes(fileName: String): List<Node> {
        val code = FileSystem.read(fileName)
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
        if (candidates.isEmpty()) {

            throw PositionalException(
                "File not found ${
                    if (importName is Link) importName.children.joinToString(
                        separator = "/"
                    ) else importName.value
                }", importName
            )
        }
        if (candidates.size > 1)
            throw PositionalException(
                "Impossible to import ${
                    if (importName is Link) importName.children.joinToString(
                        separator = "/"
                    ) else importName.value
                }. Found ${candidates.size} files: " +
                        candidates.joinToString(separator = ", "), importName
            )
        return candidates.first()
    }

    private fun checkFiles(path: String, roots: List<String>): List<String> {
        val candidates = mutableListOf<String>()
        for (root in roots) {
            if (FileSystem.exists("$root$path"))
                candidates.add("$root$path")
        }
        return candidates
    }
}