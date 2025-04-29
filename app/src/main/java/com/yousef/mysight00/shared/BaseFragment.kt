package com.yousef.mysight00.shared

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

abstract class BaseFragment : Fragment() {

    protected abstract val layoutId: Int

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    protected open fun setupViews() {}
    protected open fun setupObservers() {}

    protected fun navigateTo(destinationId: Int) {
        try {
            findNavController().navigate(destinationId)
        } catch (e: Exception) {
            showErrorToast("حدث خطأ أثناء التنقل")
        }
    }

    protected fun showErrorToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected fun showSuccessToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected fun requireAppContext(): Context = requireActivity().applicationContext

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }
} 