package com.example.addressbook

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.addressbook.data.ContactEntry
import com.example.addressbook.databinding.FragmentSecondBinding
import java.io.File
import java.lang.Exception

class ContactViewFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    private lateinit var contact: ContactEntry

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var state : ContactState = ContactState.View
        set (value) {
            val mainActivity = activity as MainActivity
            when (value)
            {
                ContactState.Edit -> {
                    binding.contactControl.setText(R.string.save)
                    binding.editTextPersonName.isEnabled = true
                    binding.editTextPhone.isEnabled = true
                    mainActivity.menuItems = listOf(resources.getString(R.string.select_Image))
                }
                ContactState.View -> {
                    binding.contactControl.setText(R.string.change)
                    binding.editTextPersonName.isEnabled = false
                    binding.editTextPhone.isEnabled = false
                    mainActivity.menuItems = listOf(resources.getString(R.string.deleteContact))
                }
            }
            field = value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as MainActivity

        if (mainActivity.currentContact == null)
            state = ContactState.Edit
        else
            state = ContactState.View

        if (mainActivity.currentContact == null)
            mainActivity.currentContact = ContactEntry(null, "", "", null)

        contact = mainActivity.currentContact!!

        if (contact.image != null) {
            binding.contactImage.setImageBitmap(contact.image?.let {
                BitmapFactory.decodeByteArray(contact.image, 0, it.size)
            })
        }
        else {
            binding.contactImage.setImageResource(R.drawable.unknow)
        }

        if (contact.id != null)
        {
            binding.contactControl.setText(R.string.change)
            binding.editTextPersonName.isEnabled = false
            binding.editTextPersonName.setText(contact.name)
            binding.editTextPhone.isEnabled = false
            binding.editTextPhone.setText(contact.phone)
        }

        binding.contactControl.setOnClickListener {
            state = when (state) {
                ContactState.Edit -> {
                    saveOrUpdate()
                    ContactState.View
                }
                ContactState.View -> {
                    ContactState.Edit
                }
            }
        }

        binding.contactImage.setOnClickListener {
            if (state == ContactState.Edit) {
                try {
                    mainActivity.takeImage()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveOrUpdate() {
        val mainActivity = activity as MainActivity

        contact.name = binding.editTextPersonName.text.toString()
        contact.phone = binding.editTextPhone.text.toString()

        var res : Long

        if (contact.id != null)
        {
            mainActivity.currentContact = contact
            res = mainActivity.databaseAdapter.update(contact)
        }
        else {
            mainActivity.currentContact = null
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            res = mainActivity.databaseAdapter.insert(contact)
        }
    }

    enum class ContactState {
        Edit,
        View
    }
}