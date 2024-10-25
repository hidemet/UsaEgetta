package com.example.hammami.presentation.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hammami.core.ui.UiText
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class BaseFragment : Fragment() {
    private val TAG = "BaseFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "${javaClass.simpleName} onCreate")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "${javaClass.simpleName} onViewCreated")
        try {
            setupUI()
            observeFlows()
        } catch (e: Exception) {
            Log.e(TAG, "${javaClass.simpleName} Error in onViewCreated", e)
            handleUnexpectedError(e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "${javaClass.simpleName} onDestroyView")
    }

    abstract fun setupUI()

    abstract fun observeFlows()

    protected open fun showLoading(isLoading: Boolean) {
        Log.d(TAG, "${javaClass.simpleName} showLoading: $isLoading")
    }

    protected fun showSnackbar(
        message: UiText,
        actionText: UiText? = null,
        action: (() -> Unit)? = null
    ) {
        Log.d(TAG, "${javaClass.simpleName} showSnackbar: ${message.asString(requireContext())}")
        view?.let {
            Snackbar.make(it, message.asString(requireContext()), Snackbar.LENGTH_LONG).apply {
                if (actionText != null && action != null) {
                    setAction(actionText.asString(requireContext())) { action() }
                }
                show()
            }
        }
    }

    protected open fun onBackClick() {
        Log.d(TAG, "${javaClass.simpleName} onBackClick")
        findNavController().navigateUp()
    }

    protected fun updateFieldValidationUI(field: TextInputLayout, errorMessage: UiText?) {
        Log.d(TAG, "${javaClass.simpleName} updateFieldValidationUI: ${field.id}, error: $errorMessage")
        field.error = errorMessage?.asString(requireContext())
        field.isErrorEnabled = errorMessage != null
    }

    protected fun <T> Flow<T>.collectLatestLifecycleFlow(collect: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG, "${javaClass.simpleName} Starting flow collection")
                this@collectLatestLifecycleFlow.collect {
                    try {
                        collect(it)
                    } catch (e: Exception) {
                        Log.e(TAG, "${javaClass.simpleName} Error in flow collection", e)
                        handleUnexpectedError(e)
                    }
                }
            }
        }
    }

    protected open fun handleUnexpectedError(e: Throwable) {
        Log.e(TAG, "${javaClass.simpleName} Unexpected error", e)
        showSnackbar(UiText.DynamicString("Si è verificato un errore inaspettato: ${e.message}"))
    }
}