package com.example.addressbook

import android.app.Dialog
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class MessageDialog(private val location: Location) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Ваши координаты")
                .setMessage("Широта: ${location.latitude}\nДолгота: ${location.longitude}")
                .setPositiveButton("Ok") {
                        dialog, id ->  dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}