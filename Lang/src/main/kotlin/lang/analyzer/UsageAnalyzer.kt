package domain.specific.lang.analyzer

import domain.specific.lang.generator.ArchGraph
import domain.specific.lang.generator.DataNode
import domain.specific.lang.model.Action
import domain.specific.lang.model.Delete
import domain.specific.lang.model.DeleteRelated
import domain.specific.lang.model.Read
import domain.specific.lang.model.ReadRelated
import domain.specific.lang.model.Save
import domain.specific.lang.model.SaveRelated
import domain.specific.lang.model.SendingTo
import domain.specific.lang.model.Update
import domain.specific.lang.model.UpdateRelated
import domain.specific.lang.model.UsingDB
import domain.specific.lang.model.UsingDBRelated

class UsageAnalyzer(val graph: ArchGraph) {

    private fun DataNode.updateDataUsage(action: UsingDB, frequency: Long) {

        if (action is UsingDBRelated && action.related.id == this.data.id) {
            this.readAmount += frequency
            this.readBytes += frequency * this.data.unitVolume
        }

        if (action is Action && action.data0.id == this.data.id) {
            when (action) {
                is Delete, is DeleteRelated -> {
                    this.deletedAmount += frequency
                    this.deletedBytes += frequency * this.data.unitVolume
                }

                is Read, is ReadRelated -> {
                    this.readAmount += frequency
                    this.readBytes += frequency * this.data.unitVolume
                }

                is Save, is SaveRelated -> {
                    this.createdAmount += frequency
                    this.createdBytes += frequency * this.data.unitVolume
                }

                is Update, is UpdateRelated -> {
                    this.updatedAmount += frequency
                    this.updatedBytes += frequency * this.data.unitVolume
                }

                else -> {}
            }
        }
    }

    private fun DataNode.updateRecipientDataUsage(frequency: Long) {
        this.readAmount += frequency
        this.readBytes += frequency * this.data.unitVolume
    }

    private fun calcDatabaseUsage() {
        for (node in graph.getAllNodes()) {
            if (node is DataNode) {
                for (fr in node.usage.keys) {
                    for (action in node.usage[fr]!!) {
                        if (action is UsingDB) {
                            node.updateDataUsage(action, fr.frequency)
                        } else if (action is SendingTo) {
                            node.updateRecipientDataUsage( fr.frequency)
                        }
                    }
                }
                val savedDuringPeriod = (node.data.retention / 86400000.0) * node.createdBytes
                val deletedDuringPeriod = (node.data.retention / 86400000.0) * node.deletedAmount
                node.volumeAfterStorageTime = savedDuringPeriod - deletedDuringPeriod
            }
        }
    }

    fun analyze(): ArchGraph {
        calcDatabaseUsage()
        return graph
    }

}