package com.example.artkeeper.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.artkeeper.R
import com.example.artkeeper.databinding.FragmentRegistrationBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper

class UpdateInfoFragment : Fragment(R.layout.fragment_registration) {
    companion object {
        const val TAG = "UpdateInfoFragment"
        val regex = Regex("^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*\$")
    }

    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!


    private val viewModel: ProfileViewModel by activityViewModels {
        ProfileViewModelFactory(
            (activity?.application as ArtKeeper).userRepository,
            (activity?.application as ArtKeeper).postRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.textInputNickname.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus)
                    binding.textInputNickname.hint = "Eventuali spazi bianchi verranno rimossi"
                else
                    binding.textInputNickname.hint = ""
            }

        viewModel.user.observe(viewLifecycleOwner) {
            binding.apply {
                textInputName.setText(it.firstName)
                textInputLastname.setText(it.lastName)
                textInputNickname.setText(it.nickName)
            }

        }
        binding.confirmButton.setOnClickListener {
            if (!checkUserInfo()) {
                viewModel.apply {
                    setName(binding.textInputName.text.toString().trim())
                    setLastName(binding.textInputLastname.text.toString().trim())
                    setNickName(
                        binding.textInputNickname.text.toString().lowercase().trim()
                            .filterNot { it.isWhitespace() })

                }
                viewModel.updateInfoUser()
                findNavController().navigate(R.id.action_updateInfoFragment_to_profileFragment)
            } else
                Toast.makeText(
                    requireContext(),
                    "Devi riempire tutti i campi...",
                    Toast.LENGTH_LONG
                ).show()
        }
    }

    private fun checkUserInfo(): Boolean {

        var hasError = false
        if (binding.textInputName.text.toString().trim()
                .isEmpty() || !(binding.textInputName.text.toString()
                .matches(regex))
        ) {
            binding.textInputName.error = "Nome non valido"
            hasError = true
        }
        if (binding.textInputLastname.text.toString().trim()
                .isEmpty() || !(binding.textInputLastname.text.toString()
                .matches(regex))
        ) {
            binding.textInputLastname.error = "Cognome non valido"
            hasError = true
        }
        if (binding.textInputNickname.text.toString().trim().isEmpty()) {
            binding.textInputNickname.error = "Il campo è vuoto"
            hasError = true
        }
        return hasError
    }
}