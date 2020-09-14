package nl.knaw.huygens.tag.mct

import org.apache.commons.text.StringEscapeUtils
import java.lang.String.format
import java.util.*


fun TAGNode.TAGTextNode.nodeString(): String = """(${this.content.replace("\n", "\\n")})"""
fun TAGNode.TAGMarkupNode.nodeString(): String = "[${this.name}]"

class DotFactory {
    companion object {
        private val layerColor: MutableMap<String, String> = HashMap()

        private val colorPicker: ColorPicker = ColorPicker(
            "red",
            "blue",
            "darkgray",
            "gray",
            "green",
            "lightgray",
            "lime",
            "magenta",
            "olive",
            "orange",
            "pink",
            "purple",
            "brown",
            "cyan",
            "teal",
            "violet",
            "black"
        )

        fun fromTAGMCT0(mct: TAGMCT): String =
            mct.nodes.joinToString("\n") { node ->
                when (node) {
                    is TAGNode.TAGTextNode -> node.nodeString()
                    is TAGNode.TAGMarkupNode -> {
                        val sourceString = node.nodeString()
                        mct.outgoingEdgesOf(node).joinToString("\n") { edge ->
                            val edgeString = "-[${edge.colors.joinToString(",")}]->"
                            val targetsString = mct.targetsOf(edge).joinToString { nodeString(it) }
                            "$sourceString $edgeString $targetsString"
                        }
                    }
                }
            }

        fun fromTAGMCT(mct: TAGMCT): String {
            layerColor.clear()
            colorPicker.reset()
            val dotBuilder = StringBuilder(
                """
                digraph TextGraph{
                  node [font="helvetica";style="filled";fillcolor="white"]
                  subgraph{

                """.trimIndent()
            )
            // textnodes
            mct.nodes.filterIsInstance<TAGNode.TAGTextNode>()
                .map { toTextNodeLine(it) }
                .forEach { dotBuilder.append(it) }
            dotBuilder.append("    rank=same\n")

            // textnode edges
            var prevNode = -1L
            mct.nodes.filterIsInstance<TAGNode.TAGTextNode>()
                .map { it.id }
                .forEach { id ->
                    if (prevNode != -1L) {
                        dotBuilder.append(toNextEdgeLine(prevNode, id))
                    }
                    prevNode = id
                }
            dotBuilder.append("  }\n")

            // markup nodes
            mct.nodes.filterIsInstance<TAGNode.TAGMarkupNode>()
                .map { toMarkupNodeLine(it) }
                .forEach { dotBuilder.append(it) }

            // markup edges
            mct.nodes.filterIsInstance<TAGNode.TAGMarkupNode>()
                .flatMap { mct.outgoingEdgesOf(it) }
                .map { toOutGoingEdgeLine(it, mct) }
                .forEach { dotBuilder.append(it) }

            dotBuilder.append("}")
            return dotBuilder.toString()
        }

        private fun toOutGoingEdgeLine(edge: TAGEdge, mct: TAGMCT): String {
            val source: Long = mct.sourceOf(edge).id
            val edgeTargets = mct.targetsOf(edge)
            val targets = edgeTargets.joinToString(",") { node ->
                when (node) {
                    is TAGNode.TAGTextNode -> "t${node.id}"
                    is TAGNode.TAGMarkupNode -> "m${node.id}"
                }
            }
            val layerName: String = edge.colors.joinToString(",")
            val label = if (layerName.isEmpty()) "" else ";label=<<font point-size=\"8\">$layerName</font>>"
            val color = getLayerColor(layerName)
            return if (edgeTargets.size == 1) {
                format("  m%d->%s[color=%s;arrowhead=none%s]\n", source, targets, color, label)
            } else {
                val hyperId = "h$source$layerName"
                format(
                    "  %s [shape=point;color=%s;label=\"\"]\n"
                            + "  m%d->%s [color=%s;arrowhead=none%s]\n"
                            + "  %s->{%s}[color=%s;arrowhead=none]\n",
                    hyperId, color, source, hyperId, color, label, hyperId, targets, color
                )
            }
        }

        private fun toNextEdgeLine(from: Long, to: Long): String =
            format("    t%d->t%d [color=invis;arrowhead=none;label=\"\"]\n", from, to)

        private fun toMarkupNodeLine(tagMarkupNode: TAGNode.TAGMarkupNode): String {
            val pre = StringBuilder()
            val post = StringBuilder()
            val iterator = tagMarkupNode.layers.iterator()
            val layerName: String = iterator.next()
            val color: String = getLayerColor(layerName)
            while (iterator.hasNext()) {
                val otherLayer: String = iterator.next()
                val otherColor = getLayerColor(otherLayer)
                pre.append("  subgraph cluster_")
                    .append(tagMarkupNode.id)
                    .append(otherLayer)
                    .append("{\n")
                    .append("    style=rounded\n    color=")
                    .append(otherColor)
                    .append("\n  ")
                post.append("  }\n")
            }
            return format(
                "%s  m%d [color=%s;label=<%s>]\n%s",
                pre, tagMarkupNode.id, color, tagMarkupNode.name, post
            )

        }

        private fun getLayerColor(layerName: String): String =
            layerColor.computeIfAbsent(layerName) { colorPicker.nextColor }

        private fun toTextNodeLine(textNode: TAGNode.TAGTextNode): String {
            val shape = "box"
            val templateStart = "    t%d [shape=%s;arrowhead=none;label="
            val templateEnd = "]\n"
            return if (textNode.content.isEmpty()) {
                format(templateStart + "\"\"" + templateEnd, textNode.id, shape)
            } else {
                format(
                    "$templateStart<%s>$templateEnd",
                    textNode.id,
                    shape,
                    escape(textNode.content)
                )
            }
        }

        private fun escape(label: String): String =
            StringEscapeUtils.escapeHtml4(label).replace("\n", "\\\\n") //        .replace(" ", "_")

        private fun nodeString(node: TAGNode): String =
            when (node) {
                is TAGNode.TAGTextNode -> node.nodeString()
                is TAGNode.TAGMarkupNode -> node.nodeString()
            }
    }

}
