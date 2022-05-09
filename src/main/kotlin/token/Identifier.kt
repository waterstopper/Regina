package token


import lexer.Parser
import properties.Type
import properties.Variable
import properties.primitive.Primitive
import table.SymbolTable
import token.statement.Assignment
import utils.Utils.toProperty

open class Identifier(
    symbol: String, value: String, position: Pair<Int, Int>, bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((token: Token, parser: Parser, token2: Token) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?
) : Token(symbol, value, position, bindingPower, nud, led, std), Assignable {
    var variable: Variable? = null

    override fun evaluate(symbolTable: SymbolTable): Any {
        if (variable == null)
            variable = symbolTable.getIdentifier(this)
        if (variable is Primitive)
            return (variable as Primitive).getPValue()
        return variable as Variable
    }

    override fun assign(assignment: Assignment, parent: Type, symbolTable: SymbolTable) {
        parent.removeAssignment(assignment)
        parent.setProperty(this, assignment.right.evaluate(symbolTable).toProperty(assignment.right, parent))
    }

    override fun getFirstUnassigned(parent: Type): Assignment? {
        TODO("Not yet implemented")
    }

    override fun getPropertyName(): Token = this
}

