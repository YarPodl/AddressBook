package com.example.addressbook.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.addressbook.R

class RecyclerContactAdapter(
    private val data : List<ContactEntry>)
    : RecyclerView.Adapter<RecyclerContactAdapter.ContactViewHolder>() {

    var onClickListener : OnContactClickListener? = null

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var nameTextView: TextView? = null
        var phoneTextView: TextView? = null

        init {
            nameTextView = itemView.findViewById(R.id.nameContact)
            phoneTextView = itemView.findViewById(R.id.phone)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_view_item, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val item = data[position]
        holder.nameTextView?.text = item.name
        holder.phoneTextView?.text = item.phone
        holder.itemView.setOnClickListener { onClickListener?.onContactClick(item, position) }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    interface OnContactClickListener {
        fun onContactClick(state: ContactEntry?, position: Int)
    }
}
