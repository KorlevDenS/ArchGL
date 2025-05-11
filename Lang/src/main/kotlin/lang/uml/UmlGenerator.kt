package domain.specific.lang.uml

import domain.specific.lang.generator.*
import net.sourceforge.plantuml.SourceStringReader
import java.io.FileOutputStream

class UmlGenerator(val graph: ArchGraph) {

    private fun generatePlantUml(nodes: List<ArchNode>, edges: List<Pair<ArchNode, ArchNode>>): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("@startuml\n")
        stringBuilder.append("!theme materia-outline\n")
        stringBuilder.append("skinparam actorStyle awesome\n")
        nodes.forEach { node ->
            stringBuilder.append(node.plantUml())
        }
        edges.forEach { edge ->
            stringBuilder.append("${edge.first} --> ${edge.second}\n")
        }
        stringBuilder.append("@enduml")
        return stringBuilder.toString()
    }

    private fun generateDiagram(umlText: String, outputFilePath: String) {
        val reader = SourceStringReader(umlText)
        val outputStream = FileOutputStream(outputFilePath)
        reader.outputImage(outputStream)
        outputStream.close()
    }

    fun generateUml(): String {
        val umlText = generatePlantUml(graph.getAllNodes(), graph.getAllConnections())
        val outputFilePath = "architecture.png"
        generateDiagram(umlText, outputFilePath)
        return umlText
    }

}