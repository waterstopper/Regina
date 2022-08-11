package lexer

//import readFile
import node.Identifier
import node.Link
import node.Node
import node.TokenFactory.changeInvocationOnSecondPositionInLink
import node.TokenFactory.createSpecificInvocation
import node.invocation.Call
import node.invocation.Invocation
import node.statement.Assignment
import properties.Function
import properties.Type
import table.FileTable
import table.SymbolTable
import utils.Utils.subList
import java.io.File

/**
 * Performs basic semantic analysis and creates symbol table for future evaluation
 */
//class SemanticAnalyzer(private val fileName: String, private val nodes: List<Node>) {
//    val files = mutableMapOf<String, FileTable>()
//
//    fun analyze(): List<Node> {
//        println("Analyzing: `$fileName`")
//        createAssociations()
//        changeIdentTokens()
//        return nodes
//    }
//
//    private fun createAssociations() {
//        globalTable.addFile(fileName)
//        globalTable = globalTable.changeFile(fileName)
//        for (token in nodes)
//            when (token.symbol) {
//                "fun" -> globalTable.addFunction(FunctionFactory.createFunction(token))
//                "class" -> {
//                    if (declarations[fileName] == null)
//                        declarations[fileName] = mutableListOf(token as Declaration)
//                    else declarations[fileName]!!.add(token as Declaration)
//                    globalTable.addType(token)
//                }
//                "object" -> globalTable.addObject(token)
//                "import" -> {
//                    if (token !is ImportNode)
//                        throw PositionalException("")
//                    if (globalTable.getImportOrNullByFileName(token.left.value) == null) {
//                        val isNewFile = globalTable.addFile(token.left.value)
//                        globalTable.addImport(token.left, token.right)
//                        if (isNewFile) {
//                            readFile(token.left.value)
//                            globalTable = globalTable.changeFile(fileName)
//                        }
//                    } else throw PositionalException("Same import found above", token.left)
//                }
//                else ->
//                    throw PositionalException("Only class, object or function can be top level declaration", token)
//            }
//    }
//
//    private fun changeIdentTokens() {
//        for (token in nodes) {
//            var table = globalTable.copy()
//            when (token.symbol) {
//                "fun" -> table = changeTableForFunctionAnalysis(token, table.changeScope())
//                "object" -> table = table.changeVariable(globalTable.getObjectOrNull((token as Declaration).name)!!)
//                "class" -> {
//                    table = table.changeVariable(globalTable.getTypeOrNull((token as Declaration).name)!!)
//                    table.addVariable("this", PString("", null))
//                }
//            }
//            changeInvocationType(token, table)
//        }
//    }
//
//    private fun changeTableForFunctionAnalysis(functionNode: Node, table: SymbolTable): SymbolTable {
//        val args = functionNode.left.children.subList(1)
//        for (arg in args)
//            when (arg) {
//                is Assignment -> table.addVariableOrNot(arg.left)
//                is Identifier -> table.addVariableOrNot(arg)
//                // else -> throw PositionalException("Expected assignment or identifier", arg)
//            }
//        return table
//    }
//
//    private fun changeInvocationType(node: Node, symbolTable: SymbolTable) {
//        when (node) {
//            is Declaration -> {
//                for ((index, child) in node.children.withIndex()) {
//                    if (child is Invocation) {
//                        if (node.symbol != "fun")
//                            createSpecificInvocation(child, symbolTable, node, index)
//                    }
//                }
//                if (node.symbol == "fun") {
//                    for (child in node.children)
//                        changeInvocationType(child, changeTableForFunctionAnalysis(node, symbolTable.changeScope()))
//                    return
//                }
//            }
//            is Link -> {
//                if (node.left is Invocation) createSpecificInvocation(node.left, symbolTable, node, 0)
//                // the only case in Link when Invocation might be a Constructor
//                if (node.right is Invocation) {
//                    if (node.left is Identifier) {
//                        // symbolTable.addVariableOrNot(token.left)
//                        changeInvocationOnSecondPositionInLink(symbolTable, node)
//                    } else node.children[1] = Call(node.right)
//                }
//                for ((index, child) in node.children.subList(2).withIndex())
//                    if (child is Invocation) {
//                        node.children[index + 2] = Call(child)
//                    }
//            }
//            else -> {
//                if (node is Assignment) {
//                    if (node.left !is Assignable)
//                        throw PositionalException("Left operand is not assignable", node.left)
//                    symbolTable.addVariableOrNot(node.left)
//                }
//                for ((index, child) in node.children.withIndex()) {
//                    if (child is Invocation) {
//                        //checkParamsOrArgs(child.children.subList(1), node.symbol != "fun")
//                        if (node.symbol != "fun")
//                            createSpecificInvocation(child, symbolTable, node, index)
//                    }
//                }
//            }
//        }
//        for (child in node.children)
//            changeInvocationType(child, symbolTable)
//    }
//
//    companion object {
//        private var declarations: MutableMap<String, MutableList<Declaration>> = mutableMapOf()
//
//        fun initializeSuperTypes() {
//            val initialFileTable = globalTable.getFileTable()
//            for ((fileName, tokenList) in declarations) {
//                val currentTable = globalTable.changeFile(fileName).getFileTable()
//                for (token in tokenList) {
//                    if (token.supertype.symbol == "")
//                        continue
//                    val supertypeTable = if (token.supertype is Link)
//                        globalTable.getImport(token.supertype.left)
//                    else currentTable
//                    val supertype =
//                        if (token.supertype is Link) supertypeTable.getType(token.supertype.right)
//                        else supertypeTable.getType(token.supertype)
//                    currentTable.getUncopiedType(token.name).supertype = supertype
//                }
//            }
//            globalTable.changeFile(initialFileTable)
//        }
//
//        fun clearAnalyzer() {
//            declarations = mutableMapOf()
//        }
//    }
//}

fun getNodes(fileName: String): List<Node> {
    val code = File(fileName).readText()
    return Parser(code).statements().map { it.toNode() }
}

fun initializeSuperTypes(superTypes: Map<Type, Node?>) {
    for ((type, node) in superTypes) {
        if (node == null)
            continue
        val superType = when (node) {
            is Identifier -> superTypes.filter { (t, _) ->
                t.fileTable == type.fileTable && t.name == node.value
            }
            is Link -> {
                if (node.children.size != 2)
                    throw PositionalException("Expected imported file name and type name", node)
                val importedFileTable = type.fileTable.getImportOrNull(node.left.value)
                    ?: throw  PositionalException("Import not found", node.left)
                superTypes.filter { (t, _) ->
                    t.fileTable == importedFileTable && t.name == node.right.value
                }
            }
            else -> throw PositionalException("Expected identifier or link", node)
        }
        if (superType.size != 1)
            throw PositionalException("One super type not found", node)
        type.supertype = superType.keys.first()
    }
}

fun analyzeSemantics(startingFileName: String, nodes: List<Node> = getNodes(startingFileName)): FileTable {
    val importGraphCreator = ImportGraphCreator(startingFileName, nodes)
    initializeSuperTypes(importGraphCreator.supertypes)
    for (fileTable in importGraphCreator.visitedTables)
        Analyzer(fileTable)
    return importGraphCreator.visitedTables.first()
}

class Analyzer(fileTable: FileTable) {
    init {
        for (type in fileTable.getTypes().values + fileTable.getObjects()) {
            analyzeType(type, fileTable)
        }
        for (function in fileTable.getFunctions()) {
            val table = addFunctionParametersToTable(function, SymbolTable(fileTable = fileTable))
            changeInvocationType(function.body, table)
        }
    }

    private fun analyzeType(type: Type, fileTable: FileTable) {
        // TODO check type with a parent property or property that is initialized through parent
        val table = SymbolTable(fileTable = fileTable)
        table.addVariable("this", type)
        for (assignment in type.assignments)
            table.addVariableOrNot(assignment.left)
        for (assignment in type.assignments)
            changeInvocationType(assignment, table.copy())

        for (function in type.functions) {
            val functionTable = addFunctionParametersToTable(function, table.copy())
            changeInvocationType(function.body, functionTable)
        }
    }

    private fun changeInvocationType(node: Node, symbolTable: SymbolTable) {
        for ((index, child) in node.children.withIndex()) {
            when (child) {
                is Assignment -> symbolTable.addVariableOrNot(child.left)
                is Invocation -> createSpecificInvocation(child, symbolTable, node, index)
                is Link -> changeInvocationsInLink(child, symbolTable)
            }

        }
        for (child in node.children)
            if (child !is Link)
                changeInvocationType(child, symbolTable)
    }

    private fun changeInvocationsInLink(node: Link, symbolTable: SymbolTable) {
        if (node.left is Invocation) createSpecificInvocation(node.left, symbolTable, node, 0)
        if (node.right is Invocation) {
            // the only case in Link when Invocation might be a Constructor
            if (node.left is Identifier)
                changeInvocationOnSecondPositionInLink(symbolTable, node)
            else {
                node.children[1] = Call(node.right)
                return changeInvocationType(node.left, symbolTable)
            }
        }
        for ((index, child) in node.children.subList(2).withIndex())
            if (child is Invocation)
                node.children[index + 2] = Call(child)
    }

    private fun addFunctionParametersToTable(function: Function, table: SymbolTable): SymbolTable {
        for (param in function.nonDefaultParams)
            table.addVariableOrNot(param)
        for (defaultParam in function.defaultParams)
            table.addVariableOrNot(defaultParam.left)
        return table
    }
}