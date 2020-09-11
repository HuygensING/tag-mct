package nl.knaw.huygens.tag.mct

import nl.knaw.huygens.tag.tagml.TAGMLParseResult
import nl.knaw.huygens.tag.tagml.TAGMLToken
import nl.knaw.huygens.tag.tagml.parse

object A {
    fun tokenize(tagml: String): List<TAGMLToken> {
        when (val result = parse(tagml)) {
            is TAGMLParseResult.TAGMLParseSuccess -> {
                return result.tokens
            }
            is TAGMLParseResult.TAGMLParseFailure -> System.err.println(result.errors.joinToString("\n"))

        }
        return listOf()
    }
}

