package axa.tex.drive.sdk.acquisition.model

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.ObjectMapper


@JsonRootName(value = "")
data class Event (val event: List<String>,
                  val timestamp: Long):Fix(timestamp){

    override fun toJson(): String {
        return try {
            val mapper = ObjectMapper();
             mapper.writeValueAsString(this);
        }catch (e : Exception){
            e.printStackTrace()
            "{}";
        }
    }

    /*fun isStart() : Boolean{
        return !event.isEmpty() && event[0] == "start"
    }

    fun isStop() : Boolean{
        return !event.isEmpty() && event[0] == "stop"
    }*/
}
