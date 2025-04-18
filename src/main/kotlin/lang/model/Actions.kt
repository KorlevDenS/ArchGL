package domain.specific.lang.model


sealed class Action {
    abstract var data0: Data
    abstract fun describe(): String
}

interface Generative
interface Intermediate
interface Absorbing
interface UsingDB
interface UsingDBRelated: UsingDB {
    var related: Data
}
interface Obtaining {
    var resData: Data
}
interface Accepting {
    var sender: Actor
}
interface PrecedeAbsorbing {
    var absorbers: MutableList<Action>
}

data class Dummy(override var data0: Data = Data.DefaultRequest): Action() {
    override fun describe(): String {
        return "Do nothing"
    }
}

// 1rst, gives data if there are no Generate or Read after it
data class AcceptRequestFrom(override var sender: Actor, override var data0: Data) : Action(), Accepting {
    override fun describe(): String {
        val request: String = if (data0 == Data.DefaultRequest) {
            "request"
        } else {
            data0.id
        }
        return "Accept $request from ${sender.id}"
    }

    companion object {
        val template: List<Token> = listOf(AcceptAction, RequestAction, FromAction, StructureName(""))
    }
}

data class AcceptFrom(
    override var sender: Actor,
    override var data0: Data,
    override var absorbers: MutableList<Action> = mutableListOf()
) : Action(), Accepting, PrecedeAbsorbing {
    override fun describe(): String {
        return "Accept ${data0.id} from ${sender.id}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AcceptFrom

        if (sender != other.sender) return false
        if (data0 != other.data0) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sender.hashCode()
        result = 31 * result + data0.hashCode()
        return result
    }

    companion object {
        val template: List<Token> = listOf(AcceptAction, StructureName(""), FromAction, StructureName(""))
    }
}

// Last one
data class Return(override var data0: Data, var recipient: Actor) : Action() {
    override fun describe(): String {
        return "Return ${data0.id} to ${recipient.id}"
    }

    companion object {
        val template: List<Token> = listOf(ReturnAction)
    }
}

// Begins data stream (only after accept) - Generative
data class Generate(
    override var data0: Data,
    override var absorbers: MutableList<Action> = mutableListOf()
) : Action(), Generative, PrecedeAbsorbing {
    override fun describe(): String {
        return "Generate ${data0.id}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Generate

        return data0 == other.data0
    }

    override fun hashCode(): Int {
        return data0.hashCode()
    }

    companion object {
        val template: List<Token> = listOf(GenerateAction, StructureName(""))
    }
}

data class Read(
    override var data0: Data,
    override var absorbers: MutableList<Action> = mutableListOf()
) : Action(), Generative, UsingDB, PrecedeAbsorbing {
    override fun describe(): String {
        return "Read ${data0.id} from storage"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Read

        return data0 == other.data0
    }

    override fun hashCode(): Int {
        return data0.hashCode()
    }

    companion object {
        val template: List<Token> = listOf(ReadAction, StructureName(""))
    }
}

data class ReadRelated(
    override var related: Data,
    override var data0: Data,
    override var absorbers: MutableList<Action> = mutableListOf()
) : Action(), Generative, UsingDBRelated, PrecedeAbsorbing {
    override fun describe(): String {
        return "Read ${related.id}-related ${data0.id} from storage"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReadRelated

        if (related != other.related) return false
        if (data0 != other.data0) return false

        return true
    }

    override fun hashCode(): Int {
        var result = related.hashCode()
        result = 31 * result + data0.hashCode()
        return result
    }

    companion object {
        val template: List<Token> = listOf(ReadAction, StructureName(""), RelatedAction, StructureName(""))
    }
}

// Intermediate
data class ProcessObtaining(
    override var data0: Data,
    override var resData: Data,
    override var absorbers: MutableList<Action> = mutableListOf()
) : Action(), Intermediate, Obtaining, PrecedeAbsorbing {
    override fun describe(): String {
        return "Process ${data0.id} obtaining ${resData.id}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProcessObtaining

        if (data0 != other.data0) return false
        if (resData != other.resData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data0.hashCode()
        result = 31 * result + resData.hashCode()
        return result
    }

    companion object {
        val template: List<Token> =
            listOf(ProcessAction, ItAction, ObtainingAction, StructureName(""))
    }
}

data class Process(
    override var data0: Data,
    override var absorbers: MutableList<Action> = mutableListOf()
) : Action(), Intermediate, PrecedeAbsorbing {
    override fun describe(): String {
        return "Process ${data0.id}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Process

        return data0 == other.data0
    }

    override fun hashCode(): Int {
        return data0.hashCode()
    }

    companion object {
        val template: List<Token> = listOf(ProcessAction, ItAction)
    }
}

data class SendToObtaining(
    override var data0: Data,
    var recipient: Actor,
    override var resData: Data,
    override var absorbers: MutableList<Action> = mutableListOf()
) : Action(), Intermediate, Obtaining, PrecedeAbsorbing {

    override fun describe(): String {
        return "Send ${data0.id} to ${recipient.id} obtaining ${resData.id}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SendToObtaining

        if (data0 != other.data0) return false
        if (recipient != other.recipient) return false
        if (resData != other.resData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data0.hashCode()
        result = 31 * result + recipient.hashCode()
        result = 31 * result + resData.hashCode()
        return result
    }

    companion object {
        val template: List<Token> = listOf(
            SendAction, ItAction, ToAction, StructureName(""),
            ObtainingAction, StructureName("")
        )
    }
}

// Actually ends data stream - Absorbing
data class SendTo(override var data0: Data, var recipient: Actor) : Action(), Absorbing {
    override fun describe(): String {
        return "Send ${data0.id} to ${recipient.id}"
    }

    companion object {
        val template: List<Token> = listOf(SendAction, ItAction, ToAction, StructureName(""))
    }
}

data class Save(override var data0: Data) : Action(), Absorbing, UsingDB {
    override fun describe(): String {
        return "Save ${data0.id} to storage"
    }

    companion object {
        val template: List<Token> = listOf(SaveAction, ItAction)
    }
}

data class SaveRelated(override var related: Data, override var data0: Data) : Action(), Absorbing, UsingDBRelated {
    override fun describe(): String {
        return "Save ${related.id}-related ${data0.id} to storage"
    }

    companion object {
        val template: List<Token> = listOf(SaveAction, StructureName(""), RelatedAction, ItAction)
    }
}

data class Update(override var data0: Data) : Action(), Absorbing, UsingDB {
    override fun describe(): String {
        return "Update ${data0.id} in storage"
    }

    companion object {
        val template: List<Token> = listOf(UpdateAction, ItAction)
    }
}

data class UpdateRelated(override var related: Data, override var data0: Data) : Action(), Absorbing, UsingDBRelated {
    override fun describe(): String {
        return "Update ${related.id}-related ${data0.id} in storage"
    }

    companion object {
        val template: List<Token> = listOf(UpdateAction, StructureName(""), RelatedAction, ItAction)
    }
}

data class Delete(override var data0: Data) : Action(), Absorbing, UsingDB {
    override fun describe(): String {
        return "Delete ${data0.id} from storage"
    }

    companion object {
        val template: List<Token> = listOf(DeleteAction, ItAction)
    }
}

data class DeleteRelated(override var related: Data, override var data0: Data) : Action(), Absorbing, UsingDBRelated {
    override fun describe(): String {
        return "Delete ${related.id}-related ${data0.id} from storage"
    }

    companion object {
        val template: List<Token> = listOf(DeleteAction, StructureName(""), RelatedAction, ItAction)
    }
}
