package token.statement

import Argumentable
import lexer.Parser
import properties.Type
import properties.Variable
import table.SymbolTable
import token.Assignable
import token.Token
import token.operator.Operator

class Assignment(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?,
    children: MutableList<Token> = mutableListOf()
) : Operator(symbol, value, position, bindingPower, nud, led, std), Argumentable {
    init {
        this.children.clear()
        this.children.addAll(children)
    }

    var parent: Type? = null
    val name: String get() = left.value

    override fun evaluate(symbolTable: SymbolTable): Any {
        val value = right.evaluate(symbolTable)
        assignLValue(left, value, symbolTable.getCurrentType(), symbolTable)
        return value
    }

    fun getAssignable(): Assignable = left as Assignable
    fun getFirstUnassigned(parent: Type): Assignment? =
        traverseUntil {
            if (it is Assignable)
                it.getFirstUnassigned(parent)
            else null
        } as Assignment?

    fun assign(parent: Type, symbolTable: SymbolTable) {
        (left as Assignable).assign(this, parent, symbolTable)
    }

    /**
     * To automatically replace assignments in type with constructor arguments
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Assignment)
            return false
        return left == other.left
    }

    override fun hashCode(): Int {
        return left.hashCode()
    }

//    override fun copy(): TokenAssignment = TokenAssignment(
//        symbol,
//        value,
//        position,
//        bindingPower,
//        nud,
//        led,
//        std,
//        children.map { it.copy() }.toMutableList()
//    )

    private fun assignLValue(token: Token, value: Any, parent: Variable?, symbolTable: SymbolTable) {
//        if (token is Identifier) {
//            symbolTable.addVariable(token.value, value.toVariable(token))
//            return
//        }
//        // all variables inside PArray property of type won't have such type as parent
//        if (token is Index) {
//            val (array, index) = token.getArrayAndIndex(symbolTable)
//            array.getPValue()[index] = value.toVariable(right)
//            return
//        }
//        var importTable = symbolTable
//        var current = token
//        while (current is Link) {
//            // left is type
//            if (importTable.getVariableOrNull(current.left.value) != null) {
//                val type = importTable.getVariableOrNull(current.left.value)
//                if (type is Type) {
//                    importTable = symbolTable.changeType(type)
//                    current = current.right
//                } else throw PositionalException("primitive does not contain properties", current.left)
//            } else if (importTable.getObjectOrNull(current.left) != null) {
//                importTable = symbolTable.changeType(importTable.getObjectOrNull(current.left)!!)
//                current = current.right
//            } else if (importTable.getImportOrNull(current.left.value) != null) {
//                importTable = symbolTable.changeFile(current.left.value)
//                current = current.right
//            }
//        }
//        if (current is Identifier)
//            importTable.addVariable(current.value, value.toVariable(current, parent))
//        else throw PositionalException("expected identifier or link", current)
    }
}