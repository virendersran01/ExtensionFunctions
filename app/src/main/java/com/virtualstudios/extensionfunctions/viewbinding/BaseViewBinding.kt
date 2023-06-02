package com.virtualstudios.extensionfunctions.viewbinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

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

inline fun <reified T : ViewBinding> inflateViewBinding(parent: ViewGroup): T {
    val layoutInflater = LayoutInflater.from(parent.context)
    return T::class.java.getMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.javaPrimitiveType
    ).invoke(null, layoutInflater, parent, false) as T
}

//val binding = inflateViewBinding<MyItemBinding>(parent)