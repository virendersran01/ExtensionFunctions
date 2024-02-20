package com.virtualstudios.extensionfunctions.spannablestrings

import android.content.Context
import android.graphics.Color
import android.text.Annotation
import android.text.SpannableString
import android.text.SpannedString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.res.TypedArrayUtils.getText
import com.virtualstudios.extensionfunctions.R

class CustomClickSpan(
    private val onClickListener: () -> Unit,
    @ColorInt private val textColor: Int,
    private val shouldUnderline: Boolean = false
) : ClickableSpan() {
    override fun onClick(p0: View) = onClickListener.invoke()

    override fun updateDrawState(ds: TextPaint) {
        ds.isUnderlineText = shouldUnderline
        ds.color = textColor
    }

}

private fun Context.initTermsConditionsAndPrivacyPolicy() {
    val spannedString = getText(R.string.terms_amp_conditions_privacy_policy) as SpannedString
    val annotations = spannedString.getSpans(0, spannedString.length, Annotation::class.java)

    val termsCopy = SpannableString(spannedString)

    for (annotation in annotations) {
        if (annotation.key == "action") {
            termsCopy.setSpan(
                createClickSpan(annotation.value),
                spannedString.getSpanStart(annotation),
                spannedString.getSpanEnd(annotation),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

   /* binding.textTermsConditions.apply {
        text = termsCopy
        movementMethod = LinkMovementMethod.getInstance()
        //highlightColor = Color.TRANSPARENT
    }*/
}

private fun createClickSpan(action: String) = when (action) {
    "acceptTC" -> CustomClickSpan({
       //action to perform
    }, Color.BLUE, true)

    "readPP" -> CustomClickSpan({

    }, Color.BLUE, true)

    else -> throw NotImplementedError("action $action not implemented")
}