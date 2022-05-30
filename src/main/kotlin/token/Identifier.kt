package token


import lexer.Parser
import lexer.PositionalException
import properties.Type
import properties.primitive.Primitive
import table.SymbolTable
import token.statement.Assignment
import utils.Utils.toProperty
import utils.Utils.toVariable

open class Identifier(
    symbol: String, value: String, position: Pair<Int, Int>, bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((token: Token, parser: Parser, token2: Token) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?
) : Token(symbol, value, position, bindingPower, nud, led, std), Assignable, Linkable {

    override fun evaluate(symbolTable: SymbolTable): Any {
        val variable = symbolTable.getIdentifierOrNull(this)
            ?: return symbolTable.getTypeOrNull(this) ?: throw PositionalException(
                "Identifier `${value}` not found",
                this
            )
        if (variable is Primitive)
            return (variable).getPValue()
        return variable
    }

    override fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any) {
        if (parent != null && assignment.isProperty) {
            parent.setProperty(this.value, value.toProperty(assignment.right, parent))
            if (parent.getProperty(this) is Type) {
                (parent.getProperty(this) as Type).parent = parent
                (parent.getProperty(this) as Type).setProperty("parent", parent)
            }
        }
        symbolTable.addVariable(this.value, value.toVariable(this))
    }

    override fun getFirstUnassigned(parent: Type, symbolTable: SymbolTable): Assignment? {
        return parent.getAssignment(this)
    }

    override fun getPropertyName(): Token = this
}

