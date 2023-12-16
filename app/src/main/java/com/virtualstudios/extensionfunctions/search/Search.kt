package com.virtualstudios.extensionfunctions.search

import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf

fun EditText.getQueryTextChangeStateFlow(): StateFlow<String> {

    val query = MutableStateFlow("")

    doOnTextChanged { text, _, _, _ ->
        query.value = text.toString()
    }

    return query
}

/*@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
private fun initSearchView(){
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED){
            binding.inputSearch.getQueryTextChangeStateFlow()
                .debounce(300)
                .filter { query ->
                    if (query.isEmpty()) {
                        //textViewResult.text = ""
                        return@filter false
                    } else {
                        return@filter true
                    }
                }.flatMapLatest { query ->
                    logDebug("query: $query")
                    flowOf(query)
                }.flowOn(Dispatchers.Default)
                .collect{ result ->
                    logDebug("Result: $result")
                }

        }
    }
}*/
