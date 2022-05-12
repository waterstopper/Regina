package token.link

import lexer.PositionalException
import properties.Function
import properties.Object
import properties.Type
import properties.Variable
import properties.primitive.Primitive
import table.SymbolTable
import token.Identifier
import token.Token
import token.invocation.Call
import token.invocation.Constructor
import token.invocation.Invocation
import token.statement.Assignment
import utils.Utils.toVariable

class PropertyLink(token: Token) : Link(token) {
    lateinit var table: SymbolTable

    override fun resolveProperty(parent: Variable): Boolean {
        val result = parent.getPropertyOrNull(children[index].value) ?: return false
        addVariable(result)
        return true
    }
}

/**
identifier.link
type.functionCall
primitive.functionCall
property.functionCall
identifier.property
type.property
package.object
package.functionCall
package.constructor
 */
//fun evaluateOld(symbolTable: SymbolTable): Any {
//    when (left) {
//        is Identifier -> {
//            if (symbolTable.getVariableOrNull(left.value) != null) {
//                return when (val variable = symbolTable.getVariable(left)) {
//                    is Type -> evaluateType(variable, this, symbolTable)
//                    is Primitive -> evaluatePrimitive(variable, this, symbolTable)
//                    is Object -> evaluateObjectToken(this, symbolTable)
//                    else -> throw PositionalException("`$left` does not have function or property", left)
//                }
//            } else if (symbolTable.getTypeOrNull(left) != null)
//                return evaluateTypeToken(this, symbolTable)
//            else if (symbolTable.getObjectOrNull(left) != null)
//                return evaluateObjectToken(this, symbolTable)
//            else if (symbolTable.getImportOrNull(left.value) != null)
//                return evaluatePackage(this, symbolTable)
//        }
//        is Call -> return evaluateFunction(this, symbolTable, symbolTable.getFunction(left))
//        is Constructor -> return evaluateTypeToken(this, symbolTable)
//    }
//    val value = left.evaluate(symbolTable).toVariable(left)
//    if (value is Primitive)
//        return evaluatePrimitive(value, this, symbolTable)
//    throw PositionalException("unexpected token in link", left)
//}


/**
 * function, object, constructor
 */
private fun evaluatePackage(link: Token, symbolTable: SymbolTable): Any {
    val fileTable = symbolTable.getImportOrNull(link.left.value)!!
    when (link.right) {
        is Constructor -> if (fileTable.getTypeOrNull(link.right.value) != null) {
            return (link.right as Constructor).evaluateType(
                fileTable.getTypeOrNull(link.right.value)!!,
                symbolTable
            )
        }
        is Call -> if (fileTable.getFunctionOrNull((link.right as Call).name.value) != null) {
            val newTable = symbolTable.changeFile(fileTable.fileName)
            (link.right as Call).argumentsToParameters(
                fileTable.getFunction(link.right as Call),
                symbolTable.changeFile(fileTable.fileName),
                newTable
            )
            return (link.right as Call).evaluateFunction(
                newTable,
                fileTable.getFunctionOrNull((link.right as Call).name.value)!!
            )
        }
        is Identifier -> if (fileTable.getObjectOrNull(link.right.value) != null)
            return fileTable.getObjectOrNull(link.right.value)!!
        is Link -> {
            when (link.right.left) {
                is Constructor -> if (fileTable.getTypeOrNull(link.right.left.value) != null) {
                    val type =
                        (link.right.left as Constructor).evaluateType(
                            fileTable.getTypeOrNull(link.right.left.value)!!,
                            symbolTable
                        ) as Type
                    return evaluateType(
                        type,
                        link.right as Link,
                        symbolTable.changeFile(link.left.value).changeVariable(type)
                    )
                }
                is Call -> {
                    val function = fileTable.getFunctionOrNull((link.right as Call).name.value)
                    if (function != null)
                        return evaluateFunction(
                            link.right as Link,
                            symbolTable.changeFile(link.left.value),
                            function
                        )
                }
            }
        }
    }
    throw PositionalException("Expected function call, constructor or object", link.right)
}

private fun evaluateObjectToken(link: Link, symbolTable: SymbolTable): Any {
    val objectInstance = link.left.evaluate(symbolTable)
    if (objectInstance !is Object)
        throw PositionalException("Expected object", link.left)
    return evaluateType(objectInstance, link, symbolTable)
}

/**
 * function, property
 */
private fun evaluateTypeToken(link: Link, symbolTable: SymbolTable): Any {
    val type = link.left.evaluate(symbolTable)
    if (type !is Type)
        throw PositionalException("Expected type", link.left)
    return evaluateType(type, link, symbolTable)
}

private fun evaluateType(type: Type, link: Link, symbolTable: SymbolTable): Any {
    when (link.right) {
        is Call -> {
            val functionTable = symbolTable.changeVariable(type)
            functionTable.addVariable("(this)", type)
            val function = type.getFunction((link.right as Call).name)
            (link.right as Call).argumentsToParameters(function, symbolTable, functionTable)
            return (link.right as Call).evaluateFunction(functionTable, function)
        }
        is Identifier -> return type.getProperty(link.right)
        is Link -> {
            when (link.right.left) {
                is Call -> {
                    val functionTable = symbolTable.changeVariable(type)
                    functionTable.addVariable("(this)", type)
                    return evaluateFunction(
                        link.right as Link,
                        functionTable,
                        type.getFunction((link.right.left as Call).name)
                    )
                }
                is Identifier -> {
                    val property = type.getProperty(link.right.left)
                    if (property is Type)
                        evaluateType(property, link.right as Link, symbolTable)
                    else if (property is Primitive)
                        evaluatePrimitiveToken(link.right as Link, symbolTable)
                }
            }
        }
    }
    throw PositionalException("Expected function call or property", link.right)
}

private fun evaluatePrimitiveToken(link: Link, symbolTable: SymbolTable): Any {
    val primitive = link.left.evaluate(symbolTable)
    return evaluatePrimitive(primitive, link, symbolTable)
}

private fun evaluatePrimitive(primitive: Any, link: Link, symbolTable: SymbolTable): Any {
    if (primitive !is Primitive)
        throw PositionalException("Expected primitive", link.left)
    when (link.right) {
        is Call -> {
            val functionTable = symbolTable.copy()
            functionTable.addVariable("(this)", primitive)
            val function = primitive.getFunctionOrNull((link.right as Call).name.value)
                ?: throw PositionalException("Function not found", link.right)
            (link.right as Call).argumentsToParameters(function, symbolTable, functionTable)
            return (link.right as Call).evaluateFunction(functionTable, function)
        }
        is Link -> if (link.right.left is Call) {
            val function =
                primitive.getFunctionOrNull((link.right.left as Call).name.value) ?: throw PositionalException(
                    "Function not found",
                    link.right.left
                )
            val functionTable = symbolTable.copy()
            functionTable.addVariable("(this)", primitive)
            return evaluateFunction(link.right as Link, functionTable, function)
        }
    }
    throw PositionalException("Expected function call", link.right)
}

/**
 * similar to ValueEvaluation.evaluateLink()
 */
fun getPropertyNameAndTable(token: Token, symbolTable: SymbolTable): Pair<String, SymbolTable> {
    var linkRoot = token
    var table = symbolTable
    while (linkRoot.value == ".") {
        val type = table.getProperty(linkRoot.left)
        if (type !is Type)
            throw PositionalException("expected class", linkRoot.left)
        linkRoot = linkRoot.right
        table = symbolTable.changeVariable(type)
    }
    return Pair(linkRoot.value, table)
}

private fun evaluateFunction(link: Link, symbolTable: SymbolTable, function: Function): Any {
    val newTable = symbolTable.copy()

    (link.left as Call).argumentsToParameters(function, symbolTable, newTable)
    val result = (link.left as Call).evaluateFunction(newTable, function)
    if (result is Primitive)
        return evaluatePrimitive(result, link, newTable)
    else if (result is Type) {
        TODO("not yet implemented")
    }
    throw PositionalException("Expected return value from function", link.left)
}


//class CallLink(token: Link, function: Function) : Link(token) {
//    init {
//        (left as Call).function = function
//    }
//
//    fun setFunction(function: Function) {
//        (left as Call).function = function
//    }
//
//    override fun evaluate(symbolTable: SymbolTable): Any {
//        val functionResult = left.evaluate(symbolTable).toVariable(this)
//        when (getAfterDot()) {
//            is Call -> {
//                val function = functionResult.getFunction(getAfterDot())
//                return if (right is CallLink) {
//                    (right as CallLink).setFunction(function)
//                    right.evaluate(symbolTable)
//                } else {
//                    (right as Call).function = function
//                    right.evaluate(symbolTable)
//                }
//            }
//            is Identifier -> {
//                val property = functionResult.getProperty(getAfterDot())
//                return if (right is Link) {
//                    val res = VariableLink(right as Link, property)
//                    val variableTable = symbolTable.copy()
//                    variableTable.addVariable(getAfterDot().value, property)
//                    res.evaluate(symbolTable)
//                } else {
//                    (right as Identifier).variable = property
//                    right.evaluate(symbolTable)
//                }
//            }
//        }
//        throw PositionalException("Expected property, call or constructor", getAfterDot())
//    }
//}
/**
 * (if a else b).c
 * a and b are identifier tokens
 */
class ConditionalLink(token: Token) : Link(token) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        val eval = left.evaluate(symbolTable).toVariable(left)
        return VariableLink(this, eval)
    }
}

class ConstructorLink(token: Token, val type: Type) : Link(token) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        val typeTable = symbolTable.copy()
        val type = symbolTable.getType(this).copy()
        typeTable.addVariable(left.value, type)
        return (left as Constructor).evaluateType(type, typeTable)
    }
}

class PackageLink(token: Link) : Link(token) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        val fileTable = symbolTable.getImportOrNull(left.value)!!
        when (getAfterDot()) {
            is Call -> {
                val function = fileTable.getFunction(getAfterDot() as Call)
//                return if (right is Link) CallLink(
//                    right as Link,
//                    function
//                ).evaluate(symbolTable) else (right as Call).evaluate(symbolTable)
            }
        }
        if (getAfterDot() !is Constructor && getAfterDot() !is Call && getAfterDot() !is Identifier)
            throw PositionalException("Expected function, constructor or object", getAfterDot())
        return right.evaluate(symbolTable.changeVariable(fileTable.getTypeOrNull(getAfterDot().value)!!))
//        when (getAfterDot()) {
//            is Constructor -> if (fileTable.getTypeOrNull(getAfterDot().value) != null) {
//                return right.evaluate(symbolTable.changeType(fileTable.getTypeOrNull(getAfterDot().value)!!))
//            }
//            is Call -> if (fileTable.getFunctionOrNull((getAfterDot() as Call).name.value) != null) {
//                val newTable = symbolTable.changeFile(fileTable.fileName)
//                (getAfterDot() as Call).argumentsToParameters(
//                    fileTable.getFunction(getAfterDot() as Call),
//                    symbolTable.changeFile(fileTable.fileName),
//                    newTable
//                )
//                return (getAfterDot() as Call).evaluateFunction(
//                    newTable,
//                    fileTable.getFunctionOrNull((getAfterDot() as Call).name.value)!!
//                )
//            }
//            is Identifier -> if (fileTable.getObjectOrNull(getAfterDot().value) != null)
//                return fileTable.getObjectOrNull(getAfterDot().value)!!
//        }
//        throw PositionalException("", this)
    }

//    private fun evaluatePackage(link: Token, symbolTable: SymbolTable): Any {
//        val fileTable = symbolTable.getImportOrNull(link.left.value)!!
//        when (link.right) {
//            is Constructor -> if (fileTable.getTypeOrNull(link.right.value) != null) {
//                return (link.right as Constructor).evaluateType(
//                    fileTable.getTypeOrNull(link.right.value)!!,
//                    symbolTable
//                )
//            }
//            is Link -> {
//                when (link.right.left) {
//                    is Constructor -> if (fileTable.getTypeOrNull(link.right.left.value) != null) {
//                        val type =
//                            (link.right.left as Constructor).evaluateType(
//                                fileTable.getTypeOrNull(link.right.left.value)!!,
//                                symbolTable
//                            ) as Type
//                        return evaluateType(
//                            type,
//                            link.right as Link,
//                            symbolTable.changeFile(link.left.value).changeType(type)
//                        )
//                    }
//                    is Call -> {
//                        val function = fileTable.getFunctionOrNull((link.right as Call).name.value)
//                        if (function != null)
//                            return evaluateFunction(
//                                link.right as Link,
//                                symbolTable.changeFile(link.left.value),
//                                function
//                            )
//                    }
//                }
//            }
//        }
//        throw PositionalException("Expected function call, constructor or object", link.right)
//    }
}

/**
 * Variable or property
 */
class VariableLink(token: Link, var variable: Variable?) :
    Link(token) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        val varTable = if (variable is Type) symbolTable.changeVariable(variable!! as Type) else symbolTable
        when (getAfterDot()) {
            is Call -> {
                varTable.addVariable("(this)", variable!!)
                val function = variable!!.getFunction((getAfterDot() as Call).name)
                (getAfterDot() as Call).argumentsToParameters(function, symbolTable, varTable)
                return (getAfterDot() as Call).evaluateFunction(varTable, function)
            }
            is Identifier -> return if (right is Link) VariableLink(
                right as Link,
                variable!!.getProperty(getAfterDot()) as Type
            ).evaluate(varTable) else variable!!.getProperty(getAfterDot())
        }
        throw PositionalException("Expected function or property", getAfterDot())
    }

    override fun isResolved(symbolTable: SymbolTable): Boolean {
        return when (right) {
            is Link -> (right as Link).isResolved(symbolTable)
            is Invocation -> true
            is Identifier -> {
                if (variable!!.hasProperty(right))
                    variable!!.getPropertyOrNull(right.value) != null
                else false
            }
            else -> throw PositionalException("Unexpected", right)
        }
    }

    override fun getFirstUnassigned(parent: Type): Assignment? {
        if (right is Link)
            return if (variable is Type)
                getFirstUnassigned(variable as Type)
            else getFirstUnassigned(parent)
        throw PositionalException("")
    }
}