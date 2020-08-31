package nl.knaw.huygens.tag.mct

import nl.knaw.huc.di.tag.tagml.TAGMLToken

fun List<TAGMLToken>.asMCT(): TAGMCT {
    val mct = TAGMCT()
    val root = TAGNode.TAGMarkupNode(":root:")
    val openMarkup: MutableList<TAGNode.TAGMarkupNode> = mutableListOf(root)
    val markupIndex: MutableMap<Long, TAGNode.TAGMarkupNode> = mutableMapOf()
    this.forEach { token ->
        when (token) {
            is TAGMLToken.TextToken -> {
                val node = TAGNode.TAGTextNode(token.rawContent)
                mct.addDirectedEdge(TAGEdge(listOf()), openMarkup.last(), node)
            }
            is TAGMLToken.MarkupOpenToken -> {
                val tagMarkupNode = TAGNode.TAGMarkupNode(token.qName)
                mct.addDirectedEdge(TAGEdge(listOf()), openMarkup.last(), tagMarkupNode)
                openMarkup += tagMarkupNode
                markupIndex[token.markupId] = tagMarkupNode
            }
            is TAGMLToken.HeaderToken -> println("Not handling HeaderToken yet")
            is TAGMLToken.MarkupSuspendToken -> openMarkup.remove(markupIndex[token.markupId])
            is TAGMLToken.MarkupResumeToken -> openMarkup += markupIndex[token.markupId]!!
            is TAGMLToken.MarkupCloseToken -> openMarkup.remove(markupIndex[token.markupId])
            is TAGMLToken.MarkupMilestoneToken -> mct.addDirectedEdge(
                TAGEdge(listOf()),
                openMarkup.last(),
                TAGNode.TAGMarkupNode(token.qName)
            )
        }
    }
    return mct
}

fun TAGMCT.asDot(): String = DotFactory.fromTAGMCT(this)
