package token.link

import table.SymbolTable

class CallLink(token: Link) :
    Link(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        return ""
    }
}
