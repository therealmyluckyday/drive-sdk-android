package axa.tex.drive.sdk.acquisition.collection.internal.db

import android.content.Context


const val PENDING_TRIP: String = "pendin-gtrips"
const val CONFIG: String = "config"


internal class CollectionDb {

    private var context: Context?

    constructor(context: Context?) {
        this.context = context
    }
}