package com.example.artkeeper.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.artkeeper.R
import com.example.artkeeper.databinding.FragmentLoginBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.utils.Resource
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    companion object {
        const val TAG = "LoginFragment"
    }

    private val viewModel by navGraphViewModels<ProfileViewModel>(R.id.profile) {
        ProfileViewModelFactory(
            (activity?.application as ArtKeeper).userRepository,
            (activity?.application as ArtKeeper).postRepository
        )
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding
        get() = _binding!!
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser != null)
            findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
        else
            activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)!!.isGone = true


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            createSignInIntent()
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {

        Log.d(TAG, "onSignInResult")
        val response = result.idpResponse

        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Successfully signed in
            Log.i(TAG, "Logged!")
            val user = FirebaseAuth.getInstance().currentUser
            Toast.makeText(requireContext(), "Welcome ${user!!.email}", Toast.LENGTH_LONG).show()
            userIsRegistered()
            Log.d(TAG, "nome: ${user.displayName}")
        } else {
            Toast.makeText(
                requireContext(),
                "Login necessario per utilizzare l'app.",
                Toast.LENGTH_LONG
            ).show()

            Log.d(TAG, "login error: ${response?.error?.errorCode}")
        }
    }

    private fun createSignInIntent() {
        Log.d(TAG, "createSignInIntent")

        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(providers)
            .build()

        signInLauncher.launch(signInIntent)
    }


    private fun userIsRegistered() {
        viewModel.checkUser().observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Resource.Loading -> Log.d(TAG, "caricamento")
                is Resource.Success -> {
                    Log.d(TAG, result.data)
                    findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                }
                is Resource.Failure -> {
                    Log.d(TAG, result.exception.message.toString())
                    findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
                }
            }
        })
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}