package node.statement

import lexer.PositionalException
import node.Node
import table.SymbolTable
import utils.Utils.toBoolean

class Block(node: Node) :
    Node(node.symbol, node.value, node.position, node.children) {
    constructor(position: Pair<Int, Int>) : this(Node("{", "{", position))

    override fun evaluate(symbolTable: SymbolTable): Any {
        return when (symbol) {
            "{" -> evaluateBlock(symbolTable)
            "if" -> evaluateConditional(symbolTable)
            "while" -> evaluateCycle(symbolTable)
            else -> throw PositionalException("Not a block", symbolTable.getFileTable().filePath, this)
        }
    }

    private fun evaluateCycle(symbolTable: SymbolTable): Any {
        cycles++
        val condition = left
        val block = right
        while (condition.evaluate(symbolTable).toBoolean(condition,symbolTable.getFileTable())) {
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
        if (condition.evaluate(symbolTable).toBoolean(condition,symbolTable.getFileTable()))
            return trueBlock.evaluate(symbolTable)
        else if (children.size == 3)
            return children[2].evaluate(symbolTable)
        return Unit
    }

    private fun evaluateBlock(symbolTable: SymbolTable): Any {
        for (token in children) {
            if (token is Block) {
                if (token.value == "{")
                    throw PositionalException("Block within a block. Maybe `if`, `else` or `while` was omitted?", symbolTable.getFileTable().filePath,token)
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
                    "break out of cycle",symbolTable.getFileTable().filePath,
                    token
                )
                "continue" -> if (cycles > 0) return CycleStatement.CONTINUE else throw PositionalException(
                    "continue out of cycle",symbolTable.getFileTable().filePath,
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
