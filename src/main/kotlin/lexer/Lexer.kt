package lexer

class Lexer() {

    var source: String = ""

    constructor(source: String = "") : this() {
        this.source = source
        getRegistry()
    }

    var tokReg: Registry = Registry()
    var index: Int = 0
    var position: Pair<Int, Int> = Pair(0, 0)
    var tok: Token = Token()
    var cached: Boolean = false
    val last: Token = Token()

    fun nextString(): Token {
        val res = StringBuilder(source[index].toString())
        move()
        while (source[index] != '"') {
            if (source[index] == '\n')
                throw Exception("Unterminated string at ${position.second}:${position.first}")
            res.append(source[index])
            move()
        }
        res.append(source[index])
        move()
        return tokReg.token("(STRING)", res.toString(), position)
    }

    fun nextIdent(): Token {
        val res = StringBuilder()
        while (isIdentChar(source[index])) {
            res.append(source[index])
            move()
        }
        if (tokReg.defined(res.toString()))
            return tokReg.token(res.toString(), res.toString(), position)
//        if (res.contains('.'))
//            return tokReg.token("(LINK)", res.toString(), position)
        return tokReg.token("(IDENT)", res.toString(), position)
    }

    fun nextNumber(): Token {
        val res = StringBuilder(source[index].toString())
        move()
        while (index < source.length && source[index].isDigit()) {
            res.append(source[index])
            move()
        }

        if (index < source.length && source[index] == '.') {
            res.append(source[index])
            move()
            while (index < source.length && source[index].isDigit()) {
                res.append(source[index])
                move()
            }
        }
        return tokReg.token("(NUMBER)", res.toString(), position)
    }

    fun nextOperator(): Token {
        if (index < source.lastIndex && isOperatorChar(source[index + 1]) &&
            tokReg.defined(source[index].toString() + source[index + 1].toString())
        ) {
            move()
            move()
            return tokReg.token(
                source[index - 2].toString() + source[index - 1].toString(),
                source[index - 2].toString() + source[index - 1].toString(),
                position
            )
        }
        if (source[index] == '\n')
            toNextLine()
        else if (tokReg.defined(source[index].toString())) move()
        else throw PositionalException("invalid operator", position = position,length = 1)
        return tokReg.token(source[index - 1].toString(), source[index - 1].toString(), position)
    }

    fun next(): Token {
        cached = false
        var tempIndex = -1
        while (index != tempIndex) {
            tempIndex = index
            consumeWhitespace()
            consumeComments()
        }

        if (index == source.length)
            return tokReg.token("(EOF)", "EOF", position)

        if (source[index] == '\\') {
            index++
            while (index != tempIndex) {
                tempIndex = index
                consumeWhitespace()
                consumeComments()
            }
            if (source[index] != '\n')
                throw PositionalException("\n", position = position,length = 1)
            else index++

            position = Pair(0, position.second + 1)
        }

        if (source[index] == '"')
            return nextString()
        else if (isFirstIdentChar(source[index]))
            return nextIdent()
        else if (source[index].isDigit())
            return nextNumber()
        else if (isOperatorChar(source[index]))
            return nextOperator()
        else throw PositionalException("invalid character", position = position,length = 1)
    }

    private fun consumeWhitespace() {
        // '\t', '\v', '\f', '\r', ' ', U+0085 (NEL), U+00A0 (NBSP).
        while (index < source.length && source[index] != '\n' && source[index].isWhitespace())
            move()
    }

    private fun consumeComments() {
        if (index < source.length && source[index] == '/' && index < source.lastIndex && source[index + 1] == '/') {
            while (source[index] != '\n')
                move()
            toNextLine()
        }
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

    fun isFirstIdentChar(c: Char): Boolean = c.isLetter() || c == '_'

    fun isIdentChar(c: Char): Boolean = c.isLetterOrDigit() || c == '_'

    fun isOperatorChar(c: Char): Boolean {
        val operators = "!@#$%^*-+=/?.,:;\"&|/(){}[]><\n"
        return operators.toCharArray().any { it == c }
    }

    private fun getRegistry(): Registry {
        tokReg = Registry()

        tokReg.symbol("(IDENT)")
       // tokReg.symbol("(LINK)")
        tokReg.symbol("(NUMBER)")
        tokReg.symbol("(STRING)")

        tokReg.symbol("true")
        tokReg.symbol("false")
        tokReg.symbol("none")

        tokReg.consumable("\n")
        tokReg.consumable(")")
        tokReg.consumable("]")
        tokReg.consumable(",")
        tokReg.consumable("else")

        tokReg.consumable("(EOF)")
        tokReg.consumable("{")
        tokReg.consumable("}")

        tokReg.infix("+", 50)
        tokReg.infix("-", 50)
        tokReg.infix("*", 60)
        tokReg.infix("/", 60)
        tokReg.infix("%", 65)

        tokReg.infix("<", 30)
        tokReg.infix(">", 30)
        tokReg.infix("<=", 30)
        tokReg.infix(">=", 30)
        tokReg.infix("==", 30)

        tokReg.infix("export", 10)
        tokReg.infix(":", 20)


        tokReg.prefix("-")
        tokReg.prefix("!")

        tokReg.infixRight("&", 25)
        tokReg.infixRight("|", 25)
        tokReg.infixRight("=", 10)

        tokReg.infixRight(".",80)
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

        //tokReg.prefixNud("if")

        // function use
        tokReg.infixLed("(", 90) { token: Token, parser: Parser, left: Token ->
            if (left.symbol != "." && left.symbol != "(IDENT)" && left.symbol != "[" && left.symbol != "(" && left.symbol != "->")
                throw  PositionalException("bad func call left operand $left", left)

            token.children.add(left)
            val t = parser.lexer.peek()

            if (t.symbol != ")") {
                while (true) {
                    val exp = parser.expression(0)
                    token.children.add(exp)
                    val tokenRes = parser.lexer.peek()
                    if (tokenRes.symbol != ",")
                        break
                    parser.advance(",")
                }
                parser.advance(")")
            } else
                parser.advance(")")
            token
        }

        // array indexing
        tokReg.infixLed("[", 80) { token: Token, parser: Parser, left: Token ->
            if (left.symbol != "." && left.symbol != "(IDENT)" && left.symbol != "[" && left.symbol != "(")
                throw  PositionalException("bad func call left operand $left", left)

            token.children.add(left)
            val t = parser.lexer.peek()
            if (t.symbol != "]") {
                while (true) {
                    val exp = parser.expression(0)
                    token.children.add(exp)
                    val tokenRes = parser.lexer.peek()
                    if (tokenRes.symbol != ",")
                        break
                    parser.advance(",")
                }
                parser.advance("]")
            } else
                parser.advance("]")
            token
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
            if (parser.lexer.peek().symbol != "]") {
                while (true) {
                    if (parser.lexer.peek().symbol == "]")
                        break
                    token.children.add(parser.expression(0))
                    if (parser.lexer.peek().symbol != ",")
                        break
                    parser.advance(",")
                }
            }
            parser.advance("]")
            token.symbol = "[]"
            token.value = "ARRAY"
            token
        }

        // functions
        tokReg.infixRightLed("->", 10) { token: Token, parser: Parser, left: Token ->
            if (left.symbol != "()" && left.symbol != "(IDENT)")
                throw PositionalException("invalid function declaration tuple $left", left)
            if (left.symbol == "()" && left.children.size != 0) {
                var named = true
                for (child in left.children) {
                    if (child.symbol != "(IDENT)") {
                        named = false
                        break
                    }
                }
                if (!named)
                    throw PositionalException("invalid function declaration tuple $left", left)
            }
            token.children.add(left)
            if (parser.lexer.peek().symbol == "{")
                token.children.add(parser.block())
            else
                token.children.add(parser.expression(0))
            token
        }

        tokReg.prefixNud("if") { token: Token, parser: Parser ->
            val cond = parser.expression(0)
            token.children.add(cond)
            token.children.add(parser.expression(0))
            parser.advance("else")
            token.children.add(parser.expression(0))
            token
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
            token.children.add(parser.expression(0))
            token.children.add(parser.block())
            var next = parser.lexer.peek()
            if (next.value == "else") {
                parser.lexer.next()
                next = parser.lexer.peek()
                if (next.value == "if")
                    token.children.add(parser.statement())
                else
                    token.children.add(parser.block())

            }
            token
        }

        tokReg.stmt("class") { token: Token, parser: Parser ->
            token.children.add(parser.expression(0))
            token.children.add(parser.block())
            token
        }

        tokReg.stmt("fun") { token: Token, parser: Parser ->
            token.children.add(parser.expression(0))
            token.children.add(parser.block())
            token
        }

        tokReg.stmt("while") { token: Token, parser: Parser ->
            token.children.add(parser.expression(0))
            token.children.add(parser.block())
            token
        }

        tokReg.stmt("{") { token: Token, parser: Parser ->
            token.children.addAll(parser.statements())
            parser.advance("}")
            token
        }

        tokReg.stmt("while") { token: Token, parser: Parser ->
            token.children.add(parser.expression(0))
            token.children.add(parser.block())
            token
        }

        tokReg.stmt("break") { token: Token, parser: Parser ->
            parser.advance("\n")
            token
        }

        tokReg.stmt("continue") { token: Token, parser: Parser ->
            parser.advance("\n")
            token
        }

        tokReg.stmt("return") { token: Token, parser: Parser ->
            if (parser.lexer.peek().symbol != "\n")
                token.children.add(parser.expression(0))
            parser.advance("\n")
            token
        }

        return tokReg
    }
}