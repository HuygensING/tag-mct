package nl.knaw.huygens.tag.mct

fun TAGNode.TAGTextNode.nodeString(): String = """(${this.content.replace("\n", "\\n")})"""
fun TAGNode.TAGMarkupNode.nodeString(): String = "[${this.name}]"

class DotFactory {

    companion object {
        fun fromTAGMCT(mct: TAGMCT): String =
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

        private fun nodeString(node: TAGNode): String =
            when (node) {
                is TAGNode.TAGTextNode -> node.nodeString()
                is TAGNode.TAGMarkupNode -> node.nodeString()
            }
    }

}