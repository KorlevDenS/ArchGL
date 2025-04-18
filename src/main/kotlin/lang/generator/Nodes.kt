package domain.specific.lang.generator

import domain.specific.lang.model.Action
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

class ServerNode(id: String) : ArchNode(id + "Server") {
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

class DataNode(id: String): ArchNode(id + "Data") {
    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("database $id [\n")
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

class ActorNode(id: String): ArchNode(id + "Actor") {
    override fun plantUml(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("actor $id [\n")
        sb.append("$id\n")
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


