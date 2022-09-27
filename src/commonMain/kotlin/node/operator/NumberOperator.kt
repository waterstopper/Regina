package node.operator

import lexer.PositionalException
import node.Node
import properties.primitive.PInt
import properties.primitive.PNumber
import table.FileTable
import table.SymbolTable
import utils.Utils.FALSE
import utils.Utils.TRUE
import utils.Utils.getPNumber
import utils.Utils.toPInt

class NumberOperator(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    children: List<Node>
) : Operator(symbol, value, position, children.toMutableList()) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        if (children.size == 1) {
            return when (symbol) {
                "-" -> getPNumber(symbolTable, left).unaryMinus()
                "!" -> getPNumber(symbolTable, left).not()
                else -> throw PositionalException(
                    "Prefix arithmetic operator not implemented",
                    symbolTable.getFileTable().filePath,
                    this
                )
            }
        }
        when (value) {
            "&&" -> return evaluateLogicOperators(FALSE, { a: PNumber -> a.getPValue() != 0 }, symbolTable)
            "||" -> return evaluateLogicOperators(TRUE, { a: PNumber -> a.getPValue() == 0 }, symbolTable)
        }
        val a = getPNumber(symbolTable, left)
        val b = getPNumber(symbolTable, right)
        return when (value) {
            ">" -> (a > b).toPInt()
            "<" -> (a < b).toPInt()
            ">=" -> (a >= b).toPInt()
            "<=" -> (a <= b).toPInt()
            // never happens, because // is for comments
            // "//" -> a / b
            else -> evaluateArithmeticOperators(a, b, this, symbolTable.getFileTable())
        }
    }

    private fun evaluateLogicOperators(
        afterFirstReturned: PInt,
        toSecondCondition: (first: PNumber) -> Boolean,
        symbolTable: SymbolTable
    ): PInt {
        val a = getPNumber(symbolTable, left)
        return if (toSecondCondition(a)) {
            val b = getPNumber(symbolTable, right)
            (b.getPValue() != 0).toPInt()
        } else afterFirstReturned
    }

    private fun evaluateArithmeticOperators(a: PNumber, b: PNumber, node: Node, fileTable: FileTable): PNumber {
        return when (node.symbol) {
            "-" -> a - b
            "*" -> a * b
            "/" -> a / b
            "%" -> a % b
            else -> throw PositionalException("Operator `${node.symbol}` not implemented", fileTable.filePath, node)
        }
    }
}
