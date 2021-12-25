package com.example.addressbook

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.example.addressbook.data.ContactEntry
import com.example.addressbook.databinding.FragmentSecondBinding

class ContactViewFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    private lateinit var contact: ContactEntry

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var state : ContactState = ContactState.View
        set (value) {
            when (value)
            {
                ContactState.Edit -> {
                    binding.contactControl.setText(R.string.save)
                    binding.editTextPersonName.isEnabled = true
                    binding.editTextPhone.isEnabled = true
                }
                ContactState.View -> {
                    binding.contactControl.setText(R.string.change)
                    binding.editTextPersonName.isEnabled = false
                    binding.editTextPhone.isEnabled = false
                    val mainActivity = activity as MainActivity
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

        contact = mainActivity.currentContact ?: ContactEntry(null, "", "")

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveOrUpdate() {
        val mainActivity = activity as MainActivity

        contact.name = binding.editTextPersonName.text.toString()
        contact.phone = binding.editTextPhone.text.toString()

        if (contact.id != null)
        {
            mainActivity.databaseAdapter.update(contact)
        }
        else {
            mainActivity.databaseAdapter.insert(contact)
        }
    }

    enum class ContactState {
        Edit,
        View
    }
}