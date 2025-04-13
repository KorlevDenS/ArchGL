package domain.specific.lang.parser

import domain.specific.lang.lexer.*


sealed class Action {
    abstract var data0: Data
}

// 1rst, gives data if there are no Generate or Read after it
data class AcceptRequestFrom(var sender: Actor, override var data0: Data): Action() {
    companion object {
        val template: List<Token> = listOf(AcceptAction, RequestAction, FromAction, StructureName(""))
    }
}

// Last one
data class Return(override var data0: Data, var recipient: Actor): Action() {
    companion object {
        val template: List<Token> = listOf(ReturnAction)
    }
}

interface Generative
interface Intermediate
interface Absorbing

// Begins data stream (only after accept) - Generative
data class Generate(override var data0: Data): Action(), Generative {
    companion object {
        val template: List<Token> = listOf(GenerateAction, StructureName(""))
    }
}
data class Read(override var data0: Data): Action(), Generative {
    companion object {
        val template: List<Token> = listOf(ReadAction, StructureName(""))
    }
}

// Intermediate
data class WorkWithObtaining(override var data0: Data, var workResult: Data): Action(), Intermediate {
    companion object {
        val template: List<Token> = listOf(WorkAction, WithAction, StructureName(""), ObtainingAction, StructureName(""))
    }
}
data class WorkWith(override var data0: Data): Action(), Intermediate {
    companion object {
        val template: List<Token> = listOf(WorkAction, WithAction, StructureName(""))
    }
}
data class SendToObtaining(override var data0: Data, var recipient: Actor, var answer: Data): Action(), Intermediate {
    companion object {
        val template: List<Token> = listOf(SendAction, StructureName(""), ToAction, StructureName(""),
            ObtainingAction, StructureName(""))
    }
}

// Actually ends data stream - Absorbing
data class SendTo(override var data0: Data, var recipient: Actor): Action(), Absorbing {
    companion object {
        val template: List<Token> = listOf(SendAction, StructureName(""), ToAction, StructureName(""))
    }
}
data class Save(override var data0: Data): Action(), Absorbing {
    companion object {
        val template: List<Token> = listOf(SaveAction, StructureName(""))
    }
}
data class Update(override var data0: Data): Action(), Absorbing {
    companion object {
        val template: List<Token> = listOf(UpdateAction, StructureName(""))
    }
}
data class Delete(override var data0: Data): Action(), Absorbing {
    companion object {
        val template: List<Token> = listOf(DeleteAction, StructureName(""))
    }
}
