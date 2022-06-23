package lexer

import Logger
import evaluation.Evaluation.globalTable
import evaluation.FunctionFactory
import readFile
import table.SymbolTable
import token.Declaration
import token.Identifier
import token.Link
import token.Token
import token.TokenFactory.changeInvocationOnSecondPositionInLink
import token.TokenFactory.createSpecificIdentifierFromInvocation
import token.invocation.Call
import token.invocation.Invocation
import token.statement.Assignment
import utils.Utils.subList

/**
 * Performs basic semantic analysis and creates symbol table for future evaluation
 */
class SemanticAnalyzer(private val fileName: String, private val tokens: List<Token>) {

    fun analyze(): List<Token> {
        // println(tokens.treeView())
        println("Analyzing: `$fileName`")
        createAssociations()
        // TODO do the same for all imports
        changeIdentTokens(fileName)
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
                    if (globalTable.getImportOrNull(token.left.value) == null) {
                        val isNewFile = globalTable.addFile(token.left.value)
                        globalTable.addImport(token.left, token.right)
                        if (isNewFile) {
                            readFile(token.left.value)
                            globalTable = globalTable.changeFile(fileName)
                        }
                    } else Logger.addWarning(token.left, "Same import found above")
                }
                else ->
                    throw PositionalException("Only class, object or function can be top level declaration", token)
            }
    }

    private fun changeIdentTokens(fileName: String) {
        for (token in tokens) {
            var table = globalTable.copy()
            when (token.symbol) {
                "fun" -> table = table.changeScope()
                "object" -> table = table.changeVariable(globalTable.getObjectOrNull((token as Declaration).name)!!)
                "class" -> table = table.changeVariable(globalTable.getTypeOrNull((token as Declaration).name)!!)
            }
            changeInvocationType(token, table)
        }
    }

    private fun changeInvocationType(token: Token, symbolTable: SymbolTable) {
        if (token is Link) {
            if (token.left is Invocation) {
                checkParamsOrArgs(token.left.children.subList(1), true)
                createSpecificIdentifierFromInvocation(token.left, symbolTable, token, 0)
            }
            if (token.right is Invocation) {
                checkParamsOrArgs(token.right.children.subList(1), true)
                if (token.left is Identifier) {
                    symbolTable.addVariableOrNot(token.left)
                    changeInvocationOnSecondPositionInLink(symbolTable, token)
                } else token.children[1] = Call(token.right)
            }
            for ((index, child) in token.children.subList(2).withIndex())
                if (child is Invocation) {
                    checkParamsOrArgs(child.children.subList(1), true)
                    token.children[index] = Call(child)
                }
//            for (child in token.children)
//                changeInvocationType(child, symbolTable)
        } else {
            if (token is Assignment)
                symbolTable.addVariableOrNot(token.left)
            for ((index, child) in token.children.withIndex()) {
                if (child is Invocation) {
                    checkParamsOrArgs(child.children.subList(1), token.symbol != "fun")
                    if (token.symbol != "fun")
                        createSpecificIdentifierFromInvocation(child, symbolTable, token, index)
                }

                // TODO there child might be alreay changed to other token, Invocation -> Call.
                // changeInvocationType(token.children[index], symbolTable)
            }
        }
        for (child in token.children)
            changeInvocationType(child, symbolTable)
    }

//    // TODO make it customized for invocation changing
//    private fun changeTokenType(
//        token: Token,
//        symbolTable: SymbolTable,
//        isInsideLink: Boolean,
//        inClass: Boolean = false
//    ) {
//        for ((index, child) in token.children.withIndex()) {
//            when (child.symbol) {
//                "(LINK)" -> {
//                    if (child.left is Identifier) {
//                    } else if (child.left is Invocation) {
//
//                    }
//                }
//                // ignoring assignments like: a.b = ...
//                "(ASSIGNMENT)" -> {
//                    symbolTable.addVariableOrNot(child.left)
//                    if (inClass)
//                        (child as Assignment).isProperty = true
//                }
//                "(" -> {
//                    if (token.value != "fun" && token.symbol != "(LINK)") {
//                        token.children[index] =
//                            createSpecificIdentifierFromInvocation(
//                                child,
//                                symbolTable,
//                                if (isInsideLink) index else 0,
//                                token
//                            )
//                    }
//                    // applicable for constructors in links too
//                    checkParamsOrArgs(
//                        token.children[index].children.subList(
//                            1, token.children[index].children.size
//                        ), areArgs = token.value != "fun"
//                    )
//                }
//                "{" -> {
//                    if (child.children.isEmpty())
//                        Logger.addWarning(child, "Empty block")
//                }
//            }
//            changeTokenType(
//                token.children[index],
//                if (token.children[index].symbol == "fun") symbolTable.changeScope() else symbolTable,
//                token.children[index] is Link,
//                if (token.value != "fun") inClass else false
//            )
//        }
//    }

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
