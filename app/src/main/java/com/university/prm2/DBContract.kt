package com.university.prm2
import android.provider.BaseColumns

object DBContract {

    class PlaceEntry : BaseColumns {
        companion object {
            val TABLE_NAME = "places"
            val COLUMN_NAME = "PlaceName"
            val COLUMN_DESC = "PlaceDesc"
            val COLUMN_USER = "User"
        }
    }
}