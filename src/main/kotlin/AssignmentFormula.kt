// every formula is assignment formula
class AssignmentFormula(content: String, val propertyName: String, val parent: OldContainer) : Formula(content) {
    override fun evaluate(): OldNode {
        val res = super.evaluate()
        if (res is String && TreeBuilder.definitions.any { it.name == res })
            return TreeBuilder.definitions.find { it.name == res }!!.copy(propertyName, parent, res)

        return OldProperty(propertyName, parent, res)
    }

    fun canEvaluate(): Boolean {
        // TODO won't work for float numbers 0.5
        return if (findFirstChain() != "")
            false
        else
            getAllWords().all { word -> TreeBuilder.definitions.any { def -> def.name == word } }
    }
}