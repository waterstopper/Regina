package token


import lexer.Parser
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
) : Token(symbol, value, position, bindingPower, nud, led, std), Assignable {

    override fun evaluate(symbolTable: SymbolTable): Any {
        val variable = symbolTable.getIdentifier(this)
        if (variable is Primitive)
            return (variable).getPValue()
        return variable
    }

    override fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any?) {
        if (parent != null) {
            parent.removeAssignment(assignment)
            parent.setProperty(this, assignment.right.evaluate(symbolTable).toProperty(assignment.right, parent))
        } else symbolTable.addVariable(this.value, value!!.toVariable(this))
    }

    override fun getFirstUnassigned(parent: Type): Assignment? {
//        println(this)
//        println(parent.getAssignment(this)?.toTreeString())
        return parent.getAssignment(this)
    }

    override fun getPropertyName(): Token = this
}

