package lexer

import token.Declaration
import token.Identifier
import token.Linkable
import token.Token
import token.invocation.Invocation
import token.operator.Index
import token.operator.TokenTernary
import token.statement.Block
import token.statement.WordStatement
import token.variable.TokenArray
import token.variable.TokenDictionary
import token.variable.TokenNumber

/**
 * Lexer creates tokens from text
 *
 * Important changes:
 * 1. ";" is replaced by \n
 * 2. ternary operator and functions follow kotlin style
 */
class Lexer() {

    private var source: String = ""
    private val operators = "!@#$%^*-+=?.,:;\"&|/(){}[]><\n"
    private val escapes = mutableMapOf('"' to '\"', '\\' to '\\', 'b' to '\b', 'n' to '\n', 'r' to '\r', 't' to '\t')
    private var tokReg: Registry = Registry()
    private var index: Int = 0
    var position: Pair<Int, Int> = Pair(0, 0)
    private var tok: Token = Token()
    private var cached: Boolean = false
    val last: Token = Token()

    constructor(source: String = "") : this() {
        this.source = source
        getRegistry()
    }

    private fun nextString(): Token {
        val res = StringBuilder(source[index].toString())
        move()
        while (source[index] != '"') {
            // escaped symbols in string
            if (source[index] == '\\' && source.length > index + 1) {
                move()
                res.append(escapes[source[index]])
                move()
                continue
            }
            // TODO probably out of bounds if multiline string
            if (source[index] == '\n')
                throw Exception("Unterminated string at ${position.second}:${position.first}")
            res.append(source[index])
            move()
        }
        res.append(source[index])
        move()
        return tokReg.string(
            "(STRING)",
            res.toString().substring(1, res.toString().length - 1),
            Pair(position.first - res.toString().length, position.second)
        )
    }

    private fun nextIdent(): Token {
        val res = StringBuilder()
        while (index < source.length && isIdentChar(source[index])) {
            res.append(source[index])
            move()
        }
        if (tokReg.defined(res.toString())) {
            return tokReg.definedIdentifier(
                res.toString(),
                res.toString(),
                Pair(position.first - res.toString().length, position.second)
            )
        }
        return tokReg.identifier(
            "(IDENT)",
            res.toString(),
            Pair(position.first - res.toString().length, position.second)
        )
        //return tokReg.token("(IDENT)", res.toString(), Pair(position.first - res.toString().length, position.second))
    }


    private fun nextNumber(): Token {
        val res = StringBuilder(source[index].toString())
        move()
        while (index < source.length && source[index].isDigit()) {
            res.append(source[index])
            move()
        }
        if (index + 1 < source.length && source[index] == '.' && source[index + 1].isDigit()) {
            res.append(source[index])
            move()
            while (index < source.length && source[index].isDigit()) {
                res.append(source[index])
                move()
            }
        }
        return TokenNumber(res.toString(), Pair(position.first - res.toString().length, position.second))
        // return tokReg.token("(NUMBER)", res.toString(), Pair(position.first - res.toString().length, position.second))
    }

    private fun nextOperator(): Token {
        if (index + 2 <= source.lastIndex && source.substring(index..index + 2) == "!is") {
            move()
            move()
            move()
            return tokReg.operator(
                source.substring(index - 3 until index),
                source.substring(index - 3 until index),
                Pair(position.first - 3, position.second)
            )
        }
//        // for !is
//        if (index + 1 < source.lastIndex && tokReg.defined(source.substring(index..index + 2))) {
//            move()
//            move()
//            move()
//            return tokReg.operator(
//                source.substring(index - 3 until index),
//                source.substring(index - 3 until index),
//                Pair(position.first - 3, position.second)
//            )
//        }
        if (index < source.lastIndex && isOperatorChar(source[index + 1]) &&
            tokReg.defined(source[index].toString() + source[index + 1].toString())
        ) {
            move()
            move()
            return tokReg.operator(
                source[index - 2].toString() + source[index - 1].toString(),
                source[index - 2].toString() + source[index - 1].toString(),
                Pair(position.first - 2, position.second)
            )
        }
        if (source[index] == '\n')
            toNextLine()
        else if (tokReg.defined(source[index].toString())) move()
        else throw PositionalException("invalid operator", position = position, length = 1)
        return tokReg.operator(
            source[index - 1].toString(),
            source[index - 1].toString(), Pair(position.first - 1, position.second)
        )
    }

    fun next(): Token {
        cached = false
        var tempIndex = -1
        while (index != tempIndex) {
            tempIndex = index
            consumeWhitespaceAndComments()
        }
        if (index == source.length)
            return tokReg.token("(EOF)", "EOF", position)
        if (source[index] == '\\') {
            index++
            while (index != tempIndex) {
                tempIndex = index
                consumeWhitespaceAndComments()
            }
            if (source[index] != '\n')
                throw PositionalException("\n", position = position, length = 1)
            else index++
            consumeWhitespaceAndComments()

            position = Pair(0, position.second + 1)
        }
        return if (source[index] == '"')
            nextString()
        else if (isFirstIdentChar(source[index]))
            nextIdent()
        else if (source[index].isDigit())
            nextNumber()
        else if (isOperatorChar(source[index]))
            nextOperator()
        else throw PositionalException("invalid character", position = position, length = 1)
    }

    private fun consumeWhitespaceAndComments(): Boolean {
        var iter = 0
        var (whitespace, comments) = listOf(true, true)
        while (whitespace || comments) {
            whitespace = consumeWhitespace()
            comments = consumeComments()
            iter++
        }
        return iter > 1
    }

    private fun consumeComments(): Boolean {
        if (index < source.length && source[index] == '/' && index < source.lastIndex && source[index + 1] == '/') {
            while (source[index] != '\n') {
                move()
                if (index == source.length)
                    return false
            }
            toNextLine()
            return true
        } else if (index < source.length && source[index] == '/' && index < source.lastIndex && source[index + 1] == '*') {
            while (!(source[index] == '*' && source[index + 1] == '/')) {
                if (source[index] == '\n')
                    toNextLine()
                else move()
                if (index + 1 >= source.length)
                    return true
            }
            move()
            move()
        }
        return false
    }

    private fun consumeWhitespace(): Boolean {
        // '\t', '\v', '\f', '\r', ' ', U+0085 (NEL), U+00A0 (NBSP).
        val res = index < source.length && source[index] != '\n' && source[index].isWhitespace()
        while (index < source.length && source[index] != '\n' && source[index].isWhitespace())
            move()
        return res
    }

    fun peek(): Token {
        if (cached)
            return tok
        val ind = index
        val pos = Pair(position.first, position.second)
        val res = next()
        tok = res
        cached = true
        index = ind
        position = pos
        return res
    }

    private fun toNextLine() {
        index++
        position = Pair(0, position.second + 1)
    }

    private fun move() {
        index++
        position = Pair(position.first + 1, position.second)
    }

    private fun isFirstIdentChar(c: Char): Boolean = c.isLetter() || c == '_'

    private fun isIdentChar(c: Char): Boolean = c.isLetterOrDigit() || c == '_'

    private fun isOperatorChar(c: Char): Boolean {
        return operators.toCharArray().any { it == c }
    }

    private fun getRegistry(): Registry {
        tokReg = Registry()

        tokReg.symbol("(IDENT)")
        // tokReg.symbol("(LINK)")
        tokReg.symbol("(NUMBER)")
        tokReg.symbol("(STRING)")

        //tokReg.symbol("parent")

        tokReg.symbol("true")
        tokReg.symbol("false")
        tokReg.symbol("none")

        tokReg.consumable("\n")
        tokReg.consumable(")")
        tokReg.consumable("]")
        tokReg.consumable(",")
        tokReg.consumable("else")
        //tokReg.consumable(":")
        tokReg.consumable("export")

        tokReg.consumable("(EOF)")
        tokReg.consumable("{")
        tokReg.consumable("}")
        tokReg.consumable("as")

        tokReg.infix("+", 50)
        tokReg.infix("-", 50)
        tokReg.infix("*", 60)
        tokReg.infix("/", 60)
        tokReg.infix(":", 10)
        // useless, because // is comment
        // tokReg.infix("//", 60)
        tokReg.infix("%", 65)

        tokReg.infix("<", 30)
        tokReg.infix(">", 30)
        tokReg.infix("<=", 30)
        tokReg.infix(">=", 30)
        tokReg.infix("==", 30)
        tokReg.infix("!=", 30)

        //tokReg.infix("export", 10)
        //tokReg.prefix("import")
        //tokReg.infix(":", 20)

        tokReg.infix("is", 15)
        tokReg.infix("!is", 15)
        //tokReg.infix("isnot", 15)

        tokReg.prefix("-")
        tokReg.prefix("!")

        tokReg.infixRight("&", 25)
        tokReg.infixRight("|", 25)
        tokReg.infixRight("=", 10)

        // tokReg.infixRight(".", 105)
//        tokReg.infixRight("+=", 10)
//        tokReg.infixRight("-=", 10)

//        tokReg.infixLed("?", 20) { token: Token, parser: Parser, left: Token ->
//            val cond = parser.expression(0)
//            token.children.add(cond)
//            parser.advance(":")
//            token.children.add(left)
//            token.children.add(parser.expression(0))
//            token
//        }

        tokReg.infixLed(".", 105) { token: Token, parser: Parser, left: Token ->
            token.children.add(left)
            token.children.add(parser.expression(105))
            isLinkable(token.children.last())
            var t = parser.lexer.peek()
            while (t.symbol == "(LINK)") {
                parser.advance("(LINK)")
                token.children.add(parser.expression(105))
                t = parser.lexer.peek()
                isLinkable(token.children.last())
            }
            token
        }

        // function use
        tokReg.infixLed("(", 120) { token: Token, parser: Parser, left: Token ->
            if (left.symbol != "(LINK)" && left.symbol != "(IDENT)" && left.symbol != "["
                && left.symbol != "(" && left.symbol != "->" && left.symbol != "!"
            )
                throw  PositionalException("bad func call left operand `$left`", left)
            token.children.add(left)
            val t = parser.lexer.peek()
            if (t.symbol != ")") {
                sequence(token, parser)
                parser.advance(")")
            } else
                parser.advance(")")
            token
        }

        // array indexing
        tokReg.infixLed("[", 110) { token: Token, parser: Parser, left: Token ->
//            if (left.symbol != "." && left.symbol != "(IDENT)" && left.symbol != "[" && left.symbol != "(")
//                throw  PositionalException("bad func call left operand $left", left)
            val res = Index(token)
            res.children.add(left)
            val t = parser.lexer.peek()
            if (t.symbol != "]") {
                sequence(res, parser)
                parser.advance("]")
            } else
                parser.advance("]")
            res
        }

        // tuples
        tokReg.prefixNud("(") { token: Token, parser: Parser ->
            var comma = false
            if (parser.lexer.peek().symbol != ")") {
                while (true) {
                    if (parser.lexer.peek().symbol == ")")
                        break
                    token.children.add(parser.expression(0))
                    if (parser.lexer.peek().symbol != ",")
                        break
                    comma = true
                    parser.advance(",")
                }
            }
            parser.advance(")")
            if (token.children.size == 0 || comma) {
                token.symbol = "()"
                token.value = "TUPLE"
                token
            } else
                token.children[0]
        }

        tokReg.prefixNud("[") { token: Token, parser: Parser ->
            val res = TokenArray(token)
            if (parser.lexer.peek().symbol != "]") {
                while (true) {
                    if (parser.lexer.peek().symbol == "]")
                        break
                    res.children.add(parser.expression(0))
                    if (parser.lexer.peek().symbol != ",")
                        break
                    parser.advance(",")
                }
            }
            parser.advance("]")
            res.symbol = "[]"
            res.value = "ARRAY"
            res
        }

        tokReg.prefixNud("{") { token: Token, parser: Parser ->
            val res = TokenDictionary(token)
            if (parser.lexer.peek().symbol != "}") {
                while (true) {
                    if (parser.lexer.peek().symbol == "}")
                        break
                    res.children.add(parser.expression(0))
                    if (res.children.last().symbol != ":")
                        throw PositionalException("Expected key and value", res.children.last())
                    if (parser.lexer.peek().symbol != ",")
                        break
                    parser.advance(",")
                }
            }
            parser.advance("}")
            res.symbol = "{}"
            res.value = "DICTIONARY"
            res
        }

        // functions
//        tokReg.infixRightLed("->", 10) { token: Token, parser: Parser, left: Token ->
//            if (left.symbol != "()" && left.symbol != "(IDENT)")
//                throw PositionalException("invalid function declaration tuple $left", left)
//            if (left.symbol == "()" && left.children.size != 0) {
//                var named = true
//                for (child in left.children) {
//                    if (child.symbol != "(IDENT)") {
//                        named = false
//                        break
//                    }
//                }
//                if (!named)
//                    throw PositionalException("invalid function declaration tuple $left", left)
//            }
//            token.children.add(left)
//            if (parser.lexer.peek().symbol == "{")
//                token.children.add(parser.block())
//            else
//                token.children.add(parser.expression(0))
//            token
//        }

        tokReg.prefixNud("if") { token: Token, parser: Parser ->
            val res = TokenTernary(token)
            parser.advance("(")
            val cond = parser.expression(0)
            res.children.add(cond)
            parser.advance(")")
            res.children.add(parser.expression(0))
            parser.advance("else")
            res.children.add(parser.expression(0))
            res
        }

//        tokReg.infixLed("if", 20) { token: Token, parser: Parser, left: Token ->
//            val cond = parser.expression(0)
//            token.children.add(cond)
//            parser.advance("else")
//            token.children.add(left)
//            token.children.add(parser.expression(0))
//            token
//        }

        // statements
        tokReg.stmt("if") { token: Token, parser: Parser ->
            val res = Block(token)
            res.children.add(parser.expression(0))
            res.children.add(parser.block())
            var next = parser.lexer.peek()
            if (next.value == "else") {
                parser.lexer.next()
                next = parser.lexer.peek()
                if (next.value == "if")
                    res.children.add(parser.statement())
                else
                    res.children.add(parser.block())
            }
            res
        }

        tokReg.stmt("import") { token: Token, parser: Parser ->
            val res = Declaration(token)
            res.children.add(parser.expression(0))
            if (parser.lexer.peek().value == "as") {
                parser.advance("as")
                res.children.add(parser.expression(0))
                if (res.right !is Identifier)
                    throw PositionalException("Expected identifier, not a link after `as` directive", res.right)
            } else {
                if (res.left !is Identifier)
                    throw PositionalException(
                        "Imports containing folders in name should be declared like:\n" +
                                "`import path as identifier` and used in code accordingly", res
                    )
                res.children.add(Token(res.left.symbol, res.left.value))
            }
            res
        }

        tokReg.stmt("class") { token: Token, parser: Parser ->
            val res = Declaration(token)
            val expr = parser.expression(0)
            if (expr.symbol == ":") {
                res.children.addAll(expr.children)
            } else res.children.addAll(listOf(expr, Token("", "")))
            if (parser.lexer.peek().value == "export") {
                parser.advance("export")
                res.children.add(parser.expression(0))
            } else res.children.add(Token("", ""))
            res.children.add(parser.block())
            res
        }

        tokReg.stmt("object") { token: Token, parser: Parser ->
            val res = Declaration(token)
            res.children.add(parser.expression(0))
            if (parser.lexer.peek().value == "export") {
                parser.advance("export")
                res.children.add(parser.expression(0))
            }
            res.children.add(parser.block())
            res
        }

        tokReg.stmt("fun") { token: Token, parser: Parser ->
            val res = Declaration(token)
            res.children.add(parser.expression(0))
            res.children.add(parser.block())
            res
        }

        tokReg.stmt("{") { token: Token, parser: Parser ->
            val res = Block(token)
            res.children.addAll(parser.statements())
            parser.advance("}")
            res
        }

        tokReg.stmt("while") { token: Token, parser: Parser ->
            val res = Block(token)
            res.children.add(parser.expression(0))
            res.children.add(parser.block())
            res
        }

        tokReg.stmt("break") { token: Token, parser: Parser ->
            if (parser.lexer.peek().symbol != "}")
                parser.advance("\n")
            WordStatement(token)
        }

        // TODO advance comment
        tokReg.stmt("continue") { token: Token, parser: Parser ->
            if (parser.lexer.peek().symbol != "}")
                parser.advance("\n")
            WordStatement(token)
        }

        tokReg.stmt("return") { token: Token, parser: Parser ->
            val res = WordStatement(token)
            if (parser.lexer.peek().symbol != "}" && parser.lexer.peek().symbol != "\n")
                res.children.add(parser.expression(0))
            //parser.advance("\n")
            res
        }

        return tokReg
    }

    private fun sequence(token: Token, parser: Parser) {
        while (true) {
            val exp = parser.expression(0)
            token.children.add(exp)
            val tokenRes = parser.lexer.peek()
            if (tokenRes.symbol != ",")
                break
            parser.advance(",")
        }
    }

    fun hasCommentAhead(): Boolean = consumeWhitespaceAndComments()

    /**
     * Check if [token] can be used as a child inside [Link][token.Link]
     *
     * First child of [Link][token.Link] can be anything
     */
    private fun isLinkable(token: Token) {
        if (token !is Linkable)
            throw ExpectedTypeException(listOf(Identifier::class, Invocation::class, Index::class), token, token)
        var index = token
        while (index is Index)
            index = index.left
        if (index !is Linkable)
            throw ExpectedTypeException(listOf(Identifier::class, Invocation::class, Index::class), token, token)
    }
}