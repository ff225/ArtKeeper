package com.example.artkeeper.ui.login

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.artkeeper.R
import com.example.artkeeper.databinding.FragmentRegistrationBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.utils.Constants.regex
import com.example.artkeeper.utils.NotificationFollowingRequest
import com.example.artkeeper.utils.Resource
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth


class RegistrationFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!
    private var isRegistered = false

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cancelRegistration()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listener()
    }

    private fun cancelRegistration() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            MaterialAlertDialogBuilder(requireContext()).setTitle(getText(R.string.cancel_registration))
                .setPositiveButton(R.string.yes) { _, _ ->
                    deleteRegistration()
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.cancel()
                }.show()
        }
    }

    private fun userRegistration() {

        if (!checkUserInfo()) {
            createUser()
            isRegistered = true
            viewModel.userRegistration(name, lastName, nickName)
                .observe(viewLifecycleOwner, { result ->
                    when (result) {
                        is Resource.Loading -> Log.d(TAG, "caricamento")
                        is Resource.Success -> {
                            val serviceIntent =
                                Intent(requireActivity(), NotificationFollowingRequest::class.java)
                            requireActivity().startService(serviceIntent)
                            findNavController().navigate(R.id.action_registrationFragment_to_home)
                            Log.d(TAG, "in userRegistration, user registered: ${result.data}")
                        }
                        is Resource.Failure -> {
                            binding.textInputNickname.error = result.exception.message
                            Log.e(
                                TAG,
                                "in userRegistration, ${result.exception.message.toString()}"
                            )
                        }
                    }
                })
        } else
            isRegistered = false
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
                .isEmpty() || !(binding.textInputName.text.toString()
                .matches(regex))
        ) {
            binding.textInputName.error = getText(R.string.value_not_valid)
            hasError = true
        }
        if (binding.textInputLastname.text.toString().trim()
                .isEmpty() || !(binding.textInputLastname.text.toString().matches(regex))
        ) {
            binding.textInputLastname.error = getText(R.string.value_not_valid)
            hasError = true
        }
        if (binding.textInputNickname.text.toString().trim().isEmpty()) {
            binding.textInputNickname.error = getString(R.string.value_not_valid)
            hasError = true
        }
        return hasError
    }

    private fun createUser() {

        name = binding.textInputName.text.toString().trim().filterNot { it.isWhitespace() }
        lastName = binding.textInputLastname.text.toString().trim().filterNot { it.isWhitespace() }
        nickName = binding.textInputNickname.text.toString().lowercase().trim()
            .filterNot { it.isWhitespace() }
    }

    private fun deleteRegistration() {
        viewModel.deleteRegistration().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.apply {
                        textInputName.isEnabled = false
                        textInputLastname.isEnabled = false
                        textInputNickname.isEnabled = false
                        confirmButton.isEnabled = false
                    }
                    Toast.makeText(
                        requireContext(),
                        getText(R.string.loading),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Success -> {
                    Toast.makeText(requireContext(), result.data, Toast.LENGTH_LONG)
                        .show()
                    AuthUI.getInstance().signOut(requireContext())
                    findNavController().navigate(R.id.profile)
                }
                is Resource.Failure -> {
                    Toast.makeText(
                        requireContext(),
                        getText(R.string.no_connection),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
    }

    private fun listener() {
        binding.textInputNickname.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus)
                    binding.textInputNickname.hint = getText(R.string.hint_textview_nickname)
                else
                    binding.textInputNickname.hint = ""
            }

        binding.confirmButton.setOnClickListener {
            userRegistration()
        }
    }

    override fun onResume() {
        if (FirebaseAuth.getInstance().currentUser == null)
            findNavController().navigate(R.id.profile)
        super.onResume()
    }

    override fun onPause() {
        if (!isRegistered)
            FirebaseAuth.getInstance().currentUser?.delete()
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        _binding = null
        super.onDestroy()
    }
}