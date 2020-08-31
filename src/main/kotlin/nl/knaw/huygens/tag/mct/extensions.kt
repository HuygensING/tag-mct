package nl.knaw.huygens.tag.mct

import nl.knaw.huc.di.tag.tagml.TAGMLToken
import nl.knaw.huc.di.tag.tagml.TAGMLToken.*
import nl.knaw.huygens.tag.mct.TAGNode.TAGMarkupNode
import nl.knaw.huygens.tag.mct.TAGNode.TAGTextNode

fun List<TAGMLToken>.asMCT(): TAGMCT {
    val mct = TAGMCT()
    val openMarkup: MutableList<TAGMarkupNode> = mutableListOf()
    val markupIndex: MutableMap<Long, TAGMarkupNode> = mutableMapOf()
    this.forEach { token ->
        when (token) {
            is HeaderToken -> handleHeaderToken()
            is MarkupMilestoneToken -> handleMarkupMilestoneToken(token, mct, openMarkup)
            is MarkupOpenToken -> handleMarkupOpenToken(token, mct, openMarkup, markupIndex)
            is TextToken -> handleTextToken(token, mct, openMarkup)
            is MarkupSuspendToken -> handleMarkupSuspendToken(token, openMarkup, markupIndex)
            is MarkupResumeToken -> handleMarkupResumeToken(token, openMarkup, markupIndex)
            is MarkupCloseToken -> handleMarkupCloseToken(token, openMarkup, markupIndex)
        }
    }
    return mct
}

private fun handleHeaderToken() {
}

private fun handleMarkupMilestoneToken(
    token: MarkupMilestoneToken,
    mct: TAGMCT,
    openMarkup: MutableList<TAGMarkupNode>
) {
    mct.addDirectedEdge(
        TAGEdge(listOf()),
        openMarkup.last(),
        TAGMarkupNode(token.qName)
    )
}

private fun handleMarkupCloseToken(
    token: MarkupCloseToken,
    openMarkup: MutableList<TAGMarkupNode>,
    markupIndex: MutableMap<Long, TAGMarkupNode>
) {
    openMarkup.remove(markupIndex[token.markupId])
}

private fun handleMarkupResumeToken(
    token: MarkupResumeToken,
    openMarkup: MutableList<TAGMarkupNode>,
    markupIndex: MutableMap<Long, TAGMarkupNode>
) {
    openMarkup += markupIndex[token.markupId]!!
}

private fun handleMarkupSuspendToken(
    token: MarkupSuspendToken,
    openMarkup: MutableList<TAGMarkupNode>,
    markupIndex: MutableMap<Long, TAGMarkupNode>
) {
    openMarkup.remove(markupIndex[token.markupId])
}

private fun handleMarkupOpenToken(
    token: MarkupOpenToken,
    mct: TAGMCT,
    openMarkup: MutableList<TAGMarkupNode>,
    markupIndex: MutableMap<Long, TAGMarkupNode>
) {
    val tagMarkupNode = TAGMarkupNode(token.qName)
    if (openMarkup.isNotEmpty()) {
        mct.addDirectedEdge(TAGEdge(listOf()), openMarkup.last(), tagMarkupNode)
    }
    openMarkup += tagMarkupNode
    markupIndex[token.markupId] = tagMarkupNode
}

private fun handleTextToken(
    token: TextToken,
    mct: TAGMCT,
    openMarkup: MutableList<TAGMarkupNode>
) {
    val node = TAGTextNode(token.rawContent)
    if (openMarkup.isNotEmpty()) { // should not be necessary!
        mct.addDirectedEdge(TAGEdge(listOf()), openMarkup.last(), node)
    }
}

fun TAGMCT.asDot(): String = DotFactory.fromTAGMCT(this)
