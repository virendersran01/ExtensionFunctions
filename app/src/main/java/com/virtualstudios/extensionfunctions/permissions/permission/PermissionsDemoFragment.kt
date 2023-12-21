package com.virtualstudios.extensionfunctions.permissions.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.virtualstudios.extensionfunctions.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PermissionsDemoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PermissionsDemoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val requestPermissionLauncher = RequestPermissionLauncher.from(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_permissions_demo, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PermissionsDemoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PermissionsDemoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun permissionsRequest(){
        requestPermissionLauncher.launch(
            arrayListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION),
            onShowRational = {
                showPermissionRationaleDialog(
                    title = "Rationale",
                    description = "You need to give permission to go on"
                )
            }, onPermanentlyDenied = {
                showPermanentlyDeniedPermissionDialog(
                    title = "Permanently Denied",
                    description = "You need to open setttings and give permission to go on"
                )
            },
            onResult = {

            }
        )

    }

    private fun showPermissionRationaleDialog(title: String, description: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder
            .setMessage(description)
            .setTitle(title)
            .setPositiveButton("Give permission"){ dialog, which ->
                requestPermissionLauncher.requestPermission()
            }
            .setNegativeButton("Cancel"){ dialog, which ->
                dialog.dismiss()
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showPermanentlyDeniedPermissionDialog(title: String, description: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder
            .setMessage(description)
            .setTitle(title)
            .setPositiveButton("Go to settings"){ dialog, which ->
                openSettings()
            }
            .setNegativeButton("Cancel"){ dialog, which ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun openSettings() {
        val packageName = activity?.packageName // or replace with your target package name

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}