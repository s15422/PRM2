package com.university.prm2

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.university.prm2.Place
import java.util.*

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    val context: Context = context;

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    @Throws(SQLiteConstraintException::class)
    fun insertPlace(place: Place): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(DBContract.PlaceEntry.COLUMN_NAME, place.name)
        values.put(DBContract.PlaceEntry.COLUMN_DESC, place.desc)
        values.put(DBContract.PlaceEntry.COLUMN_USER, place.user)

        // Insert the new row, returning the primary key value of the new row
        val newRowId = db.insert(DBContract.PlaceEntry.TABLE_NAME, null, values)

        return true
    }

    @Throws(SQLiteConstraintException::class)
    fun updatePlace(placeName: String, placeDesc : String): Boolean {
        val db = writableDatabase

        db.execSQL("UPDATE "+DBContract.PlaceEntry.TABLE_NAME+" SET "
                + DBContract.PlaceEntry.COLUMN_DESC + " = " + placeDesc + " WHERE "
                + DBContract.PlaceEntry.COLUMN_NAME + " = " + "'" + placeName + "'");
        return true
    }

    @Throws(SQLiteConstraintException::class)
    fun deletePlace(placeName: String): Boolean {
        // Gets the data repository in write mode
        val db = writableDatabase
        // Define 'where' part of query.
        val selection = DBContract.PlaceEntry.COLUMN_NAME + " LIKE ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(placeName)
        // Issue SQL statement.
        db.delete(DBContract.PlaceEntry.TABLE_NAME, selection, selectionArgs)

        return true
    }

    fun readPlace(placeName: String): ArrayList<Place> {
        val places = ArrayList<Place>()
        val db = writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("select * from " + DBContract.PlaceEntry.TABLE_NAME +
                    " WHERE " + DBContract.PlaceEntry.COLUMN_NAME + "='" + placeName + "'", null)
        } catch (e: SQLiteException) {
            // if table not yet present, create it
            db.execSQL(SQL_CREATE_ENTRIES)
            return ArrayList()
        }

        var name: String
        var desc: String
        var user: String
        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                name = cursor.getString(cursor.getColumnIndex(DBContract.PlaceEntry.COLUMN_NAME))
                desc = cursor.getString(cursor.getColumnIndex(DBContract.PlaceEntry.COLUMN_DESC))
                user = cursor.getString(cursor.getColumnIndex(DBContract.PlaceEntry.COLUMN_DESC))

                places.add(Place(name, desc, user))
                cursor.moveToNext()
            }
        }
        return places
    }

    fun readAllfromUser(user: String): ArrayList<Place> {
        val places = ArrayList<Place>()
        val db = writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("select * from " + DBContract.PlaceEntry.TABLE_NAME +
                    " WHERE " + DBContract.PlaceEntry.COLUMN_USER + "='" + user + "'", null)
        } catch (e: SQLiteException) {
            db.execSQL(SQL_CREATE_ENTRIES)
            return ArrayList()
        }

        var name: String
        var desc: String
        var user: String
        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                name = cursor.getString(cursor.getColumnIndex(DBContract.PlaceEntry.COLUMN_NAME))
                desc = cursor.getString(cursor.getColumnIndex(DBContract.PlaceEntry.COLUMN_DESC))
                user = cursor.getString(cursor.getColumnIndex(DBContract.PlaceEntry.COLUMN_USER))

                places.add(Place(name, desc, user))
                cursor.moveToNext()
            }
        }
        return places
    }


    companion object {
        // If you change the database schema, you must increment the database version.
        val DATABASE_VERSION = 2
        val DATABASE_NAME = "localDB.db"

        private val SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DBContract.PlaceEntry.TABLE_NAME + " (" +
                    DBContract.PlaceEntry.COLUMN_NAME + " TEXT," +
                    DBContract.PlaceEntry.COLUMN_DESC + " TEXT," +
                    DBContract.PlaceEntry.COLUMN_USER + " TEXT)"

        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBContract.PlaceEntry.TABLE_NAME
    }

}