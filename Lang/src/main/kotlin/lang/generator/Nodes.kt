package domain.specific.lang.generator

import domain.specific.lang.model.Action
import domain.specific.lang.model.Actor
import domain.specific.lang.model.Data
import domain.specific.lang.model.FR

sealed class ArchNode(
    val id: String,
    var usage: MutableMap<FR, MutableList<Action>> = mutableMapOf(),
) {

    abstract fun plantUml(): String

    fun addUsage(fr: FR, action: Action) {
        if (fr !in usage.keys) {
            usage.putIfAbsent(fr, mutableListOf())
        }
        usage[fr]!!.add(action)
    }

    override fun toString(): String {
        return id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArchNode

        return id == other.id
    }
}


class LoadBalancerNode(id: String): ArchNode(id + "LoadBalancer") {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("hexagon $id [\n")
        sb.append("$id\n")
        sb.append("]\n")
        return sb.toString()
    }

}

class DNS(): ArchNode( "DNS") {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("storage $id [\n")
        sb.append("$id\n")
        sb.append("]\n")
        return sb.toString()
    }

}

class ServiceDiscovery(node: ArchNode): ArchNode(node.id + "Discovery") {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("storage $id [\n")
        sb.append("$id\n")
        sb.append("]\n")
        return sb.toString()
    }

}

class ServerNode(id: String): ArchNode(id + "Server") {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("rectangle $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        for (key in usage.keys) {
            for (action in usage[key]!!) {
                sb.append("${key.id}: ${action.describe()}\n")
            }
        }
        sb.append("]\n")
        return sb.toString()
    }

}

class BalancedSeverNode(serverNode: ServerNode): ArchNode(serverNode.id + "s", serverNode.usage) {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("collections $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        for (key in usage.keys) {
            for (action in usage[key]!!) {
                sb.append("${key.id}: ${action.describe()}\n")
            }
        }
        sb.append("]\n")
        return sb.toString()
    }

}

class ProcessingServiceNode(id: String): ArchNode(id +  "ProcessingService") {
    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("rectangle $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        for (key in usage.keys) {
            for (action in usage[key]!!) {
                sb.append("${key.id}: ${action.describe()}\n")
            }
        }
        sb.append("]\n")
        return sb.toString()
    }
}

class GeneratingServiceNode(id: String): ArchNode(id +  "GeneratingService") {
    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("rectangle $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        for (key in usage.keys) {
            for (action in usage[key]!!) {
                sb.append("${key.id}: ${action.describe()}\n")
            }
        }
        sb.append("]\n")
        return sb.toString()
    }
}

class TransformingServiceNode(from: String, to: String): ArchNode("${from}To${to}Service") {
    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("rectangle $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        for (key in usage.keys) {
            for (action in usage[key]!!) {
                sb.append("${key.id}: ${action.describe()}\n")
            }
        }
        sb.append("]\n")
        return sb.toString()
    }
}

class DataServiceNode(id: String): ArchNode(id +  "DataService") {
    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("rectangle $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        for (key in usage.keys) {
            for (action in usage[key]!!) {
                sb.append("${key.id}: ${action.describe()}\n")
            }
        }
        sb.append("]\n")
        return sb.toString()
    }
}

class MessageQueue(): ArchNode("Queue") {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("queue $id #line:gray [\n")
        sb.append("$id\n")
        sb.append("\n")
        sb.append("]\n")
        return sb.toString()
    }

}

class MessageServiceNode(id: String): ArchNode(id +  "MessageService") {
    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("rectangle $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        for (key in usage.keys) {
            for (action in usage[key]!!) {
                sb.append("${key.id}: ${action.describe()}\n")
            }
        }
        sb.append("]\n")
        return sb.toString()
    }
}

class ExternalService(val data: Data): ArchNode( data.id +  "ExternalService") {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("storage $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        sb.append("Some ${data.type}\n")
        sb.append("external service\n")
        sb.append("\n")
        sb.append("]\n")
        return sb.toString()
    }

}

class ServerCondition(val serverNode: ServerNode): ArchNode(serverNode.id +  "Condition") {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("database $id #line:green;text:green [\n")
        sb.append("$id\n")
        sb.append("{}\n")
        sb.append("NO SQL\n")
        sb.append("\n")
        sb.append("]\n")
        return sb.toString()
    }

}

class DataNode(val data: Data): ArchNode(data.id + "Data") {

    var readBytes = 0L
    var readAmount = 0L

    var createdBytes = 0L
    var createdAmount = 0L

    var updatedBytes = 0L
    var updatedAmount = 0L

    var deletedBytes = 0L
    var deletedAmount = 0L

    var volumeAfterStorageTime = 0.0

    fun getPumlDataInfo(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Daily reading: ${readAmount}x ${data.type}, ${readBytes / (3 * 1024)} GB\n")
        sb.append("Daily creating: ${createdAmount}x ${data.type}, ${createdBytes / (3 * 1024)} GB\n")
        sb.append("Daily updating: ${updatedAmount}x ${data.type}, ${updatedBytes / (3 * 1024)} GB\n")
        sb.append("Daily deleting: ${deletedAmount}x ${data.type}, ${deletedBytes / (3 * 1024)} GB\n")
        sb.append("Volume after ${data.retention / 86400000} days: ${volumeAfterStorageTime.toLong() / (3 * 1024)} GB\n")
        sb.append("\n")
        for (key in usage.keys) {
            sb.append("${key.id}:\n")
            for (action in usage[key]!!) {
                sb.append("${action.describe()}\n")
            }
        }
        return sb.toString()
    }

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("database $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        sb.append(getPumlDataInfo())
        sb.append("]\n")
        return sb.toString()
    }

}

class Replication(val node: ArchNode): ArchNode(node.id + "Replicas") {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("rectangle $id #line.dashed {\n")
        sb.append(node.plantUml())
        sb.append("}\n")
        return sb.toString()
    }

}

interface SpecifiesData {
    val dataNode: DataNode
}

class SQLDatabaseNode(override val dataNode: DataNode): ArchNode(dataNode.data.id + "SQLDatabase"), SpecifiesData {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("database $id #line:aqua;text:aqua [\n")
        sb.append("$id\n")
        sb.append("\n")
        sb.append(dataNode.getPumlDataInfo())
        sb.append("]\n")
        return sb.toString()
    }

}

class ObjectStorageNode(override val dataNode: DataNode): ArchNode(dataNode.data.id + "ObjectStorage"), SpecifiesData {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("database $id #line:green;text:green [\n")
        sb.append("$id\n")
        sb.append("\n")
        sb.append(dataNode.getPumlDataInfo())
        sb.append("]\n")
        return sb.toString()
    }

}

class CacheNode(override val dataNode: DataNode): ArchNode(dataNode.data.id + "Cache"), SpecifiesData {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("frame $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        sb.append(dataNode.getPumlDataInfo())
        sb.append("]\n")
        return sb.toString()
    }

}

class DBCacheNode(val dataStore: SpecifiesData): ArchNode(dataStore.dataNode.data.id + "Cache") {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("frame $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        sb.append("Daily reading up to\n${dataStore.dataNode.readAmount}x ${dataStore.dataNode.data.type}, " +
                "${dataStore.dataNode.readBytes / (3 * 1024)} GB\n")
        sb.append("]\n")
        return sb.toString()
    }

}

class CDN(override val dataNode: DataNode): ArchNode(dataNode.data.id + "CDN"), SpecifiesData {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("cloud $id [\n")
        sb.append("$id\n")
        sb.append("Priority way of reading ${dataNode.data.id}!\n")
        sb.append("\n")
        sb.append("Daily reading up to\n${dataNode.readAmount}x ${dataNode.data.type}, " +
                "${dataNode.readBytes / (3 * 1024)} GB\n")
        sb.append("]\n")
        return sb.toString()
    }

}

class StaticDataCDN(val actorNode: ActorNode): ArchNode(actorNode.id + "CDN") {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("cloud $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        sb.append("Static data for ${actorNode.id}\n")
        sb.append("]\n")
        return sb.toString()
    }

}

class ActorNode(val actor: Actor): ArchNode(actor.id + "Actor") {

    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("actor $id [\n")
        sb.append("$id\n")
        sb.append("${actor.type}\n")
        sb.append("]\n")
        return sb.toString()
    }
}

class ActorConsumerNode(id: String): ArchNode(id + "ActorConsumer") {
    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("actor $id [\n")
        sb.append("$id\n")
        sb.append("\n")
        for (key in usage.keys) {
            for (action in usage[key]!!) {
                sb.append("${key.id}: ${action.describe()}\n")
            }
        }
        sb.append("]\n")
        return sb.toString()
    }
}


