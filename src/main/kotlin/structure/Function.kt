package structure

import lexer.Token

class Function(name: String, parent: Type?, val args: List<String>, val body: Token) : Node(name, parent) {
    override fun toString(): String {
        return "$name(${args.joinToString(separator = ",")})     ${parent ?: ""}"
    }
}