package export

import Container
import Property
import com.github.nwillc.ksvg.elements.Element
import com.github.nwillc.ksvg.elements.LINE
import com.github.nwillc.ksvg.elements.SVG
import java.util.*

class Painter(val root: Container) {
    val svg = SVG.svg(true) {
    }

    fun build(): String {
        val stack = Stack<Pair<Container, Element>>()
        var current = root
        stack.add(Pair(current, matchType(current)))
        svg.children.add(stack.peek().second)
        while (stack.isNotEmpty()) {
            current = stack.peek().first
            while (current.children.filterIsInstance<Container>().isNotEmpty()) {
                current = current.children.filterIsInstance<Container>()[0]
                stack.peek().first.children.minusAssign(current)
                stack.add(Pair(current, matchType(current)))
                svg.children.add(stack.peek().second)
            }

            stack.pop()
        }

        return svg.toString()
    }

    private fun matchType(container: Container): Element {
        val type = TreeBuilder.getType(container)
        return when (type) {
            "Line" -> reassignLine(container, defaultLine())
            else -> throw Exception("no such type: $type")
        }
    }

    private fun reassignLine(container: Container, line: LINE): LINE {
        for (i in container.children.filterIsInstance<Property>())
            when (i.name) {
                "x" -> line.x1 = i.value.toString()
                "y" -> line.y1 = i.value.toString()
                "x2" -> line.x2 = i.value.toString()
                "y2" -> line.y2 = i.value.toString()
                "outline" -> line.stroke = i.value.toString()
                "rotation" -> line.attributes["transform"] = "rotate(${i.value} 0 0)"
            }

        return line
    }

    fun export() {
        build()
        println(svg.toString())
    }

    fun defaultLine(): LINE {
        return SVG.svg {
            height = "100"
            width = "100"
            line {
                x1 = "0"
                y1 = "0"
                x2 = "1"
                y2 = "0"
                stroke = "black"
                strokeWidth = "0.5"
            }
        }.children[0] as LINE
    }

    fun defaultEllipse() {

    }

}