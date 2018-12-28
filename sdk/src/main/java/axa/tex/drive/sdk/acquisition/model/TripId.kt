package axa.tex.drive.sdk.acquisition.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.util.*

class TripId{

   var value : String = ""


    constructor(value : String){
        this.value  = value
    }

    constructor()

    fun toJson() : String{
        val mapper = ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true)
        return mapper.writeValueAsString(this)
    }

}