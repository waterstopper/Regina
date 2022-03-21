package lexer

import structure.TypeManager
import java.util.*

class Interpreter(val declarations: List<Token>) {
    init {
        initializeTypes()
        initializeFunctions()
    }

    private fun initializeFunctions() {
        declarations.filter { it.value == "fun" }.forEach { FunctionEvaluation.addFunction(it) }
    }

    fun interpret(token: Token) {
        when (token.symbol) {
            "fun" -> interpretFunction(token)
            "class" -> interpretClass(token)
            else -> throw PositionalException("class or function can be top level declaration", token)
        }
    }

    private fun interpretClass(token: Token) {
        getTypeName(token.children[0])
        interpretClassBlock(token.children[1])
    }

    private fun interpretClassBlock(token: Token) {
        for (assignment in token.children)
            interpretAssignment(assignment)
    }

    private fun interpretAssignment(assignment: Token) {
        if (assignment.symbol != "=")
            throw PositionalException("class contains assignments only", assignment)
    }

    private fun interpretFunction(token: Token) {
        throw PositionalException("functions not yet implemented", token)
    }

    private fun getTypeName(token: Token) {
        if (token.symbol != ":")
            throw PositionalException("class should have superclass", token)
        token.children[0].children[0].symbol
    }

    private fun initializeTypes() {
        val stack = Stack<Token>()
        val classDeclarations = declarations.filter { it.symbol == "class" }.toMutableList()
        while (true) {
            if (stack.isEmpty()) {
                if (classDeclarations.isEmpty())
                    break
                stack.push(classDeclarations.first())
                classDeclarations.removeAt(0)
            }
            while (stack.isNotEmpty()) {
                val typeToken = stack.pop()
                val supertypeName = TypeManager.resolvedSupertype(typeToken)
                if (supertypeName == "")
                    TypeManager.addType(typeToken)
                else {
                    val foundSupertype = classDeclarations.find { TypeManager.getName(it) == supertypeName }
                        ?: throw Exception("no class with name $supertypeName")
                    stack.push(typeToken)
                    stack.push(foundSupertype)
                    classDeclarations.remove(foundSupertype)
                }
            }
        }
    }
}