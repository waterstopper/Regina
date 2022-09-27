package lexer

import TokenDictionary
import TokenList
import TokenNumber
import node.Identifier
import node.Linkable
import node.invocation.Invocation
import node.operator.Index
import token.*
import token.declaration.TokenFunction
import token.declaration.TokenImport
import token.declaration.TokenObject
import token.declaration.TokenType
import token.variable.TokenNull

object RegistryFactory {
    /**
     * Add parsing templates
     */
    fun getRegistry(filePath: String): Registry {
        val registry = Registry()

        registry.symbol("(IDENT)")
        // tokReg.symbol("(LINK)")
        registry.symbol("(NUMBER)")
        registry.symbol("(STRING)")

        registry.prefixNud("false") { node: Token, _: Parser ->
            TokenNumber("0", node.position)
        }

        registry.prefixNud("true") { node: Token, _: Parser ->
            TokenNumber("1", node.position)
        }
        registry.prefixNud("null") { node: Token, _: Parser ->
            TokenNull(node.position)
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
        registry.consumable("in")

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

        registry.infix("??", 20)
        registry.infixRight("&&", 25)
        registry.infixRight("||", 25)
        registry.infixRight("=", 10)

        // tokReg.infixRight(".", 105)
        // tokReg.infixRight("+=", 10)
        // tokReg.infixRight("-=", 10)

        registry.infixLed(".", 105) { node: Token, parser: Parser, left: Token ->
            registerLink(node as Link, parser, left, filePath)
        }

        registry.infixLed("?.", 105) { node: Token, parser: Parser, left: Token ->
            registerLink(node as Link, parser, left, filePath, true)
        }

        // function use
        registry.infixLed("(", 120) { node: Token, parser: Parser, left: Token ->
            if (left.symbol != "(LINK)" && left.symbol != "(IDENT)" && left.symbol != "[" &&
                left.symbol != "(" && left.symbol != "!"
            ) {
                throw SyntaxException("`$left` is not invokable", filePath, left)
            }
            node.children.add(left)
            val t = parser.lexer.peek()
            if (t.symbol != ")") {
                sequence(node, parser)
                parser.advance(")")
            } else {
                parser.advance(")")
            }
            node
        }

        // list indexing
        registry.infixLed("[", 110) { node: Token, parser: Parser, left: Token ->
            val res = TokenIndex(node)
            res.children.add(left)
            val t = parser.lexer.peek()
            if (t.symbol != "]") {
                sequence(res, parser)
                parser.advance("]")
            } else throw SyntaxException("Expected index", filePath, t)
            res
        }

        // arithmetic and redundant parentheses
        registry.prefixNud("(") { node: Token, parser: Parser ->
            var comma = false
            if (parser.lexer.peek().symbol != ")") {
                comma = sequence(node, parser)
            }
            parser.advance(")")
            if (comma) {
                throw SyntaxException("Tuples are not implemented", filePath, node)
            } else if (node.children.size == 0) {
                throw SyntaxException("Empty parentheses", filePath, node)
            }
            /* Return first child when parentheses are redundant e.g. condition for `if` or `while`
               or if parentheses are inside arithmetic expression. Then, this will return an expression inside them */
            else {
                node.children[0]
            }
        }

        registry.prefixNud("[") { node: Token, parser: Parser ->
            val res = TokenList(node)
            if (parser.lexer.peek().symbol != "]") {
                sequence(res, parser)
            }
            parser.advance("]")
            res.symbol = "[]"
            res.value = "(LIST)"
            res
        }

        registry.prefixNud("{") { node: Token, parser: Parser ->
            val res = TokenDictionary(node)
            if (parser.lexer.peek().symbol != "}") {
                while (true) {
                    res.children.add(parser.expression(0))
                    if (res.children.last().symbol != ":") {
                        throw SyntaxException("Expected key and value", filePath, res.children.last())
                    }
                    if (parser.lexer.peek().symbol != ",") {
                        break
                    }
                    parser.advance(",")
                }
            }
            parser.advance("}")
            res.symbol = "{}"
            res.value = "(DICTIONARY)"
            res
        }

        registry.prefixNud("if") { node: Token, parser: Parser ->
            val res = TokenTernary(node)
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
        registry.stmt("if") { node: Token, parser: Parser ->
            val res = TokenBlock(node)
            parser.advance("(")
            res.children.add(parser.expression(0))
            parser.advance(")")
            parser.lexer.moveAfterTokenLineSeparator()
            res.children.add(parser.block(canBeSingleStatement = true))
            parser.lexer.moveAfterTokenLineSeparator()
            var next = parser.lexer.peek()
            if (next.value == "else") {
                parser.lexer.next()
                parser.lexer.moveAfterTokenLineSeparator()
                next = parser.lexer.peek()
                if (next.value == "if") {
                    res.children.add(parser.statement())
                } else {
                    res.children.add(parser.block(canBeSingleStatement = true))
                }
            }
            res
        }

        registry.stmt("import") { node: Token, parser: Parser ->
            val res = TokenImport(node)
            res.children.add(parser.expression(0))
            if (parser.lexer.peek().value == "as") {
                parser.advance("as")
                res.children.add(parser.expression(0))
                if (!checkIdentifierInImport(res.right)) {
                    throw SyntaxException("Expected non-link identifier after `as` directive", filePath, res.right)
                }
            } else {
                if (!checkIdentifierInImport(res.left)) {
                    throw SyntaxException(
                        "Imports containing folders in name should be declared like:\n" +
                            "`import path as identifier` and used in code with specified identifier",
                        filePath,
                        res
                    )
                }
                res.children.add(Token(res.left.symbol, res.left.value))
            }
            if (res.left is token.Link) {
                checkImportedFolder(res.left as Link, filePath)
            } else if (!checkIdentifierInImport(res.left)) {
                throw SyntaxException("Expected link or identifier before `as` directive", filePath, res.right)
            }
            res
        }

        registry.stmt("class") { node: Token, parser: Parser ->
            val res = TokenType(node)
            val expr = parser.expression(0)
            if (expr.symbol == ":") {
                res.children.addAll(expr.children)
            } else res.children.addAll(listOf(expr, Token("", "")))

            res.children.add(parser.block())
            res
        }

        registry.stmt("object") { node: Token, parser: Parser ->
            val res = TokenObject(node)
            res.children.add(parser.expression(0))

            res.children.add(parser.block())
            res
        }

        registry.stmt("fun") { node: Token, parser: Parser ->
            val res = TokenFunction(node)
            res.children.add(parser.expression(0))
            parser.lexer.moveAfterTokenLineSeparator()
            res.children.add(parser.block())
            res
        }

        registry.stmt("{") { node: Token, parser: Parser ->
            val res = TokenBlock(node)
            res.children.addAll(parser.statements())
            parser.advance("}")
            res
        }

        registry.stmt("while") { node: Token, parser: Parser ->
            val res = TokenBlock(node)
            parser.advance("(")
            res.children.add(parser.expression(0))
            parser.advance(")")
            parser.lexer.moveAfterTokenLineSeparator()
            res.children.add(parser.block(canBeSingleStatement = true))
            res
        }

        registry.stmt("foreach") { node: Token, parser: Parser ->
            val res = TokenBlock(node)
            parser.advance("(")
            res.children.add(parser.expression(0))
            if (res.left !is TokenIdentifier) {
                throw SyntaxException("Expected identifier", filePath, res.left)
            }
            parser.advance("in")
            res.children.add(parser.expression(0))
            parser.advance(")")
            parser.lexer.moveAfterTokenLineSeparator()
            res.children.add(parser.block(canBeSingleStatement = true))
            res
        }

        registry.stmt("break") { node: Token, parser: Parser ->
            if (parser.lexer.peek().symbol != "}") {
                parser.advanceSeparator()
            }
            TokenWordStatement(node)
        }

        registry.stmt("continue") { node: Token, parser: Parser ->
            if (parser.lexer.peek().symbol != "}") {
                parser.advanceSeparator()
            }
            TokenWordStatement(node)
        }

        registry.stmt("return") { node: Token, parser: Parser ->
            val res = TokenWordStatement(node)
            if (parser.lexer.peek().symbol != "}" && !parser.lexer.peekSeparator()) {
                res.children.add(parser.expression(0))
            }
            res
        }

        registry.stmt("#stop") { node: Token, _: Parser -> node }

        return registry
    }

    private fun sequence(node: Token, parser: Parser): Boolean {
        var comma = false
        while (true) {
            node.children.add(parser.expression(0))
            if (parser.lexer.peek().symbol != ",") {
                return comma
            }
            parser.advance(",")
            comma = true
        }
    }

    /**
     * Check if [node] can be used as a child inside [Link][node.Link]
     *
     * First child of [Link][node.Link] can be anything
     */
    private fun isLinkable(node: Token, filePath: String) {
        if (node !is Linkable) {
            throw TokenExpectedTypeException(listOf(Identifier::class, Invocation::class, Index::class), filePath, node)
        }
        var index = node
        while (index is TokenIndex)
            index = index.left
        if (index !is Linkable) {
            throw TokenExpectedTypeException(
                listOf(Identifier::class, Invocation::class, Index::class),
                filePath,
                index,
                index
            )
        }
    }

    private fun checkImportedFolder(link: Link, filePath: String) {
        for (ident in link.children)
            if (!checkIdentifierInImport(ident)) {
                throw SyntaxException("Each folder should be represented as identifier", filePath, ident)
            }
    }

    private fun checkIdentifierInImport(node: Token): Boolean = node is TokenIdentifier || node.children.size == 0

    private fun registerLink(
        node: Link,
        parser: Parser,
        left: Token,
        filePath: String,
        nullable: Boolean = false
    ): Token {
        if (nullable) {
            node.nullable.add(1)
        }
        node.children.add(left)
        node.children.add(parser.expression(105))
        isLinkable(node.children.last(), filePath)
        var t = parser.lexer.peek()
        var i = 2
        while (t.symbol == "(LINK)") {
            parser.advance("(LINK)")
            if (t.value == "?.") {
                node.nullable.add(i)
            }
            node.children.add(parser.expression(105))
            t = parser.lexer.peek()
            isLinkable(node.children.last(), filePath)
            i++
        }
        return node
    }
}
