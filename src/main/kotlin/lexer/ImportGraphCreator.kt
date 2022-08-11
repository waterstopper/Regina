package lexer

import evaluation.FunctionFactory
import node.*
import properties.Type
import table.FileTable

class ImportGraphCreator(mainFileName: String, startingNodes: List<Node>) {
    val visitedTables = mutableListOf<FileTable>()
    val supertypes = mutableMapOf<Type, Node?>()
    private val imports = mutableMapOf<String, FileTable>()
    private val importStack = mutableListOf<FileTable>()

    init {
        visitedTables.add(FileTable(mainFileName))
        addDeclarationsToFileTable(visitedTables.first(), startingNodes)
        while (importStack.isNotEmpty()) {
            val nextFileTable = importStack.removeLast()
            visitedTables.add(nextFileTable)
            addDeclarationsToFileTable(nextFileTable, getNodes(nextFileTable.fileName))
        }
    }

    private fun addDeclarationsToFileTable(fileTable: FileTable, nodes: List<Node>) {
        for (node in nodes)
            when (node) {
                is ImportNode -> fileTable.addImport(node, getFileTableByName(node.fileName))
                is FunctionNode -> fileTable.addFunction(FunctionFactory.createFunction(node))
                is TypeNode -> {
                    val type = fileTable.addType(node)
                    supertypes[type] = node.superTypeNode
                }
                is ObjectNode -> fileTable.addObject(node)
                else -> throw PositionalException("Only class, object or function can be top level declaration", node)
            }
    }

    private fun getFileTableByName(name: String): FileTable {
        if (imports[name] == null) {
            imports[name] = FileTable(name)
            importStack.add(imports[name]!!)
        }
        return imports[name]!!
    }

    fun clearAll() {
        visitedTables.clear()
        supertypes.clear()
        imports.clear()
        importStack.clear()
    }
}