package domain.specific.lang.generator

import domain.specific.lang.model.*
import kotlin.jvm.Throws

class ArchGenerator(
    private val semanticTree: Application
) {
    private val graph: ArchGraph = ArchGraph()

    private fun addAbsorbBranches(action: Action, nodeToContinue: ArchNode, fr: FR) {
        if (action is PrecedeAbsorbing) {
            for (a in action.absorbers) {
                when (a) {
                    is UsingDB -> {
                        val dataServiceNode = DataServiceNode(a.data0.id)
                        dataServiceNode.addUsage(fr, a)
                        graph.addConnection(nodeToContinue, dataServiceNode)
                        val DataNode = DataNode(a.data0)
                        DataNode.addUsage(fr, a)
                        graph.addConnection(dataServiceNode, DataNode)
                        if (a is UsingDBRelated) {
                            val relatedDataNode = DataNode(a.related)
                            relatedDataNode.addUsage(fr, a)
                            graph.addConnection(dataServiceNode, relatedDataNode)
                        }
                    }
                    is SendTo -> {
                        val messageServiceNode = MessageServiceNode(a.data0.id)
                        messageServiceNode.addUsage(fr, a)
                        graph.addConnection(nodeToContinue, messageServiceNode)
                        val DataNode = DataNode(semanticTree.findDataById(a.recipient.id))
                        DataNode.addUsage(fr, a)
                        graph.addConnection(messageServiceNode, DataNode)
                        val actorConsumerNode = ActorConsumerNode(a.recipient.id)
                        actorConsumerNode.addUsage(fr, a)
                        graph.addConnection(messageServiceNode, actorConsumerNode)
                    }
                    else -> {}
                }
            }
        }
    }

    fun getFirstNodeToContinue(action: Action): ArchNode {
        when (action) {

            is AcceptingLink -> {
                return ServerNode("Web")
            }

            is Process -> {
                return ProcessingServiceNode(action.data0.id)
            }

            is Generate -> {
                return GeneratingServiceNode(action.data0.id)
            }

            is ProcessObtaining -> {
                return TransformingServiceNode(action.data0.id, action.resData.id)
            }

            is SendToObtaining -> {
                return MessageServiceNode(action.data0.id)
            }

            is Read, is ReadRelated -> {
                return DataServiceNode(action.data0.id)
            }

            else -> {
                throw StructureException("Unknown first node action")
            }
        }
    }

    @Throws(StructureException::class)
    fun generate(): ArchGraph {

        for (fr in semanticTree.frs) {
            var nodeToContinue: ArchNode

            if (fr.actions[0] is Accepting) {
                val serverNode: ServerNode = when ((fr.actions[0] as Accepting).sender.type) {
                    "web-client" -> {
                        ServerNode("Web")
                    }

                    "service" -> {
                        ServerNode("Consumer")
                    }

                    "scheduler" -> {
                        ServerNode("Scheduler")
                    }

                    else -> {
                        ServerNode("")
                    }
                }
                serverNode.addUsage(fr, fr.actions[0])
                val actorNode = ActorNode((fr.actions[0] as Accepting).sender)
                actorNode.addUsage(fr, fr.actions[0])
                graph.addConnection(actorNode, serverNode)
                nodeToContinue = serverNode
                addAbsorbBranches(fr.actions[0], nodeToContinue, fr)
            } else {
                nodeToContinue = getFirstNodeToContinue(fr.actions[0])
            }

            for (index in 1..<fr.actions.size) {
                when (val action = fr.actions[index]) {
                    is Accepting -> {

                    }
                    is Process -> {
                        val dataProcessingServiceNode = ProcessingServiceNode(action.data0.id)
                        dataProcessingServiceNode.addUsage(fr, action)
                        graph.addConnection(nodeToContinue, dataProcessingServiceNode)
                        nodeToContinue = dataProcessingServiceNode
                    }
                    is Generate -> {
                        val dataGeneratingServiceNode = GeneratingServiceNode(action.data0.id)
                        dataGeneratingServiceNode.addUsage(fr, action)
                        graph.addConnection(nodeToContinue, dataGeneratingServiceNode)
                        nodeToContinue = dataGeneratingServiceNode
                    }
                    is ProcessObtaining -> {
                        val dataProcessingServiceNode = TransformingServiceNode(
                            action.data0.id,
                            action.resData.id
                        )
                        dataProcessingServiceNode.addUsage(fr, action)
                        graph.addConnection(nodeToContinue, dataProcessingServiceNode)
                        nodeToContinue = dataProcessingServiceNode
                    }
                    is SendToObtaining -> {
                        val messageServiceNode = MessageServiceNode(action.data0.id)
                        messageServiceNode.addUsage(fr, action)
                        graph.addConnection(nodeToContinue, messageServiceNode)
                        val DataNode = DataNode(semanticTree.findDataById(action.recipient.id))
                        DataNode.addUsage(fr, action)
                        graph.addConnection(messageServiceNode, DataNode)
                        val actorConsumerNode = ActorConsumerNode(action.recipient.id)
                        actorConsumerNode.addUsage(fr, action)
                        graph.addConnection(messageServiceNode, actorConsumerNode)
                        nodeToContinue = messageServiceNode
                    }
                    is Read, is ReadRelated -> {
                        val dataServiceNode = DataServiceNode(action.data0.id)
                        dataServiceNode.addUsage(fr, action)
                        graph.addConnection(nodeToContinue, dataServiceNode)
                        val DataNode = DataNode(action.data0)
                        DataNode.addUsage(fr, action)
                        graph.addConnection(dataServiceNode, DataNode)
                        nodeToContinue = dataServiceNode
                        if (action is UsingDBRelated) {
                            val relatedDataNode = DataNode(action.related)
                            relatedDataNode.addUsage(fr, action)
                            graph.addConnection(dataServiceNode, relatedDataNode)
                        }
                    }
                    else -> {}
                }
                addAbsorbBranches(fr.actions[index], nodeToContinue, fr)
            }
        }

        return graph
    }

}