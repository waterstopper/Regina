package raw

import kotlinx.serialization.Serializable
import primitives.Node
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

@Serializable
sealed class RawNode {
    abstract val name: String
    //abstract val parent: String

    abstract var position: String//Pair<Double, Double>
    abstract var color: String//List<Int>
    abstract var scale: String//Pair<Double, Double>
    abstract var rotation: String//Double

    fun calculateLinks(drawnNodes: List<Node>) {
        for (i in this::class.memberProperties.filterIsInstance<KMutableProperty<*>>()) {
            i.setter.call(this, calculateLinksInField(i.getter.call(this).toString(), drawnNodes))
        }
    }

    private fun getSequenceOfConventionSymbols(startIndex: Int, str: String): String {
        var endIndex = startIndex + 1
        while (endIndex < str.length && conventionSymbol(str[endIndex]))
            endIndex++

        return str.substring(startIndex + 1 until endIndex)
    }

    fun calculateLinksInField(data: String, drawnNodes: List<Node>): String {
        var field = data

        while (field.contains("@")) {
            val nodeName = getSequenceOfConventionSymbols(field.indexOf('@'), field)
            val fieldName = getSequenceOfConventionSymbols(
                field.indexOf('.', field.indexOf('@')), field
            )

            val searched = drawnNodes.find { it.name == nodeName }
                ?: throw IllegalArgumentException(
                    "no such name $nodeName " +
                            "or node tree is incorrect"
                )

            field = field.replaceRange(
                field.indexOf('@')
                        ..field.indexOf('@') + nodeName.length + fieldName.length + 1,
                searched::class.memberProperties.find { it.name == fieldName }!!.getter.call(searched).toString()
            )
        }

        return field
    }

    private fun conventionSymbol(c: Char): Boolean {
        return c.isLetterOrDigit() || c == '_'
    }
}