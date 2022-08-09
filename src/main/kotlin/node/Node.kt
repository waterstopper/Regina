package node

abstract class Node() {
    open fun evaluate() {}
}

class Ternary(val condition: Node, val ifTrue: Node, val ifFalse: Node) {}
open class Assignment(assigned: Node, expression: Node) {}
class PropertyAssignment(assigned: Node, expression: Node) : Assignment(assigned, expression) {}
class Link(elements: List<Node>) : Node() {}

open class PosNode(val position: Pair<Int, Int>) : Node() {}

class Identifier(val name: String, position: Pair<Int, Int>) : PosNode(position) {}

open class Block(val statements: List<Node>, position: Pair<Int, Int>) : PosNode(position) {}
class IfBlock(condition: Node, statements: List<Node>, position: Pair<Int, Int>) : Block(statements, position) {}
class WhileBlock(condition: Node, statements: List<Node>, position: Pair<Int, Int>) : Block(statements, position) {}

abstract class Operator(position: Pair<Int, Int>) : PosNode(position) {}

class Index(val indexed: Node, val index: Node, position: Pair<Int, Int>) : Operator(position) {}
class BinaryOperator(val function: (Node, Node) -> Any, val left: Node, val right: Node, position: Pair<Int, Int>) :
    Operator(position) {}

class IsOperator(val isNot: Boolean, position: Pair<Int, Int>) : Operator(position) {}
class UnaryOperator(val function: (Node) -> Any, val operand: Node, position: Pair<Int, Int>) : Operator(position) {}

class Import(val fileName: String, val importName: String, position: Pair<Int, Int>) : PosNode(position) {}