package com.example.jatzuk.criminalintent.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CrimeBaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, VERSION) {
    companion object {
        private const val VERSION = 1
        private const val DATABASE_NAME = "crimeBase.db"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "create table $NAME (" +
                    "_id integer primary key autoincrement, " +
                    "${Cols.UUID}, " +
                    "${Cols.TITLE}, " +
                    "${Cols.DATE}, " +
                    "${Cols.SOLVED}," +
                    "${Cols.SUSPECT})"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}