package delete

import table.SymbolTable

abstract class Delete(val position: Pair<Int, Int>) {
    open fun evaluate(symbolTable: SymbolTable): Any {
        throw NotImplementedError()
    }
}

class Ternary(val condition: Delete, val ifTrue: Delete, val ifFalse: Delete, position: Pair<Int, Int>) :
    Delete(position)

open class Assignment(val assigned: Delete, val expression: Delete, position: Pair<Int, Int>) : Delete(position)
class PropertyAssignment(assigned: Delete, expression: Delete, position: Pair<Int, Int>) :
    Assignment(assigned, expression, position)

class Link(elements: List<Delete>, position: Pair<Int, Int>) : Delete(position)

class Identifier(val name: String, position: Pair<Int, Int>) : Delete(position)

open class Block(val statements: List<Delete>, position: Pair<Int, Int>) : Delete(position)
class IfBlock(condition: Delete, statements: List<Delete>, position: Pair<Int, Int>) : Block(statements, position)
class WhileBlock(condition: Delete, statements: List<Delete>, position: Pair<Int, Int>) : Block(statements, position)

abstract class Operator(position: Pair<Int, Int>) : Delete(position)

class Index(val indexed: Delete, val index: Delete, position: Pair<Int, Int>) : Operator(position)
class BinaryOperator(
    val function: (Delete, Delete) -> Any,
    val left: Delete,
    val right: Delete,
    position: Pair<Int, Int>
) :
    Operator(position)

class IsOperator(val isNot: Boolean, position: Pair<Int, Int>) : Operator(position)
class UnaryOperator(val function: (Delete) -> Any, val operand: Delete, position: Pair<Int, Int>) :
    Operator(position)

class Import(val fileName: String, val importName: String, position: Pair<Int, Int>) : Delete(position)