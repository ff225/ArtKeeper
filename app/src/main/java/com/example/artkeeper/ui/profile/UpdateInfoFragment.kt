package com.example.artkeeper.ui.profile

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.artkeeper.R
import com.example.artkeeper.databinding.FragmentRegistrationBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.utils.Constants.regex
import com.example.artkeeper.utils.Resource

class UpdateInfoFragment : Fragment(R.layout.fragment_registration) {
    private val TAG: String = javaClass.simpleName
    private lateinit var prevNickname: String
    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!


    private lateinit var name: String
    private lateinit var lastName: String
    private lateinit var nickName: String

    private val viewModel by navGraphViewModels<ProfileViewModel>(R.id.profile) {
        ProfileViewModelFactory(
            (requireActivity().application as ArtKeeper).userRepository,
            (requireActivity().application as ArtKeeper).postRepository,
            (requireActivity().application as ArtKeeper).workManager
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
        listener()
        viewModel.user.observe(viewLifecycleOwner) {
            binding.apply {
                textInputName.setText(it.firstName)
                textInputLastname.setText(it.lastName)
                textInputNickname.setText(it.nickName)
            }
            prevNickname = it.nickName
        }
    }

    private fun listener() {

        binding.apply {
            textInputNickname.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus)
                        binding.textInputNickname.hint = getString(R.string.hint_textview_nickname)
                    else
                        binding.textInputNickname.hint = ""
                }
            confirmButton.setOnClickListener {
                Log.d(TAG, "in confirmButton, previous name: $prevNickname")
                if (!checkUserInfo()) {
                    updateInfo(prevNickname)
                }
            }
        }
    }

    private fun updateInfo(prevNickname: String) {
        if (!checkUserInfo()) {
            createUser()
            viewModel.updateUserInfo(name, lastName, nickName, prevNickname)
                .observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Resource.Loading -> Log.d(TAG, "caricamento...")
                        is Resource.Success -> {
                            findNavController().navigate(R.id.action_updateInfoFragment_to_profileFragment)
                        }
                        is Resource.Failure -> {
                            binding.textInputNickname.error = result.exception.message.toString()
                        }
                    }
                }
        }
    }

    private fun createUser() {

        name = binding.textInputName.text.toString().trim()
        lastName = binding.textInputLastname.text.toString().trim()
        nickName = binding.textInputNickname.text.toString().lowercase().trim()
            .filterNot { it.isWhitespace() }
    }

    private fun checkUserInfo(): Boolean {
        var hasError = false
        val connMgr =
            (requireActivity()).getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        if (networkInfo?.isConnected == null) {
            hasError = true
            Toast.makeText(requireContext(), getString(R.string.no_connection), Toast.LENGTH_SHORT)
                .show()
        }
        if (binding.textInputName.text.toString().trim()
                .isEmpty() || !(binding.textInputName.text.toString().trim()
                .matches(regex))
        ) {
            binding.textInputName.error = getString(R.string.invalid_name)
            hasError = true
        }
        if (binding.textInputLastname.text.toString().trim()
                .isEmpty() || !(binding.textInputLastname.text.toString().trim()
                .matches(regex))
        ) {
            binding.textInputLastname.error = getString(R.string.invalid_last_name)
            hasError = true
        }
        if (binding.textInputNickname.text.toString().trim().isEmpty()) {
            binding.textInputNickname.error = getString(R.string.empty_field)
            hasError = true
        }
        return hasError
    }
}