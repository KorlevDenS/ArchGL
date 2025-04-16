package domain.specific.lang.generator

import domain.specific.lang.model.*

class ArchGenerator(
    private val semanticTree: Application
) {
    private val graph: ArchGraph = ArchGraph()

    fun generate(): ArchGraph {

        for (fr in semanticTree.frs) {
            var nodeToContinue: ArchNode

            val serverNode = ServerNode("Web")
            graph.addConnection(ActorNode((fr.actions[0] as Accepting).sender.id), serverNode)
            nodeToContinue = serverNode

            for (index in 1..<fr.actions.size) {
                when (val action = fr.actions[index]) {
                    is UsingDB -> {
                        val serviceNode = ServiceNode(action.data0.id)
                        graph.addConnection(nodeToContinue, serviceNode)
                        graph.addConnection(serviceNode, DataNode(action.data0.id))
                        nodeToContinue = serviceNode
                        if (action is UsingDBRelated) {
                            graph.addConnection(serviceNode, DataNode(action.related.id))
                        }
                    }
                    is WorkWith -> {
                        val serviceNode = ServiceNode(action.data0.id)
                        graph.addConnection(nodeToContinue, serviceNode)
                        nodeToContinue = serviceNode
                    }
                    is WorkWithObtaining -> {
                        val serviceNode = ServiceNode(
                            action.data0.id + "To" + action.resData.id
                        )
                        graph.addConnection(nodeToContinue, serviceNode)
                        nodeToContinue = serviceNode
                    }
                    is SendTo -> {
                        val messageServiceNode = MessageServiceNode(action.data0.id)
                        graph.addConnection(nodeToContinue, messageServiceNode)
                        graph.addConnection(messageServiceNode, DataNode(action.recipient.id))
                        graph.addConnection(messageServiceNode, ActorConsumerNode(action.recipient.id))
                    }
                    else -> {}
                }
            }
        }

        return graph
    }

}