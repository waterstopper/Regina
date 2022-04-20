package token

import lexer.Parser
import lexer.PositionalException
import properties.Type
import table.SymbolTable
import token.invocation.TokenCall
import token.invocation.TokenInvocation

class TokenLink(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?
) : Token(symbol, value, position, bindingPower, nud, led, std) {

    /**
    Left is either invocation or identifier.
    if invocation, then result should be an instance.
    if identifier, then check what it is in this order:
    1 is variable in this scope
    2 is object in this file
    3 is package name

    Right is either ident (check for object if left is package)
    or invocation or (LINK)
     */
    override fun evaluate(symbolTable: SymbolTable): Any {
        if (left is TokenInvocation) {
            val instance = left.evaluate(symbolTable)
            if (instance is Type)
                return right.evaluate(symbolTable.changeType(instance))
            throw PositionalException(
                "expected class instance${if (left is TokenCall) " as return value" else ""}",
                left
            )
        }
        if (symbolTable.getVariableOrNull(left.value) != null) {
            val variable = symbolTable.getVariableOrNull(left.value)
            if (variable is Type)
                return right.evaluate(symbolTable.changeType(variable))
            throw PositionalException("expected class instance", left)
        }
        if (symbolTable.getObjectOrNull(left) != null)
            return right.evaluate(symbolTable.changeType(symbolTable.getObjectOrNull(left)!!))
        if (symbolTable.getImportOrNull(left.value) != null) {
            val importTable =
                symbolTable.changeFile(left.value)//SymbolTable(symbolTable.getVariables(), currentFile = left.value)
            return right.evaluate(importTable)
//            if (right is TokenLink)
//                return right.evaluate(importTable)
//            if (right is TokenCall) {
//                (right as TokenCall).argumentsToParameters(symbolTable, importTable)
//                return right.evaluate(importTable)
//            } else if (right is TokenConstructor) {
//                TODO("create assignments for constructor")
//
//            } else
//            // object
//                if (right is TokenIdentifier) {
//                    if (importTable.getObjectOrNull(right) != null)
//                        return importTable.getObjectOrNull(right)!!.symbolTable
//                }
        }
        throw PositionalException("unexpected token in link", left)
    }
}