package domain.specific.lang.generator

class ArchGraph {

    private val adjacencyList: MutableMap<ArchNode, MutableList<ArchNode>> = mutableMapOf()

    fun addAndFillNodeIfNotFound(node: ArchNode): ArchNode {
        val maybeExistingNode = findNodeById(node.id)
        maybeExistingNode?.let {
            for ((key, value) in node.usage) {
                if (maybeExistingNode.usage.containsKey(key)) {
                    maybeExistingNode.usage[key]?.addAll(value)
                } else {
                    maybeExistingNode.usage[key] = value.toMutableList()
                }
            }
            return maybeExistingNode
        } ?: run {
            adjacencyList.putIfAbsent(node, mutableListOf())
            return node
        }
    }

    fun addNodeIfNotFound(node: ArchNode): ArchNode {
        val maybeExistingNode = findNodeById(node.id)
        maybeExistingNode?.let {
            return maybeExistingNode
        } ?: run {
            adjacencyList.putIfAbsent(node, mutableListOf())
            return node
        }
    }

    fun simpleAddConnection(from: ArchNode, to: ArchNode) {
        val fromNode = addNodeIfNotFound(from)
        val toNode = addNodeIfNotFound(to)
        if (toNode !in adjacencyList[fromNode]!!) {
            adjacencyList[fromNode]?.add(toNode)
        }
    }

    fun addConnection(from: ArchNode, to: ArchNode) {
        val fromNode = addNodeIfNotFound(from)
        val toNode = addAndFillNodeIfNotFound(to)
        if (toNode !in adjacencyList[fromNode]!!) {
            adjacencyList[fromNode]?.add(toNode)
        }
    }

    fun removeConnection(from: ArchNode, to: ArchNode) {
        if (from in adjacencyList.keys && to in adjacencyList.keys && to in adjacencyList[from]!!) {
            adjacencyList[from]!!.remove(to)
        }
    }

    fun removeNode(node: ArchNode) {
        if (node in adjacencyList.keys) {
            val predecessors = this.getPredecessors(node)
            for (predecessor in predecessors) {
                this.removeConnection(predecessor, node)
            }
            adjacencyList.remove(node)
        }
    }

    fun replaceNode(old: ArchNode, new: ArchNode) {
        if (old in adjacencyList.keys) {
            val predecessors = this.getPredecessors(old)
            for (predecessor in predecessors) {
                this.simpleAddConnection(predecessor, new)
            }
            val followers = this.getFollowers(old)
            for (follower in followers) {
                this.simpleAddConnection(new, follower)
            }
            this.removeNode(old)
        }
    }

    fun getPredecessors(node: ArchNode): List<ArchNode> {
        val predecessors: MutableList<ArchNode> = mutableListOf()
        for (key in adjacencyList.keys) {
            if (node in adjacencyList[key]!!) {
                predecessors.add(key)
            }
        }
        return predecessors
    }

    fun getFollowers(node: ArchNode): List<ArchNode> {
        val followers = adjacencyList[node]
        followers?.let {
            return followers
        } ?: run {
            throw StructureException("No node $node in ArchGraph!")
        }
    }

    fun findNodeById(id: String): ArchNode? {
        return adjacencyList.keys.find { it.id == id }
    }

    fun getAllNodes(): List<ArchNode> {
        return adjacencyList.keys.toList()
    }

    fun getAllConnections(): List<Pair<ArchNode, ArchNode>> {
        val connections: MutableList<Pair<ArchNode, ArchNode>> = mutableListOf()
        for ((vertex, neighbors) in adjacencyList) {
            if (neighbors.isNotEmpty()) {
                for (neighbor in neighbors) {
                    connections.add(Pair(vertex, neighbor))
                }
            }
        }
        return connections
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("Directed Graph:\n")
        for ((vertex, neighbors) in adjacencyList) {
            if (neighbors.isNotEmpty()) {
                sb.append("$vertex -> ${neighbors.joinToString(", ")}\n")
            }
        }
        return sb.toString()
    }

}