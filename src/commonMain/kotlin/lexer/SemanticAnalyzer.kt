package lexer

import Message
import lexer.PathBuilder.getNodes
import node.Identifier
import node.Link
import node.Meta
import node.Node
import node.TokenFactory.changeInvocationOnSecondPositionInLink
import node.TokenFactory.createSpecificInvocation
import node.invocation.Call
import node.invocation.Constructor
import node.invocation.Invocation
import node.statement.Assignment
import node.statement.Block
import node.statement.WordStatement
import properties.RFunction
import properties.Type
import sendMessage
import table.FileTable
import table.SymbolTable
import utils.Utils.subList

fun initializeSuperTypes(superTypes: Map<Type, Node?>) {
    for ((type, node) in superTypes) {
        if (node == null)
            continue
        var localSuperType: Type? = null
        val superType = when (node) {
            is Identifier -> superTypes.filter { (t, _) ->
                if (t.fileTable == type.fileTable && t.name == node.value)
                    localSuperType = t
                t.name == node.value
            }
            is Link -> {
                if (node.children.size != 2)
                    throw PositionalException(
                        "Expected imported file name and type name",
                        type.fileTable.filePath,
                        node
                    )
                val importedFileTable = type.fileTable.getImportOrNull(node.left.value)
                    ?: throw  PositionalException("Import not found", type.fileTable.filePath, node.left)
                superTypes.filter { (t, _) ->
                    t.fileTable == importedFileTable && t.name == node.right.value
                }
            }
            else -> throw PositionalException("Expected identifier or link", type.fileTable.filePath, node)
        }
        if (localSuperType != null)
            type.supertype = localSuperType
        else {
            if (superType.size != 1)
                throw PositionalException("One super type not found", type.fileTable.filePath, node)
            type.supertype = superType.keys.first()
        }
    }
}

fun analyzeSemantics(
    startingFileName: String,
    roots: List<String>,
    nodes: List<Node> = getNodes(startingFileName)
): FileTable {
    val importGraphCreator = ImportGraphCreator(startingFileName, nodes, roots)
    importGraphCreator.createGraph()
    initializeSuperTypes(importGraphCreator.supertypes)
    for (fileTable in importGraphCreator.visitedTables)
        Analyzer(fileTable)
    return importGraphCreator.visitedTables.first()
}

/**
 * Changes invocations to calls and constructors
 */
class Analyzer(fileTable: FileTable) {
    init {
        for (type in fileTable.getTypes().values + fileTable.getObjects()) {
            analyzeType(type, fileTable)
        }
        for (function in fileTable.getFunctions()) {
            val table = addFunctionParametersToTable(
                function, SymbolTable(fileTable = fileTable, resolvingType = false)
            )
            changeInvocationType(function.body, table, 0)
        }
    }

    private fun analyzeType(type: Type, fileTable: FileTable) {
        val table = SymbolTable(fileTable = fileTable, variableTable = type, resolvingType = false)
        table.addVariable("this", type)
        table.addVariable("parent", type)
        for (assignment in type.assignments)
            table.addVariableOrNot(assignment.left)
        for (assignment in type.assignments)
            changeInvocationType(assignment, table.copy(), 0, inProperty = true)

        for (function in type.functions) {
            val functionTable = addFunctionParametersToTable(function, table.copy())
            changeInvocationType(function.body, functionTable, 0)
        }
    }

    private fun changeInvocationType(node: Node, symbolTable: SymbolTable, cycles: Int, inProperty: Boolean = false) {
        for ((index, child) in node.children.withIndex()) {
            when (child) {
                is WordStatement -> {
                    if (cycles < 1 && (child.symbol == "break" || child.symbol == "continue"))
                        throw PositionalException(
                            "${child.symbol} out of cycle",
                            symbolTable.getFileTable().filePath,
                            child
                        )
                }
                is Assignment -> {
                    if (node !is Invocation && node !is Block && node !is Assignment)
                        throw PositionalException(
                            "unexpected assignment ${node.value}, ${node::class}",
                            symbolTable.getFileTable().filePath, child
                        )
                    symbolTable.addVariableOrNot(child.left)
                }
                is Invocation -> if (isInvocation(child))
                    createSpecificInvocation(child, symbolTable, node, index)
                is Link -> changeInvocationsInLink(child, symbolTable, inProperty)
                is Meta -> sendMessage(Message("breakpoint", child.position))
            }
        }
        for (child in node.children)
            if (child !is Link) {
                if (child is Block && (child.symbol == "foreach" || child.symbol == "while"))
                    changeInvocationType(child, symbolTable, cycles + 1)
                else changeInvocationType(child, symbolTable, cycles)
            } else {
                for (linkChild in child.children)
                    changeInvocationType(linkChild, symbolTable, cycles)
            }
    }

    private fun changeInvocationsInLink(node: Link, symbolTable: SymbolTable, inProperty: Boolean = false) {
        if (isInvocation(node.left)) createSpecificInvocation(node.left, symbolTable, node, 0)
        if (isInvocation(node.right)) {
            // the only case in Link when Invocation might be a Constructor
            if (node.left is Identifier)
                changeInvocationOnSecondPositionInLink(symbolTable, node, inProperty)
            else {
                node.children[1] = Call(node.right)
                return changeInvocationType(node.left, symbolTable, 0)
            }
        }
        for ((index, child) in node.children.subList(2).withIndex())
            if (isInvocation(child))
                node.children[index + 2] = Call(child)
    }

    private fun isInvocation(node: Node) = node is Invocation && node !is Call && node !is Constructor

    private fun addFunctionParametersToTable(function: RFunction, table: SymbolTable): SymbolTable {
        for (param in function.nonDefaultParams)
            table.addVariableOrNot(param)
        for (defaultParam in function.defaultParams) {
            changeInvocationType(defaultParam, table, 0)
            table.addVariableOrNot(defaultParam.left)
        }
        return table
    }
}