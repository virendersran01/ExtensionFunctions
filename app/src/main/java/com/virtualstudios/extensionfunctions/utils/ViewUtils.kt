package com.virtualstudios.extensionfunctions.utils

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.google.android.material.textfield.TextInputLayout

fun getText(view: View): String {
    return if (view is TextView) view.text.toString().trim { it <= ' ' } else ""
}

fun enabledEditing(vararg editText: EditText) {
    for (et in editText) {
        et.isFocusable = true
        et.isFocusableInTouchMode = true
        et.isClickable = true
    }
}

fun disabledEditing(vararg editText: EditText) {
    for (et in editText) {
        et.isFocusable = false
        et.isFocusableInTouchMode = false
        et.isClickable = false
    }
}


fun getAttrRes(context: Context, @AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.resourceId
}

fun getAttrRes(view: View, @AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    view.context.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.resourceId
}

fun hideKeyboardFrom(context: Context?, view: View?) {
    try {
        if (context == null) return
        val imm = ContextCompat.getSystemService(
            context,
            InputMethodManager::class.java
        )
        if (imm != null && view != null && view.windowToken != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun setText(inputLayout: TextInputLayout, text: String) {
    if (inputLayout.editText != null) inputLayout.editText!!.setText(text)
}

fun setTextWithLinkClick(
    textView: TextView,
    text: String,
    onLinkClick: HelperMethods.DataMethod<String?>
) {
    val builder =
        SpannableStringBuilder.valueOf(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT))
    val spans = builder.getSpans(0, builder.length, URLSpan::class.java)
    if (spans != null) {
        for (span in spans) {
            builder.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onLinkClick.execute(span.url)
                    }
                },
                builder.getSpanStart(span),
                builder.getSpanEnd(span),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            builder.removeSpan(span)
        }
    }
    textView.text = builder
    textView.movementMethod = LinkMovementMethod.getInstance()
}