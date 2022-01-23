package com.example.databaseadapterapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import com.example.addressbook.data.AddressBookDbHelper
import com.example.addressbook.data.ContactEntry
import java.util.ArrayList

class DatabaseAdapter(context: Context) {
    private val dbHelper: AddressBookDbHelper = AddressBookDbHelper(context.applicationContext)
    private var database: SQLiteDatabase? = null

    fun open(): DatabaseAdapter {
        database = dbHelper.writableDatabase
        return this
    }

    fun close() {
        dbHelper.close()
    }

    private val allEntries: Cursor
        private get() {
            val columns = arrayOf<String>(
                AddressBookDbHelper.Contract.COLUMN_ID,
                AddressBookDbHelper.Contract.COLUMN_NAME,
                AddressBookDbHelper.Contract.COLUMN_PHONE,
                AddressBookDbHelper.Contract.COLUMN_IMAGE
            )
            return database!!.query(AddressBookDbHelper.Contract.TABLE_NAME, columns, null, null, null, null, AddressBookDbHelper.Contract.COLUMN_NAME)
        }

    val contacts: List<ContactEntry>
        get() {
            val contacts: ArrayList<ContactEntry> = ArrayList<ContactEntry>()
            val cursor = allEntries

            val indexId = cursor.getColumnIndex(AddressBookDbHelper.Contract.COLUMN_ID)
            val indexName = cursor.getColumnIndex(AddressBookDbHelper.Contract.COLUMN_NAME)
            val indexPhone = cursor.getColumnIndex(AddressBookDbHelper.Contract.COLUMN_PHONE)
            val indexImage = cursor.getColumnIndex(AddressBookDbHelper.Contract.COLUMN_IMAGE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(indexId)
                val name = cursor.getString(indexName)
                val year = cursor.getString(indexPhone)
                val image = cursor.getBlob(indexImage)
                contacts.add(ContactEntry(id, name, year, image))
            }
            cursor.close()
            return contacts
        }

    val count: Long
        get() = DatabaseUtils.queryNumEntries(database, AddressBookDbHelper.Contract.TABLE_NAME)

    fun insert(entry: ContactEntry): Long {
        val cv = ContentValues()
        cv.put(AddressBookDbHelper.Contract.COLUMN_NAME, entry.name)
        cv.put(AddressBookDbHelper.Contract.COLUMN_PHONE, entry.phone)
        cv.put(AddressBookDbHelper.Contract.COLUMN_IMAGE, entry.image)
        return database!!.insert(AddressBookDbHelper.Contract.TABLE_NAME, null, cv)
    }

    fun delete(id: Long): Long {
        val whereClause = "_id = ?"
        val whereArgs = arrayOf(id.toString())
        return database!!.delete(AddressBookDbHelper.Contract.TABLE_NAME, whereClause, whereArgs).toLong()
    }

    fun update(entry: ContactEntry): Long {
        val whereClause: String = AddressBookDbHelper.Contract.COLUMN_ID.toString() + "=" + entry.id
        val cv = ContentValues()
        cv.put(AddressBookDbHelper.Contract.COLUMN_NAME, entry.name)
        cv.put(AddressBookDbHelper.Contract.COLUMN_PHONE, entry.phone)
        cv.put(AddressBookDbHelper.Contract.COLUMN_IMAGE, entry.image)
        return database!!.update(AddressBookDbHelper.Contract.TABLE_NAME, cv, whereClause, null).toLong()
    }

}