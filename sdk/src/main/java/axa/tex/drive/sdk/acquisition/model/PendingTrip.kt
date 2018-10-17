package axa.tex.drive.sdk.acquisition.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity
class PendingTrip {

    @PrimaryKey
    internal var id : String

    @ColumnInfo
    private  var tripId : String


    constructor(id: String,tripId : String){
        this.tripId = tripId
        this.id = id;
    }
}