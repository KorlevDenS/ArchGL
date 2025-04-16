package domain.specific.lang.model


sealed class Action {
    abstract var data0: Data
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

// 1rst, gives data if there are no Generate or Read after it
data class AcceptRequestFrom(override var sender: Actor, override var data0: Data) : Action(), Accepting {
    companion object {
        val template: List<Token> = listOf(AcceptAction, RequestAction, FromAction, StructureName(""))
    }
}

data class AcceptFrom(override var sender: Actor, override var data0: Data) : Action(), Accepting {
    companion object {
        val template: List<Token> = listOf(AcceptAction, StructureName(""), FromAction, StructureName(""))
    }
}

// Last one
data class Return(override var data0: Data, var recipient: Actor) : Action() {
    companion object {
        val template: List<Token> = listOf(ReturnAction)
    }
}

// Begins data stream (only after accept) - Generative
data class Generate(override var data0: Data) : Action(), Generative {
    companion object {
        val template: List<Token> = listOf(GenerateAction, StructureName(""))
    }
}

data class Read(override var data0: Data) : Action(), Generative, UsingDB {
    companion object {
        val template: List<Token> = listOf(ReadAction, StructureName(""))
    }
}

data class ReadRelated(override var related: Data, override var data0: Data) : Action(), Generative, UsingDBRelated {
    companion object {
        val template: List<Token> = listOf(ReadAction, StructureName(""), RelatedAction, StructureName(""))
    }
}

// Intermediate
data class WorkWithObtaining(override var data0: Data, override var resData: Data) : Action(), Intermediate, Obtaining {
    companion object {
        val template: List<Token> =
            listOf(WorkAction, WithAction, ItAction, ObtainingAction, StructureName(""))
    }
}

data class WorkWith(override var data0: Data) : Action(), Intermediate {
    companion object {
        val template: List<Token> = listOf(WorkAction, WithAction, ItAction)
    }
}

data class SendToObtaining(override var data0: Data, var recipient: Actor, override var resData: Data) : Action(),
    Intermediate, Obtaining {
    companion object {
        val template: List<Token> = listOf(
            SendAction, ItAction, ToAction, StructureName(""),
            ObtainingAction, StructureName("")
        )
    }
}

// Actually ends data stream - Absorbing
data class SendTo(override var data0: Data, var recipient: Actor) : Action(), Absorbing {
    companion object {
        val template: List<Token> = listOf(SendAction, ItAction, ToAction, StructureName(""))
    }
}

data class Save(override var data0: Data) : Action(), Absorbing, UsingDB {
    companion object {
        val template: List<Token> = listOf(SaveAction, ItAction)
    }
}

data class SaveRelated(override var related: Data, override var data0: Data) : Action(), Absorbing, UsingDBRelated {
    companion object {
        val template: List<Token> = listOf(SaveAction, StructureName(""), RelatedAction, ItAction)
    }
}

data class Update(override var data0: Data) : Action(), Absorbing, UsingDB {
    companion object {
        val template: List<Token> = listOf(UpdateAction, ItAction)
    }
}

data class UpdateRelated(override var related: Data, override var data0: Data) : Action(), Absorbing, UsingDBRelated {
    companion object {
        val template: List<Token> = listOf(UpdateAction, StructureName(""), RelatedAction, ItAction)
    }
}

data class Delete(override var data0: Data) : Action(), Absorbing, UsingDB {
    companion object {
        val template: List<Token> = listOf(DeleteAction, ItAction)
    }
}

data class DeleteRelated(override var related: Data, override var data0: Data) : Action(), Absorbing, UsingDBRelated {
    companion object {
        val template: List<Token> = listOf(DeleteAction, StructureName(""), RelatedAction, ItAction)
    }
}
