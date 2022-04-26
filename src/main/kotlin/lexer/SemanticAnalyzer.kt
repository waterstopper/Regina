package lexer

import Logger
import evaluation.Evaluation.globalTable
import evaluation.FunctionEvaluation
import readFile
import table.SymbolTable
import token.Token
import token.Declaration
import token.TokenFactory.Companion.createSpecificIdentifierFromInvocation
import token.Identifier
import token.link.Link

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
                "fun" -> globalTable.addFunction(FunctionEvaluation.createFunction(token))
                "class" -> {
                    if (declarations[fileName] == null)
                        declarations[fileName] = mutableListOf(token)
                    else declarations[fileName]!!.add(token)
                    globalTable.addType(token)
                }
                "object" -> globalTable.addObject(token)
                "import" -> {
                    if (globalTable.getImportOrNull(token.left.value) == null) {
                        val isNewFile = globalTable.addFile(token.left.value)
                        globalTable.addImport(token.left)
                        if (isNewFile) {
                            readFile(token.left.value)
                            globalTable = globalTable.changeFile(fileName)
                        }
                    } else Logger.addWarning(token.left, "Same import found above")
                }
                else -> throw PositionalException("class or function can be top level declaration", token)
            }

        // TODO implement
        // initializeSuperTypes()
    }

    private fun changeIdentTokens(fileName: String) {
        for (token in tokens) {
            var table = globalTable.copy()
            when (token.symbol) {
                "fun" -> table = table.changeScope()
                "object" -> table = table.changeType(globalTable.getObjectOrNull((token as Declaration).name)!!)
                "class" -> table = table.changeType(globalTable.getTypeOrNull((token as Declaration).name)!!)
            }
            changeTokenType(token, table, 0)
        }
    }

    private fun changeTokenType(token: Token, symbolTable: SymbolTable, linkLevel: Int) {
        for ((index, child) in token.children.withIndex()) {
            when (child.symbol) {
                // ignoring assignments like: a.b = ...
                "(ASSIGNMENT)" -> symbolTable.addVariableOrNot(child.left)
                "." -> {
//                    if (symbolTable.getVariableOrNull(child.left.value) != null) {
//                        val variable = symbolTable.getVariable(child.left)
//                        if(variable is Type)
//                    }
                }
                "(" -> {
                    if (token.value != "fun") {
                        token.children[index] =
                            createSpecificIdentifierFromInvocation(child, symbolTable, linkLevel, token)
                    }
                }
                // "[]" -> token.children[index] = TokenArray(child)
                //"[" -> token.children[index] = TokenIndexing(child)
            }
            changeTokenType(
                token.children[index],
                if (token.children[index].symbol == "fun") symbolTable.changeScope() else symbolTable,
                if (token.children[index] is Link) linkLevel + 1 else 0
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
                    checkParams(token.left.children.subList(1, token.left.children.size))
                }
            }
        }
        val intersections = classes.intersect(functions)
        if (intersections.isNotEmpty())
            throw PositionalException("`$fileName` contains functions and classes with same names: $intersections")
    }

    private fun checkParams(params: List<Token>) {
        for (param in params)
            if (param.symbol != "(IDENT)") throw PositionalException("expected identifier as function parameter", param)
    }

    /**
     * constructor params are assignments, because of the dynamic structure of type
     */
    private fun checkConstructorParams(params: List<Token>) {
        for (param in params)
            if (param.value != "=") throw PositionalException(
                "expected assignment as constructor parameter",
                param
            )
    }

    companion object {
        private var declarations: MutableMap<String, MutableList<Token>> = mutableMapOf()

        fun initializeSuperTypes() {
            val types = globalTable.getTypes()
            for ((fileName, tokenList) in declarations) {
                for (token in tokenList) {
                    if (token.right.symbol == "")
                        continue
                    val type = types[fileName]!!.find { it.name == token.left.value }!!
                    val superType = when (token.right) {
                        is Link -> {
                            val file = types[token.right.left.value] ?: throw PositionalException(
                                "File `${token.right.left.value}` not found",
                                token.right.left
                            )
                            file.find { it.name == token.right.right.value }
                                ?: throw PositionalException(
                                    "Superclass `${token.right.right.value}` not found in `${token.right.left.value}`",
                                    token.right.right
                                )
                        }
                        is Identifier -> {
                            types[fileName]!!.find { it.name == token.right.value } ?: throw PositionalException(
                                "Superclass `${token.right.value}` not found in `$fileName`",
                                token.right
                            )
                        }
                        else -> throw PositionalException("Expected identifier", token.right)
                    }
                    type.supertype = superType
                }
            }
        }
    }
}