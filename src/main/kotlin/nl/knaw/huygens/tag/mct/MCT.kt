package nl.knaw.huygens.tag.mct

open class MCT<N, E> {
    val nodes: Set<N>
        get() = _nodes

    private val _nodes: MutableSet<N> = mutableSetOf()
    private val incomingEdges: MutableMap<N, MutableList<E>> = mutableMapOf()
    private val outgoingEdges: MutableMap<N, MutableList<E>> = mutableMapOf()
    private val sourceNode: MutableMap<E, N> = mutableMapOf()
    private val targetNodes: MutableMap<E, MutableList<N>> = mutableMapOf()

    fun addDirectedEdge(edge: E, source: N, vararg targets: N) {
        sourceNode[edge] = source
        _nodes += source
        targetNodes[edge] = mutableListOf(*targets)
        for (target in targets) {
            _nodes += target
            incomingEdges.getOrPut(target) { mutableListOf() }.add(edge)
        }
        outgoingEdges.getOrPut(source) { mutableListOf() }.add(edge)
    }

    fun outgoingEdgesOf(node: N): List<E> = outgoingEdges.getOrDefault(node, listOf())

    fun incomingEdgesOf(node: N): List<E> = incomingEdges.getOrDefault(node, listOf())

    fun targetsOf(edge: E): List<N> = targetNodes.getOrDefault(edge, listOf())

}

class TAGMCT : MCT<TAGNode, TAGEdge>()

sealed class TAGNode {
    class TAGTextNode(val content: String) : TAGNode() {
        override fun toString(): String = "Text($content)"
    }

    class TAGMarkupNode(val name: String) : TAGNode() {
        override fun toString(): String = "Markup($name)"
    }
}

data class TAGEdge(val colors: Set<String>) {
    override fun equals(other: Any?): Boolean =
        this === other

    override fun hashCode(): Int {
        return colors.hashCode()
    }
}
