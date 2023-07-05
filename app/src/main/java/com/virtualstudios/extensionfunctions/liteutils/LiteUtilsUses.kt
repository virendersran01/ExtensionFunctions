package com.virtualstudios.extensionfunctions.liteutils

/*
RecyclerUtils
RecyclerUtils contain a very handy class named RecyclerAdapterUtil which can be used to make recycler adapters in as little as 4 lines. No need to create a separate adapter class for every recycler view.

The constructor of RecyclerAdapterUtil takes 3 parameters.

Context - Application Context.
ItemList - List of objects of type T which will be used as the primary data source for setting data to view holder items.
LayoutResourceId - Resource id of the layout that represents single item view for RecyclerView.
So to create a recycler adapter that displays a list of strings, you would write something like this:

val list = listOf("Test", "1", "2", "3", "This is a test", "123")
val recyclerAdapter = RecyclerAdapterUtil<String>(this, list, R.layout.item_recycler_view)
recyclerAdapter.addViewsList(R.id.textView, R.id.imageView)
The addViewsList function is important, pass the id's of all views contained in the single layout file provided in constructor (in this case it isR.layout.item_recycler_view) that you want to refer to while binding data. There are two ways to pass these id's.

All the views that you want to reference while binding data should be provided before hand, else the app will not function properly.
recyclerAdapter.addViewsList(R.id.textView, R.id.imageView)
*/
/* OR *//*

val listOfViews = listOf(R.id.textView, R.id.imageView)
recyclerAdapter.addViewsList(listOfViews)
To bind data, add data bind listener:

recyclerAdapter.addOnDataBindListener { itemView, item, position. innerViews ->
    val textView = innerViews[R.id.textView] as TextView
    textView.text = item
}
addOnDataBindListener is a lambda which provides three items:

itemView - The ViewHolder itself.
item - Data item from the list.
position - Position of the data item in the list.
innerViews - A Map<Int, View> containing the reference to the views that were provided in the addViewsList function.
Click Listeners
You can also add OnClickListener and OnLongClickListener simply by implementing two lambdas.

//OnClickListener
recyclerAdapter.addOnClickListener { item, position ->
    //Take action when item is pressed
}

//OnLongClickListener
recyclerAdapter.addOnLongClickListener { item, position ->
    //Take action when item is long pressed
}
Both addOnClickListener and addOnLongClickListener provide lambda with two parameters:

item - Data item from the list.
position - Position of the data item in the list.
Using Builder pattern for more consice code
Use RecyclerAdapterUtil.Builder to chain functions as shown below.

RecyclerAdapterUtil.Builder(this, list, R.layout.item_recycler_view)
.viewsList(R.id.textView, R.id.imageView)
.bindView { itemView, item, position, innerViews ->
    val textView = innerViews[R.id.textView] as TextView
    textView.text = item
}
.addClickListener { item, position ->
    //Take action when item is pressed
}
.addLongClickListener { item, position ->
    //Take action when item is long pressed
}
.into(recyclerView)
into(RecyclerView) function takes the reference of RecyclerView and directly sets the adapter to it so you don't have to do it explicitly. If you want the object of adapter and want to set it manually use build() instead of into(RecyclerView).

ScrollUtils
Hide FloatingActionButton when user scrolls up and show it again when scrolled down. You can achieve this by using function hideFloatingActionButtonOnScroll on NestedScrollView and RecyclerView. These functions are implemented as extension functions.

val nestedScrollView = findViewById(R.id.nestedScrollView) as NestedScrollView
val floatingActionButton = findViewById(R.id.floatingActionButton) as FloatingActionButton

nestedSrollView.hideFloatingActionButtonOnScroll(floatingActionButton)
Take custom action when scrolled up and down
If you want to take custom action when scrolled up or down you can implement ScrollListener using the function addScrollListener(ScrollListener). This works with both NestedScrollView and RecyclerView.

nestedScrollView.addScrollListener(object : ScrollListener {
    override fun scrolledDown() {
        //Take Action when user scrolls down
    }

    override fun scrolledUp() {
        //Take Action when user scrolls up
    }
})
ToastUtils
Making toast has never been easier. Just use shortToast(String) for making short toast and longToast(String) for making long ones. These functions are implemented as extension functions on Context, so wherever Context is available, these functions can be used.

shortToast("This is a short toast")
longToast("This is a long toast")
Making colored Toasts
To make a toast with custom background and text color use coloredShortToast(message, backgroundColor, textColor) or coloredLongToast(message, backgroundColor, textColor).

Both of these functions take three parameters:

message: String displayed by the toast.
backgroundColor: Background Color of the toast.
textColor: Color of the text shown.
coloredShortToast("Colored short toast", R.color.darker_gray, R.color.black)
coloredLongToast("Colored long toast", R.color.darker_gray, R.color.black)
SPUtils
Easy DSL for sharedpreferences. No need to write long lines of code when using SharedPreferences. The below functions are implemented as extension functions on Context, so they are available wherever Context is available.

Storing values
To use the default SharedPreferences file which is provided by the library itself, use defaultSharedPreferences function which takes a lambda for required operations, much easier to understand with an example. The mode used to open file is MODE_PRIVATE.

defaultSharedPreferences {
    putString("string", "Some Value 123")
    putInt("integer", 1)
}
If you want to use your own file and mode the use sharedPreferences(fileName, mode, lambda).

sharedPreferences("SP", Context.MODE_PRIVATE) {
    putString("string", "Some Value 123")
    putInt("integer", 1)
}
Fetching values
To get value from default SharedPreferences use getFromDefaultSharedPreferences<T>(key, defaultValue).

getFromDefaultSharedPreferences<String>("string", "default value")
You can also eliminate the need to specify a type explicitly, but in that case the type will be inferred from the type of defaultValue which is the second parameter so you can write.

getFromDefaultSharedPreferences("string", "default value")
To get from custom SharedPreferences file use getFromSharedPreferences<T>(fileName, key, defaultValue).

getFromSharedPreferences<String>("SP", "string", "default")
*/
/* OR *//*

getFromSharedPreferences("SP", "string", "default")
ValidatorUtils
Set of functions that provide easy and fast text validation. More than 15+ validation types available. Use the Validator class to access all the validation functions. This class is available using an extension function on EditText and TextInputEditText. Just call the validator() function.

Using Validator directly on EditText.

var result: Boolean = editText.validator()
    .email()
    .atLeastOneUpperCase()
    .atLeastOneLowerCase()
    .maximumLength(20)
    .minimumLength(5)
    .noNumbers()
    .validate()
The Validator class has all the validation functions, chain all the functions that you require and call validate() to process. The result returned is a Boolean, true if all validation are passed, false if any one of them fails.

Adding callbacks to listen to results
If you want to take action after the validation is complete, there are two callbacks available, addSuccessCallback() and addErrorCallback(ValidationError). The addSuccessCallback is invoked when the valdiation passes, addErrorCallback is invoked when validation fails.

var result = editText.validator()
    .email()
    .atLeastOneUpperCase()
    .atLeastOneLowerCase()
    .maximumLength(20)
    .minimumLength(5)
    .noNumbers()
    .addSuccessCallback {
        //Proceed
    }
    .addErrorCallback { errorType ->
        when (errorType) {
            ValidationError.EMAIL -> {
                editText.error = "Email format is incorrect"
            }
            ValidationError.AT_LEAST_ONE_LOWER_CASE -> {
                editText.error = "Please provide at-least one lower case letter"
            }
            ValidationError.AT_LEAST_ONE_UPPER_CASE -> {
                editText.error = "Please provide at-least one upper case letter"
            }
            else -> {
                editText.error = "Not Enough"
            }
        }
    }
    .validate()
The addErrorCallback also provides a parameter of type ValidationError. This parameter provies the type of validation error that has occured. ValidationError is an enum, the naming convention is very simple, the names are same as the corresponding validation functions. For example, for validation function atLeastOneLowerCase, the validation error will be ValidationError.AT_LEAST_ONE_LOWER_CASE.

Using Validator independently
Validator class can also be used independently, just instantiate an object of it.

val validator = Validator("somePassword#123")
validator.atLeastOneNumber()
.atLeastOneUpperCase()
.minimumLength(8)
.maximumLength(32)
.atLeastOneSpecialCharacter()
.validate()
LogUtils
Simple and easy logging for android using LogUtils. Supported 6 log levels. To begin, set the log levels that you want to be logged. Messages will not be logged if no LogLevel is added.

LogUtils.addLevel(LogLevel.DEBUG)
LogUtils.addLevel(LogLevel.INFO)
LogUtils.addLevel(LogLevel.ERROR)
LogUtils.addLevel(LogLevel.VERBOSE)
LogUtils.addLevel(LogLevel.WARN)
LogUtils.addLevel(LogLevel.WTF)
To enable all log levels just use LogLevel.ALL

LogUtils.addLevel(LogLevel.ALL)
Use the following functions throughout your app for logging purposes.

debug("This is a debug message")
error("Some error occurred")
warn("This is a warning")
info("Some information")
verbose("VERBOSE!")
wtf("Ignore this")
json("{message:'This is a message', version: {num: 10}}")
shout("Shout this message loud!\nThank YOU")
exception(Exception("ERROR"))
JSON
To log a json string use the json(jsonString) function. This function will throw error if json structure is not right. To see the error LogLevel.ERROR should be enabled.

json("{message:'This is a message', version: {num: 10}}")
Output:

{
    "message": "This is a message",
    "version": {
    "num": 10
}
}
Shout
shout(message) prints a big box around the message.

shout("Shout this message loud!\nThank YOU")*/
