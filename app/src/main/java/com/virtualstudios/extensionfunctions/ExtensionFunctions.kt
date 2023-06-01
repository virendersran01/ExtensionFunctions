import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.virtualstudios.extensionfunctions.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Locale
import java.util.regex.Pattern


/**
 * Base activity for accessing view binding
 *
 * @param VB
 * @property bindingInflater
 * @constructor Create empty Base activity
 */
abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : AppCompatActivity() {

    lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)
    }
}

/**
 * Base fragment for accessing view binding
 *
 * @param VB
 * @property bindingInflater
 * @constructor Create empty Base fragment
 */
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

/** Set the View visibility to GONE*/
fun View.gone() {
    this.visibility = View.GONE
}

/** Set the View visibility to GONE*/
fun View.visible() {
    this.visibility = View.VISIBLE
}


/**
 * Toast
 *
 * @param msg
 */
fun Context.toast(msg: String) {
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



fun Context.drawable(@DrawableRes resId: Int) =
    ResourcesCompat.getDrawable(resources, resId, null)

fun Context.font(@FontRes resId: Int) =
    ResourcesCompat.getFont(this, resId)

fun Context.dimen(@DimenRes resId: Int) =
    resources.getDimension(resId)

fun Context.anim(@AnimRes resId: Int) =
    AnimationUtils.loadAnimation(this, resId)

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

