package com.example.artkeeper.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.artkeeper.R
import com.example.artkeeper.databinding.FragmentRegistrationBinding
import com.example.artkeeper.presentation.RegistrationViewModel
import com.example.artkeeper.presentation.RegistrationViewModelFactory
import com.example.artkeeper.utils.ArtKeeper


class RegistrationFragment : Fragment() {
    companion object {
        const val TAG = "RegistrationFragment"
    }

    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!


    private val viewModel: RegistrationViewModel by viewModels {
        RegistrationViewModelFactory(
            (activity?.application as ArtKeeper).userRepository
        )
    }


    //TODO onBackPressed show dialog (sei sicuro di voler abbandonare? dovrai registrarti...)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.confirmButton.setOnClickListener {
            Log.d(TAG, "Confirm")

            if (createUser()) {
                findNavController().navigate(R.id.action_registrationFragment_to_mainFragment)
                Log.d(TAG, "user registered")
            } else
                Log.d(TAG, "registration failed")
        }
    }

    private fun createUser(): Boolean {
        //Log.d(TAG, "$name, $lastName, $nickName")
        viewModel.setName(binding.textInputName.text.toString())
        viewModel.setLastName(binding.textInputLastname.text.toString())
        viewModel.setNickname(binding.textInputNickname.text.toString())
        return viewModel.confirmUserCreation()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        _binding = null
        super.onDestroy()
    }
}