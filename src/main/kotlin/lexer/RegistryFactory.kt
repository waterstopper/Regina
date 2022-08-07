package lexer

import token.*
import token.invocation.Invocation
import token.operator.Index
import token.operator.TokenTernary
import token.statement.Block
import token.statement.WordStatement
import token.variable.TokenArray
import token.variable.TokenDictionary
import token.variable.TokenNumber

object RegistryFactory {
    /**
     * Add parsing templates
     */
    fun getRegistry(): Registry {
        val registry = Registry()

        registry.symbol("(IDENT)")
        // tokReg.symbol("(LINK)")
        registry.symbol("(NUMBER)")
        registry.symbol("(STRING)")

        registry.prefixNud("false") { token: Token, _: Parser ->
            TokenNumber("0", token.position)
        }

        registry.prefixNud("true") { token: Token, _: Parser ->
            TokenNumber("1", token.position)
        }
        registry.symbol("true")
        registry.symbol("false")

        /* separators are placed between statements. Separator values are:
         1. end of line
         2. comment
         3. semicolon
         4. end of file
        */
        registry.consumable("(SEP)")
        registry.consumable("\n")
        registry.consumable("\r")
        registry.consumable("\r\n")
        registry.consumable(";")
        registry.consumable("(EOF)")

        registry.consumable(")")
        registry.consumable("]")
        registry.consumable(",")
        registry.consumable("else")

        registry.consumable("{")
        registry.consumable("}")
        registry.consumable("as")

        registry.infix("+", 50)
        registry.infix("-", 50)
        registry.infix("*", 60)
        registry.infix("/", 60)
        registry.infix(":", 10)
        // useless, because // is comment
        // tokReg.infix("//", 60)
        registry.infix("%", 65)

        registry.infix("<", 30)
        registry.infix(">", 30)
        registry.infix("<=", 30)
        registry.infix(">=", 30)
        registry.infix("==", 30)
        registry.infix("!=", 30)

        registry.infix("is", 15)
        registry.infix("!is", 15)

        registry.unaryMinus("-")
        registry.prefix("!")

        registry.infixRight("&&", 25)
        registry.infixRight("||", 25)
        registry.infixRight("=", 10)

        // tokReg.infixRight(".", 105)
        // tokReg.infixRight("+=", 10)
        // tokReg.infixRight("-=", 10)

        registry.infixLed(".", 105) { token: Token, parser: Parser, left: Token ->
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
        registry.infixLed("(", 120) { token: Token, parser: Parser, left: Token ->
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
        registry.infixLed("[", 110) { token: Token, parser: Parser, left: Token ->
            val res = Index(token)
            res.children.add(left)
            val t = parser.lexer.peek()
            if (t.symbol != "]") {
                sequence(res, parser)
                parser.advance("]")
            } else throw PositionalException("Expected index", t)
            res
        }

        // arithmetic and redundant parentheses
        registry.prefixNud("(") { token: Token, parser: Parser ->
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

        registry.prefixNud("[") { token: Token, parser: Parser ->
            val res = TokenArray(token)
            if (parser.lexer.peek().symbol != "]")
                sequence(res, parser)
            parser.advance("]")
            res.symbol = "[]"
            res.value = "(ARRAY)"
            res
        }

        registry.prefixNud("{") { token: Token, parser: Parser ->
            val res = TokenDictionary(token)
            if (parser.lexer.peek().symbol != "}") {
                while (true) {
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

        registry.prefixNud("if") { token: Token, parser: Parser ->
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
        registry.stmt("if") { token: Token, parser: Parser ->
            val res = Block(token)
            res.children.add(parser.expression(0))
            res.children.add(parser.block(canBeSingleStatement = true))
            var next = parser.lexer.peek()
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

        registry.stmt("import") { token: Token, parser: Parser ->
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

        registry.stmt("class") { token: Token, parser: Parser ->
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

        registry.stmt("object") { token: Token, parser: Parser ->
            val res = Declaration(token)
            res.children.add(parser.expression(0))
            if (parser.lexer.peek().value == "export") {
                parser.advance("export")
                res.children.add(parser.expression(0))
            }
            res.children.add(parser.block())
            res
        }

        registry.stmt("fun") { token: Token, parser: Parser ->
            val res = Declaration(token)
            res.children.add(parser.expression(0))
            res.children.add(parser.block())
            res
        }

        registry.stmt("{") { token: Token, parser: Parser ->
            val res = Block(token)
            res.children.addAll(parser.statements())
            parser.advance("}")
            res
        }

        registry.stmt("while") { token: Token, parser: Parser ->
            val res = Block(token)
            res.children.add(parser.expression(0))
            res.children.add(parser.block(canBeSingleStatement = true))
            res
        }

        registry.stmt("break") { token: Token, parser: Parser ->
            if (parser.lexer.peek().symbol != "}")
                parser.advanceSeparator()
            WordStatement(token)
        }

        registry.stmt("continue") { token: Token, parser: Parser ->
            if (parser.lexer.peek().symbol != "}")
                parser.advanceSeparator()
            WordStatement(token)
        }

        registry.stmt("return") { token: Token, parser: Parser ->
            val res = WordStatement(token)
            if (parser.lexer.peek().symbol != "}" && !parser.lexer.peekSeparator())
                res.children.add(parser.expression(0))
            res
        }

        registry.stmt("#stop") { token: Token, _: Parser -> token }

        return registry
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
            throw ExpectedTypeException(listOf(Identifier::class, Invocation::class, Index::class), index, index)
    }

    private fun checkImportedFolder(link: Link) {
        for (ident in link.children)
            if (!checkIdentifierInImport(ident))
                throw PositionalException("Each folder should be represented as identifier", ident)
    }

    private fun checkIdentifierInImport(token: Token): Boolean = token is Identifier || token.children.size == 0
}
