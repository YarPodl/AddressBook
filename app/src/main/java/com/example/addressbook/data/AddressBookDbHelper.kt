package com.example.addressbook.data
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AddressBookDbHelper(contex : Context) :
    SQLiteOpenHelper(contex, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION) {


    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(Contract.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${Contract.TABLE_NAME}");
        onCreate(db);
    }

    object Contract {
        val DATABASE_VERSION = 1;
        val DATABASE_NAME = "AddressBook.db";

        val TABLE_NAME = "contacts"
        val COLUMN_ID = "_id"
        val COLUMN_NAME = "name"
        val COLUMN_PHONE = "phone"
        val COLUMN_IMAGE = "image"
        val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_IMAGE + " BLOB" + ")"
    }
}

