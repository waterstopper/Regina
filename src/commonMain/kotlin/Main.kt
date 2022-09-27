import evaluation.Evaluation.evaluate
import lexer.Analyzer
import lexer.ImportGraphCreator
import lexer.PathBuilder.getNodes
import lexer.initializeSuperTypes
import node.invocation.ResolvingMode
import table.FileTable
import table.SymbolTable

fun main(args: Array<String>) {
    // eval(args[0], args.toList().subList(1, args.size))
    evaluate("a", args.toList().subList(1, args.size))
    val igc = ImportGraphCreator("a", getNodes("a"), listOf())
    // after

    // useful
    createGraphJS(igc)
    addNextImportJS(igc, "a")
    requestNextImportJS(igc)
}

fun createGraphJS(importGraphCreator: ImportGraphCreator) {
    importGraphCreator.visitedTables.add(
        FileTable(
            importGraphCreator.mainFileName,
            importGraphCreator.imports.size + 1
        )
    )
    importGraphCreator.imports[importGraphCreator.mainFileName] = importGraphCreator.visitedTables.last()
    importGraphCreator.addDeclarationsToFileTable(
        importGraphCreator.visitedTables.first(),
        importGraphCreator.startingNodes
    )
}

fun addNextImportJS(importGraphCreator: ImportGraphCreator, fileName: String) {
    val nodes = getNodes(fileName)
    val nextFileTable = importGraphCreator.importStack.removeLast()
    importGraphCreator.visitedTables.add(nextFileTable)
    importGraphCreator.addDeclarationsToFileTable(nextFileTable, nodes)
}

fun requestNextImportJS(importGraphCreator: ImportGraphCreator) {
    if (importGraphCreator.importStack.isNotEmpty()) {
        sendMessage(Message("import", importGraphCreator.importStack.last().filePath))
    } else startEvaluationJS(importGraphCreator)
}

fun startEvaluationJS(igc: ImportGraphCreator) {
    initializeSuperTypes(igc.supertypes)
    for (fileTable in igc.visitedTables)
        Analyzer(fileTable)
    igc.visitedTables.first().getMain().body.evaluate(
        SymbolTable(
            fileTable = igc.visitedTables.first(),
            resolvingType = ResolvingMode.FUNCTION
        )
    )
    sendMessage(Message("finished", ""))
}
