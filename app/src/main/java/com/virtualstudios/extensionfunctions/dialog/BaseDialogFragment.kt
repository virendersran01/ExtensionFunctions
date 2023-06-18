package com.virtualstudios.extensionfunctions.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.virtualstudios.extensionfunctions.R

open class BaseDialogFragment : DialogFragment() {

    override fun getTheme(): Int = R.style.DialogFragmentTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(requireContext(), theme)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()
    }

    private fun setupView() {
        setStyle(STYLE_NO_TITLE, R.style.DialogFragmentStyle)

        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}