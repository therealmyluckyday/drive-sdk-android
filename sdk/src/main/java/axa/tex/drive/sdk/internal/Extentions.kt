package axa.tex.drive.sdk.internal.extension

import com.fasterxml.jackson.databind.ObjectMapper


fun Any.toJson() : String{
    return try {
        val mapper = ObjectMapper();
        mapper.writeValueAsString(this);
    }catch (e : Exception){
        e.printStackTrace()
        "{}";
    }
}

