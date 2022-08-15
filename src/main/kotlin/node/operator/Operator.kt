package node.operator

import lexer.PositionalException
import node.Node
import properties.primitive.PNumber
import table.SymbolTable
import utils.Utils.toInt
import utils.Utils.toVariable

open class Operator(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    children: List<Node>
) : Node(symbol, value, position, children.toMutableList()) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        return when (value) {
            "+" -> left.evaluate(symbolTable).plus(right.evaluate(symbolTable), this)
            "==" -> left.evaluate(symbolTable).eq(right.evaluate(symbolTable)).toInt()
            "!=" -> left.evaluate(symbolTable).neq(right.evaluate(symbolTable)).toInt()
            else -> throw PositionalException("Operator `$value` not implemented", this)
        }
    }

    private fun Any.plus(other: Any, node: Node): Any {
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
        if (this is String || other is String)
            return this.toString() + other.toString()
        if (this is Double && other is Number || this is Number && other is Double)
            return this.toString().toDouble() + other.toString().toDouble()
        if (this is Int && other is Int)
            return this + other
        else throw PositionalException("Operator not applicable to operands", node)
    }

    private fun Any.eq(other: Any): Boolean {
        if (this is Number && other is Number)
            return this.toDouble() == other.toDouble()
        if (this is PNumber && other is PNumber)
            return this.getPValue().toDouble() == other.getPValue().toDouble()
        if (this is MutableList<*> && other is MutableList<*>) {
            if (this.size != other.size)
                return false
            var res = true
            this.forEachIndexed { index, _ ->
                if (!this[index]!!.eq(other[index]!!))
                    res = false
            }
            return res
        }
        if (this is MutableMap<*, *> && other is MutableMap<*, *>) {
            if (this.size != other.size)
                return false
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
}
