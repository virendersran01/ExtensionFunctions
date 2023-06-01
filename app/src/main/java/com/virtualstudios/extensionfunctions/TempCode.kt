package com.virtualstudios.extensionfunctions

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



/** Making this extension function cleans out the code a lot by removing the repetitive
 *  flowWithLifecycle() or repeatOnLifecycle()
 *  This Extension function can easily be used like
 *  flow.safeCollect(viewLifecycleOwner){
 *          Doing the work
 *    }
 **/
fun <T> Flow<T>.safeCollect(
    owner: LifecycleOwner,
    block: (T.() -> Unit)? = null
) = owner.lifecycleScope.launch {
    flowWithLifecycle(owner.lifecycle).collectLatest { block?.invoke(it) }
}


/**
 *  Linkify finds and colors all urls contained in a string
 *
 * @param linkColor color for the url default is blue
 * @param linkClickAction action to perform when user click that link
 * @return
 */
fun String.linkify(linkColor:Int = Color.BLUE, linkClickAction:((link:String) -> Unit)? = null): SpannableStringBuilder {
    val builder = SpannableStringBuilder(this)
    val matcher = Patterns.WEB_URL.matcher(this)
    while(matcher.find()){
        val start = matcher.start()
        val end = matcher.end()
        builder.setSpan(ForegroundColorSpan(Color.BLUE),start,end,0)
        val onClick = object : ClickableSpan(){
            override fun onClick(p0: View) {
                linkClickAction?.invoke(matcher.group())
            }
        }
        builder.setSpan(onClick,start,end,0)
    }
    return builder
}


/**
 * Show toast using string resource
 * @param msg
 *  uses -> showToast(R.string.greeting)
 */
fun Fragment.showToast(@StringRes msg: Int) {
    Toast.makeText(requireActivity(), msg, Toast.LENGTH_LONG).show()
}


/**
 * Toast
 *
 * @param msg
 * @param length
 */
fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, length).show()
}


/**
 * Visible view with animation
 *  ex. -> myView.visible()
 * @param animate
 */
fun View.visible(animate: Boolean = true) {
    if (animate) {
        animate().alpha(1f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                visibility = View.VISIBLE
            }
        })
    } else {
        visibility = View.VISIBLE
    }
}

/**
 * fades out a view making in completely invisible by default
 * @param fadeDuration the duration of fade effect in milliseconds default value is 300ms
 * @param endAlpha the alpha value that view will have after the animation completes default value is 0
 * ex.->myView.fadeOut() or pass your values if required
 *
 */
fun View.fadeOut(fadeDuration:Long = 300,endAlpha:Float = 0f){
    ValueAnimator.ofFloat(1f,endAlpha).apply {
        duration = fadeDuration
        addUpdateListener {
            val animatedValue = it.animatedValue as Float
            alpha = animatedValue
        }
    }.start()
}



/**
 * fades in a view
 * @param fadeDuration the duration of fade effect in milliseconds default value is 300ms
 * ex.->myView.fadeIn()
 */
fun View.fadeIn(fadeDuration:Long = 300){
    ValueAnimator.ofFloat(0f,1f).apply {
        duration = fadeDuration
        addUpdateListener {
            val animatedValue = it.animatedValue as Float
            alpha = animatedValue
        }
    }.start()
}


/**
 * Get as drawable
 *ex.->getAsDrawable(id)
 * @param id
 */
fun Context.getAsDrawable(id:Int) = ContextCompat.getDrawable(this, id)


/**
 * Get as color
 *
 * @param id
 */
fun Context.getAsColor(id:Int) = ContextCompat.getColor(this,id)



////////////////////////////





/**
 * Kotlin Extensions for simpler, easier way
 * of launching of Activities
 */




















enum class AppLanguage {
    ENGLISH, FRENCH
}






/////


Another very common operator extension function that not many people would know about.




I am sure you are aware of retrieving a character from a string using str[index]. Now, what if you require a substring but hate to type out that long function name?



/*
 *
 *
 */




/////////////////

Complex units
You need to set the font size, or box width programmatically but in terms of dp and sp? In Java, you’d rather use a function; however, in Kotlin, you can create an Extension value.




/////////////////////////////////////////////////////////////


/** show desired loader in any fragment
@param rootView  , loader will go in the midpoint of root view
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

just say showLoader(yourlayout) loader will be centered in the layout


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

/////////////////////////////

fun View.translate(from: Float, to: Float) {
    with(ValueAnimator.ofFloat(from, to)) {
        addUpdateListener {
            val animatedValue = it.animatedValue as Float
            translationY = animatedValue
        }
        start()
    }
}
just say yourView.translate(from,to) will translate with animation.


fun View.animateScale(from: Int, to: Int, duration:Long = 1000) {
    val valueAnimator = ValueAnimator.ofInt(from, to)
    valueAnimator.duration = duration
    valueAnimator.addUpdateListener {
        val animatedValue = it.animatedValue as Int
        scaleX = animatedValue.toFloat()
        scaleY = animatedValue.toFloat()
    }
    valueAnimator.start()
}

just say yourView.animateScale(100,200)

///////////////////////////////////////////////////////////////////

Do not hit Api for every character in a search view
you have an app with search feature you want to show results as the user is typing.

ex — user typing shoes, you are hitting api in ontextchanged() so api calls will be for s, sh , sho, shoe , shoes 5 apis calls in total not including when user mistypes and then press back spaces your apis call are ≥ number of letters in the search

Just hit it once by using below utility it will wait 1 second before hitting the api do not hit it for duplicate queries and do not hit for empty string find more details here
https://blog.mindorks.com/instant-search-using-kotlin-flow-operators

fun EditText.afterTextChangedFlow(): Flow<String> {
    val query = MutableStateFlow("")
    doOnTextChanged { text, start, before, count ->
        query.value = text.toString()
    }
    return query
        .debounce(1000)
        .distinctUntilChanged()
        .flowOn(Dispatchers.Main)
        .filter { query.value.isNotBlank() }
}

////////////////////////////////////////

fun Fragment.launchIO(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch(Dispatchers.IO) {
        block.invoke(this)
    }
}


fun Fragment.launchDefault(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch(Dispatchers.Default) {
        block.invoke(this)
    }
}


fun Fragment.launchMain(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch(Dispatchers.Main) {
        block.invoke(this)
    }
}

just say launchIO { // your code} , launchMain{ // your code}

/////////////////////////////////////////////////////////////////////////

    fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
    just day 5.toPx() that’s it


////////////////////////////////////////////////////////////////////////

    fun Fragment.hideSoftKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
    hideSoftKeyboard()

//////////////////////////////////////////////

    fun View.doOnDoubleClick(onDoubleClick: (View) -> Unit) {
        val safeClickListener = DoubleClickListener {
            onDoubleClick(it)
        }
        setOnClickListener(safeClickListener)
    }

    class DoubleClickListener(
        private var defaultInterval: Int = 300,
        private val onDoubleClick: (View) -> Unit
    ) : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < defaultInterval) {
                onDoubleClick(v)
                lastClickTime = 0
            }
            lastClickTime = clickTime
        }
    }

    just say yourimageView.doOnDoubleClick{ }

    /////////////////////////////////////////////////////////////////

    inline fun View.OnDebouncedListener(
        delayInClick: Long = 500L,
        crossinline listener: (View) -> Unit
    ){
        val enabledAgain = Runnable { isEnabled = true }
        setOnClickListener {
            if (isEnabled){
                isEnabled = false
                postDelayed(enabledAgain, delayInClick)
                listener(it)
            }
        }
    } //uses view.onDebouncedListener{ perform Any Action here }

///////////////////////////////////////////////////////////////////////////

    fun Intent?.toDebugString(): String {
        val intent = this ?: return ""
        return StringBuilder().apply {
            appendLine("--- Intent ---")
            appendLine("type: ${intent.type}")
            appendLine("package: ${intent.`package`}")
            appendLine("scheme: ${intent.scheme}")
            appendLine("component: ${intent.component}")
            appendLine("flags: ${intent.flags}")
            appendLine("categories: ${intent.categories}")
            appendLine("selector: ${intent.selector}")
            appendLine("action: ${intent.action}")
            appendLine("dataString: ${intent.dataString}")
            intent.extras?.keySet()?.forEach { key ->
                appendLine("* extra: $key=${intent.extras!![key]}")
            }
        }.toString()
    }

    Skip to content
    About Me
            Public Speaking, Writing & Videos
    in Updates
    Debugging Android Intents
    With new behaviors for apps using targetSdk=33 (Android 13) regarding Intents, it may be necessary to dive in and figure out how to make things compatible.

    In doing this myself, I needed to figure out what was in the Intent, so I could handle it appropriately.

    I started with this StackOverflow post, but ended up adding more info and doing it cleanly in Kotlin.

    fun Intent?.toDebugString(): String {
        val intent = this ?: return ""
        return StringBuilder().apply {
            appendLine("--- Intent ---")
            appendLine("type: ${intent.type}")
            appendLine("package: ${intent.`package`}")
            appendLine("scheme: ${intent.scheme}")
            appendLine("component: ${intent.component}")
            appendLine("flags: ${intent.flags}")
            appendLine("categories: ${intent.categories}")
            appendLine("selector: ${intent.selector}")
            appendLine("action: ${intent.action}")
            appendLine("dataString: ${intent.dataString}")
            intent.extras?.keySet()?.forEach { key ->
                appendLine("* extra: $key=${intent.extras!![key]}")
            }
        }.toString()
    }
    Use the extension function above with println(myIntent.toDebugString()).

////////////////////////////////////////////////

    ViewBinding by Delegate in Fragments

    fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
        FragmentViewBindingDelegate(this, viewBindingFactory)

    class FragmentViewBindingDelegate<T : ViewBinding>(
        val fragment: Fragment,
        val viewBindingFactory: (View) -> T
    ) : ReadOnlyProperty<Fragment, T> {
        private var binding: T? = null

        init {
            val viewLifecycleOwnerLiveDataObserver =
                Observer<LifecycleOwner?> {
                    val viewLifecycleOwner = it ?: run {
                        /**
                         * this block will run when fragment viewLifecycleOwner is null
                         * If the fragment onDestroyView runs before the view lifecycle is initialized (for example when navigating in onViewCreated),
                         * the binding reference must be cleared here because the viewLifeCycleObserver will not be triggered.
                         */
                        binding = null
                        return@Observer
                    }

                    //binding reference set to be null when view life cycle is destroyed
                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            binding = null
                        }
                    })
                }

            fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    fragment.viewLifecycleOwnerLiveData.observeForever(
                        viewLifecycleOwnerLiveDataObserver
                    )
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    fragment.viewLifecycleOwnerLiveData.removeObserver(
                        viewLifecycleOwnerLiveDataObserver
                    )
                }
            })
        }

        override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
            val binding = binding
            if (binding != null) {
                return binding
            }

            val lifecycle = fragment.viewLifecycleOwner.lifecycle
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed: ${fragment.javaClass.simpleName}")
            }

            return viewBindingFactory(thisRef.requireView()).also { this.binding = it }
        }
    }

    private val binding by viewBinding(ExampleFragmentBinding::bind)

///////////////////////////////////////////////////////////////////////////////

// https://github.com/Zhuinden/fragmentviewbindingdelegate-kt

    import android.view.View
            import androidx.fragment.app.Fragment
            import androidx.lifecycle.DefaultLifecycleObserver
            import androidx.lifecycle.Lifecycle
            import androidx.lifecycle.LifecycleOwner
            import androidx.lifecycle.Observer
            import androidx.viewbinding.ViewBinding
            import kotlin.properties.ReadOnlyProperty
            import kotlin.reflect.KProperty

    class FragmentViewBindingDelegate<T : ViewBinding>(
        val fragment: Fragment,
        val viewBindingFactory: (View) -> T
    ) : ReadOnlyProperty<Fragment, T> {
        private var binding: T? = null

        init {
            fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
                val viewLifecycleOwnerLiveDataObserver =
                    Observer<LifecycleOwner?> {
                        val viewLifecycleOwner = it ?: return@Observer

                        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                            override fun onDestroy(owner: LifecycleOwner) {
                                binding = null
                            }
                        })
                    }

                override fun onCreate(owner: LifecycleOwner) {
                    fragment.viewLifecycleOwnerLiveData.observeForever(viewLifecycleOwnerLiveDataObserver)
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    fragment.viewLifecycleOwnerLiveData.removeObserver(viewLifecycleOwnerLiveDataObserver)
                }
            })
        }

        override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
            val binding = binding
            if (binding != null) {
                return binding
            }

            val lifecycle = fragment.viewLifecycleOwner.lifecycle
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
            }

            return viewBindingFactory(thisRef.requireView()).also { this.binding = it }
        }
    }

    fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
        FragmentViewBindingDelegate(this, viewBindingFactory)

    private val binding by viewBinding(FirstFragmentBinding::bind)

//////////////////////////////////////////////////////////////////
    For Activity
            inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
        crossinline bindingInflater: (LayoutInflater) -> T) =
        lazy(LazyThreadSafetyMode.NONE) {
            bindingInflater.invoke(layoutInflater)
        }

    private val binding by viewBinding(MainActivityBinding::inflate)

    /////////////////////////////////////////////



    Extension Function For On Backpressed Callback

            implementation ‘androidx.activity:activity-ktx:1.6+’



    Add android:enableOnBackInvokedCallBack="true” to your applications manifest <application> tag.
    fun AppCompatActivity.onBackPressed(isEnabled: Boolean, callback: () -> Unit) {

        onBackPressedDispatcher.addCallback(this,

            object : OnBackPressedCallback(isEnabled) {

                override fun handleOnBackPressed() {

                    callback()

                }

            })

    }

// How To Use it :

    class ExampleActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)

            setContentView(R.layout.activity_example)

            onBackPressed(true) {

                // do what do you want when get back

            }

        }

    }

    fun FragmentActivity.onBackPressed(callback: () -> Unit) {

        onBackPressedDispatcher.addCallback(this,

            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    callback()

                }

            }

        )

    }

// How To Use it :

    class ExampleFragment : Fragment() {

        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)

            requireActivity().onBackPressed {

                // do what do you want when get back

            }

        }

    }

    /** Making this extension function cleans out the code a lot by removing the repetitive
    flowWithLifecycle() or repeatOnLifecycle()
     **/
    fun <T> Flow<T>.safeCollect(
        owner: LifecycleOwner,
        block: (T.() -> Unit)? = null
    ) = owner.lifecycleScope.launch {
        flowWithLifecycle(owner.lifecycle).collectLatest { block?.invoke(it) }
    }

//This Extension function can easily be used like
    flow.safeCollect(viewLifecycleOwner){
        //Doing the work
    }

//////////////////////////////////////////////////////////////////////////////////////////

    //If your date is not in formate,this is the function for formatting your date
    fun String?.formatStringDate(inputFormat : String, outputFormat : String) : String {
        return if (this.isNullOrEmpty()){
            ""
        }else{
            val dateFormatter = SimpleDateFormat(inputFormat, Locale.getDefault())
            val date = dateFormatter.parse(this)
            date?.let { SimpleDateFormat(outputFormat, Locale.getDefault()).format(it) }.orEmpty()
        }
    }



    fun String?.getYesterdayToday(date: String, format : String): String {
        try {
            val formatter = SimpleDateFormat(format)
            val date = formatter.parse(date)
            val timeInMilliseconds = date.time

            return when {
                DateUtils.isToday(timeInMilliseconds) -> {
                    "today"
                }
                DateUtils.isToday(timeInMilliseconds+ DateUtils.DAY_IN_MILLIS) -> {
                    "yesterday"
                }
                else -> {
                    ""
                }
            }
        } catch (ex: Exception) {
            ex.message?.let { Log.d("date exception", it) }
        }
        return ""
    }


    class MainActivity : AppCompatActivity() {

        //here I am using the date which is already formatted
        var date1 = "2022-11-25T00:00:00"

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
//If your date is not in formate, you can use this line to formate your date
            var formattedDate = date1.formatStringDate("YOUR_DATE_TO_FORMAT", DATE_FORMAT)

            var isTodayYesterday =
                date1.getYesterdayToday(date1, DATE_FORMAT)

            //check if date is today or yesterday
            when (isTodayYesterday) {
                TODAY -> {
                    Log.e("message", "Today")
                }
                YESTERDAY -> {
                    Log.e("message","Yesterday")
                }
            }
        }

        companion object {
            private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss" //2021-05-20T11:28:24
            private const val TODAY = "today"
            private const val YESTERDAY = "yesterday"
        }
    }


////////////////////////////////////////////////////////////////////////////////////

    Here are two extension methods that I use for Bundle & Intent:

    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T
    }

    I also requested this to be added to the support library

            And if you need the ArrayList support there is:

    inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
        SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
    }

    inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
        SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
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

    ////////////////////////////////////////////////////////

    /**
     * Configures an [ImageView] passing a [Drawable] and an ID of a color resource
     *
     * @param drawable A [Drawable] to set to the [ImageView]
     * @param colorResId A resource ID from the desired color
     */
    internal fun ImageView.setDrawableWithColor(
        drawable: Drawable?,
        @ColorRes colorResId: Int
    ) {
        setImageDrawable(drawable)
        setColor(colorResId)
    }

    internal fun ImageView.setColor(
        @ColorRes colorResId: Int
    ) = ImageViewCompat.setImageTintList(
        this,
        ColorStateList.valueOf(
            ResourcesCompat.getColor(resources, colorResId, null)
        )
    )

///////////////////////////////////////////////////////

    /**
     * Get all children of a [View]
     *
     * @return A [List] of [View] with the children of a given [View]
     */
    fun View.getAllChildren(): List<View> {
        val childrenList: MutableList<View> = mutableListOf()
        val viewGroup = this as? ViewGroup
        // null-check because if this is not an instance of ViewGroup,
        // val viewGroup will be null
        viewGroup?.let {
            for (i in 0 until viewGroup.childCount) {
                childrenList.add(viewGroup.getChildAt(i))
            }
        }
        return childrenList
    }

///////////////////////////////////////////////////////

    /**
     * Change view children state to the value in parameter
     *
     * @param state The [Boolean] state to set
     */
    fun View.setChildrenEnabledState(state: Boolean) {
        this.getAllChildren().forEach { it.isEnabled = state }
    }

///////////////////////////////////////////////////////////////

    /**
     * Takes an object of type [T], makes a null-check and if it's not null, executes [block] with [V]
     * as the receiver, and [T] as implicit param. **If [data] is null, the view will be hidden.**
     * @param data An object with useful data to pass to the view
     * @param view The view to configure
     * @param block A function to execute in the context of [view] with param [T]
     */
    fun <T, V : View> configureViewWithNullableData(data: T?, view: V, block: V.(T) -> Unit) {
        with(view) {
            data?.let { block(it) } ?: this.hide()
        }
    }

/////////////////////////////////////////////////////////////

// adding some useful imports to simplify the code

    import android.view.View
            import androidx.constraintlayout.widget.ConstraintLayout
            import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
            import androidx.constraintlayout.widget.ConstraintSet
            import androidx.constraintlayout.widget.ConstraintSet.START
            import androidx.constraintlayout.widget.ConstraintSet.END
            import androidx.constraintlayout.widget.ConstraintSet.TOP
            import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
            import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID

    /**
     * Add a view into a ConstraintLayout, below of the last view (like a stack).
     *
     * @param child A [View] to add
     */
    fun ConstraintLayout.addChildOnStack(
        child: View,
        layoutParams: LayoutParams = LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT)
    ) {
        // get all children of this, and the last child
        val children = this.getAllChildren()
        val lastChild = if (children.isNotEmpty()) children.last() else null

        // check if no exists lastChild, then attach child to parent in top constraint
        val topParentId = lastChild?.id ?: PARENT_ID
        val parentSide = if (lastChild == null) TOP else BOTTOM

        // generate an ID for child, and add child into this
        child.id = View.generateViewId()
        this.addView(child, layoutParams)

        // create constraints to child
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        constraintSet.connect(child.id, TOP, topParentId, parentSide)
        constraintSet.connect(child.id, START, PARENT_ID, START)
        constraintSet.connect(child.id, END, PARENT_ID, END)
        constraintSet.applyTo(this)
    }

    /**
     * Add a view into a ConstraintLayout, at the right of the last view (like a horizontal row).
     *
     * @param child A [View] to add
     * @param layoutParams (optional) A [LayoutParams] object with some previous configurations
     */
    internal fun ConstraintLayout.addChildOnRow(
        child: View,
        layoutParams: LayoutParams = LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).also {
            it.horizontalChainStyle = LayoutParams.CHAIN_SPREAD
        }
    ) {
        // get all children of this, and the last child
        val children = this.getAllChildren()
        val lastChild = if (children.isNotEmpty()) children.last() else null

        // check if doesn't exists lastChild, then attach child to parent view in start constraint;
        // otherwise, it attaches the view at end constraint of lastChild.
        val topParentId = lastChild?.id ?: PARENT_ID
        val parentSide = if (lastChild == null) START else END

        // generate an ID for child, and add child into this
        child.id = View.generateViewId()
        this.addView(child, layoutParams)

        // create constraints to child
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        constraintSet.connect(child.id, TOP, PARENT_ID, TOP)
        constraintSet.connect(child.id, BOTTOM, PARENT_ID, BOTTOM)
        constraintSet.connect(child.id, START, topParentId, parentSide)
        constraintSet.connect(child.id, END, PARENT_ID, END)
        if (lastChild != null) constraintSet.connect(lastChild.id, END, child.id, START)
        constraintSet.applyTo(this)
    }


/////////////////////////////////////////////////////////////////////

    /**
     * Executes a function inside a [TextWatcher] [onTextChanged] method.
     */
    internal fun TextView.onTextChanged(block: (currentText: String) -> Unit) {
        textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                /* Nothing to do */
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                /* Nothing to do */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                block(s?.toString().orEmpty())
            }
        }
    }


////////////////////////////////////////////////////////////////////

    /**
     * Get a query parameter from the Uri data in the intent,
     * and an empty string if the data or value is null
     */
    fun Activity.deeplinkParam(key: String) = intent.data?.getQueryParameter(key) ?: ""


/////////////////////////////////////////////////////////////////////////

    /**
     * Add a new fragment with a [FragmentManager]
     * @param fragment A [Fragment] to add
     * @param containerView A [FrameLayout] that must contains the fragment
     */
    fun FragmentManager.addNewFragment(fragment: Fragment, containerView: FrameLayout) {
        if (containerView.id == ConstraintLayout.NO_ID) containerView.id = View.generateViewId()
        val transaction = beginTransaction()
        transaction.add(containerView.id, fragment)
        transaction.commit()
    }

////////////////////////////////////////////////////////////

    /**
     * Open a raw resource according to [resId] parameter and returns a [T] object.
     */
    internal inline fun <reified T> Context.getJsonFromRawResource(@RawRes resId: Int): T {
        try {
            val rawResource = resources.openRawResource(resId)
            val buffer = ByteArray(rawResource.available())
            rawResource.read(buffer)
            val json = String(buffer)
            return Gson().fromJson(json, T::class.java)
        } catch (e: Resources.NotFoundException) {
            throw e
        } catch (e: JsonSyntaxException) {
            throw JsonSyntaxException("Error reading JSON: ${e.message}", e)
        }
    }

////////////////////////////////////////////////////////////////////////

    class BaseViewModel : ViewModel() {
        infix fun <T> MutableLiveData<UiState<T>>.updateValueWith(call: suspend () -> T) {
            viewModelScope.launch {
                try {
                    value = UiState.Loading()
                    val result = call()
                    value = UiState.Success(result)
                } catch (ex: Exception) {
                    value = UiState.Error(ex)
                }
            }
        }
    }

    sealed class UiState<T>(
        val data: T? = null,
        val exception: Exception? = null
    ) {
        internal class Loading<T> : UiState<T>()
        internal class Success<T>(data: T) : UiState<T>(data = data)
        internal class Error<T>(exception: Exception) : UiState<T>(exception = exception)
    }

//////////////////////////////////////////////////////////////////////////////////

    fun String.toTitleCase(): String {
        return this.split(" ").joinToString(" ") { it.capitalize() }
    }

    println("hello world".toTitleCase())  // Output: "Hello World"

///////////////////////////////////////////////////////////////


    //Int to Enum
    inline fun <reified T : Enum<T>> Int.toEnum(): T? {
        return enumValues<T>().firstOrNull { it.ordinal == this }
    }

    //Enum to Int
    inline fun <reified T : Enum<T>> T.toInt(): Int {
        return this.ordinal
    }

////////////////////////////////////////////////////////////

    inline fun <reified T : ViewBinding> inflateViewBinding(parent: ViewGroup): T {
        val layoutInflater = LayoutInflater.from(parent.context)
        return T::class.java.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.javaPrimitiveType
        ).invoke(null, layoutInflater, parent, false) as T
    }

    val binding = inflateViewBinding<MyItemBinding>(parent)

////////////////////////////////////////////////////////

    fun Any?.printToLog(tag: String = "DEBUG_LOG") {
        Log.d(tag, toString())
    }

    val text = "This is text"
    text.printToLog()

//////////////////////////////////////////////////////

    fun View.gone() = run { visibility = View.GONE }

    fun View.visible() = run { visibility = View.VISIBLE }

    fun View.invisible() = run { visibility = View.INVISIBLE }

    infix fun View.visibleIf(condition: Boolean) =
        run { visibility = if (condition) View.VISIBLE else View.GONE }

    infix fun View.goneIf(condition: Boolean) =
        run { visibility = if (condition) View.GONE else View.VISIBLE }

    infix fun View.invisibleIf(condition: Boolean) =
        run { visibility = if (condition) View.INVISIBLE else View.VISIBLE }


    view.gone()
    view.visible()
    view.invisible()

// dataFound, loading, and condition should be a valid boolean expression
    view goneIf dataFound
    view visibleIf loading
    view invisibleIf condition

//////////////////////////////////////////////////////

    fun View.snackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }

    fun View.snackbar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }

    rootView.snackbar("This is snackbar message")
    rootView.snackbar(R.string.snackbar_message)

// Custom Duration Length
    rootView.snackbar("This is snackbar message", duration = Snackbar.LENGTH_SHORT)

/////////////////////////////////////////////////////////////////////////

// Convert px to dp
    val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

//Convert dp to px
    val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    params.setMargins(16.px, 16.px, 16.px, 16.px)

//////////////////////////////////////////////////////////////////////////////

    val String.isDigitOnly: Boolean
    get() = matches(Regex("^\\d*\$"))

    val String.isAlphabeticOnly: Boolean
    get() = matches(Regex("^[a-zA-Z]*\$"))

    val String.isAlphanumericOnly: Boolean
    get() = matches(Regex("^[a-zA-Z\\d]*\$"))

    val isValidNumber = "1234".isDigitOnly // Return true
    val isValid = "1234abc".isDigitOnly // Return false
    val isOnlyAlphabetic = "abcABC".isAlphabeticOnly // Return true
    val isOnlyAlphabetic2 = "abcABC123".isAlphabeticOnly // Return false
    val isOnlyAlphanumeric = "abcABC123".isAlphanumericOnly // Return true
    val isOnlyAlphanumeric2 = "abcABC@123.".isAlphanumericOnly // Return false

    ////////////////////////////////////////////////////////////////////////////////

    val Any?.isNull get() = this == null

    if (obj.isNull) {
        // Run if object is null
    } else {
        // Run if object is not null
    }

////////////////////////////////////////////////////////////////////////////////////

    fun Any?.ifNull(block: () -> Unit) = run {
        if (this == null) {
            block()
        }
    }

    obj.ifNull {
        // Write code
    }

////////////////////////////////////////////////////////////////////////

    fun String.toDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date? {
        val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
        return dateFormatter.parse(this)
    }

    fun Date.toStringFormat(format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
        return dateFormatter.format(this)
    }

    val currentDate = Date().toStringFormat()
    val currentDate2 = Date().toStringFormat(format = "dd-MM-yyyy")
    val date = "2023-01-01".toDate(format = "yyyy-MM-dd")

////////////////////////////////////////////////////////////////////////////////



    val Context.isConnected: Boolean
    get() {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                val nw = connectivityManager.activeNetwork ?: return false
                val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
                when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
            else -> {
                // Use depreciated methods only on older devices
                val nwInfo = connectivityManager.activeNetworkInfo ?: return false
                nwInfo.isConnected
            }
        }
    }

    ////////////////////////

    fun setUpOneDimenNoAspectRatioImage(imageUrl: String?, imageView: AppCompatImageView, @DimenRes defaultHeight: Int, @DimenRes marginToBeAdjusted: Int, availableWidth: Int? = null) {
        if (imageUrl?.isNotEmpty() == true) {
            imageView.visibility = View.VISIBLE
            GlideApp.with(imageView.context)
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


    fun Date.format(format: String): String = SimpleDateFormat(format).format(this)
    /*val date = Date()
    val formattedDate = date.format("yyyy-MM-dd")
    println(formattedDate) // prints something like "2023-04-08"*/



    /**
     * On click
     * uses -> button.onClick { perform action here}
     * @param action
     * @receiver
     */
    fun View.onClick(action: () -> Unit) {
        setOnClickListener { action() }
    }


    /**
     * Animate property
     *
     * @param property
     * @param fromValue
     * @param toValue
     * @param duration
     * @param onComplete
     * @receiver
     *
     * uses -> view.animateProperty(View.TRANSLATION_X,fromValue = 0f,toValue = 100f,duration = 500,onComplete = { onAnimationComplete() })
     */
    fun View.animateProperty(
        property: KProperty0<Float>,
        fromValue: Float,
        toValue: Float,
        duration: Long,
        onComplete: () -> Unit = {}
    ) {
        val animator = ObjectAnimator.ofFloat(this, property.name, fromValue, toValue).apply {
            setDuration(duration)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    onComplete()
                }
            })
        }
        animator.start()
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
     * Run on background thread
     *
     * @param T
     * @param backgroundFunc
     * @param callback
     * @receiver
     * @receiver
     *
     *  uses -> runOnBackgroundThread({ doExpensiveCalculation() },{ onResultLoaded(it) })
     */
    fun <T> runOnBackgroundThread(backgroundFunc: () -> T, callback: (T) -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        Thread {
            val result = backgroundFunc()
            handler.post { callback(result) }
        }.start()
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
     * Validate
     *
     * @param validationFunc
     * @receiver
     * @return
     *
     * uses -> val input = "example input"
     * val isInputValid = input.validate { input -> input.isNotEmpty() }
     */
    fun String.validate(validationFunc: (String) -> Boolean): Boolean {
        return validationFunc(this)
    }

    /**
     *
     * uses -> val name = etName.value
     */
    val EditText.value
    get() = text?.toString() ?: ""


    /**
     * Is network available
     *
     * @return
     *
     * uses ->
     * if (isNetworkAvailable()) {
     * // Called when network is available
     * } else {
     * // Called when network not available
     * }
     *
     */
    fun Context.isNetworkAvailable(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
        return if (capabilities != null) {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else false
    }

    fun Fragment.isNetworkAvailable() = requireContext().isNetworkAvailable()


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


    fun String.removeAllWhitespaces(): String {
        return this.replace("\\s+".toRegex(), "")
    }

    fun String.removeDuplicateWhitespaces(): String {
        return this.replace("\\s+".toRegex(), " ")
    }


    /**
     * To editable
     *
     * @return
     * uses -> etName.text = "First name".toEditable()
     *
     */
    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)


    /**
     * Screen size
     *
     * uses ->
     * val size = screenSize
     * val deviceHeight = size.height
     * val deviceWidth = size.width
     */
    val Context.screenSize: Size
    get() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val size = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val windowInsets = metrics.windowInsets
            val insets: Insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars()
                        or WindowInsets.Type.displayCutout()
            )

            val insetsWidth: Int = insets.right + insets.left
            val insetsHeight: Int = insets.top + insets.bottom
            val bounds: Rect = metrics.bounds
            Size(
                bounds.width() - insetsWidth,
                bounds.height() - insetsHeight
            )
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay?.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val width = displayMetrics.widthPixels
            Size(width, height)
        }
        return size
    }


    val Context.windowManager
    get() = ContextCompat.getSystemService(this, WindowManager::class.java)

    val Context.connectivityManager
    get() = ContextCompat.getSystemService(this, ConnectivityManager::class.java)

    val Context.notificationManager
    get() = ContextCompat.getSystemService(this, NotificationManager::class.java)

    val Context.downloadManager
    get() = ContextCompat.getSystemService(this, DownloadManager::class.java)



    /**
     * Copy to clipboard
     *
     * @param context
     *
     * uses -> "This is clipboard".copyToClipboard(context)
     *
     *
     */
    fun String.copyToClipboard(context: Context) {
        val clipboardManager = ContextCompat.getSystemService(context, ClipboardManager::class.java)
        val clip = ClipData.newPlainText("clipboard", this)
        clipboardManager?.setPrimaryClip(clip)
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
