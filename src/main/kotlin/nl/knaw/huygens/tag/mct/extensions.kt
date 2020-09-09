package nl.knaw.huygens.tag.mct

import nl.knaw.huc.di.tag.tagml.TAGMLToken
import nl.knaw.huc.di.tag.tagml.TAGMLToken.*
import nl.knaw.huygens.tag.mct.TAGNode.TAGMarkupNode
import nl.knaw.huygens.tag.mct.TAGNode.TAGTextNode

fun TAGMCT.asDot(): String = DotFactory.fromTAGMCT(this)

fun List<TAGMLToken>.asMCT(): TAGMCT {
    val mct = TAGMCT()
    val openMarkup: MutableMap<String, MutableList<TAGMarkupNode>> =
        mutableMapOf<String, MutableList<TAGMarkupNode>>().withDefault { mutableListOf() }
    val markupIndex: MutableMap<Long, TAGMarkupNode> = mutableMapOf()
    this.forEach { token ->
        when (token) {
            is HeaderToken -> handleHeaderToken()
            is TextToken -> handleTextToken(token, mct, openMarkup)
            is MarkupMilestoneToken -> handleMarkupMilestoneToken(token, mct, openMarkup)
            is MarkupOpenToken -> handleMarkupOpenToken(token, mct, openMarkup, markupIndex)
            is MarkupCloseToken -> handleMarkupCloseToken(token, openMarkup, markupIndex)
            is MarkupSuspendToken -> handleMarkupSuspendToken(token, openMarkup, markupIndex)
            is MarkupResumeToken -> handleMarkupResumeToken(token, openMarkup, markupIndex)
        }
    }
    return mct
}

private fun handleHeaderToken() {}

private fun handleTextToken(
    token: TextToken,
    mct: TAGMCT,
    openMarkup: MutableMap<String, MutableList<TAGMarkupNode>>
) {
//    if (openMarkup.values.any { it.isNotEmpty() }) { // after closing of root markup, there should be no more texttokens
    val node = TAGTextNode(token.rawContent)
    for (layer in openMarkup.keys) {
        mct.addDirectedEdge(
            TAGEdge(setOf(layer)),
            openMarkup.getValue(layer).last(),
            node
        )
    }
//    }
}

private fun handleMarkupMilestoneToken(
    token: MarkupMilestoneToken,
    mct: TAGMCT,
    openMarkup: MutableMap<String, MutableList<TAGMarkupNode>>
) {
    for (layer in token.layers) {
        mct.addDirectedEdge(
            TAGEdge(setOf(layer)),
            openMarkup.getValue(layer).last(),
            TAGMarkupNode(token.extendedName())
        )
    }
}

private fun handleMarkupOpenToken(
    token: MarkupOpenToken,
    mct: TAGMCT,
    openMarkup: MutableMap<String, MutableList<TAGMarkupNode>>,
    markupIndex: MutableMap<Long, TAGMarkupNode>
) {
    val tagMarkupNode = TAGMarkupNode(token.extendedName())
    for (layer in token.layers) {
        if (openMarkup.getValue(layer).isNotEmpty()) {
            mct.addDirectedEdge(
                TAGEdge(setOf(layer)),
                openMarkup.getValue(layer).last(),
                tagMarkupNode
            )
        }
        openMarkup[layer] = openMarkup.getValue(layer).apply { add(tagMarkupNode) }
    }
    markupIndex[token.markupId] = tagMarkupNode
}

private fun handleMarkupCloseToken(
    token: MarkupCloseToken,
    openMarkup: MutableMap<String, MutableList<TAGMarkupNode>>,
    markupIndex: MutableMap<Long, TAGMarkupNode>
) {
    for (layer in token.layers) {
        openMarkup[layer] = openMarkup.getValue(layer).apply { remove(markupIndex[token.markupId]) }
    }
}

private fun handleMarkupSuspendToken(
    token: MarkupSuspendToken,
    openMarkup: MutableMap<String, MutableList<TAGMarkupNode>>,
    markupIndex: MutableMap<Long, TAGMarkupNode>
) {
    for (layer in token.layers) {
        openMarkup[layer] = openMarkup.getValue(layer).apply { remove(markupIndex[token.markupId]) }
    }
}

private fun handleMarkupResumeToken(
    token: MarkupResumeToken,
    openMarkup: MutableMap<String, MutableList<TAGMarkupNode>>,
    markupIndex: MutableMap<Long, TAGMarkupNode>
) {
    for (layer in token.layers) {
        openMarkup[layer] = openMarkup.getValue(layer).apply { add(markupIndex[token.markupId]!!) }
    }
}

private fun MarkupOpenToken.extendedName() =
    extendedName(qName, layers)

private fun MarkupMilestoneToken.extendedName() =
    extendedName(qName, layers)

private fun extendedName(qName: String, layers: Set<String>): String {
    val layerPostfix = layers.filter { it.isNotEmpty() }.joinToString()
    return if (layerPostfix.isNotEmpty()) {
        "$qName|$layerPostfix"
    } else {
        qName
    }
}

