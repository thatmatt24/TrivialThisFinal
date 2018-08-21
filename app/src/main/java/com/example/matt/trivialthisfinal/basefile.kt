package com.example.matt.trivialthisfinal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.UserHandle
import android.widget.Toast
import junit.runner.Version
import org.w3c.dom.UserDataHandler
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Created by CEOCarlAllen on 5/5/18.
 */
val DATABASE_NAME = "MyDB"
val TABLE_NAME = "Users"
val COL_IMAGES = "images"
val COL_ID = "id"
val COL_NAME = "name"


class baseFile(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null,1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE" + TABLE_NAME + "(" +
                COL_NAME + "VARCHAR(50)" +
                COL_ID + "INTEGER PRIMARY KEY AUTOINCREMENT)";

        db?.execSQL(createTable)
    }

   override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun insertData(user: UserDataHandler) {
        val db = this.writableDatabase
        var cv = ContentValues()

        cv.put(COL_NAME, user.toString())
        cv.put(COL_ID, user.toString())
        var result = db.insert(TABLE_NAME,null, cv)
        if(result == -1.toLong())
            Toast.makeText(context, "Falied", Toast.LENGTH_SHORT) .show()
        else
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT) .show()
    }

}
