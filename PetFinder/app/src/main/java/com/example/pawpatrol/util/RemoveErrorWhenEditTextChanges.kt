package com.example.pawpatrol.util

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputLayout

class RemoveErrorWhenEditTextChanges(
    private val holder: TextInputLayout
) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        holder.error = null
    }
}