package com.virtualstudios.extensionfunctions

import android.graphics.drawable.Drawable
import androidx.annotation.DimenRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

//fun showSearchableSpinnerDialog(
//    context: Context,
//    title: String,
//    itemList: List<NameIdModel>,
//    onItemSelected: (data: NameIdModel) -> Unit
//) {
//    var alertDialog: AlertDialog? = null
//    val builder = MaterialAlertDialogBuilder(context)
//    builder.setCancelable(false)
//    val binding = LayoutSpinnerSearchableDialogBinding.inflate(LayoutInflater.from(context))
//    builder.setView(binding.root)
//    binding.textHeader.text = title
//    val adapter = SearchableSpinnerAdapter {
//        binding.inputSearch.hideKeyboard()
//        onItemSelected.invoke(it)
//        alertDialog?.dismiss()
//    }
//    binding.recyclerView.adapter = adapter
//    adapter.submitList(itemList)
//
//    if (itemList.isEmpty()) {
//        binding.textError.visible()
//    } else {
//        binding.textError.gone()
//    }
//
//    binding.inputSearch.requestFocus()
//    binding.inputSearch.showKeyboard()
//
//    binding.inputSearch.addTextChangedListener(object : TextWatcher {
//        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//        }
//
//        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//        }
//
//        override fun afterTextChanged(s: Editable?) {
//            if (s.isNullOrEmpty()) {
//                adapter.submitList(itemList)
//                binding.textError.gone()
//            } else {
//                val filteredList = itemList.filter { it.name.contains(s, true) }
//                if (filteredList.isEmpty()) {
//                    binding.textError.visible()
//                } else {
//                    binding.textError.gone()
//                }
//                adapter.submitList(filteredList)
//            }
//        }
//
//    })
//
//    binding.imageCancel.setOnClickListener {
//        alertDialog?.dismiss()
//    }
//
//    alertDialog = builder.create()
//    alertDialog.show()
//
//}


package com.virtualstudios.extensionfunctions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.graphics.Insets
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowInsets
import android.view.WindowManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import androidx.core.view.doOnLayout
import java.util.Base64




/** show desired loader in any fragment
 *
 * @param rootView  , loader will go in the midpoint of root view
 * showLoader(yourlayout) loader will be centered in the layout
 **/
@SuppressLint("ResourceType")
fun Fragment.showLoader(rootView : ViewGroup) {
    val loaderAnimation = LottieAnimationView(requireActivity()).apply {
        id = 12345
        setAnimation(R.raw.loader)
        repeatMode = LottieDrawable.INFINITE
        loop(true)
        layoutParams = ViewGroup.LayoutParams(50.toPx(), 50.toPx())
        playAnimation()
    }
    loaderAnimation.doOnLayout {
        it.x = rootView.width/2f - it.width/2
        it.y = rootView.height/2f - it.height/2
    }
    rootView.addView(loaderAnimation)
}



// pass same viewgroup that was paased in showLoader(ViewGroup)
@SuppressLint("ResourceType")
fun removeLoader(rootView: ViewGroup){
    val animationView = rootView.findViewById<LottieAnimationView>(12345)
    rootView.removeView(animationView)
}

then just say removeLoader(yourlayout) to remove this loader from memory

/////////////////////////////////////////////////////

fun Fragment.enableTouch() {
    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
}

fun Fragment.disableTouch() {
    requireActivity().window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    )
}




////////////////////////////////////////////////////////////////////////////////////////////////////////

Solving Android multiple clicks problem with coroutines — Kotlin

import kotlinx.coroutines.Job

fun Job?.runOnceUntilComplete(callback: () -> Unit) {
        this?.let {
            if (it.isCompleted) {
                callback.invoke()
            }
        } ?: run {
            callback.invoke()
        }
    }

    private var initJob: Job? = null

// call this function where you want

fun onLoginClicked() {
        initJob.runOnceUntilComplete {
            doLogin()
        }
}

private fun doLogin() {
        initJob = viewModelScope.launch {
            viewState = PayLoginViewState.Loading
            when (val result = loginUserUseCase(email = email, password = password)) {
                is Outcome.Error -> setLoginErrorState(result.message)
                is Outcome.Completed -> {
                    navigateToOnboarding()
                    viewState = PayLoginViewState.Idle
                }
                else -> Unit
            }
        }
}





////////////////////////////////////////////////////////////////////////////////

fun setUpOneDimenNoAspectRatioImage(imageUrl: String?, imageView: AppCompatImageView, @DimenRes defaultHeight: Int, @DimenRes marginToBeAdjusted: Int, availableWidth: Int? = null) {
    if (imageUrl?.isNotEmpty() == true) {
        imageView.visibility = View.VISIBLE
        Glide.with(imageView.context)
            .load(imageUrl)
            .centerCrop()
            .into(object : CustomTarget<Drawable>() {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                }

                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    val aspectRatio = resource.intrinsicWidth.toFloat() / resource.intrinsicHeight.toFloat()
                    val bitmapWidth = (imageView.context.getDimensionPixelOffset(defaultHeight)) * aspectRatio
                    // if we care about available width there can be a case where
                    // new bitmap width is greater than available width, this code will handle that.
                    if (availableWidth != null && bitmapWidth > availableWidth - imageView.context.getDimensionPixelOffset(marginToBeAdjusted)) {
                        var newWidth = availableWidth - imageView.context.getDimensionPixelOffset(marginToBeAdjusted)
                        var newHeight = newWidth.toFloat() / aspectRatio.toFloat()
                        while (newHeight > (imageView.context.getDimensionPixelOffset(defaultHeight))) {
                            newWidth -= newWidth / 10
                            newHeight = newWidth.toFloat() / aspectRatio.toFloat()
                        }
                        imageView.layoutParams.apply {
                            width = newWidth
                            height = newHeight.toInt()
                        }
                    } else {
                        imageView.layoutParams.apply {
                            width = bitmapWidth.toInt()
                            height = imageView.context.getDimensionPixelOffset(defaultHeight)
                        }
                    }
                    imageView.setImageDrawable(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    } else {
        imageView.visibility = View.GONE
    }
}














    /**
     * Bind data
     *
     * @param T
     * @param data
     * @param layoutRes
     * @param bindFunc
     * @param clickListener
     * @receiver
     * uses -> recyclerView.bindData(data = listOf("item1", "item2", "item3"),layoutRes = R.layout.list_item,bindFunc = { view, item -> view.findViewById<TextView>(R.id.text_view).text = item },clickListener = { item -> onItemClick(item) })
     *
     *
     *
     */
    fun <T> RecyclerView.bindData(
        data: List<T>,
        layoutRes: Int,
        bindFunc: (View, T) -> Unit,
        clickListener: ((T) -> Unit)? = null
    ) {
        adapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
                return ViewHolder(view)
            }
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val item = data[position]
                bindFunc(holder.itemView, item)
                clickListener?.let { listener ->
                    holder.itemView.setOnClickListener { listener(item) }
                }
            }
            override fun getItemCount() = data.size
            inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
        }
    }





    /**
     * With permissions
     *
     * @param permissions
     * @param callback
     * @receiver
     *
     * uses -> withPermissions(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE) {// Code to execute when permissions are granted}
     */
    fun Activity.withPermissions(vararg permissions: String, callback: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val ungrantedPermissions = permissions.filter {
                checkSelfPermission(it) == PackageManager.PERMISSION_DENIED
            }
            if (ungrantedPermissions.isEmpty()) {
                // All permissions are granted, execute callback
                callback()
            } else {
                // Request permissions
                requestPermissions(ungrantedPermissions.toTypedArray(), 0)
            }
        } else {
            // Pre-Marshmallow devices, execute callback
            callback()
        }
    }








    /**
     * Is permission granted
     *
     * @param permission
     *
     * uses ->
     * if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
     * // Block runs if permission is granted
     * } else {
     * // Ask for permission
     * }
     *
     */
    fun Context.isPermissionGranted(permission: String) = run {
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }














    @file:OptIn(ExperimentalContracts::class)

    import kotlin.contracts.ExperimentalContracts
            import kotlin.contracts.contract

    fun Boolean?.isTrue(): Boolean {
        contract {
            returns(true) implies (this@isTrue != null)
        }
        return this == true
    }

    fun Boolean?.isFalse(): Boolean {
        contract {
            returns(true) implies (this@isFalse != null)
        }
        return this == false
    }

    val Boolean?.orTrue: Boolean
    get() = this ?: true

    val Boolean?.orFalse: Boolean
    get() = this ?: false

/*lateinit var any: Boolean? // Assume that, this property is already assigned
if (any.isTrue()) {
    // Run when any is true only
}
if (any.isFalse()) {
    // Run when any is false only
}
val any1: Boolean = any.orTrue // If any is null then any1 = true otherwise any1 = any
val any2: Boolean = any.orFalse // If any is null then any1 = false otherwise any1 = any*/



//    /**
//     * Center title in a toolbar
//     * ex.-> just say toolbar.centerTitle() that’s it
//     */
//    fun Toolbar.centerTitle() {
//        doOnLayout {
//            children.forEach {
//                if (it is TextView) {
//                    it.x = width / 2f - it.width / 2f
//                    return@forEach
//                }
//            }
//        }
//    }




//    fun String.decode(): String {
//        return Base64.decode(this, Base64.DEFAULT).toString(Charsets.UTF_8)
//    }
//
//    fun String.encode(): String {
//        return Base64.encodeToString(this.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
//    }
