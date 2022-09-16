package node.statement

import lexer.PositionalException
import node.Node
import node.invocation.Call
import properties.primitive.Indexable
import properties.primitive.PDictionary
import properties.primitive.PInt
import properties.primitive.Primitive
import table.SymbolTable
import utils.Utils.toBoolean
import utils.Utils.toVariable

class Block(node: Node) :
    Node(node.symbol, node.value, node.position, node.children) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        return when (symbol) {
            "{" -> evaluateBlock(symbolTable)
            "if" -> evaluateConditional(symbolTable)
            "while" -> evaluateCycle(symbolTable)
            "foreach" -> evaluateForeach(symbolTable)
            else -> throw PositionalException("Not a block", symbolTable.getFileTable().filePath, this)
        }
    }

    /**
     * children[0] is always an identifier [RegistryFactory] is responsible
     * children[1] is iterable
     * children[2] is a block
     * Method with duplicated code, but it is understandable
     */
    private fun evaluateForeach(symbolTable: SymbolTable): Any {
        var (iterable, isRange) = getIterable(symbolTable)
        if (isRange) {
            iterable = (iterable as List<PInt>).map { it.getPValue() }
            for (i in (if (iterable[0] < iterable[1]) iterable[0]..iterable[1] else iterable[0] downTo iterable[1])
                    step iterable[2]) {
                symbolTable.addVariable(left.value, PInt(i).toVariable(left))
                when (val res = children[2].evaluate(symbolTable)) {
                    CycleStatement.CONTINUE -> continue
                    CycleStatement.BREAK -> break
                    !is Unit -> return res
                }
            }
        } else
            for (i in iterable) {
                symbolTable.addVariable(left.value, i!!.toVariable(left))
                when (val res = children[2].evaluate(symbolTable)) {
                    CycleStatement.CONTINUE -> continue
                    CycleStatement.BREAK -> break
                    !is Unit -> return res
                }
            }
        return Unit
    }

    private fun getIterable(symbolTable: SymbolTable): Pair<Iterable<*>, Boolean> {
        var iterable: Any = right.evaluate(symbolTable).toVariable(right)
        if (right is Call && (right as Call).name.value == "range")
            return Pair((iterable as Primitive).getPValue() as Iterable<*>, true)
        if (iterable !is Indexable || iterable is PDictionary)
            throw PositionalException(
                "Expected array, string or range",
                symbolTable.getFileTable().filePath,
                right
            )
        iterable = (iterable as Primitive).getPValue()
        if (iterable is String)
            iterable = iterable.map { it.toString() }
        return Pair(iterable as Iterable<*>, false)
    }

    private fun evaluateCycle(symbolTable: SymbolTable): Any {
        val condition = left
        val block = right
        while (condition.evaluate(symbolTable).toBoolean(condition, symbolTable.getFileTable())) {
            when (val res = block.evaluate(symbolTable)) {
                CycleStatement.CONTINUE -> continue
                CycleStatement.BREAK -> break
                !is Unit -> {
                    return res
                }
            }
        }
        return Unit
    }

    private fun evaluateConditional(symbolTable: SymbolTable): Any {
        val condition = left
        val trueBlock = right
        if (condition.evaluate(symbolTable).toBoolean(condition, symbolTable.getFileTable()))
            return trueBlock.evaluate(symbolTable)
        else if (children.size == 3)
            return children[2].evaluate(symbolTable)
        return Unit
    }

    private fun evaluateBlock(symbolTable: SymbolTable): Any {
        for (token in children) {
            if (token is Block) {
                if (token.value == "{")
                    throw PositionalException(
                        "Block within a block. Maybe `if`, `else` or `while` was omitted?",
                        symbolTable.getFileTable().filePath,
                        token
                    )
                val res = token.evaluate(symbolTable)
                if (res !is Unit)
                    return res
            } else when (token.symbol) {
                "return" -> {
                    return if (token.children.size == 0)
                        Unit
                    else token.left.evaluate(symbolTable)
                }
                "break" -> return CycleStatement.BREAK
                "continue" -> return CycleStatement.CONTINUE
                else -> token.evaluate(symbolTable)
            }
        }
        return Unit
    }

    enum class CycleStatement {
        BREAK,
        CONTINUE
    }
}
