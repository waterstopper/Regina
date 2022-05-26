package properties.primitive

import token.Token

interface Indexable {
    operator fun get(index: Any, token: Token): Any
    operator fun set(index: Any, value: Any, tokenIndex: Token, tokenValue: Token)
}