package domain.specific.lang.expander

import domain.specific.lang.generator.ActorNode
import domain.specific.lang.generator.ArchGraph
import domain.specific.lang.generator.ArchNode
import domain.specific.lang.generator.BalancedSeverNode
import domain.specific.lang.generator.CacheNode
import domain.specific.lang.generator.DBCacheNode
import domain.specific.lang.generator.DNS
import domain.specific.lang.generator.LoadBalancerNode
import domain.specific.lang.generator.DataNode
import domain.specific.lang.generator.ObjectStorageNode
import domain.specific.lang.generator.SQLDatabaseNode
import domain.specific.lang.generator.ServerCondition
import domain.specific.lang.generator.ServerNode
import domain.specific.lang.generator.ServiceDiscovery
import domain.specific.lang.generator.SpecifiesData
import domain.specific.lang.model.Application

class Expander(
    private val graph: ArchGraph,
    private val semanticTree: Application
) {

    private fun expandActors() {
        val actors = graph.getAllNodes().filter { it is ActorNode && it.actor.type == "web-client"}
        val dnsNode = DNS()
        for (actor in actors) {
            graph.simpleAddConnection(actor, dnsNode)
        }
    }

    private fun expandFaultTolerance() {
        if (semanticTree.faultTolerance == "yes") {
            val servers = graph.getAllNodes().filter { it is ServerNode }
            for (server in servers) {
                val serverCondition = ServerCondition(server as ServerNode)
                graph.simpleAddConnection(server, serverCondition)
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
                    graph.simpleAddConnection(predecessor, loadBalancerNode)
                }
                val balancedSeverNode = BalancedSeverNode(server as ServerNode)
                graph.simpleAddConnection(loadBalancerNode, balancedSeverNode)
                val followers = graph.getFollowers(server)
                for (node in followers) {
                    graph.simpleAddConnection(balancedSeverNode, node)
                }
                graph.removeNode(server)
                val serviceDiscoveryNode = ServiceDiscovery(balancedSeverNode)
                graph.simpleAddConnection(loadBalancerNode, serviceDiscoveryNode)
            }
        }
    }

    private fun specifyStorages() {
        val dataNodes = graph.getAllNodes().filter { it is DataNode }.map { it as DataNode }
        for (db in dataNodes) {
            if (db.data.retention <= 300000) {
                val cacheNode = CacheNode(db)
                graph.replaceNode(db, cacheNode)
            } else {
                when (db.data.type) {
                    "image", "text", "html", "file", "video", "videoStream", "audio", "audioStream" -> {
                        val objectStorageNode = ObjectStorageNode(db)
                        graph.replaceNode(db, objectStorageNode)
                    }
                    "structuredText", "number", "statusCode",  "notification", "actor-data" -> {
                        val sqlDatabaseNode = SQLDatabaseNode(db)
                        graph.replaceNode(db, sqlDatabaseNode)
                    }
                }
            }
        }
    }

    private fun addCachesToStorages() {
        val dataNodes = graph.getAllNodes().filter { it is SpecifiesData && it !is CacheNode}.map { it as SpecifiesData }
        for (db in dataNodes) {
            val dailyMutableAmount = db.dataNode.createdAmount + db.dataNode.updatedAmount + db.dataNode.deletedAmount
            if (db.dataNode.readAmount >= dailyMutableAmount && db.dataNode.readAmount != 0L) {
                val dbCacheNode = DBCacheNode(db)
                val predecessors = graph.getPredecessors(db as ArchNode).filter { it.id != db.id }
                for (predecessor in predecessors) {
                    graph.simpleAddConnection(predecessor, dbCacheNode)
                    graph.removeConnection(predecessor, db)
                }
                graph.simpleAddConnection(dbCacheNode, db)
            }
        }
    }

    private fun addCDN() {

    }

    private fun expandLatency() {
        if (semanticTree.latency == "middle" || semanticTree.latency == "low") {
            specifyStorages()
            addCachesToStorages()
        }
        if (semanticTree.latency == "low") {

        }
    }

    fun expand(): ArchGraph {
        expandActors()
        expandFaultTolerance()
        expandEfficiency()
        expandLatency()
        return graph
    }

}