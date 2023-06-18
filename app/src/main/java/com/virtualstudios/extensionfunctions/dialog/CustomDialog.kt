package com.virtualstudios.extensionfunctions.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.virtualstudios.extensionfunctions.databinding.CustomDialogBinding

class CustomDialog : BaseDialogFragment() {

    private lateinit var binding: CustomDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CustomDialogBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSeeCertificate.setOnClickListener {
            dismiss()
        }
    }
}