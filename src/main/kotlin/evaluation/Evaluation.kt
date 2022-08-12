package evaluation

import lexer.Parser
//import lexer.SemanticAnalyzer.Companion.clearAnalyzer
import lexer.analyzeSemantics
import table.SymbolTable
//import table.SymbolTable.Companion.clearTable

/**
 * Facade class for language execution
 */
object Evaluation {
    /**
     * Enabling training wheels will run code slower but will give meaningful feedback if something is wrong.
     */
    var trainingWheels = true
    var globalTable = SymbolTable(resolvingType = false)

    fun eval(code: String) {
//        val statements = SemanticAnalyzer("@NoFile", Parser(code).statements().map { it.toNode() }).analyze()
//        SemanticAnalyzer.initializeSuperTypes() // TODO do before analyzing file, but after imports.
        val fileTable = analyzeSemantics("@NoFile", Parser(code).statements().map { it.toNode() })//globalTable.getMain()
        fileTable.getMain().body.evaluate(SymbolTable(fileTable = fileTable, resolvingType = false))
//        for (stat in statements)
//            stat.evaluate(symbolTable) // TODO here use some default symbol table
      //  clear()
    }

    fun evaluate(fileName: String) {
//        val code = File(if (fileName.contains(".")) fileName else "$fileName.redi").readText()
//        val sts = SemanticAnalyzer(fileName, Parser(code).statements().map { it.toNode() }).analyze()
//
//        SemanticAnalyzer.initializeSuperTypes()
        val fileTable = analyzeSemantics(fileName)//globalTable.getMain()
        fileTable.getMain().body.evaluate(SymbolTable(fileTable = fileTable, resolvingType = false))
        //clear()
    }

//    fun clear() {
//        clearTable()
//        clearAnalyzer()
//    }
}
