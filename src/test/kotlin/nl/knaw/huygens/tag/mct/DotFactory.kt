package nl.knaw.huygens.tag.mct


fun TAGNode.TAGTextNode.nodeString(): String = """(${this.content.replace("\n", "\\n")})"""
fun TAGNode.TAGMarkupNode.nodeString(): String = "[${this.name}]"

class DotFactory {

    companion object {
        fun fromTAGMCT(mct: TAGMCT): String =
            mct.nodes.joinToString("\n") { node ->
                when (node) {
                    is TAGNode.TAGTextNode -> node.nodeString()
                    is TAGNode.TAGMarkupNode -> "${node.nodeString()} -> ${
                        mct.outgoingEdgesOf(node).map { e -> mct.targetsOf(e) }.flatten()
                            .joinToString { nodeString(it) }
                    }"
                }
            }

        private fun nodeString(node: TAGNode): String =
            when (node) {
                is TAGNode.TAGTextNode -> node.nodeString()
                is TAGNode.TAGMarkupNode -> node.nodeString()
            }

    }


}


