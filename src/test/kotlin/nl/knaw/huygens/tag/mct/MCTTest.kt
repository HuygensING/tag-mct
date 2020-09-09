package nl.knaw.huygens.tag.mct

import nl.knaw.huc.di.tag.tagml.TAGMLParseResult
import nl.knaw.huc.di.tag.tagml.parse
import org.junit.jupiter.api.Test
import kotlin.test.fail

class MCTTest {

    @Test
    fun mct_with_nested_markup() {
        val tagml = ("""
            |[!{
            |  ":ontology": {
            |    "root": "tagml"
            |  }
            |}!]
            |[tagml>[book>[title>Foo Bar<title][chapter>[l>Lorem ipsum dolar amacet.<l]<chapter]<book]<tagml]
            |""".trimMargin())
        assertParsesToMCT(tagml)
    }

    @Test
    fun mct_with_overlap() {
        val tagml = ("""
            |[!{
            |  ":ontology": {
            |    "root": "tagml"
            |  }
            |}!]
            |[tagml|+X,+Y>[x|X>Romeo [y|Y>loves<x] Juliet<y]<tagml]
            |""".trimMargin())
        assertParsesToMCT(tagml)
    }

    @Test
    fun mct_with_discontinuity() {
        val tagml = ("""
            |[!{
            |  ":ontology": {
            |    "root": "tagml"
            |  }
            |}!]
            |[tagml>[q>To be<-q], wrote Shakespeare, [x a=1][+q>or not to be!<q]<tagml]
            |""".trimMargin())
        assertParsesToMCT(tagml)
    }

    private fun assertParsesToMCT(tagml: String) {
        when (val result = parse(tagml)) {
            is TAGMLParseResult.TAGMLParseSuccess -> {
                val mct = result.tokens.asMCT()
                val mctDot = mct.asDot()
                assert(mctDot.isNotEmpty())
                println(mctDot)
            }
            is TAGMLParseResult.TAGMLParseFailure -> fail(result.errors.joinToString("\n"))
        }
    }
}