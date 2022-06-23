package lexer

import Logger
import token.*
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
    private val operators = "!@#$%^*-+=?.,:;\"&|/(){}[]><\n\r"
    private val escapes = mutableMapOf('"' to '\"', '\\' to '\\', 'b' to '\b', 'n' to '\n', 'r' to '\r', 't' to '\t')
    private var tokReg: Registry = Registry()
    private var index: Int = 0
    var position: Pair<Int, Int> = Pair(0, 0)
    private val tokens = mutableListOf<Token>()
    private var tokenIndex = -1

    constructor(source: String = "") : this() {
        this.source = source
        getRegistry()
        addTokens()
    }

    private fun addTokens() {
        while (index < source.length)
            tokens.add(createNextToken())
        if (tokens.last().symbol != "(EOF)")
            tokens.add(Token("(EOF)", "(EOF)", position))
        if (tokens.size >= 100000)
            Logger.addWarning(tokens.last(), "File too large")
    }

    fun next(): Token = tokens[++tokenIndex]
    fun prev(): Token = tokens[--tokenIndex]

    private fun isLineSeparator(): Boolean {
        if (index == source.lastIndex)
            return source[index] == '\r' || source[index] == '\n'
        if (source[index] == '\r' && source[index + 1] == '\n')
            return true
        return source[index] == '\r' || source[index] == '\n'
    }

    private fun moveAfterLineSeparator(): Int {
        if (index == source.lastIndex)
            return if (source[index] == '\r' || source[index] == '\n') index + 1 else index
        if (source[index] == '\r' && source[index + 1] == '\n')
            return index + 2
        return if (source[index] == '\r' || source[index] == '\n') index + 1 else index
    }

    private fun nextString(): Token {
        val res = StringBuilder()
        moveAndAppend(res)
        while (source[index] != '"') {
            // escaped symbols in string
            if (source[index] == '\\' && source.length > index + 1) {
                move()
                res.append(escapes[source[index]])
                move()
            } else if (isLineSeparator() || index == source.lastIndex)
                throw Exception("Unterminated string at ${position.second}:${position.first}")
            else moveAndAppend(res)
        }
        moveAndAppend(res)
        return tokReg.string(
            "(STRING)",
            res.toString().substring(1, res.toString().length - 1),
            Pair(position.first - res.toString().length, position.second)
        )
    }

    private fun nextIdent(): Token {
        val res = StringBuilder()
        while (index < source.length && isIdentChar(source[index]))
            moveAndAppend(res)
        if (tokReg.defined(res.toString()))
            return tokReg.definedIdentifier(
                res.toString(), res.toString(),
                Pair(position.first - res.toString().length, position.second)
            )
        return tokReg.identifier(
            "(IDENT)",
            res.toString(),
            Pair(position.first - res.toString().length, position.second)
        )
    }

    private fun nextNumber(): Token {
        val res = StringBuilder()
        moveAndAppend(res)
        while (index < source.length && source[index].isDigit())
            moveAndAppend(res)
        if (index + 1 < source.length && source[index] == '.' && source[index + 1].isDigit()) {
            moveAndAppend(res)
            while (index < source.length && source[index].isDigit())
                moveAndAppend(res)
        }
        return TokenNumber(res.toString(), Pair(position.first - res.toString().length, position.second))
    }

    private fun nextOperator(): Token {
        for (ind in index + 2 downTo index) {
            if (ind < source.length && tokReg.defined(source.substring(index..ind))) {
                val value = source.substring(index..ind)
                val operator = tokReg.operator(value, value, Pair(position.first - value.length, position.second))
                if (operator.symbol == "(SEP)" && operator.value != ";") {
                    toNextLine()
                }
                else for (i in index..ind)
                    move()
                return operator
            }
        }
        throw PositionalException("Invalid operator", position = position)
    }

    private fun createNextToken(): Token {
        consumeWhitespaceAndComments()
        if (index == source.length)
            return tokReg.token("(EOF)", "(EOF)", position)
        // go to next line symbol - to separate long expressions
        if (source[index] == '\\') {
            index++
            consumeWhitespaceAndComments()
            if (!isLineSeparator())
                throw PositionalException("Expected new line after \\", position = position, length = 1)
            else index = moveAfterLineSeparator()
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
        else throw PositionalException("Invalid character", position = position, length = 1)
    }

    private fun consumeWhitespaceAndComments(): Boolean {
        var iter = 0
        var (whitespace, comments) = listOf(true, true)
        while (whitespace || comments) {
            whitespace = consumeWhitespace()
            comments = consumeComments()
            if (comments)
                tokens.add(tokReg.token("(SEP)", "//", position))
            iter++
        }
        return iter > 1
    }

    private fun consumeComments(): Boolean {
        if (index < source.length && source[index] == '/' && index < source.lastIndex && source[index + 1] == '/') {
            while (!isLineSeparator()) {
                move()
                if (index == source.length)
                    return true
            }
            return true
        } else if (index < source.length && source[index] == '/'
            && index < source.lastIndex && source[index + 1] == '*'
        ) {
            move(2)
            if (index + 1 >= source.length)
                throw PositionalException("Unterminated comment", position = position)
            while (!(source[index] == '*' && source[index + 1] == '/')) {
                if (isLineSeparator())
                    toNextLine()
                else move()
                if (index + 1 >= source.length)
                    throw PositionalException("Unterminated comment", position = position)
            }
            move(2)
            return true
        }
        return false
    }

    private fun consumeWhitespace(): Boolean {
        // '\t', '\v', '\f', '\r', ' ', U+0085 (NEL), U+00A0 (NBSP).
        val res = index < source.length && !isLineSeparator() && source[index].isWhitespace()
        while (index < source.length && !isLineSeparator() && source[index].isWhitespace())
            move()
        return res
    }

    fun peek(offset: Int = 1): Token {
        if (tokenIndex + offset >= tokens.size)
            return tokens.last()
        return tokens[tokenIndex + offset]
    }

    private fun toNextLine() {
        index = moveAfterLineSeparator()
        position = Pair(0, position.second + 1)
    }

    private fun move(step: Int = 1) {
        index += step
        position = Pair(position.first + step, position.second)
    }

    private fun moveAndAppend(sb: java.lang.StringBuilder) {
        sb.append(source[index])
        move()
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

        tokReg.prefixNud("false") { token: Token, _: Parser ->
            TokenNumber("0", token.position)
        }

        tokReg.prefixNud("true") { token: Token, _: Parser ->
            TokenNumber("1", token.position)
        }
        tokReg.symbol("true")
        tokReg.symbol("false")

        /* separators are placed between statements. Separator values are:
         1. end of line
         2. comment
         3. ;
         4. end of file
        */
        tokReg.consumable("(SEP)")
        tokReg.consumable("\n")
        tokReg.consumable("\r")
        tokReg.consumable("\r\n")
        tokReg.consumable(";")
        tokReg.consumable("(EOF)")

        tokReg.consumable(")")
        tokReg.consumable("]")
        tokReg.consumable(",")
        tokReg.consumable("else")

        tokReg.consumable("export")

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

        tokReg.infix("is", 15)
        tokReg.infix("!is", 15)

        tokReg.prefix("-")
        tokReg.prefix("!")

        tokReg.infixRight("&", 25)
        tokReg.infixRight("|", 25)
        tokReg.infixRight("=", 10)

        // tokReg.infixRight(".", 105)
        // tokReg.infixRight("+=", 10)
        // tokReg.infixRight("-=", 10)

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
            if (left.symbol != "(LINK)" && left.symbol != "(IDENT)" && left.symbol != "[" &&
                left.symbol != "(" && left.symbol != "!"
            )
                throw PositionalException("`$left` is not invokable", left)
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

        // arithmetic and redundant parentheses
        tokReg.prefixNud("(") { token: Token, parser: Parser ->
            var comma = false
            if (parser.lexer.peek().symbol != ")")
                comma = sequence(token, parser)
            parser.advance(")")
            if (comma)
                throw PositionalException("Tuples are not implemented", token)
            else if (token.children.size == 0)
                throw  PositionalException("Empty parentheses", token)
            /* Return first child when parentheses are redundant e.g. condition for `if` or `while`
               or if parentheses are inside arithmetic expression. Then, this will return an expression inside them */
            else
                token.children[0]
        }

        tokReg.prefixNud("[") { token: Token, parser: Parser ->
            val res = TokenArray(token)
            if (parser.lexer.peek().symbol != "]")
                sequence(res, parser)
            parser.advance("]")
            res.symbol = "[]"
            res.value = "(ARRAY)"
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
            res.value = "(DICTIONARY)"
            res
        }

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

        // statements
        tokReg.stmt("if") { token: Token, parser: Parser ->
            val res = Block(token)
            res.children.add(parser.expression(0))
            res.children.add(parser.block(canBeSingleStatement = true))
            var next = parser.lexer.peek()
            if (next.value == "(SEP)" && parser.lexer.peek(2).value == "else") {
                parser.lexer.next()
                next = parser.lexer.peek()
            }
            if (next.value == "else") {
                parser.lexer.next()
                next = parser.lexer.peek()
                if (next.value == "if")
                    res.children.add(parser.statement())
                else
                    res.children.add(parser.block(canBeSingleStatement = true))
            }
            res
        }

        tokReg.stmt("import") { token: Token, parser: Parser ->
            val res = Declaration(token)
            res.children.add(parser.expression(0))
            if (parser.lexer.peek().value == "as") {
                parser.advance("as")
                res.children.add(parser.expression(0))
                if (!checkIdentifierInImport(res.right))
                    throw PositionalException("Expected non-link identifier after `as` directive", res.right)
            } else {
                if (!checkIdentifierInImport(res.left))
                    throw PositionalException(
                        "Imports containing folders in name should be declared like:\n" +
                                "`import path as identifier` and used in code with specified identifier",
                        res
                    )
                res.children.add(Token(res.left.symbol, res.left.value))
            }
            if (res.left is Link)
                checkImportedFolder(res.left as Link)
            else if (!checkIdentifierInImport(res.left))
                throw PositionalException("Expected link or identifier before `as` directive", res.right)
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
            res.children.add(parser.block(canBeSingleStatement = true))
            res
        }

        tokReg.stmt("break") { token: Token, parser: Parser ->
            if (parser.lexer.peek().symbol != "}")
                parser.advance("(SEP)")
            WordStatement(token)
        }

        // TODO advance comment
        tokReg.stmt("continue") { token: Token, parser: Parser ->
            if (parser.lexer.peek().symbol != "}")
                parser.advance("(SEP)")
            WordStatement(token)
        }

        tokReg.stmt("return") { token: Token, parser: Parser ->
            val res = WordStatement(token)
            if (parser.lexer.peek().symbol != "}" && parser.lexer.peek().symbol != "(SEP)")
                res.children.add(parser.expression(0))
            // parser.advance("\n")
            res
        }

        return tokReg
    }

    private fun sequence(token: Token, parser: Parser): Boolean {
        var comma = false
        while (true) {
            token.children.add(parser.expression(0))
            if (parser.lexer.peek().symbol != ",")
                return comma
            parser.advance(",")
            comma = true
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

    private fun checkImportedFolder(link: Link) {
        for (ident in link.children)
            if (!checkIdentifierInImport(ident))
                throw PositionalException("Each folder should be represented as identifier", ident)
    }

    private fun checkIdentifierInImport(token: Token): Boolean = token is Identifier || token.children.size == 0
}
