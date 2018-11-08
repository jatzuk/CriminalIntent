package com.example.jatzuk.criminalintent

import android.content.ContentValues
import com.example.jatzuk.criminalintent.database.Cols
import com.example.jatzuk.criminalintent.database.CrimeBaseHelper
import com.example.jatzuk.criminalintent.database.CrimeCursorWrapper
import com.example.jatzuk.criminalintent.database.NAME
import java.io.File
import java.util.*

object CrimeLab {
    private val database = CrimeBaseHelper(ApplicationContext.context.applicationContext!!).writableDatabase

    fun addCrime(crime: Crime) {
        database.insert(NAME, null, getContentValues(crime))
    }

    fun updateCrime(crime: Crime) {
        if (crime.title.isNotEmpty())
            database.update(NAME, getContentValues(crime), Cols.UUID + " = ?", arrayOf(crime.uuid.toString()))
        else removeCrime(crime.uuid)
    }

    fun getCrime(uuid: UUID): Crime? {
        queryCrimes(Cols.UUID + " = ?", arrayOf("$uuid")).use {
            if (it.count == 0) return null
            it.moveToFirst()
            return it.getCrime()
        }
    }

    fun removeCrime(uuid: UUID) {
        database.delete(NAME, Cols.UUID + " = ?", arrayOf("$uuid"))
    }

    fun getCrimes(): List<Crime> {
        val crimes = ArrayList<Crime>()
        queryCrimes(null, null).use { cursor ->
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                crimes.add(cursor.getCrime())
                cursor.moveToNext()
            }
        }
        return crimes
    }

    fun getPhotoFile(crime: Crime) = File(ApplicationContext.context.filesDir, crime.getPhotoFileName())

    private fun getContentValues(crime: Crime): ContentValues {
        return ContentValues().apply {
            put(Cols.UUID, crime.uuid.toString())
            put(Cols.TITLE, crime.title)
            put(Cols.DATE, crime.date.time)
            put(Cols.SOLVED, if (crime.isSolved) 1 else 0)
            put(Cols.SUSPECT, crime.suspect)
        }
    }

    private fun queryCrimes(whereClause: String?, whereArgs: Array<String>?): CrimeCursorWrapper {
        return CrimeCursorWrapper(
                database.query(
                        NAME,
                        null,
                        whereClause,
                        whereArgs,
                        null,
                        null,
                        null))
    }
}