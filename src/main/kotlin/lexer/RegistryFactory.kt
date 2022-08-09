package lexer

import node.*
import node.invocation.Invocation
import node.operator.Index
import node.operator.NodeTernary
import node.statement.Block
import node.statement.WordStatement
import node.variable.NodeArray
import node.variable.NodeDictionary
import node.variable.NodeNumber

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

        registry.prefixNud("false") { node: Node, _: Parser ->
            NodeNumber("0", node.position)
        }

        registry.prefixNud("true") { node: Node, _: Parser ->
            NodeNumber("1", node.position)
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

        registry.infixLed(".", 105) { node: Node, parser: Parser, left: Node ->
            node.children.add(left)
            node.children.add(parser.expression(105))
            isLinkable(node.children.last())
            var t = parser.lexer.peek()
            while (t.symbol == "(LINK)") {
                parser.advance("(LINK)")
                node.children.add(parser.expression(105))
                t = parser.lexer.peek()
                isLinkable(node.children.last())
            }
            node
        }

        // function use
        registry.infixLed("(", 120) { node: Node, parser: Parser, left: Node ->
            if (left.symbol != "(LINK)" && left.symbol != "(IDENT)" && left.symbol != "[" &&
                left.symbol != "(" && left.symbol != "!"
            )
                throw PositionalException("`$left` is not invokable", left)
            node.children.add(left)
            val t = parser.lexer.peek()
            if (t.symbol != ")") {
                sequence(node, parser)
                parser.advance(")")
            } else
                parser.advance(")")
            node
        }

        // array indexing
        registry.infixLed("[", 110) { node: Node, parser: Parser, left: Node ->
            val res = Index(node)
            res.children.add(left)
            val t = parser.lexer.peek()
            if (t.symbol != "]") {
                sequence(res, parser)
                parser.advance("]")
            } else throw PositionalException("Expected index", t)
            res
        }

        // arithmetic and redundant parentheses
        registry.prefixNud("(") { node: Node, parser: Parser ->
            var comma = false
            if (parser.lexer.peek().symbol != ")")
                comma = sequence(node, parser)
            parser.advance(")")
            if (comma)
                throw PositionalException("Tuples are not implemented", node)
            else if (node.children.size == 0)
                throw  PositionalException("Empty parentheses", node)
            /* Return first child when parentheses are redundant e.g. condition for `if` or `while`
               or if parentheses are inside arithmetic expression. Then, this will return an expression inside them */
            else
                node.children[0]
        }

        registry.prefixNud("[") { node: Node, parser: Parser ->
            val res = NodeArray(node)
            if (parser.lexer.peek().symbol != "]")
                sequence(res, parser)
            parser.advance("]")
            res.symbol = "[]"
            res.value = "(ARRAY)"
            res
        }

        registry.prefixNud("{") { node: Node, parser: Parser ->
            val res = NodeDictionary(node)
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

        registry.prefixNud("if") { node: Node, parser: Parser ->
            val res = NodeTernary(node)
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
        registry.stmt("if") { node: Node, parser: Parser ->
            val res = Block(node)
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

        registry.stmt("import") { node: Node, parser: Parser ->
            val res = Declaration(node)
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
                res.children.add(Node(res.left.symbol, res.left.value))
            }
            if (res.left is Link)
                checkImportedFolder(res.left as Link)
            else if (!checkIdentifierInImport(res.left))
                throw PositionalException("Expected link or identifier before `as` directive", res.right)
            res
        }

        registry.stmt("class") { node: Node, parser: Parser ->
            val res = Declaration(node)
            val expr = parser.expression(0)
            if (expr.symbol == ":") {
                res.children.addAll(expr.children)
            } else res.children.addAll(listOf(expr, Node("", "")))
            
            res.children.add(parser.block())
            res
        }

        registry.stmt("object") { node: Node, parser: Parser ->
            val res = Declaration(node)
            res.children.add(parser.expression(0))
            
            res.children.add(parser.block())
            res
        }

        registry.stmt("fun") { node: Node, parser: Parser ->
            val res = Declaration(node)
            res.children.add(parser.expression(0))
            res.children.add(parser.block())
            res
        }

        registry.stmt("{") { node: Node, parser: Parser ->
            val res = Block(node)
            res.children.addAll(parser.statements())
            parser.advance("}")
            res
        }

        registry.stmt("while") { node: Node, parser: Parser ->
            val res = Block(node)
            res.children.add(parser.expression(0))
            res.children.add(parser.block(canBeSingleStatement = true))
            res
        }

        registry.stmt("break") { node: Node, parser: Parser ->
            if (parser.lexer.peek().symbol != "}")
                parser.advanceSeparator()
            WordStatement(node)
        }

        registry.stmt("continue") { node: Node, parser: Parser ->
            if (parser.lexer.peek().symbol != "}")
                parser.advanceSeparator()
            WordStatement(node)
        }

        registry.stmt("return") { node: Node, parser: Parser ->
            val res = WordStatement(node)
            if (parser.lexer.peek().symbol != "}" && !parser.lexer.peekSeparator())
                res.children.add(parser.expression(0))
            res
        }

        registry.stmt("#stop") { node: Node, _: Parser -> node }

        return registry
    }

    private fun sequence(node: Node, parser: Parser): Boolean {
        var comma = false
        while (true) {
            node.children.add(parser.expression(0))
            if (parser.lexer.peek().symbol != ",")
                return comma
            parser.advance(",")
            comma = true
        }
    }

    /**
     * Check if [node] can be used as a child inside [Link][node.Link]
     *
     * First child of [Link][node.Link] can be anything
     */
    private fun isLinkable(node: Node) {
        if (node !is Linkable)
            throw ExpectedTypeException(listOf(Identifier::class, Invocation::class, Index::class), node, node)
        var index = node
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

    private fun checkIdentifierInImport(node: Node): Boolean = node is Identifier || node.children.size == 0
}
