package domain.specific.lang.parser


sealed class Action

data class SendTo(val recipient: Actor, val dataToSend: Data): Action()
data class Save(val dataToSave: Data): Action()
data class Read(val dataToRead: Data): Action()
data class Update(val dataToUpdate: Data): Action()
data class Delete(val dataToDelete: Data): Action()
data class WorkWith(val dataToWork: Data, val workResult: Data): Action()
data class Generate(val dataToGenerate: Data): Action()
data class Return(val dataToReturn: Data, val recipient: Actor): Action()
data class AcceptRequestFrom(val sender: Actor, val requestData: Data): Action()
