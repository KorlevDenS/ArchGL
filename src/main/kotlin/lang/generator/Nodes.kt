package domain.specific.lang.generator

sealed class ArchNode(val id: String) {

    abstract fun plantUml(): String

    override fun toString(): String {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ArchNode) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

class ServerNode(id: String) : ArchNode(id + "Server") {
    override fun plantUml(): String {
        return "rectangle $id {\n}\n"
    }
}

class ServiceNode(id: String): ArchNode(id +  "Service") {
    override fun plantUml(): String {
        return "rectangle $id {\n}\n"
    }
}

class MessageServiceNode(id: String): ArchNode(id +  "MessageService") {
    override fun plantUml(): String {
        return "rectangle $id {\n}\n"
    }
}

class DataNode(id: String): ArchNode(id + "Data") {
    override fun plantUml(): String {
        return "database $id {\n}\n"
    }
}

class ActorNode(id: String): ArchNode(id + "Actor") {
    override fun plantUml(): String {
        return "actor $id [\n" +
                "$id\n"+
                "]\n"
    }
}

class ActorConsumerNode(id: String): ArchNode(id + "ActorConsumer") {
    override fun plantUml(): String {
        return "rectangle $id [\n" +
                //"    <size:120><&wifi></size>\n" +
                "    $id\n" +
                "]\n"
    }
}


