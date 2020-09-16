package nl.knaw.huygens.tag.mct

import java.io.Writer
import java.util.*
import javax.xml.stream.XMLOutputFactory
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet

/*
* MCT classes and traversal and transformation
* author: Ronald Haentjens Dekker
 */

sealed class Event
data class MarkupOpen(val node: TAGNode.TAGMarkupNode) : Event()
data class MarkupClose(val node: TAGNode.TAGMarkupNode) : Event()
data class TextEvent(val node: TAGNode.TAGTextNode) : Event()

/*
 * Traverse MCT based on a topological sort based on a depth first search.
// Traverse the MCT (markup nodes and text nodes) in a depth first fashion.
 */

fun topologicalSort(mct: TAGMCT): List<TAGNode> {
    val l = arrayListOf<TAGNode>()
    val temporaryMarks = hashSetOf<TAGNode>()
    val permanentMarks = hashSetOf<TAGNode>()
    //Note: we start with the root and since everything is connected we never have to check other nodes.
    visitNode(mct, mct.rootNode, temporaryMarks, permanentMarks, l)
    return l
}

fun visitNode(mct: TAGMCT, n: TAGNode, temporaryMarks: HashSet<TAGNode>, permanentMarks: HashSet<TAGNode>, l: ArrayList<TAGNode>) {
//    println("Visit node: $n")
    if (permanentMarks.contains(n)) {
        return
    }
    if (temporaryMarks.contains(n)) {
        throw Exception("Cycle detected!")
    }
    temporaryMarks.add(n)
    when (n) {
        is TAGNode.TAGMarkupNode -> {
            mct.outgoingEdgesOf(n).reversed().forEach { visitNode(mct, it, temporaryMarks, permanentMarks, l) }
        }
        else -> {
        }
    }

    temporaryMarks.remove(n)
    permanentMarks.add(n)
    l.add(0, n)
}

// For the creation of events we use topological sort of the graph and stacks...
// Keep a stack per color so that we know when we have to send a close event
// We also keep a stack for all the open markup regardless of the color on the node
// Check whether the value on the stack is one of the parents!
// When one of stack per color values is not one of the parents we have to pop and send a close event!
// In the end we close all the markup that is still open
fun traverseMCT(mct: TAGMCT): List<Event> {
    val nodes = topologicalSort(mct)
    val result = arrayListOf<Event>()
    val colorToStackMap = HashMap<String, Stack<TAGNode.TAGMarkupNode>>()
    val globalStack = LinkedHashSet<TAGNode.TAGMarkupNode>()
    for (node in nodes) {
        val parents = mct.incomingEdges.getOrElse(node) { emptySet<TAGNode.TAGMarkupNode>() }
        val stacksToCheck: List<Stack<TAGNode.TAGMarkupNode>> =
            when (node) {
                is TAGNode.TAGMarkupNode -> colorToStackMap.entries.filter { node.colors.contains(it.key) }.map { it.value }
                is TAGNode.TAGTextNode -> colorToStackMap.values.toList()
            }
        for (stack in stacksToCheck) {
            while (stack.peek() !in parents) {
                val nodeToPop = stack.pop()
                if (globalStack.remove(nodeToPop)) result.add(MarkupClose(nodeToPop))
            }
        }
        when (node) {
            is TAGNode.TAGMarkupNode -> {
                node.colors.map { colorToStackMap.getOrPut(it) { Stack() } }.forEach { it.push(node) }
                globalStack.add(node)
                result.add(MarkupOpen(node))
            }
            is TAGNode.TAGTextNode -> result.add(TextEvent(node))
        }
    }
    globalStack.reversed().forEach { node -> result.add(MarkupClose(node)) }
    return result
}

// Trojan horse markup is defined as: <s sID="foo"/>The sun is yellow<s eID="foo"/>
fun createXML(mct: TAGMCT, leadingHierarchy: String, writer: Writer) {
    val events = traverseMCT(mct)
    val xml = XMLOutputFactory.newFactory().createXMLStreamWriter(writer)
    for (event in events) {
        when (event) {
            is TextEvent -> xml.writeCharacters(event.node.content)
            is MarkupOpen -> 	if (event.node.colors.contains(leadingHierarchy)) xml.writeStartElement(event.node.label)
            else xml.apply {	writeEmptyElement(event.node.label)
                writeAttribute("sID", event.node.id.toString()) }
            is MarkupClose -> if (event.node.colors.contains(leadingHierarchy)) xml.writeEndElement()
            else xml.apply {	writeEmptyElement(event.node.label)
                writeAttribute("eID", event.node.id.toString()) }
        }
    }
    writer.close()
}
