package lexer

import token.Token
import token.TokenFactory.Companion.createSpecificIdentifierFromInvocation
import SymbolTable
import evaluation.Evaluation
import evaluation.FunctionEvaluation

class SemanticAnalyzer(private val fileName: String, private val tokens: List<Token>) {
    private val classes = mutableSetOf<String>()
    private val functions = SymbolTable.getEmbeddedNames()

    fun analyze(): List<Token> {
        createAssociations()
        changeIdentTokens()
        return tokens
    }

    private fun createAssociations() {
        for (token in tokens)
            when (token.value) {
                "class" -> {
                    Evaluation.globalTable.addType(token, fileName)
                    classes.add(getSupertype(getExport(token.left)).value)
                }
                "fun" -> {
                    Evaluation.globalTable.addFunction(FunctionEvaluation.createFunction(token), fileName)
                    functions.add(token.left.left.value)
                }
                else -> {
                }
            }
        val intersections = classes.intersect(functions)
        if (intersections.isNotEmpty())
            throw PositionalException("$fileName contains functions and classes with same names: $intersections")
    }

    private fun changeIdentTokens() {
        for (token in tokens)
            changeIdentToken(token)
    }

    private fun changeIdentToken(token: Token) {
        for ((index, child) in token.children.withIndex()) {
            if (child.symbol == "(" && token.value != "fun")
                token.children[index] = createSpecificIdentifierFromInvocation(child, classes, functions)
            changeIdentToken(child)
        }
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
}