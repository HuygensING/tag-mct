package nl.knaw.huygens.tag.mct

import nl.knaw.huc.di.tag.tagml.TAGMLParseResult
import nl.knaw.huc.di.tag.tagml.parse
import org.junit.jupiter.api.Test
import kotlin.test.fail

class MCTTest {

    @Test
    fun mct() {
        val tagml = ("""
            |[!{
            |  ":ontology": {
            |    "root": "tagml"
            |  }
            |}!]
            |[tagml>[book>[title>Foo Bar<title][chapter>[l>Lorem ipsum dolar amacet.<l]<chapter]<book]<tagml]
            |""".trimMargin())
        when (val result = parse(tagml)) {
            is TAGMLParseResult.TAGMLParseSuccess -> {
                val mct = result.tokens.asMCT()
                val mctDot = mct.asDot()
                println(mctDot)
            }
            is TAGMLParseResult.TAGMLParseFailure -> fail(result.errors.joinToString("\n"))
        }
    }
}

