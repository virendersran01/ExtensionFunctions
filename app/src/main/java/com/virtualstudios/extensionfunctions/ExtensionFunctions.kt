package com.virtualstudios.extensionfunctions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

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

/*
  finds and colors all urls contained in a string
  @param linkColor color for the url default is blue
  @param linkClickAction action to perform when user click that link
 */
fun String.linkify(linkColor:Int = Color.BLUE,linkClickAction:((link:String) -> Unit)? = null): SpannableStringBuilder {
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
        //builder.setSpan(onClick,start,end,0)
    }
    return builder
}


// easily show a toast
fun Fragment.showToast(@StringRes msg: Int) {
    Toast.makeText(requireActivity(), msg, Toast.LENGTH_LONG).show()
}

showToast(R.string.greeting)

fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, length).show()
}

fun Fragment.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    requireContext().toast(msg, length)
}


/////
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

myView.visible()

///////////


// extension property to make menu invisible
var Menu.visibility: Boolean
    get() = false
    set(value) {
        iterator().forEach {
            it.isVisible = value
        }
    }
menu.visibility = boolea

////////

Center title in a toolbar

fun Toolbar.centerTitle() {
    doOnLayout {
        children.forEach {
            if (it is TextView) {
                it.x = width / 2f - it.width / 2f
                return@forEach
            }
        }
    }
}

just say toolbar.centerTitle() that’s it

/**
 * fades out a view making in completely invisible by default
 * @param fadeDuration the duration of fade effect in milliseconds default value is 300ms
 * @param endAlpha the alpha value that view will have after the animation completes default value is 0
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

myView.fadeOut() or pass your values if required

/**
 * fades in a view
 * @param fadeDuration the duration of fade effect in milliseconds default value is 300ms
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

myView.fadeIn()

//////////////////

fun Fragment.getAsDrawable(id:Int) = ContextCompat.getDrawable(this.requireActivity(),id)!!

getAsDrawable(id) and it is not nullable also

fun Fragment.getAsColor(id:Int) = ContextCompat.getColor(this.requireActivity(),id)!!
getAsColor()

/////////////////////////////////
Encode and decode your strings to base64 in easy way

fun String.decode(): String {
    return Base64.decode(this, Base64.DEFAULT).toString(Charsets.UTF_8)
}

fun String.encode(): String {
    return Base64.encodeToString(this.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
}

just say myString.encode() , myString.decode()


////////////////////////////


/**
 * Kotlin Extensions for simpler, easier way
 * of launching of Activities
 */

inline fun <reified T : Any> Activity.launchActivity(
    requestCode: Int = -1,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    startActivityForResult(intent, requestCode, options)
}

inline fun <reified T : Any> Context.launchActivity(
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    startActivity(intent, options)
}

inline fun <reified T : Any> newIntent(context: Context): Intent =
    Intent(context, T::class.java)

/**
 * Kotlin Extensions for simpler, easier way
 * of launching of Activities
 */

/** Set the View visibility to INVISIBLE */
fun View.invisible() {
    this.visibility = View.INVISIBLE
}

/** Set the View visibility to GONE*/
fun View.gone() {
    this.visibility = View.GONE
}

/** Set the View visibility to GONE*/
fun View.visible() {
    this.visibility = View.VISIBLE
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

fun EditText.onAction(action: Int, runAction: () -> Unit) {
    this.setOnEditorActionListener { _, actionId, _ ->
        return@setOnEditorActionListener when (actionId) {
            action -> {
                runAction.invoke()
                true
            }
            else -> false
        }
    }
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt() //fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt() //fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()


class SpaceItemDecoration(
    private val space: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    init {
        require(space >= 0) { "Space between items can not be negative" }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val itemCount =
            parent.adapter?.itemCount ?: throw IllegalStateException("Adapter must not be null")
        val itemPosition = parent.getChildAdapterPosition(view)
        val layoutDirection = parent.layoutDirection
        val sideSize = if (layoutDirection == LinearLayout.VERTICAL) view.width else view.height
        val (spans, orientation) = with(parent.layoutManager!!) {
            when (this) {
                is GridLayoutManager -> Pair(
                    this.spanCount,
                    this.orientation
                ) // grid is itself a linear!
                is LinearLayoutManager -> Pair(1, this.orientation)
                else -> throw IllegalArgumentException("For now, only LinearLayout and GridLayout managers are supported")
            }
        }

        val params = DecorationParams(
            itemCount = itemCount,
            itemPosition = itemPosition,
            spanCount = spans,
            itemSideSize = sideSize,
            layoutDirection = layoutDirection,
            orientation = orientation
        )
        setItemMargin(outRect, params, includeEdge)
    }

    private fun setItemMargin(outRect: Rect, params: DecorationParams, includeEdge: Boolean): Rect {

        val (top, right, bottom, left) = LinearLayoutMarginFormula.apply {
            updateItem(params, space, includeEdge)
        }.calculate()

        outRect.top = top
        outRect.right = right
        outRect.bottom = bottom
        outRect.left = left
        return outRect
    }

    internal data class DecorationParams(
        val itemCount: Int,
        val itemPosition: Int,
        val spanCount: Int,
        val itemSideSize: Int,
        val layoutDirection: Int,
        val orientation: Int,
    )

    // -----------------------------------------------------------------

    internal data class Margin(val top: Int, val right: Int, val bottom: Int, val left: Int)

    private object LinearLayoutMarginFormula {

        private fun findCorrectListVariation(
            layoutDirection: Int,
            orientation: Int
        ): LinearListVariation {
            return listVariations[layoutDirection + (2 * orientation)]
        }

        private var includeEdge: Boolean = false
        private var margin = 0
        private lateinit var params: DecorationParams

        // LTR = 0, RTL = 1, Horizontal = 0, Vertical = 1
        private val listVariations = arrayOf(
            HorizontalLTR(),
            HorizontalRTL(),
            VerticalLTR(),
            VerticalRTL(),
        )

        fun updateItem(params: DecorationParams, margin: Int, includeEdge: Boolean) {
            this.params = params
            this.margin = margin
            this.includeEdge = includeEdge
        }

        fun calculate(): Margin {
            val borderMargin = if (includeEdge) margin else 0
            val numberOfMargins = params.spanCount + (if (includeEdge) 1 else -1)
            val itemLayoutWidth =
                params.itemSideSize - ((numberOfMargins * margin) / params.spanCount)

            val top = topMargin(borderMargin)
            val left = leftMargin(params.itemSideSize, itemLayoutWidth, borderMargin)
            val right = rightMargin(params.itemSideSize, itemLayoutWidth, left)
            val bottom = bottomMargin(borderMargin)

            val list: LinearListVariation = findCorrectListVariation(
                layoutDirection = params.layoutDirection,
                orientation = params.orientation
            )
            return list.adaptVerticalLtrMargin(Margin(top, right, bottom, left))
        }

        private fun leftMargin(itemWidth: Int, layoutWidth: Int, borderMargin: Int): Int {
            fun spanIdx() = params.itemPosition % params.spanCount

            return spanIdx() * (margin + layoutWidth - itemWidth) + borderMargin
        }

        fun topMargin(borderMargin: Int): Int {
            fun isAtTop() = params.itemPosition < params.spanCount

            return if (isAtTop()) borderMargin else (margin / 2)
        }

        fun rightMargin(itemWidth: Int, layoutWidth: Int, leftMargin: Int): Int {
            return itemWidth - layoutWidth - leftMargin
        }

        fun bottomMargin(borderMargin: Int): Int {
            fun isAtBottom() = row(params.itemPosition, params.spanCount) ==
                    rows(params.itemCount, params.spanCount)

            return if (isAtBottom()) borderMargin else (margin / 2)
        }


        private fun row(position: Int, spanCount: Int): Int {
            return ceil((position + 1).toFloat() / spanCount.toFloat()).toInt()
        }

        private fun rows(itemCount: Int, spanCount: Int): Int {
            return ceil(itemCount.toFloat() / spanCount.toFloat()).toInt()
        }


        interface LinearListVariation {
            fun adaptVerticalLtrMargin(margin: Margin): Margin
        }

        class VerticalLTR : LinearListVariation {
            override fun adaptVerticalLtrMargin(margin: Margin) = margin
        }

        class VerticalRTL : LinearListVariation {
            override fun adaptVerticalLtrMargin(margin: Margin) = Margin(
                top = margin.top,
                right = margin.left,
                bottom = margin.bottom,
                left = margin.right
            )
        }

        class HorizontalLTR : LinearListVariation {
            override fun adaptVerticalLtrMargin(margin: Margin) = Margin(
                top = margin.left,
                right = margin.bottom,
                bottom = margin.right,
                left = margin.top
            )
        }

        class HorizontalRTL : LinearListVariation {
            override fun adaptVerticalLtrMargin(margin: Margin) = Margin(
                top = margin.left,
                right = margin.top,
                bottom = margin.right,
                left = margin.bottom
            )
        }
    }
}

//BaseActivity with ViewBinding
abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : AppCompatActivity() {

    lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)
    }

//    override fun attachBaseContext(newBase: Context) {
//        val localeToSwitchTo = when(UserPreference.getAppLanguage()){
//            AppLanguage.ENGLISH -> Constants.LANGUAGE_ENGLISH_CODE
//            AppLanguage.FRENCH -> Constants.LANGUAGE_FRENCH_CODE
//        }
//
//        val localeUpdatedContext: ContextWrapper = ContextUtils.updateLocale(newBase, Locale(localeToSwitchTo))
//        super.attachBaseContext(localeUpdatedContext)
//    }


}

//BaseFragment with ViewBinding
abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : Fragment() {

    private var _binding: VB? = null
    val binding: VB
        get() = _binding as VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater)
        if (_binding == null)
            throw java.lang.IllegalArgumentException("Binding cannot be null")
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}

fun isEmailValid(email: String): Boolean {
    val regExpn = ("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$")
    val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(email)
    return matcher.matches()
}

fun isPhoneNumberValid(phoneNumber: String, countryCode: String): Boolean {
    val phoneUtil = PhoneNumberUtil.getInstance()
    try {
        val numberProto = phoneUtil.parse(phoneNumber, countryCode)
        return phoneUtil.isValidNumber(numberProto)
    } catch (e: NumberParseException) {
        System.err.println("NumberParseException was thrown: $e")
    }
    return false
}

fun generateRequestBody(text: String): RequestBody {
    return text.toRequestBody("text/plain".toMediaTypeOrNull())
}

fun getFileBody(keyName: String, uri: String): MultipartBody.Part? {
    var body: MultipartBody.Part? = null
    if (!TextUtils.isEmpty(uri)) {
        val file = File(uri)
        val requestFile =
            file.asRequestBody("image/*".toMediaTypeOrNull())
        body = MultipartBody.Part.createFormData(keyName, file.name, requestFile)
    }
    return body
}

fun Context.progressAlertDialog(): AlertDialog {
    val alertDialog: AlertDialog?
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(false)
    val binding = LayoutProgressBinding.inflate(LayoutInflater.from(this))
    builder.setView(binding.root)
    alertDialog = builder.create()
    alertDialog.setCanceledOnTouchOutside(false)
    alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    return alertDialog
}

val width: Int
    get() = Resources.getSystem().displayMetrics.widthPixels

val height: Int
    get() = Resources.getSystem().displayMetrics.heightPixels

fun EditText.setEmojiFilter() {
    this.filters = arrayOf(emojiFilter)
}

private val emojiFilter = InputFilter { source, start, end, _, _, _ ->
    for (index in start until end) {

        when (Character.getType(source[index])) {
            '*'.code,
            Character.OTHER_SYMBOL.toInt(),
            Character.SURROGATE.toInt() -> {
                return@InputFilter ""
            }
            Character.LOWERCASE_LETTER.toInt() -> {
                val index2 = index + 1
                if (index2 < end && Character.getType(source[index + 1]) == Character.NON_SPACING_MARK.toInt())
                    return@InputFilter ""
            }
            Character.DECIMAL_DIGIT_NUMBER.toInt() -> {
                val index2 = index + 1
                val index3 = index + 2
                if (index2 < end && index3 < end &&
                    Character.getType(source[index2]) == Character.NON_SPACING_MARK.toInt() &&
                    Character.getType(source[index3]) == Character.ENCLOSING_MARK.toInt()
                )
                    return@InputFilter ""
            }
            Character.OTHER_PUNCTUATION.toInt() -> {
                val index2 = index + 1

                if (index2 < end && Character.getType(source[index2]) == Character.NON_SPACING_MARK.toInt()) {
                    return@InputFilter ""
                }
            }
            Character.MATH_SYMBOL.toInt() -> {
                val index2 = index + 1
                if (index2 < end && Character.getType(source[index2]) == Character.NON_SPACING_MARK.toInt())
                    return@InputFilter ""
            }
        }
    }
    return@InputFilter null
}

fun Context.isNetworkConnected(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

enum class AppLanguage {
    ENGLISH, FRENCH
}

@RequiresApi(Build.VERSION_CODES.M)
fun isInternetConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
    return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}


fun setAppLocale(context: Context, language: String) {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.createConfigurationContext(config)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

/////

Repeating a string
Another very common operator extension function that not many people would know about.

operator fun String.times(n: Int): String {
    val sb = StringBuilder()
    repeat(n) {
        sb.append(this)
    }
    return sb.toString()
}

String substring
I am sure you are aware of retrieving a character from a string using str[index]. Now, what if you require a substring but hate to type out that long function name?

operator fun String.get(range: IntRange) =
    substring(range.first, range.last + 1)

/*
 * val mainStr = "Interesting"
 * val substr = mainStr[2..8] // "teresti"
 */

Android resources
fun Context.drawable(@DrawableRes resId: Int) =
    ResourcesCompat.getDrawable(resources, resId, null)

fun Context.font(@FontRes resId: Int) =
    ResourcesCompat.getFont(this, resId)

fun Context.dimen(@DimenRes resId: Int) =
    resources.getDimension(resId)

fun Context.anim(@AnimRes resId: Int) =
    AnimationUtils.loadAnimation(this, resId)


/////////////////

Complex units
You need to set the font size, or box width programmatically but in terms of dp and sp? In Java, you’d rather use a function; however, in Kotlin, you can create an Extension value.


val Float.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

val Float.sp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

val Int.dp get() = toFloat().dp
val Int.sp get() = toFloat().sp

/*
 * use as 18.dp
 * or 22.5f.sp
 */


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


