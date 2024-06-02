package com.mafazer.storyappsubmission.view.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.mafazer.storyappsubmission.R

class MyEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                error = if (s.toString().length < 8) {
                    context.getString(R.string.invalid_password)
                } else {
                    null
                }
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }
}