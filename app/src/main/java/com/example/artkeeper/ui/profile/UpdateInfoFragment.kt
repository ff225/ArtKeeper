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
        binding.textInputName.setText(viewModel.user.value?.firstName)
        binding.textInputLastname.setText(viewModel.user.value?.lastName)
        binding.textInputNickname.setText(viewModel.user.value?.nickName)

        binding.confirmButton.setOnClickListener {
            viewModel.setName(binding.textInputName.text.toString())
            viewModel.setLastName(binding.textInputLastname.text.toString())
            viewModel.setNickName(binding.textInputNickname.text.toString())

            if (viewModel.checkUserInfo()) {
                viewModel.updateInfoUser()
                findNavController().navigate(R.id.action_updateInfoFragment_to_profileFragment)
            }else
                Toast.makeText(
                    requireContext(),
                    "Devi riempire tutti i campi...",
                    Toast.LENGTH_LONG
                ).show()
        }
    }
}