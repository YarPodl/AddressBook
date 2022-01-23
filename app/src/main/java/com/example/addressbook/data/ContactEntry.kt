package com.example.addressbook.data

import java.io.Serializable

class ContactEntry(
    var id : Long?,
    var name: String,
    var phone: String,
    var image: ByteArray?,
) : Serializable;