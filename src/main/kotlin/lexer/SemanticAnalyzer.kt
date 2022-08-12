package lexer

import node.Identifier
import node.Link
import node.Node
import node.TokenFactory.changeInvocationOnSecondPositionInLink
import node.TokenFactory.createSpecificInvocation
import node.invocation.Call
import node.invocation.Constructor
import node.invocation.Invocation
import node.statement.Assignment
import properties.Function
import properties.Type
import table.FileTable
import table.SymbolTable
import utils.Utils.subList
import java.io.File

fun getNodes(fileName: String): List<Node> {
    val code = File("$fileName.redi").readText()
    return Parser(code).statements().map { it.toNode() }
}

fun initializeSuperTypes(superTypes: Map<Type, Node?>) {
    for ((type, node) in superTypes) {
        if (node == null)
            continue
        val superType = when (node) {
            is Identifier -> superTypes.filter { (t, _) ->
                t.fileTable == type.fileTable && t.name == node.value
            }
            is Link -> {
                if (node.children.size != 2)
                    throw PositionalException("Expected imported file name and type name", node)
                val importedFileTable = type.fileTable.getImportOrNull(node.left.value)
                    ?: throw  PositionalException("Import not found", node.left)
                superTypes.filter { (t, _) ->
                    t.fileTable == importedFileTable && t.name == node.right.value
                }
            }
            else -> throw PositionalException("Expected identifier or link", node)
        }
        if (superType.size != 1)
            throw PositionalException("One super type not found", node)
        type.supertype = superType.keys.first()
    }
}

fun analyzeSemantics(startingFileName: String, nodes: List<Node> = getNodes(startingFileName)): FileTable {
    val importGraphCreator = ImportGraphCreator(startingFileName, nodes)
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
            changeInvocationType(function.body, table)
        }
    }

    private fun analyzeType(type: Type, fileTable: FileTable) {
        val table = SymbolTable(fileTable = fileTable, variableTable = type, resolvingType = false)
        table.addVariable("this", type)
        table.addVariable("parent", type)
        for (assignment in type.assignments)
            table.addVariableOrNot(assignment.left)
        for (assignment in type.assignments)
            changeInvocationType(assignment, table.copy(), inProperty = true)

        for (function in type.functions) {
            val functionTable = addFunctionParametersToTable(function, table.copy())
            changeInvocationType(function.body, functionTable)
        }
    }

    private fun changeInvocationType(node: Node, symbolTable: SymbolTable, inProperty: Boolean = false) {
        for ((index, child) in node.children.withIndex()) {
            when (child) {
                is Assignment -> symbolTable.addVariableOrNot(child.left)
                is Invocation -> if (isInvocation(child))
                    createSpecificInvocation(child, symbolTable, node, index)
                is Link -> changeInvocationsInLink(child, symbolTable, inProperty)
            }

        }
        for (child in node.children)
            if (child !is Link)
                changeInvocationType(child, symbolTable)
            else {
                for(linkChild in child.children)
                    changeInvocationType(linkChild, symbolTable)
            }
    }

    private fun changeInvocationsInFunctionParameters(function: Function, symbolTable: SymbolTable) {

    }

    private fun changeInvocationsInLink(node: Link, symbolTable: SymbolTable, inProperty: Boolean = false) {
        if (isInvocation(node.left)) createSpecificInvocation(node.left, symbolTable, node, 0)
        if (isInvocation(node.right)) {
            // the only case in Link when Invocation might be a Constructor
            if (node.left is Identifier)
                changeInvocationOnSecondPositionInLink(symbolTable, node, inProperty)
            else {
                node.children[1] = Call(node.right)
                return changeInvocationType(node.left, symbolTable)
            }
        }
        for ((index, child) in node.children.subList(2).withIndex())
            if (isInvocation(child))
                node.children[index + 2] = Call(child)
    }

    private fun isInvocation(node: Node) = node is Invocation && node !is Call && node !is Constructor

    private fun addFunctionParametersToTable(function: Function, table: SymbolTable): SymbolTable {
        for (param in function.nonDefaultParams)
            table.addVariableOrNot(param)
        for (defaultParam in function.defaultParams) {
            changeInvocationType(defaultParam, table)
            table.addVariableOrNot(defaultParam.left)
        }
        return table
    }
}