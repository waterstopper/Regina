package lexer

import evaluation.FunctionFactory
import lexer.PathBuilder.getFullPath
import lexer.PathBuilder.getNodes
import node.*
import properties.Type
import table.FileTable

class ImportGraphCreator(
    val mainFileName: String,
    val startingNodes: List<Node>,
    val roots: List<String>
) {
    val visitedTables = mutableListOf<FileTable>()
    val supertypes = mutableMapOf<Type, Node?>()
    val imports = mutableMapOf<String, FileTable>()
    val importStack = mutableListOf<FileTable>()

    fun createGraph() {
        visitedTables.add(FileTable(mainFileName, imports.size + 1))
        imports[mainFileName] = visitedTables.last()
        addDeclarationsToFileTable(visitedTables.first(), startingNodes)

        while (importStack.isNotEmpty()) {
            val nextFileTable = importStack.removeLast()
            visitedTables.add(nextFileTable)
            addDeclarationsToFileTable(nextFileTable, getNodes(nextFileTable.filePath))
        }
    }

    fun addDeclarationsToFileTable(fileTable: FileTable, nodes: List<Node>) {
        for (node in nodes)
            when (node) {
                is ImportNode -> fileTable.addImport(
                    node,
                    getFileTableByName(getFullPath(node.fileName, roots, fileTable))
                )
                is FunctionNode -> fileTable.addFunction(FunctionFactory.createFunction(node, fileTable))
                is TypeNode -> {
                    val type = fileTable.addType(node)
                    supertypes[type] = node.superTypeNode
                }
                is ObjectNode -> fileTable.addObject(node)
                is Meta -> {
                    // skip sendMessage(Message("breakpoint", node.position.first))
                }
                else -> throw PositionalException(
                    "Only class, object or function can be top level declaration",
                    fileTable.filePath,
                    node
                )
            }
    }

    private fun getFileTableByName(name: String): FileTable {
        if (imports[name] == null) {
            imports[name] = FileTable(name, imports.size + 1)
            importStack.add(imports[name]!!)
        }
        return imports[name]!!
    }
}
