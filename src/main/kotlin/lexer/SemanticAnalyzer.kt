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
import token.TokenFactory.Companion.createSpecificIdentifierFromInvocation
import token.statement.Assignment

/**
 * Performs basic semantic analysis and creates symbol table for future evaluation
 */
class SemanticAnalyzer(private val fileName: String, private val tokens: List<Token>) {

    fun analyze(): List<Token> {
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
                else -> throw PositionalException("class or function can be top level declaration", token)
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
            changeTokenType(token, table, 0, table.getCurrentType() != null)
        }
    }

    private fun changeTokenType(token: Token, symbolTable: SymbolTable, linkLevel: Int, inClass: Boolean = false) {
        for ((index, child) in token.children.withIndex()) {
            when (child.symbol) {
                // ignoring assignments like: a.b = ...
                "(ASSIGNMENT)" -> {
                    symbolTable.addVariableOrNot(child.left)
                    if (inClass)
                        (child as Assignment).isProperty = true
                }
                "(LINK)" -> {
//                    if (symbolTable.getVariableOrNull(child.left.value) != null) {
//                        val variable = symbolTable.getVariable(child.left)
//                        if(variable is Type)
//                    }
                }
                "(" -> {
                    if (token.value != "fun" && token.symbol != "(LINK)") {
                        token.children[index] =
                            createSpecificIdentifierFromInvocation(child, symbolTable, linkLevel, token)
                        checkParamsOrArgs(
                            token.children[index].children.subList(
                                1, token.children[index].children.size
                            ), areArgs = true
                        )
                    }
                }
                "{" -> {
                    if (child.children.isEmpty())
                        Logger.addWarning(child, "Empty block")
                }
                // "[]" -> token.children[index] = TokenArray(child)
                //"[" -> token.children[index] = TokenIndexing(child)
            }
            changeTokenType(
                token.children[index],
                if (token.children[index].symbol == "fun") symbolTable.changeScope() else symbolTable,
                if (token.children[index] is Link) linkLevel + 1 else 0,
                if (token.value != "fun") inClass else false
            )
        }
    }

    private fun checkIntersections(tokens: List<Token>) {
        val classes = mutableSetOf<String>()
        val functions = SymbolTable.getEmbeddedNames()
        for (token in tokens) {
            when (token.symbol) {
                "class" -> {
                    classes.add((token as Declaration).name.value)
                }
                "fun" -> {
                    val added = functions.add(token.left.left.value)
                    if (!added)
                        throw  PositionalException(
                            if (SymbolTable.getEmbeddedNames()
                                    .contains(functions.last())
                            ) "reserved function name" else "same function name within one file", token.left.left
                        )
                    checkParamsOrArgs(token.left.children.subList(1, token.left.children.size))
                }
            }
        }
        val intersections = classes.intersect(functions)
        if (intersections.isNotEmpty())
            throw PositionalException("`$fileName` contains functions and classes with same names: $intersections")
    }

    private fun checkParamsOrArgs(params: List<Token>, areArgs: Boolean = false) {
        var wasAssignment = false
        for (param in params)
            when (param) {
                is Assignment -> wasAssignment = true
                is Identifier -> if (wasAssignment)
                    throw PositionalException("Default values should be after other", param)
                else -> if (!areArgs) throw PositionalException("expected identifier as function parameter", param)
                else if (wasAssignment) throw  PositionalException("Named args should be after other", param)
            }
    }

    /**
     * Constructor params are assignments, because of the dynamic structure of type
     */
    private fun checkConstructorParams(params: List<Token>) {
        for (param in params)
            if (param.value != "=") throw PositionalException(
                "expected assignment as constructor parameter",
                param
            )
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
    }
}