package deprecated

import deprecated.evaluation.Evaluation
import deprecated.evaluation.Evaluation.isNameSymbol
import kotlin.reflect.KProperty1
@Deprecated("")
open class Formula(open var content: String) {

    open fun evaluate(): Any {
        try {
            return Evaluation.evaluate(content)
        } catch (e: Exception) {
            println(content)
            throw e
        }
    }

    fun getAllWords(): List<String> {
        val res = mutableListOf<String>()
        val word = StringBuilder()
        var ignored = false
        for (c in content) {
            if (c == '@')
                ignored = true
            else if (c.isLetter() && word.isEmpty())
                word.append(c)
            else if (c.isNameSymbol() && word.isNotEmpty())
                word.append(c)
            else if (word.isNotEmpty()) {
                if (!ignored)
                    res.add(word.toString())
                word.clear()
                ignored = false
            }
        }
        if (word.isNotEmpty() && !ignored)
            res.add(word.toString())

        return res
    }

    fun replaceFirst(value: Any) {
        val replaced = findFirstChain()
        content = content.replaceFirst(replaced, value.toString())
    }

    fun neededForEvaluation(): List<String> {
//        if(findFirstChain()=="Segment"){
//            println()
//        }
        return findFirstChain().split('.')
    }

    fun findFirstChain(): String {
        val chain = StringBuilder()
        var ignored = false
        for (c in content) {
            if (c == '@')
                ignored = true
            else if (c.isLetter() && chain.isEmpty())
                chain.append(c)
            else if ((c.isNameSymbol() || c == '.') && chain.isNotEmpty())
                chain.append(c)
            else if (chain.isNotEmpty()) {
                if (!ignored)
                    return chain.toString()
                chain.clear()
                ignored = false
            }
        }
        if (chain.isNotEmpty() && !ignored)
            return chain.toString()

        // will not be executed
        return ""
    }

    fun getDependencies(properties: List<KProperty1<Any, *>>) = properties.filter { getLinks().contains(it.name) }

    fun getLinks(): List<String> {
        val res = mutableListOf<String>()

        for (i in content.indices) {
            if (content[i] == '@') {
                var name = ""
                for (j in i + 1..content.lastIndex) {
                    if (!content[j].isLetterOrDigit())
                        break
                    name += content[j]
                }
                res.add(name)
            }
        }

        return res
    }


}

