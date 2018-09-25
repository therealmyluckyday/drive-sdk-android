package axa.tex.drive.sdk.acquisition.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class Data (val timestamp: Long,val motion : Fix? = null, val location : Fix? = null, val battery : Fix? = null)