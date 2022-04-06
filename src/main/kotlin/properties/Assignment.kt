package properties

import evaluation.FunctionEvaluation
import evaluation.TypeManager
import lexer.Token
import evaluation.ValueEvaluation
import lexer.PositionalException
import structure.SymbolTable

class Assignment(val token: Token) {
    val name: String get() = token.children[0].value

    // should be val, but no way to do it
    lateinit var parent: Type

    fun copy() = Assignment(token.copy())

    fun canEvaluate(): Boolean = token.children[1].find("(IDENT)") == null
            && token.children[1].find("parent") == null

    fun getLink() = token.find(".")!!

    fun replaceFirst(value: Any) {
        // TODO think whether first encountered link is the right one in all cases
        // TODO it will hinder resolving same type next time. Or we can have copy of that token type
        token.find(".")!!.value = value.toString()
        token.children.clear()
    }


    fun evaluate(): Property {
        val value = ValueEvaluation.evaluateValue(
            token.children[1],
            SymbolTable((TypeManager.types) as (MutableMap<String, Variable>), FunctionEvaluation.functions)
        )
        if (value is String)
            return TypeManager.find(value) ?: Primitive(name, value, parent)
        // number or array
        return Primitive(name, value, parent)
    }

    override fun toString() = "$name=-"

    companion object {
        fun evaluateAssignment(token: Token, symbolTable: SymbolTable) {
            when (token.children[0].symbol) {
                "[" -> {
                    val element = ValueEvaluation.evaluateIndex(token, symbolTable)
                    if (element is Primitive) {
                        element.value = ValueEvaluation.evaluateValue(token.children[1], symbolTable)
                    } else throw PositionalException("expected variable for assignment", token.children[0])
                }
                "." -> {
                    val link = ValueEvaluation.evaluateLink(token.children[0], symbolTable)
                    if (link is Primitive)
                        link.value = ValueEvaluation.evaluateValue(token.children[1], symbolTable)
                    else throw PositionalException("class reassignment is prohibited", token)
                }
                "(IDENT)" -> {
                    val symbol = symbolTable.findIndentfier(token.children[0].value)
                    if (symbol != null) {
                        if (symbol is Primitive)
                            symbol.value = ValueEvaluation.evaluateValue(token.children[1], symbolTable)
                        else throw PositionalException("class reassignment is prohibited", token)
                    } else {
                        val value = ValueEvaluation.evaluateValue(token.children[1], symbolTable)
                        if (value is Type) {
                            // value.name = token.children[0].value
                            symbolTable.variables[token.children[0].value] = value
                        } else symbolTable.variables[token.children[0].value] =
                            Primitive(token.children[0].value, value, null)
                    }
                }
                else -> throw PositionalException("identifier, reference or index expected", token)
            }
        }
    }
}
