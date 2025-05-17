package com.yousef.mysight00.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.RetrofitInstance
import com.yousef.mysight00.databinding.FragmentForgetPassBinding
import com.yousef.mysight00.model.forgotPasswordRequest
import com.yousef.mysight00.model.loginRequest
import kotlinx.coroutines.launch


class ForgetPassFragment : Fragment() {

    private var _binding: FragmentForgetPassBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgetPassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtCancelForget.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnDoneForget.setOnClickListener {
        //    findNavController().navigate(R.id.action_forget_password_to_new_password)
            Log.i("ForgetpasswordFragment", "onViewCreated: ${binding.phNumForget.text.toString()}")
            val forget = forgotPasswordRequest(binding.phNumForget.text.toString())
            lifecycleScope.launch {
                val response = RetrofitInstance.api.forgotPassword(forget)
                if (response.isSuccessful){
                    Log.i("ForgetpasswordFragment", "onViewCreated sucess : ${response.body()?.message}")
                    findNavController().navigateUp()
                }

                else{
                    Log.i("ForgetpasswordFragment", "onViewCreated failed : ${response.code()} ${response.message()}")
                    binding.phNumForget.error = "Invalid Eamail "
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
