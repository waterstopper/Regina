package properties

import evaluation.Evaluation.globalTable
import evaluation.FunctionEvaluation
import evaluation.FunctionEvaluation.toVariable
import lexer.Token
import evaluation.ValueEvaluation
import evaluation.ValueEvaluation.evaluateIndex
import evaluation.ValueEvaluation.evaluateValue
import lexer.PositionalException
import properties.primitive.Primitive
import structure.SymbolTable

class Assignment(val token: Token) {
    val name: String get() = token.left.value

    // should be val, but no way to do it
    lateinit var parent: Type

    fun copy() = Assignment(token.copy())

    fun canEvaluate(): Boolean = token.right.find("(IDENT)") == null
            && token.right.find("parent") == null

    fun getLink() = token.find(".")!!

    fun replaceFirst(value: Any) {
        // TODO think whether first encountered link is the right one in all cases
        // TODO it will hinder resolving same type next time. Or we can have copy of that token type
        token.find(".")!!.value = value.toString()
        token.children.clear()
    }


    fun evaluate(): Property {
        val value = evaluateValue(
            token.right,
            globalTable//SymbolTable((TypeManager.types) as (MutableMap<String, Variable>), FunctionEvaluation.functions)
        )
        if (value is String)
            return globalTable.getType(value) ?: Primitive.createPrimitive(value, parent)
        // number or array
        return Primitive.createPrimitive(value, parent)
    }

    override fun toString() = "$name=-"

    companion object {
        fun evaluateAssignment(token: Token, symbolTable: SymbolTable) {
            when (token.left.symbol) {
                "[" -> {
                    val element = evaluateIndex(token.left, symbolTable)
                    if (element is Primitive) {
                        element.value = evaluateValue(token.right, symbolTable)
                    } else throw PositionalException("expected variable for assignment", token.left)
                }
                "." -> {
                    val (name, table) = Type.getPropertyNameAndTable(
                        token.left,
                        symbolTable
                    )
                    table.addVariable(name, evaluateValue(token.right, symbolTable).toVariable(token.right))
//                    if (property is Primitive && newValue !is Type)
//                        property.value = newValue
//                    else {
//
//                    }throw PositionalException("class reassignment is prohibited", token)
                }
                "(IDENT)" -> {
                    symbolTable.addVariable(
                        token.left.value,
                        evaluateValue(token.right, symbolTable).toVariable(token.right)
                    )
//                   val symbol = symbolTable.findIndentfier(token.left.value)
//                    if (symbol != null) {
//                        if (symbol is Primitive)
//                            symbol.value = evaluateValue(token.right, symbolTable)
//                        else symbolTable.variables[token.left.value] =
//                            evaluateValue(token.right, symbolTable).toVariable(token.right)
//                    } else {
//                        val value = evaluateValue(token.right, symbolTable)
//                        if (value is Type) {
//                            // value.name = token.left.value
//                            symbolTable.variables[token.left.value] = value
//                        } else symbolTable.variables[token.left.value] =
//                            Primitive.createPrimitive(value, null)
//                    }
                }
                else -> throw PositionalException("identifier, reference or index expected", token)
            }
        }
    }
}
