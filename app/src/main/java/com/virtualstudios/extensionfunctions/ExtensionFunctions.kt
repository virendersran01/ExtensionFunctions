import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Insets
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.os.SystemClock
import android.text.Editable
import android.text.Html
import android.text.InputFilter
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.DateUtils
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.SuperscriptSpan
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.util.Size
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Placeholder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.toSpannable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.virtualstudios.extensionfunctions.BuildConfig
import com.virtualstudios.extensionfunctions.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.reflect.KProperty0

/**
 * Parcelable
 *
 * @param T
 * @param key
 * @return
 */
inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

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
 * Start activity
 *
 * @param cls
 * @param finishCallingActivity
 * @param block
 *
 * uses ->
 * startActivity(MainActivity::class.java) // Without Intent modification
 * startActivity(MainActivity::class.java) {
 * // You can access the intent object in this block
 *    putExtra("key", "value")
 * }
 */
fun Activity.startActivity(
    cls: Class<*>,
    finishCallingActivity: Boolean = true,
    block: (Intent.() -> Unit)? = null
) {
    val intent = Intent(this, cls)
    block?.invoke(intent)
    startActivity(intent)
    if (finishCallingActivity) finish()
}

/** Set the View visibility to INVISIBLE */
fun View.invisible() {
    this.visibility = View.INVISIBLE
}
//fun View.invisible() = run { visibility = View.INVISIBLE }

/** Set the View visibility to GONE*/
fun View.gone() {
    this.visibility = View.GONE
}
//fun View.gone() = run { visibility = View.GONE }

/** Set the View visibility to GONE*/
fun View.visible() {
    this.visibility = View.VISIBLE
}
//fun View.visible() = run { visibility = View.VISIBLE }

/**
 * Visible if
 * uses ->  view visibleIf loading
 * @param condition
 */
infix fun View.visibleIf(condition: Boolean) =
    run { visibility = if (condition) View.VISIBLE else View.GONE }

/**
 * Gone if
 * uses -> view goneIf dataFound
 * @param condition
 */
infix fun View.goneIf(condition: Boolean) =
    run { visibility = if (condition) View.GONE else View.VISIBLE }

/**
 * Invisible if
 * uses-> view invisibleIf condition
 * @param condition
 */
infix fun View.invisibleIf(condition: Boolean) =
    run { visibility = if (condition) View.INVISIBLE else View.VISIBLE }


/**
 * Toast
 *
 * @param msg
 * @param length
 */
fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

/**
 * Show toast using string resource
 * @param msg
 *  uses -> showToast(R.string.greeting)
 */
fun Context.showToast(@StringRes msg: Int) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}


/**
 * Hide keyboard
 *
 */
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Fragment.hideSoftKeyboard() {
    val imm =
        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
}

fun View.hideKeyboardTry(): Boolean {
    try {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    } catch (ignored: RuntimeException) {
    }
    return false
}


/**
 * Show keyboard
 *
 */
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

/*
val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt() //fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt() //fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
*/


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

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()


fun String.isEmailValid(): Boolean {
    val regExpn = ("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$")
    val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

fun String.isPhoneNumberValid(countryCode: String): Boolean {
    val phoneUtil = PhoneNumberUtil.getInstance()
    try {
        val numberProto = phoneUtil.parse(this, countryCode)
        return phoneUtil.isValidNumber(numberProto)
    } catch (e: NumberParseException) {
        System.err.println("NumberParseException was thrown: $e")
    }
    return false
}

fun generateRequestBody(text: String): RequestBody {
    return text.toRequestBody("text/plain".toMediaTypeOrNull())
}

fun generateFileBody(keyName: String, uri: String): MultipartBody.Part? {
    var body: MultipartBody.Part? = null
    if (!TextUtils.isEmpty(uri)) {
        val file = File(uri)
        val requestFile =
            file.asRequestBody("image/*".toMediaTypeOrNull())
        body = MultipartBody.Part.createFormData(keyName, file.name, requestFile)
    }
    return body
}

val width: Int
    get() = Resources.getSystem().displayMetrics.widthPixels

val height: Int
    get() = Resources.getSystem().displayMetrics.heightPixels

fun isLettersOrDigits(chars: String): Boolean {
    return chars.matches("^[a-zA-Z0-9]*$".toRegex())
}

fun EditText.setEmojiFilter() {
    this.filters = arrayOf(emojiFilter)
}

fun EditText.setEmojiFilterWithMaxLength(length: Int) {
    this.filters = arrayOf(emojiFilter, InputFilter.LengthFilter(length))
}

fun EditText.setEmojiNumberFilterWithMaxLength(length: Int) {
    this.filters = arrayOf(emojiFilter, numberFilter, InputFilter.LengthFilter(length))
}

fun EditText.setOnlyCharactersFilterWithMaxLength(length: Int) {
    this.filters = arrayOf(onlyCharactersFilter, InputFilter.LengthFilter(length))
}

fun EditText.setOnlyCharactersFilter() {
    this.filters = arrayOf(onlyCharactersFilter)
}

fun EditText.setAlphaNumericFilter() {
    this.filters = arrayOf(alphaNumericFilter)
}

fun EditText.setNumericFilter() {
    this.filters = arrayOf(numericFilter)
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

private val numberFilter = InputFilter { source, start, end, _, _, _ ->
    for (index in start until end) {
        if (Character.isDigit(source[index])) {
            return@InputFilter ""
        }
    }
    return@InputFilter null
}

private val onlyCharactersFilter = InputFilter { source, start, end, _, _, _ ->
    var filtered: String? = ""
    for (index in start until end) {
        val character: Char = source[index]
        if (Character.isWhitespace(character) || Character.isLetter(character)) {
            filtered += character
        }
    }
    return@InputFilter filtered
}

private val alphaNumericFilter = InputFilter { source, start, end, _, _, _ ->
    var filtered: String? = ""
    for (index in start until end) {
        val character: Char = source[index]
        if (Character.isWhitespace(character) || Character.isLetterOrDigit(character)) {
            filtered += character
        }
    }
    return@InputFilter filtered
}

private val numericFilter = InputFilter { source, start, end, _, _, _ ->
    var filtered: String? = ""
    for (index in start until end) {
        val character: Char = source[index]
        if (Character.isDigit(character)) {
            filtered += character
        }
    }
    return@InputFilter filtered
}


private val emojiExcludeFilter = InputFilter { source, start, end, _, _, _ ->
    var filtered: String? = ""
    for (i in start until end) {
        val type = Character.getType(source[i])
        if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
            filtered = ""
        }
    }
    return@InputFilter filtered
}

fun Context.isNetworkConnected(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return when {
        SDK_INT >= Build.VERSION_CODES.M -> {
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

@RequiresApi(Build.VERSION_CODES.M)
fun isInternetConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
    return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

fun Context.progressAlertDialog(): AlertDialog {
    val alertDialog: AlertDialog?
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(false)
//    val binding = LayoutProgressBinding.inflate(LayoutInflater.from(this))
//    builder.setView(binding.root)
    alertDialog = builder.create()
    alertDialog.setCanceledOnTouchOutside(false)
    alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    return alertDialog
}

fun Context.setAppLocale(language: String) {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = this.resources.configuration
    config.setLocale(locale)
    this.createConfigurationContext(config)
    this.resources.updateConfiguration(config, this.resources.displayMetrics)
}

/**
 * Repeating a string n Times
 *
 * @param n
 * @return
 */
operator fun String.times(n: Int): String {
    val sb = StringBuilder()
    repeat(n) {
        sb.append(this)
    }
    return sb.toString()
}

/**
 * String substring
 *
 * @param range
 *  uses -> val str = "Kotlin"
 *  val subStr = str[2..4] // "otl"
 */
operator fun String.get(range: IntRange) =
    substring(range.first, range.last + 1)

/**
 * Get as drawable
 *ex.->getAsDrawable(id)
 * @param id
 */
fun Context.getAsDrawable(id: Int) = ContextCompat.getDrawable(this, id)

fun Context.drawable(@DrawableRes resId: Int) =
    ResourcesCompat.getDrawable(resources, resId, null)

fun Context.font(@FontRes resId: Int) =
    ResourcesCompat.getFont(this, resId)

fun Context.dimen(@DimenRes resId: Int) =
    resources.getDimension(resId)

fun Context.anim(@AnimRes resId: Int) =
    AnimationUtils.loadAnimation(this, resId)

/**
 * Get as color
 *
 * @param id
 */
fun Context.getAsColor(id: Int) = ContextCompat.getColor(this, id)

/**
 * Log debug
 *
 * @param tag
 * @param message
 */
fun Any.logDebug(tag: String = "", message: String) {
    if (BuildConfig.DEBUG) {
        // Log.d(this::class.java.simpleName, message)
        Log.d(tag.ifEmpty { "lOGD" }, message)
    }
}


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
fun String.linkify(
    linkColor: Int = Color.BLUE,
    linkClickAction: ((link: String) -> Unit)? = null
): SpannableStringBuilder {
    val builder = SpannableStringBuilder(this)
    val matcher = Patterns.WEB_URL.matcher(this)
    while (matcher.find()) {
        val start = matcher.start()
        val end = matcher.end()
        builder.setSpan(ForegroundColorSpan(Color.BLUE), start, end, 0)
        val onClick = object : ClickableSpan() {
            override fun onClick(p0: View) {
                linkClickAction?.invoke(matcher.group())
            }
        }
        builder.setSpan(onClick, start, end, 0)
    }
    return builder
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
fun View.fadeOut(fadeDuration: Long = 300, endAlpha: Float = 0f) {
    ValueAnimator.ofFloat(1f, endAlpha).apply {
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
fun View.fadeIn(fadeDuration: Long = 300) {
    ValueAnimator.ofFloat(0f, 1f).apply {
        duration = fadeDuration
        addUpdateListener {
            val animatedValue = it.animatedValue as Float
            alpha = animatedValue
        }
    }.start()
}


/**
 * Translate
 *
 * @param from
 * @param to
 * uses -> yourView.translate(from,to)
 */
fun View.translate(from: Float, to: Float) {
    with(ValueAnimator.ofFloat(from, to)) {
        addUpdateListener {
            val animatedValue = it.animatedValue as Float
            translationY = animatedValue
        }
        start()
    }
}

/**
 * Animate scale
 *
 * @param from
 * @param to
 * @param duration
 * uses -> yourView.animateScale(100,200)
 */
fun View.animateScale(from: Int, to: Int, duration: Long = 1000) {
    val valueAnimator = ValueAnimator.ofInt(from, to)
    valueAnimator.duration = duration
    valueAnimator.addUpdateListener {
        val animatedValue = it.animatedValue as Int
        scaleX = animatedValue.toFloat()
        scaleY = animatedValue.toFloat()
    }
    valueAnimator.start()
}


/**
 * After text changed flow
 *
 * @return
 */
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

/**
 * Launch io
 * uses -> launchIO { // your code }
 * @param block
 * @receiver
 */
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


/**
 * Double click listener
 *
 * @property defaultInterval
 * @property onDoubleClick
 * @constructor Create empty Double click listener
 */
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

/**
 * Do on double click
 * uses -> view.doOnDoubleClick{ }
 * @param onDoubleClick
 * @receiver
 */
fun View.doOnDoubleClick(onDoubleClick: (View) -> Unit) {
    val safeClickListener = DoubleClickListener {
        onDoubleClick(it)
    }
    setOnClickListener(safeClickListener)
}

/**
 * On debounced listener
 *
 * @param delayInClick
 * @param listener
 * @receiver
 *
 * uses -> view.onDebouncedListener{ perform Any Action here }
 */
inline fun View.onDebouncedListener(
    delayInClick: Long = 500L,
    crossinline listener: (View) -> Unit
) {
    val enabledAgain = Runnable { isEnabled = true }
    setOnClickListener {
        if (isEnabled) {
            isEnabled = false
            postDelayed(enabledAgain, delayInClick)
            listener(it)
        }
    }
}

/**
 * On click
 *
 * @param debounceDuration
 * @param action
 * @receiver
 * uses -> button.onClick(debounceDuration = 500L) { //code }
 */
fun View.onClick(debounceDuration: Long = 300L, action: (View) -> Unit) {
    setOnClickListener(DebouncedOnClickListener(debounceDuration) {
        action(it)
    })
}

private class DebouncedOnClickListener(
    private val debounceDuration: Long,
    private val clickAction: (View) -> Unit
) : View.OnClickListener {

    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime >= debounceDuration) {
            lastClickTime = now
            clickAction(v)
        }
    }
}


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
 * To debug string
 * uses -> myIntent.toDebugString()
 * @return
 */
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

fun String?.formatStringDate(inputFormat: String, outputFormat: String): String {
    return if (this.isNullOrEmpty()) {
        ""
    } else {
        val dateFormatter = SimpleDateFormat(inputFormat, Locale.getDefault())
        val date = dateFormatter.parse(this)
        date?.let { SimpleDateFormat(outputFormat, Locale.getDefault()).format(it) }.orEmpty()
    }
}

fun String?.getYesterdayToday(date: String, format: String): String {
    try {
        val formatter = SimpleDateFormat(format)
        val date = formatter.parse(date)
        val timeInMilliseconds = date.time

        return when {
            DateUtils.isToday(timeInMilliseconds) -> {
                "today"
            }

            DateUtils.isToday(timeInMilliseconds + DateUtils.DAY_IN_MILLIS) -> {
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


/*
private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss" //2021-05-20T11:28:24
private const val TODAY = "today"
private const val YESTERDAY = "yesterday"
var date1 = "2022-11-25T00:00:00"
var formattedDate = date1.formatStringDate("YOUR_DATE_TO_FORMAT", DATE_FORMAT)

var isTodayYesterday =
    date1.getYesterdayToday(date1, DATE_FORMAT)
    */

fun String.toDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date? {
    fun String.toDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date? {
        val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
        return dateFormatter.parse(this)
    }

    fun Date.toStringFormat(format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
        return dateFormatter.format(this)
    }

//val currentDate = Date().toStringFormat()
//val currentDate2 = Date().toStringFormat(format = "dd-MM-yyyy")
//val date = "2023-01-01".toDate(format = "yyyy-MM-dd")

    fun Date.format(format: String): String = SimpleDateFormat(format).format(this)
    /*val date = Date()
    val formattedDate = date.format("yyyy-MM-dd")
    println(formattedDate) // prints something like "2023-04-08"*/


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

    /**
     * Change view children state to the value in parameter
     *
     * @param state The [Boolean] state to set
     */
    fun View.setChildrenEnabledState(state: Boolean) {
        this.getAllChildren().forEach { it.isEnabled = state }
    }


    /**
     * Takes an object of type [T], makes a null-check and if it's not null, executes [block] with [V]
     * as the receiver, and [T] as implicit param. **If [data] is null, the view will be hidden.**
     * @param data An object with useful data to pass to the view
     * @param view The view to configure
     * @param block A function to execute in the context of [view] with param [T]
     */
    fun <T, V : View> configureViewWithNullableData(data: T?, view: V, block: V.(T) -> Unit) {
        with(view) {
            data?.let { block(it) } ?: this.gone()
        }
    }

    /**
     * Add a view into a ConstraintLayout, below of the last view (like a stack).
     *
     * @param child A [View] to add
     */
    fun ConstraintLayout.addChildOnStack(
        child: View,
        layoutParams: LayoutParams = LayoutParams(
            LayoutParams.MATCH_CONSTRAINT,
            LayoutParams.WRAP_CONTENT
        )
    ) {
        // get all children of this, and the last child
        val children = this.getAllChildren()
        val lastChild = if (children.isNotEmpty()) children.last() else null

        // check if no exists lastChild, then attach child to parent in top constraint
        val topParentId = lastChild?.id ?: ConstraintSet.PARENT_ID
        val parentSide = if (lastChild == null) ConstraintSet.TOP else ConstraintSet.BOTTOM

        // generate an ID for child, and add child into this
        child.id = View.generateViewId()
        this.addView(child, layoutParams)

        // create constraints to child
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        constraintSet.connect(child.id, ConstraintSet.TOP, topParentId, parentSide)
        constraintSet.connect(
            child.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        constraintSet.connect(
            child.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )
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
        layoutParams: LayoutParams = LayoutParams(
            LayoutParams.MATCH_CONSTRAINT,
            LayoutParams.WRAP_CONTENT
        ).also {
            it.horizontalChainStyle = LayoutParams.CHAIN_SPREAD
        }
    ) {
        // get all children of this, and the last child
        val children = this.getAllChildren()
        val lastChild = if (children.isNotEmpty()) children.last() else null

        // check if doesn't exists lastChild, then attach child to parent view in start constraint;
        // otherwise, it attaches the view at end constraint of lastChild.
        val topParentId = lastChild?.id ?: ConstraintSet.PARENT_ID
        val parentSide = if (lastChild == null) ConstraintSet.START else ConstraintSet.END

        // generate an ID for child, and add child into this
        child.id = View.generateViewId()
        this.addView(child, layoutParams)

        // create constraints to child
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        constraintSet.connect(
            child.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        constraintSet.connect(
            child.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM
        )
        constraintSet.connect(child.id, ConstraintSet.START, topParentId, parentSide)
        constraintSet.connect(
            child.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )
        if (lastChild != null) constraintSet.connect(
            lastChild.id,
            ConstraintSet.END, child.id,
            ConstraintSet.START
        )
        constraintSet.applyTo(this)
    }

    /**
     * Executes a function inside a [TextWatcher] [onTextChanged] method.
     */
    internal fun TextView.onTextChanged(block: (currentText: String) -> Unit) {
        val textWatcher = object : TextWatcher {
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

    /**
     * Get a query parameter from the Uri data in the intent,
     * and an empty string if the data or value is null
     */
    fun Activity.deeplinkParam(key: String) = intent.data?.getQueryParameter(key) ?: ""

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

    /**
     * To title case
     * uses -> println("java kotlin".toTitleCase())  // Output: "Java Kotlin"
     * @return
     */
    fun String.toTitleCase(): String {
        return this.split(" ").joinToString(" ") {
            it.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }
    }

    //Int to Enum
    inline fun <reified T : Enum<T>> Int.toEnum(): T? {
        return enumValues<T>().firstOrNull { it.ordinal == this }
    }

    //Enum to Int
    inline fun <reified T : Enum<T>> T.toInt(): Int {
        return this.ordinal
    }


    /**
     * Snackbar
     * uses -> rootView.snackbar("This is snackbar message")
     * @param message
     * @param duration
     */
    fun View.snackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }

    /**
     * Snackbar
     * uses -> rootView.snackbar(R.string.snackbar_message)
     * @param message
     * @param duration
     */
    fun View.snackbar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }

    val String.isDigitOnly: Boolean
    get() = matches(Regex("^\\d*\$"))

    val String.isAlphabeticOnly: Boolean
    get() = matches(Regex("^[a-zA-Z]*\$"))

    val String.isAlphanumericOnly: Boolean
    get() = matches(Regex("^[a-zA-Z\\d]*\$"))

//val isValidNumber = "1234".isDigitOnly // Return true
//val isValid = "1234abc".isDigitOnly // Return false
//val isOnlyAlphabetic = "abcABC".isAlphabeticOnly // Return true
//val isOnlyAlphabetic2 = "abcABC123".isAlphabeticOnly // Return false
//val isOnlyAlphanumeric = "abcABC123".isAlphanumericOnly // Return true
//val isOnlyAlphanumeric2 = "abcABC@123.".isAlphanumericOnly // Return false


    /**
     * Is null
     * uses -> if(obj.isNull){  }else { }
     */
    val Any?.isNull get() = this == null

    /**
     * If null
     * uses -> obj.ifNull { }
     * @param block
     * @receiver
     */
    fun Any?.ifNull(block: () -> Unit) = run {
        if (this == null) {
            block()
        }
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
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    onComplete()
                }
            })
        }
        animator.start()
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
     *
     * uses -> val name = etName.value
     */
    val EditText.value
    get() = text?.toString() ?: ""

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
     * To editable
     *
     * @return
     * uses -> etName.text = "First name".toEditable()
     *
     */
    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)


    fun String.removeAllWhitespaces(): String {
        return this.replace("\\s+".toRegex(), "")
    }

    fun String.removeDuplicateWhitespaces(): String {
        return this.replace("\\s+".toRegex(), " ")
    }

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

        val size = if (SDK_INT >= Build.VERSION_CODES.R) {
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


    fun fromHtml(source: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(source)
        }
    }

    /*fun bearingBetweenLocations(latLng1: LatLng, latLng2: LatLng): Float {
        val pi = 3.14159
        val lat1 = latLng1.latitude * pi / 180
        val long1 = latLng1.longitude * pi / 180
        val lat2 = latLng2.latitude * pi / 180
        val long2 = latLng2.longitude * pi / 180
        val dLon = long2 - long1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - (sin(lat1)
                * cos(lat2) * cos(dLon))
        var bearing = atan2(y, x)
        bearing = Math.toDegrees(bearing)
        bearing = (bearing + 360) % 360
        return bearing.toFloat()
    }*/

    fun View.showHideView(state: Boolean) = run { if (state) visible() else gone() }

    fun Context.getDrawableExt(@DrawableRes id: Int) = AppCompatResources.getDrawable(this, id)


    private fun CharSequence.toSpannableStringBuilder() = SpannableStringBuilder(this)

    fun SpannableStringBuilder.spanText(span: Any): SpannableStringBuilder {
        setSpan(span, 0, length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        return this
    }

    fun CharSequence.foregroundColor(@ColorInt color: Int): SpannableStringBuilder {
        val span = ForegroundColorSpan(color)
        return toSpannableStringBuilder().spanText(span)
    }

    fun CharSequence.backgroundColor(@ColorInt color: Int): SpannableStringBuilder {
        val span = BackgroundColorSpan(color)
        return toSpannableStringBuilder().spanText(span)
    }

    fun CharSequence.relativeSize(size: Float): SpannableStringBuilder {
        val span = RelativeSizeSpan(size)
        return toSpannableStringBuilder().spanText(span)
    }

    fun CharSequence.superScript(): SpannableStringBuilder {
        val span = SuperscriptSpan()
        return toSpannableStringBuilder().spanText(span)
    }

    fun CharSequence.strike(): SpannableStringBuilder {
        val span = StrikethroughSpan()
        return toSpannableStringBuilder().spanText(span)
    }

    operator fun SpannableStringBuilder.plus(other: SpannableStringBuilder): SpannableStringBuilder {
        return this.append(other)
    }

    operator fun SpannableStringBuilder.plus(other: CharSequence): SpannableStringBuilder {
        return this + other.toSpannable()
    }

    /**
     * With not null
     *
     * @param T
     * @param R
     * @param block
     * @receiver
     * @return
     * uses -> val nullableValue: String? = null
     *  nullableValue.withNotNull { value -> //code }
     *
     */
    inline fun <T : Any, R> T?.withNotNull(block: (T) -> R): R? {
        return this?.let(block)
    }

    /**
     * To live data
     *
     * @param T
     * @return
     *
     */
    fun <T> Flow<T>.toLiveData(): LiveData<T> {
        return liveData {
            collect {
                emit(it)
            }
        }
    }

    /**
     * Not empty
     *
     * @param T
     * @return
     *  uses -> if (list.notEmpty()) { //code }
     *
     */
    fun <T> Collection<T>?.notEmpty(): Boolean {
        return this?.isNotEmpty() == true
    }

    /**
     * Get or throw
     *
     * @param K
     * @param V
     * @param key
     * @return
     * uses -> val map = mapOf("key1" to "value1", "key2" to "value2")
     * val value = map.getOrThrow("key3")
     */
    fun <K, V> Map<K, V>.getOrThrow(key: K): V {
        return this[key] ?: throw NoSuchElementException("Key $key not found in map")
    }

    fun Int.toFormattedString(): String {
        return NumberFormat.getInstance().format(this)
    }

    fun Long.toFormattedString(): String {
        return NumberFormat.getInstance().format(this)
    }

    fun Date.toFormattedString(): String {
        return SimpleDateFormat.getDateInstance().format(this)
    }

    /**
     * To bitmap
     *
     * @return
     * uses -> val drawable = ContextCompat.getDrawable(context, R.drawable.my_drawable)
     * val bitmap = drawable.toBitmap()
     */
    fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) {
            return bitmap
        }

        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)

        return bitmap
    }


    /**
     * To uri
     *
     * @return
     * uses ->val filePath = "/storage/emulated/0/Download/my_file.pdf"
     * val fileUri = filePath.toUri()
     */
    fun String.toUri(): Uri {
        return Uri.parse(this)
    }

    /**
     * Apply if
     *
     * @param T
     * @param condition
     * @param block
     * @receiver
     * @return
     * uses -> val number = 5
     * val formattedNumber = number.applyIf(number > 10) { toFormattedString() }
     *
     */
    inline fun <T> T.applyIf(condition: Boolean, block: T.() -> Unit): T {
        return if (condition) {
            this.apply(block)
        } else {
            this
        }
    }

    fun String?.isNullOrEmpty(): Boolean {
        return this == null || this.isEmpty()
    }

    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                afterTextChanged.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun ImageView.loadImageWithGlide(url: String) {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_background)
            .into(this)
    }

    inline fun <reified T : Any> SharedPreferences.get(key: String, defaultValue: T? = null): T? {
        val value = when (T::class) {
            String::class -> getString(key, defaultValue as? String) as T?
            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
            Long::class -> getLong(key, defaultValue as? Long ?: -1L) as T?
            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
            else -> throw IllegalArgumentException("Unsupported type: ${T::class.java}")
        }
        return value
    }

    inline fun <reified T : Any> SharedPreferences.put(key: String, value: T?) {
        val editor = edit()
        when (T::class) {
            String::class -> editor.putString(key, value as? String)
            Int::class -> editor.putInt(key, value as? Int ?: -1)
            Long::class -> editor.putLong(key, value as? Long ?: -1L)
            Float::class -> editor.putFloat(key, value as? Float ?: -1f)
            Boolean::class -> editor.putBoolean(key, value as? Boolean ?: false)
            else -> throw IllegalArgumentException("Unsupported type: ${T::class.java}")
        }
        editor.apply()
    }

    fun ViewGroup.inflate(layoutRes: Int): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, false)
    }

    fun Context.getScreenWidth(): Int {
        val displayMetrics = resources.displayMetrics
        return displayMetrics.widthPixels
    }

    fun Date.getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dateFormat.format(this)
    }

    fun Context.isPermissionGranted(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun WebView.loadUrl(url: String?) {
        if (!url.isNullOrEmpty()) {
            loadUrl(url)
        }
    }

    fun String.formatPhoneNumber(): String {
        val cleanedNumber = replace("[^0-9]".toRegex(), "")
        return cleanedNumber.chunked(3).joinToString("-")
    }

    /**
     * Decimal format
     * uses -> 1.decimalFormat("0.0") -> 1.0
     * @param pattern
     * @return
     */
    fun Any.decimalFormat(pattern: String): String {
        val decimalFormat = DecimalFormat(pattern)
        return decimalFormat.format(this)
    }

    //https://github.com/sohaibkhaa/FullScreenSample.git
    fun Activity.enableFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    fun Activity.disableFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }

    fun Activity.setStatusBarColor(color: Int) {
        window.statusBarColor = ContextCompat.getColor(this, color)
    }

    fun Activity.setNavBarColor(color: Int) {
        window.navigationBarColor = ContextCompat.getColor(this, color)
    }

    fun View.applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view. Here the system is setting
            // only the bottom, left, and right dimensions, but apply whichever insets are
            // appropriate to your layout. You can also update the view padding
            // if that's more appropriate.
            (layoutParams as ViewGroup.MarginLayoutParams).setMargins(
                insets.left, insets.top, insets.right, insets.bottom
            )
            // Return CONSUMED if you don't want want the window insets to keep being
            // passed down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
    }

    fun Activity.hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    fun Activity.showSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }


    /**
     * Show dialog
     * showDialog(context) {
     * setTitle("Important Message")
     * setMessage("This is an important dialog.")
     * setPositiveButton("OK") { dialog, _ ->dialog.dismiss()}
     * setNegativeButton("Cancel") { dialog, _ ->dialog.dismiss()}
     * }
     *
     * @param context
     * @param init
     * @receiver
     */
    fun showDialog(context: Context, init: AlertDialog.Builder.() -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.init()
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Edit
     *
     * val sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
     * sharedPreferences.edit {
     * putString("key", "value")
     * putInt("count", 5)
     * commit()
     * }
     *
     * @param func
     * @receiver
     */
    inline fun SharedPreferences.edit(func: SharedPreferences.Editor.() -> Unit) {
        val editor = edit()
        editor.func()
        editor.apply()
    }

    fun ImageView.loadImageUrl(imageUrl: String, placeholder: Int) {
        val options = RequestOptions().centerCrop().placeholder(placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
            .dontAnimate()
            .dontTransform()

        Glide.with(this).load(imageUrl).apply(options).into(this)

    }

    fun formatTimeAgo(date1: String): String {  // Note : date1 must be in   "yyyy-MM-dd hh:mm:ss"   format
        var conversionTime = ""
        try {
            val format = "yyyy-MM-dd'T'HH:mm:ss'Z'"

            val sdf = SimpleDateFormat(format)

            val datetime = Calendar.getInstance()
            var date2 = sdf.format(datetime.time).toString()

            val dateObj1 = sdf.parse(date1)
            val dateObj2 = sdf.parse(date2)
            val diff = dateObj2.time - dateObj1.time

            val diffDays = diff / (24 * 60 * 60 * 1000)
            val diffhours = diff / (60 * 60 * 1000)
            val diffmin = diff / (60 * 1000)
            val diffsec = diff / 1000
            if (diffDays > 1) {
                conversionTime += diffDays.toString() + " days "
            } else if (diffhours > 1) {
                conversionTime += (diffhours - diffDays * 24).toString() + " hours "
            } else if (diffmin > 1) {
                conversionTime += (diffmin - diffhours * 60).toString() + " min "
            } else if (diffsec > 1) {
                conversionTime += (diffsec - diffmin * 60).toString() + " sec "
            }
        } catch (ex: java.lang.Exception) {
            Log.e("formatTimeAgo", ex.toString())
        }
        if (conversionTime != "") {
            conversionTime += "ago"
        }
        return conversionTime
    }

    fun DateTimeHourAgo(dateTime: String?): String? {
        val prettyTime = PrettyTime(Locale.getDefault())
        var isTime: String? = null
        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val date = simpleDateFormat.parse(dateTime)
            isTime = prettyTime.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return isTime
    }

    fun DateFormat(dateNews: String?): String? {
        val isDate: String?
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy - HH:mm:ss", Locale(getCountry()))
        isDate = try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(dateNews)
            dateFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            dateNews
        }
        return isDate
    }

    fun getCountry(): String {
        val locale = Locale.getDefault()
        val strCountry = locale.country
        return strCountry.toLowerCase()
    }

    fun <T : Any?> fragmentArgs() = object : ReadWriteProperty<Fragment, T> {

        override fun getValue(thisRef: Fragment, property: KProperty<*>): T =
            thisRef.arguments?.get(property.name) as T

        override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
            if (thisRef.arguments == null) thisRef.arguments = bundleOf()

            thisRef.requireArguments().putAll(
                bundleOf(property.name to value)
            )
        }
    }

/*
    // setting the arguments and initlizing the frgament
    DelegateFragment().apply {
        userId = "hello"
    }

// inside DelegateFragment using the delegate fun
    private var userId:String by fragmentArgs()*/


    // Function literals with receiver
    inline fun IO(crossinline block: suspend CoroutineScope.() -> Unit) =
        CoroutineScope(Dispatchers.IO).launch {
            block.invoke(this)
        }

/*
// usage
    IO{ //Coroutine with IO Dispatcher
        repository.apiCall()
    }*/
