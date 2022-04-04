package properties

import evaluation.FunctionEvaluation
import evaluation.TypeManager
import lexer.Token
import evaluation.ValueEvaluation
import structure.SymbolTable

class Assignment(val token: Token) {
    val name: String get() = token.children[0].value

    // should be val, but no way to do it
    lateinit var parent: Type

    fun canEvaluate(): Boolean = token.children[1].find("(IDENT)") == null
            && token.children[1].find("parent") == null

    fun getLink() = token.find(".")!!

    fun replaceFirst(value: Any) {
        // TODO think whether first encountered link is the right one in all cases
        // TODO it will hinder resolving same type next time. Or we can have copy of that token type
        //token.findValue(){
        token.find(".")!!.value = value.toString()
        token.children.clear()
    }


    fun evaluate(): Property {
        val value = ValueEvaluation.evaluateValue(
            token.children[1],
            SymbolTable((TypeManager.types) as (MutableMap<String, Variable>), FunctionEvaluation.functions)
        )
        if (value is String)
            return TypeManager.find(value) ?: Primitive(name, value, parent)
        // number or array
        return Primitive(name, value, parent)
    }

    override fun toString() = "$name=-"
}
