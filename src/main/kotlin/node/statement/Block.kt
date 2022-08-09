package node.statement

import lexer.PositionalException
import table.SymbolTable
import node.Node
import utils.Utils.toBoolean

class Block(node: Node) :
    Node(node.symbol, node.value, node.position, node.bindingPower, node.nud, node.led, node.std) {
    constructor(position: Pair<Int, Int>) : this(Node("{", "{", position))

    override fun evaluate(symbolTable: SymbolTable): Any {
        return when (symbol) {
            "{" -> evaluateBlock(symbolTable)
            "if" -> evaluateConditional(symbolTable)
            "while" -> evaluateCycle(symbolTable)
            else -> throw PositionalException("Not a block", this)
        }
    }

    private fun evaluateCycle(symbolTable: SymbolTable): Any {
        cycles++
        val condition = left
        val block = right
        while (condition.evaluate(symbolTable).toBoolean(condition)) {
            when (val res = block.evaluate(symbolTable)) {
                CycleStatement.CONTINUE -> continue
                CycleStatement.BREAK -> break
                !is Unit -> {
                    cycles--
                    return res
                }
            }
        }
        cycles--
        return Unit
    }

    private fun evaluateConditional(symbolTable: SymbolTable): Any {
        val condition = left
        val trueBlock = right
        if (condition.evaluate(symbolTable).toBoolean(condition))
            return trueBlock.evaluate(symbolTable)
        else if (children.size == 3)
            return children[2].evaluate(symbolTable)
        return Unit
    }

    private fun evaluateBlock(symbolTable: SymbolTable): Any {
        for (token in children) {
            if (token is Block) {
                if (token.value == "{")
                    throw PositionalException("Block within a block. Maybe `if`, `else` or `while` was omitted?", token)
                val res = token.evaluate(symbolTable)
                if (res !is Unit)
                    return res
            } else when (token.symbol) {
                "return" -> {
                    return if (token.children.size == 0)
                        Unit
                    else token.left.evaluate(symbolTable)
                }
                "break" -> if (cycles > 0) return CycleStatement.BREAK else throw PositionalException(
                    "break out of cycle",
                    token
                )
                "continue" -> if (cycles > 0) return CycleStatement.CONTINUE else throw PositionalException(
                    "continue out of cycle",
                    token
                )
                else -> token.evaluate(symbolTable)
            }
        }
        return Unit
    }

    enum class CycleStatement {
        BREAK,
        CONTINUE
    }

    companion object {
        var cycles = 0
    }
}
