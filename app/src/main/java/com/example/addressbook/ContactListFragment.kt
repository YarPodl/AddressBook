package com.example.addressbook

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.addressbook.data.ContactEntry
import com.example.addressbook.data.RecyclerContactAdapter
import com.example.addressbook.databinding.FragmentFirstBinding

class ContactListFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        var mainActivity : MainActivity = activity as MainActivity
        var recyclerView = binding.root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(binding.root.context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.root.findViewById<RecyclerView>(R.id.recyclerView)
        val mainActivity : MainActivity = activity as MainActivity


        val adapter = RecyclerContactAdapter(mainActivity.databaseAdapter.contacts)
        recyclerView.adapter = adapter
        adapter.onClickListener = object : RecyclerContactAdapter.OnContactClickListener {
            override fun onContactClick(contactEntry: ContactEntry?, position: Int) {
                mainActivity.position = position
                mainActivity.currentContact = contactEntry
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            }
        }
        //recyclerView.scrollToPosition(mainActivity.position)

        mainActivity.menuItems = listOf(resources.getString(R.string.newContact), resources.getString(R.string.currentLocation))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}