package axa.tex.drive.sdk.acquisition.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*


class PendingTrip {


     var id : String = ""
     var tripId : String = ""
     var containsStop : Boolean = false

    constructor(){

    }


    constructor(id: String,tripId : String, containsStop: Boolean){
        this.tripId = tripId
        this.id = id;
        this.containsStop = containsStop
    }
}