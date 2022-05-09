package token.link

import lexer.PositionalException
import properties.Type
import table.SymbolTable
import token.Identifier
import token.Token
import token.invocation.Call
import token.invocation.Constructor

class PackageLink(token: Link) : Link(token) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        val fileTable = symbolTable.getImportOrNull(left.value)!!
        when (getAfterDot()) {
            is Call -> {
                val function = fileTable.getFunction(getAfterDot() as Call)
                return if (right is Link) CallLink(
                    right as Link,
                    function
                ).evaluate(symbolTable) else (right as Call).evaluate(symbolTable)
            }
        }
        if (getAfterDot() !is Constructor && getAfterDot() !is Call && getAfterDot() !is Identifier)
            throw PositionalException("Expected function, constructor or object", getAfterDot())
        return right.evaluate(symbolTable.changeType(fileTable.getTypeOrNull(getAfterDot().value)!!))
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