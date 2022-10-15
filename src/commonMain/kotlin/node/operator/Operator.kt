package node.operator

import lexer.PositionalException
import node.Node
import properties.primitive.PNumber
import table.SymbolTable
import utils.Utils.NULL
import utils.Utils.toPInt
import utils.Utils.toVariable

open class Operator(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    children: List<Node>
) : Node(symbol, value, position, children.toMutableList()) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        return when (value) {
            "+" -> left.evaluate(symbolTable).plus(right.evaluate(symbolTable), this, symbolTable)
            "==" -> left.evaluate(symbolTable).eq(right.evaluate(symbolTable)).toPInt()
            "!=" -> left.evaluate(symbolTable).neq(right.evaluate(symbolTable)).toPInt()
            "??" -> nullCoalesce(symbolTable)
            else -> throw PositionalException(
                "Operator `$value` not implemented",
                symbolTable.getFileTable().filePath,
                this
            )
        }
    }

    private fun Any.plus(other: Any, node: Node, symbolTable: SymbolTable): Any {
        if (this is MutableList<*>) {
            return if (other is MutableList<*>) {
                val res = this.toMutableList()
                res.addAll(other)
                res
            } else {
                val res = this.toMutableList()
                res.add(other.toVariable())
                res
            }
        }
        if (this is String) {
            return this.toString() + other.toString()
        }
        if (this is PNumber && other is PNumber) {
            // to make MAX_VALUE + n equal to MIN_VALUE + n - 1
            return this + other
        } else throw PositionalException(
            "Operator not applicable to operands",
            symbolTable.getFileTable().filePath,
            node
        )
    }

    private fun Any.eq(other: Any): Boolean {
        if (this is PNumber && other is PNumber) {
            return this == other
        }
        if (this is MutableList<*> && other is MutableList<*>) {
            if (this.size != other.size) {
                return false
            }
            var res = true
            this.forEachIndexed { index, _ ->
                if (!this[index]!!.eq(other[index]!!)) {
                    res = false
                }
            }
            return res
        }
        if (this is MutableMap<*, *> && other is MutableMap<*, *>) {
            if (this.size != other.size) {
                return false
            }
            var res = true
            this.forEach { (key, value) ->
                if (!other.contains(key)) res = false
                else if (!value!!.eq(other[key]!!)) res = false
            }
            return res
        }
        return this == other
    }

    private fun Any.neq(other: Any) = !this.eq(other)

    private fun nullCoalesce(symbolTable: SymbolTable): Any {
        val leftEvaluated = left.evaluate(symbolTable)
        return if (leftEvaluated == NULL) right.evaluate(symbolTable) else leftEvaluated
    }
}
