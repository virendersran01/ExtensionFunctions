package com.virtualstudios.extensionfunctions.compose

import android.util.Log
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.virtualstudios.extensionfunctions.compose.ui.theme.ExtensionFunctionsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SideEffects(modifier: Modifier = Modifier) {
    Surface {
        DerivedStateOfEx()
    }
}

@Preview(showBackground = true)
@Composable
private fun SideEffectsPreview() {
    ExtensionFunctionsTheme {
        SideEffects()
    }
}

@Composable
fun DerivedStateOfEx(modifier: Modifier = Modifier) {
    val tableOf = remember {
        mutableStateOf(5)
    }
    val indexOf = produceState(initialValue = 1) {
        repeat(9){
            delay(1000)
            value = value+1
        }
    }

    val state = derivedStateOf {
        "${tableOf.value} * ${indexOf.value} = ${tableOf.value * indexOf.value}"
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(text = state.value)
    }
}

@Composable
fun Loader(modifier: Modifier = Modifier) {
    val state = produceState(initialValue = 0) {
        while (true) {
            delay(1000)
            value = (value + 5) % 360
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .rotate(state.value.toFloat())
            )
            Text(text = "Loading")
        }

    }
}

@Composable
fun ProduceStateEx(modifier: Modifier = Modifier) {
    val state = produceState(initialValue = 0 ) {
        for (i in 1..10) {
            delay(1000)
            value += 1
        }
    }
    Text(
        text = state.value.toString(),
        style = MaterialTheme.typography.headlineLarge
    )
}

@Composable
fun KeyboardVisibity(modifier: Modifier = Modifier) {
    KeyboardComposable()
    TextField(value = "", onValueChange = {})
}

@Composable
fun KeyboardComposable(modifier: Modifier = Modifier) {
    val view = LocalView.current
    DisposableEffect(key1 = Unit) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener { 
            val insets = ViewCompat.getRootWindowInsets(view)
            val isKeyboardVisible = insets?.isVisible(WindowInsetsCompat.Type.ime())
            Log.d("TAG", "KeyboardComposable: $isKeyboardVisible")
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { 
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
}

@Composable
fun DisposableEffectEx(modifier: Modifier = Modifier) {
    val state = remember {
        mutableStateOf(false)
    }
    DisposableEffect(key1 = state.value) {
        Log.d("TAG", "DisposableEffectEx: Started")
        onDispose {
            Log.d("TAG", "DisposableEffectEx: cleaning up")
        }
    }
    Button(onClick = { state.value = !state.value }) {
        Text(text = "Dispose")
    }
}

fun a(){ Log.d("TAG", "A fun")}
fun b(){ Log.d("TAG", "B fun")}

@Composable
fun LandingScreenApp(modifier: Modifier = Modifier) {
    val state = remember {
        mutableStateOf(::a)
    }
    Button(onClick = { state.value = ::b }) {
        Text(text = "Click to change state")
    }
    LandingScreen(state.value)
}

@Composable
fun LandingScreen(timeout: () -> Unit) {
    val currentOnTimeOut by rememberUpdatedState(newValue = timeout)
    LaunchedEffect(key1 = true) {
        delay(5000)
        currentOnTimeOut()
    }
}

@Composable
fun CounterApp(modifier: Modifier = Modifier) {
    var count by remember {
        mutableStateOf(0)
    }
    LaunchedEffect(key1 = Unit) {
        delay(2000)
        count = 10
    }
    Counter1(count)
}

@Composable
fun Counter1(count: Int) {
    var state = rememberUpdatedState(newValue = count)
    LaunchedEffect(key1 = Unit) {
        delay(5000)
        Log.d("TAG", "Counter1: ${state.value}")
    }
    Text(text = "Count is $count")
}

@Composable
fun CoroutineScopeComposable(modifier: Modifier = Modifier) {
    var count by remember {
        mutableStateOf(0)
    }
    val scope = rememberCoroutineScope()
    var text = "Counter is running $count"
    if (count == 10){
        text = "Counter Stopped"
    }
    Column {
        Text(text = text)
        Button(onClick = {
            scope.launch {
                Log.d("TAG", "CoroutineScopeComposable: coroutine started")
                try {
                    for (i in 1..10) {
                        count++
                        delay(1000)
                    }
                } catch (e: Exception) {
                    Log.d("TAG", "CoroutineScopeComposable: ex: ${e.message}")
                }
            }
        }) {
            Text(text = "Start")
        }
    }
}

@Composable
fun LaunchEffectComposable(modifier: Modifier = Modifier) {
    var count by remember {
        mutableStateOf(0)
    }
    LaunchedEffect(key1 = Unit) {
        Log.d("TAG", "LaunchEffectComposable: Started")
        try {
            for (i in 1..10){
                count++
                delay(1000)
            }
        }catch (e: Exception){
            Log.d("TAG", "LaunchEffectComposable: ${e.message.toString()}")
        }
    }
    var text = "Counter is running $count"
    if (count == 10){
        text = "Counter Stopped"
    }
    Text(text = text)
}

@Composable
fun Counter(modifier: Modifier = Modifier) {
    var count by remember {
        mutableStateOf(0)
    }
    val key = count % 3 == 0
    LaunchedEffect(key1 = key) {
        Log.d("TAG", "Counter: $count")
    }
    Button(onClick = { count++ }) {
        Text(text = "Increment Counter $count")
    }
}

@Composable
fun ListComposable(modifier: Modifier = Modifier) {
    val cat = remember {
        mutableStateOf(emptyList<String>())
    }
    LaunchedEffect(key1 = Unit) {
        cat.value = fetchCat()
    }
    LazyColumn {
        items(cat.value){
            Text(text = it)
        }
    }
}

fun fetchCat(): List<String>{
    Log.d("TAG", "fetchCat: ")
    return listOf("cat1", "cat2", "cat3")
}
