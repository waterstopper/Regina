package lexer

import evaluation.Evaluation.globalTable
import evaluation.FunctionFactory
import properties.primitive.PString
import readFile
import table.SymbolTable
import token.*
import token.TokenFactory.changeInvocationOnSecondPositionInLink
import token.TokenFactory.createSpecificInvocation
import token.invocation.Call
import token.invocation.Invocation
import token.statement.Assignment
import token.variable.TokenNumber
import utils.Utils.subList

/**
 * Performs basic semantic analysis and creates symbol table for future evaluation
 */
class SemanticAnalyzer(private val fileName: String, private val tokens: List<Token>) {

    fun analyze(): List<Token> {
        // println(tokens.treeView())
        println("Analyzing: `$fileName`")
        createAssociations()
        changeIdentTokens()
        return tokens
    }

    private fun createAssociations() {
        globalTable.addFile(fileName)
        globalTable = globalTable.changeFile(fileName)
        for (token in tokens)
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
        for (token in tokens) {
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

    private fun changeTableForFunctionAnalysis(functionToken: Token, table: SymbolTable): SymbolTable {
        val args = functionToken.left.children.subList(1)
        for (arg in args)
            when (arg) {
                is Assignment -> table.addVariableOrNot(arg.left)
                is Identifier -> table.addVariableOrNot(arg)
                // else -> throw PositionalException("Expected assignment or identifier", arg)
            }
        return table
    }

    private fun changeInvocationType(token: Token, symbolTable: SymbolTable) {
        when (token) {
            is Declaration -> {
                for ((index, child) in token.children.withIndex()) {
                    if (child is Invocation) {
                        checkParamsOrArgs(child.children.subList(1), token.symbol != "fun")
                        if (token.symbol != "fun")
                            createSpecificInvocation(child, symbolTable, token, index)
                    }
                }
                if (token.symbol == "fun") {
                    for (child in token.children)
                        changeInvocationType(child, changeTableForFunctionAnalysis(token, symbolTable.changeScope()))
                    return
                }
            }
            is TokenNumber -> checkNumberBounds(token)
            is Link -> {
                if (token.left is Invocation) {
                    checkParamsOrArgs(token.left.children.subList(1), true)
                    createSpecificInvocation(token.left, symbolTable, token, 0)
                }
                // the only case in Link when Invocation might be a Constructor
                if (token.right is Invocation) {
                    checkParamsOrArgs(token.right.children.subList(1), true)
                    if (token.left is Identifier) {
                        // symbolTable.addVariableOrNot(token.left)
                        changeInvocationOnSecondPositionInLink(symbolTable, token)
                    } else token.children[1] = Call(token.right)
                }
                for ((index, child) in token.children.subList(2).withIndex())
                    if (child is Invocation) {
                        checkParamsOrArgs(child.children.subList(1), true)
                        token.children[index + 2] = Call(child)
                    }
                //            for (child in token.children)
                //                changeInvocationType(child, symbolTable)
            }
            else -> {
                if (token is Assignment) {
                    if (token.left !is Assignable)
                        throw PositionalException("Left operand is not assignable", token.left)
                    symbolTable.addVariableOrNot(token.left)
                }
                for ((index, child) in token.children.withIndex()) {
                    if (child is Invocation) {
                        checkParamsOrArgs(child.children.subList(1), token.symbol != "fun")
                        if (token.symbol != "fun")
                            createSpecificInvocation(child, symbolTable, token, index)
                    }
                }
            }
        }
        for (child in token.children)
            changeInvocationType(child, symbolTable)
    }

    private fun checkParamsOrArgs(params: List<Token>, areArgs: Boolean = false) {
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

    private fun checkNumberBounds(number: TokenNumber) {
        if (isInt(number)) {
            val parsedDouble = number.value.toDouble()
            if (parsedDouble < Int.MIN_VALUE || parsedDouble > Int.MAX_VALUE)
                throw PositionalException("Integer can be in range [${Int.MIN_VALUE}, ${Int.MAX_VALUE}]", number)
            number.number = number.value.toInt()
        } else number.number = number.value.toDouble()
    }

    private fun isInt(number: TokenNumber) = !number.value.contains(".")

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
