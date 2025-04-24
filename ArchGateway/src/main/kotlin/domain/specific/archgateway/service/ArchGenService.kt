package domain.specific.archgateway.service

import domain.specific.lang.generator.ArchGenerator
import domain.specific.lang.lexer.Lexer
import domain.specific.lang.model.Application
import domain.specific.lang.model.EOF
import domain.specific.lang.model.Token
import domain.specific.lang.parser.Parser
import domain.specific.lang.parser.ParserException
import domain.specific.lang.uml.UmlGenerator
import org.springframework.stereotype.Service

@Service
class ArchGenService {

    fun generateArch(program: String): String {
        val tokens: MutableList<Token> = mutableListOf()
        val tokenTextPositions: MutableList<Int> = mutableListOf()
        val lexer = Lexer(program)

        while (true) {
            val token = lexer.nextToken()
            if (token.second == EOF) {
                break
            } else {
                tokenTextPositions.add(token.first)
                tokens.add(token.second)
            }
        }

        val parser = Parser(tokens)
        val semanticTree: Application
        try {
            semanticTree = parser.parse()
        } catch (e: ParserException) {
            if (e.tokenIndex < tokenTextPositions.size) {
                e.tokenIndex = tokenTextPositions[e.tokenIndex]
            } else {
                e.tokenIndex = tokenTextPositions.last()
            }
            throw e
        }
//        println(semanticTree)

        val generator = ArchGenerator(semanticTree)
        val graph = generator.generate()
        val umlGenerator = UmlGenerator(graph)
        println(umlGenerator.generateUml())
        return umlGenerator.generateUml()
//        println(graph)
    }

}
