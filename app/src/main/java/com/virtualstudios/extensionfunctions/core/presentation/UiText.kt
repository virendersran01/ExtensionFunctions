package com.virtualstudios.extensionfunctions.core.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicText(val text: String): UiText
    data class StringResource(
        val resId: Int,
        val args: Array<Any> = arrayOf()
    ): UiText

    @Composable
    fun asString(): String {
        return when(this){
            is DynamicText -> text
            is StringResource -> stringResource(resId, args)
        }
    }

    fun asString(context: Context): String {
        return when(this){
            is DynamicText -> text
            is StringResource -> context.getString(resId, args)
        }
    }

}