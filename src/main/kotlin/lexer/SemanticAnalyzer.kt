package lexer

import SymbolTable
import SymbolTable.Type.Companion.initializeSuperTypes
import evaluation.Evaluation
import evaluation.FunctionEvaluation
import readFile
import token.Token
import token.TokenFactory.Companion.createSpecificIdentifierFromInvocation
import java.util.*

class SemanticAnalyzer(private val fileName: String, private val tokens: List<Token>) {

    fun analyze(): List<Token> {
        createAssociations()
        changeIdentTokens()
        return tokens
    }

    private fun createAssociations() {
        Evaluation.globalTable.currentFile = fileName

        val queue = ArrayDeque<Pair<Token, String>>()
        queue.addAll(tokens.map { Pair(it, fileName) })
        while (queue.isNotEmpty()) {
            val (token, currentFileName) = queue.pop()
            when (token.symbol) {
                "fun" -> Evaluation.globalTable.addFunction(FunctionEvaluation.createFunction(token), currentFileName)
                "class" -> {
                    Evaluation.globalTable.addType(token, currentFileName)
                }
                "object" -> {
                    Evaluation.globalTable.addObject(token, currentFileName)
                }
                "import" -> {
                    if (!Evaluation.globalTable.getImportOrNull(currentFileName, token.left.value)) {
                        Evaluation.globalTable.addImport(currentFileName, token.left.value)
                        queue.addAll(readFile(tokenPath = token.left).map { Pair(it, token.left.value) })
                    }
                    /**
                     * TODO: warn about this code (doubling imports):
                     * import abc
                     * import abc
                     * ...
                     */
                }
                else -> throw PositionalException("class or function can be top level declaration", token)
            }
        }
        initializeSuperTypes()
    }

    private fun changeIdentTokens() {
        for (token in tokens)
            changeTokenType(token)
    }

    private fun changeTokenType(token: Token) {
        for ((index, child) in token.children.withIndex()) {
            when (child.symbol) {
                "(" -> {
                    if (token.value != "fun")
                        token.children[index] = createSpecificIdentifierFromInvocation(child, classes, functions)
                }
                // "[]" -> token.children[index] = TokenArray(child)
                //"[" -> token.children[index] = TokenIndexing(child)
            }
            changeTokenType(token.children[index])
        }
    }

    private fun checkIntersections(tokens: List<Token>) {
        val classes = mutableSetOf<String>()
        val functions = SymbolTable.getEmbeddedNames()
        for (token in tokens) {
            when (token.symbol) {
                "class" -> {
                    classes.add(getSupertype((getExport(token.left))).value)
                }
                "fun" -> {
                    functions.add(token.left.left.value)
                    checkParams(token.left.children.subList(1, token.left.children.size))
                }
            }
        }
        val intersections = classes.intersect(functions)
        if (intersections.isNotEmpty())
            throw PositionalException("$fileName contains functions and classes with same names: $intersections")
    }

    private fun getExport(token: Token): Token {
        return if (token.value == "export")
            token.left
        else token
    }

    private fun getSupertype(token: Token): Token {
        return if (token.value == ":")
            token.left
        else token
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
}