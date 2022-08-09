package lexer

import evaluation.Evaluation.globalTable
import evaluation.FunctionFactory
import properties.primitive.PString
import readFile
import table.FileTable
import table.SymbolTable
import node.*
import node.TokenFactory.changeInvocationOnSecondPositionInLink
import node.TokenFactory.createSpecificInvocation
import node.invocation.Call
import node.invocation.Invocation
import node.statement.Assignment
import node.variable.NodeNumber
import utils.Utils.subList

/**
 * Performs basic semantic analysis and creates symbol table for future evaluation
 */
class SemanticAnalyzer(private val fileName: String, private val nodes: List<Node>) {
    val files = mutableMapOf<String, FileTable>()

    fun analyze(): List<Node> {
        println("Analyzing: `$fileName`")
        createAssociations()
        changeIdentTokens()
        return nodes
    }

    private fun createImportGraph(fileTable: FileTable, nodes: List<Node>) {
        for (token in nodes) {
            if (token.symbol != "import")
                break
        }
    }

    private fun createAssociations() {
        globalTable.addFile(fileName)
        globalTable = globalTable.changeFile(fileName)
        for (token in nodes)
            when (token.symbol) {
                "fun" -> globalTable.addFunction(FunctionFactory.createFunction(token))
                "class" -> {
                    if (declarations[fileName] == null)
                        declarations[fileName] = mutableListOf(token as Declaration)
                    else declarations[fileName]!!.add(token as Declaration)
                    globalTable.addType(token)
                }
                "object" -> globalTable.addObject(token)
                "import" -> {
                    if (globalTable.getImportOrNullByFileName(token.left.value) == null) {
                        val isNewFile = globalTable.addFile(token.left.value)
                        globalTable.addImport(token.left, token.right)
                        if (isNewFile) {
                            readFile(token.left.value)
                            globalTable = globalTable.changeFile(fileName)
                        }
                    } else throw PositionalException("Same import found above", token.left)
                }
                else ->
                    throw PositionalException("Only class, object or function can be top level declaration", token)
            }
    }

    private fun changeIdentTokens() {
        for (token in nodes) {
            var table = globalTable.copy()
            when (token.symbol) {
                "fun" -> table = changeTableForFunctionAnalysis(token, table.changeScope())
                "object" -> table = table.changeVariable(globalTable.getObjectOrNull((token as Declaration).name)!!)
                "class" -> {
                    table = table.changeVariable(globalTable.getTypeOrNull((token as Declaration).name)!!)
                    table.addVariable("this", PString("", null))
                }
            }
            changeInvocationType(token, table)
        }
    }

    private fun changeTableForFunctionAnalysis(functionNode: Node, table: SymbolTable): SymbolTable {
        val args = functionNode.left.children.subList(1)
        for (arg in args)
            when (arg) {
                is Assignment -> table.addVariableOrNot(arg.left)
                is Identifier -> table.addVariableOrNot(arg)
                // else -> throw PositionalException("Expected assignment or identifier", arg)
            }
        return table
    }

    private fun changeInvocationType(node: Node, symbolTable: SymbolTable) {
        when (node) {
            is Declaration -> {
                for ((index, child) in node.children.withIndex()) {
                    if (child is Invocation) {
                        checkParamsOrArgs(child.children.subList(1), node.symbol != "fun")
                        if (node.symbol != "fun")
                            createSpecificInvocation(child, symbolTable, node, index)
                    }
                }
                if (node.symbol == "fun") {
                    for (child in node.children)
                        changeInvocationType(child, changeTableForFunctionAnalysis(node, symbolTable.changeScope()))
                    return
                }
            }
            is NodeNumber -> checkNumberBounds(node)
            is Link -> {
                if (node.left is Invocation) {
                    checkParamsOrArgs(node.left.children.subList(1), true)
                    createSpecificInvocation(node.left, symbolTable, node, 0)
                }
                // the only case in Link when Invocation might be a Constructor
                if (node.right is Invocation) {
                    checkParamsOrArgs(node.right.children.subList(1), true)
                    if (node.left is Identifier) {
                        // symbolTable.addVariableOrNot(token.left)
                        changeInvocationOnSecondPositionInLink(symbolTable, node)
                    } else node.children[1] = Call(node.right)
                }
                for ((index, child) in node.children.subList(2).withIndex())
                    if (child is Invocation) {
                        checkParamsOrArgs(child.children.subList(1), true)
                        node.children[index + 2] = Call(child)
                    }
                //            for (child in token.children)
                //                changeInvocationType(child, symbolTable)
            }
            else -> {
                if (node is Assignment) {
                    if (node.left !is Assignable)
                        throw PositionalException("Left operand is not assignable", node.left)
                    symbolTable.addVariableOrNot(node.left)
                }
                for ((index, child) in node.children.withIndex()) {
                    if (child is Invocation) {
                        checkParamsOrArgs(child.children.subList(1), node.symbol != "fun")
                        if (node.symbol != "fun")
                            createSpecificInvocation(child, symbolTable, node, index)
                    }
                }
            }
        }
        for (child in node.children)
            changeInvocationType(child, symbolTable)
    }

    private fun checkParamsOrArgs(params: List<Node>, areArgs: Boolean = false) {
        var wasAssignment = false
        for (param in params)
            when (param) {
                is Assignment -> wasAssignment = true
                is Identifier -> if (wasAssignment)
                    throw PositionalException("Default params should be after other", param)
                else -> if (!areArgs) throw PositionalException("Expected identifier as function parameter", param)
                else if (wasAssignment) throw PositionalException("Named args should be after other", param)
            }
    }

    private fun checkNumberBounds(number: NodeNumber) {
        if (isInt(number)) {
            val parsedDouble = number.value.toDouble()
            if (parsedDouble < Int.MIN_VALUE || parsedDouble > Int.MAX_VALUE)
                throw PositionalException("Integer can be in range [${Int.MIN_VALUE}, ${Int.MAX_VALUE}]", number)
            number.number = number.value.toInt()
        } else number.number = number.value.toDouble()
    }

    private fun isInt(number: NodeNumber) = !number.value.contains(".")

    companion object {
        private var declarations: MutableMap<String, MutableList<Declaration>> = mutableMapOf()

        fun initializeSuperTypes() {
            val initialFileTable = globalTable.getFileTable()
            for ((fileName, tokenList) in declarations) {
                val currentTable = globalTable.changeFile(fileName).getFileTable()
                for (token in tokenList) {
                    if (token.supertype.symbol == "")
                        continue
                    val supertypeTable = if (token.supertype is Link)
                        globalTable.getImport(token.supertype.left)
                    else currentTable
                    val supertype =
                        if (token.supertype is Link) supertypeTable.getType(token.supertype.right)
                        else supertypeTable.getType(token.supertype)
                    currentTable.getUncopiedType(token.name).supertype = supertype
                }
            }
            globalTable.changeFile(initialFileTable)
        }

        fun clearAnalyzer() {
            declarations = mutableMapOf()
        }
    }
}
