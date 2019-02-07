package axa.tex.drive.sdk.acquisition.collection.internal.db.queue

class PersistablePacket : Comparable<PersistablePacket>{

    var tripId : String? = null
    var appName : String? = null
    var clientId : String? = null
    var data : String? = null
    var packetNumber : Int = 0
    var end : Boolean = false

    constructor()

    constructor(tripId : String,appName : String, clientId : String, data : String, packetNumber: Int, end:Boolean){
        this.tripId = tripId
        this.appName = appName
        this.clientId = clientId
        this.data = data
        this.packetNumber = packetNumber
        this.end = end
    }

    override fun compareTo(other: PersistablePacket): Int {
        return packetNumber - other.packetNumber
    }

}