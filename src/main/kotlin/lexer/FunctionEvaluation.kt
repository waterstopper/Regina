package lexer

import lexer.ValueEvaluation.evaluateIndex
import lexer.ValueEvaluation.evaluateValue
import structure.*
import structure.Function

object FunctionEvaluation {
    val functions = mutableListOf<Function>()

    fun addFunction(token: Token) {
        val func = Function(
            token.children[0].children[0].value,
            null,
            token.children[0].children.subList(1, token.children[0].children.size).map { it.value },
            token.children[1]
        )
        functions.add(func)
    }

    fun evaluateInvokation(token: Token, symbolTable: SymbolTable) {
        if (symbolTable.findFunction(token.children[0].value) != null)
            return evaluateFunction(
                token,
                symbolTable.findFunction(token.children[0].value)
                    ?: throw PositionalException(
                        "no function with name ${token.children[0].value} in scope",
                        token
                    ),
                token.children.subList(1, token.children.size), symbolTable
            )
        else {

        }
    }

    fun evaluateFunction(token: Token, function: Function, args: List<Token>, symbolTable: SymbolTable) {
        val localSymbolTable = args.mapIndexed { index, it ->
            ValueEvaluation.evaluateValue(it, symbolTable).toProperty(function.args[index])
        }
        evaluateBlock(token, symbolTable)
    }

    fun evaluateWhile(token: Token, symbolTable: SymbolTable) {}

    fun evaluateIf(token: Token, symbolTable: SymbolTable) {}

    fun evaluateBlock(token: Token, symbolTable: SymbolTable) {
        val localSymbolTable = symbolTable.copy()
        for (stmt in token.children) {
            when (stmt.value) {
                "while" -> evaluateWhile(stmt, localSymbolTable)
                "if" -> evaluateIf(stmt, localSymbolTable)
                "=" -> (evaluateAssignment(stmt, localSymbolTable))
                "(" -> evaluateInvokation(stmt, localSymbolTable)
                else -> throw PositionalException("expected assignment, invokation or block", stmt)
            }
        }
    }

    fun evaluateAssignment(token: Token, symbolTable: SymbolTable) {
        when (token.children[0].symbol) {
            "[" -> {
                val element = evaluateIndex(token, symbolTable)
                if (element is Property) {
                    element.value = evaluateValue(token.children[1], symbolTable)
                } else throw PositionalException("expected variable for assignment", token.children[0])
            }
            "." -> {
                val link = ValueEvaluation.evaluateLink(token.children[0], symbolTable)
                if (link is Property)
                    link.value = ValueEvaluation.evaluateValue(token.children[1], symbolTable)
                else throw PositionalException("class reassignment is prohibited", token)
            }
            "(IDENT)" -> {
                val symbol = symbolTable.findIndentfier(token.children[0].value)
                if (symbol != null) {
                    if (symbol is Property)
                        symbol.value = ValueEvaluation.evaluateValue(token.children[1], symbolTable)
                    else throw PositionalException("class reassignment is prohibited", token)
                } else symbolTable.identifiers.add(
                    Property(
                        token.children[0].value,
                        ValueEvaluation.evaluateValue(token.children[1], symbolTable), null
                    )
                )
            }
            else -> throw PositionalException("identifier or reference expected", token)
        }
    }

    fun Any.toProperty(name: String, parent: Type? = null): Node {
        if (this is Type)
            return this
        return Property(name, this, parent)
    }
}