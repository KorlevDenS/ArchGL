package domain.specific.lang.expander

import domain.specific.lang.generator.ActorNode
import domain.specific.lang.generator.ArchGraph
import domain.specific.lang.generator.DNS
import domain.specific.lang.generator.LoadBalancerNode
import domain.specific.lang.generator.ServerCondition
import domain.specific.lang.generator.ServerNode
import domain.specific.lang.model.Application

class Expander(
    private val graph: ArchGraph,
    private val semanticTree: Application
) {

    private fun expandActors() {
        val actors = graph.getAllNodes().filter { it is ActorNode && it.actor.type == "web-client"}
        val dnsNode = DNS()
        for (actor in actors) {
            graph.addConnection(actor, dnsNode)
        }
    }

    private fun expandFaultTolerance() {
        if (semanticTree.faultTolerance == "yes") {
            val servers = graph.getAllNodes().filter { it is ServerNode }
            for (server in servers) {
                val serverCondition = ServerCondition(server as ServerNode)
                graph.addConnection(server, serverCondition)
            }
        }
    }

    private fun expandEfficiency() {
        if (semanticTree.onlineUsersNumber > 500) {
            val servers = graph.getAllNodes().filter { it is ServerNode }
            for (server in servers) {
                val loadBalancerNode = LoadBalancerNode(server.id)
                val predecessors = graph.getPredecessors(server).filter { it.id != server.id }
                for (predecessor in predecessors) {
                    graph.removeConnection(predecessor, server)
                }
                //TODO
            }
        }
    }

    fun expand(): ArchGraph {
        expandActors()
        expandFaultTolerance()
        return graph
    }

}