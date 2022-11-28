package com.example.artkeeper.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.artkeeper.R
import com.example.artkeeper.databinding.FragmentRegistrationBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class RegistrationFragment : Fragment() {
    companion object {
        const val TAG = "RegistrationFragment"
    }

    private val uid = FirebaseAuth.getInstance().currentUser!!.uid
    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!


    private val viewModel by activityViewModels<ProfileViewModel> {
        ProfileViewModelFactory(
            (requireActivity().application as ArtKeeper).userRepository,
            (requireActivity().application as ArtKeeper).postRepository
        )
    }

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
                runBlocking {
                    val job = launch {
                        viewModel.insertUser(uid)
                    }
                    job.join()
                }
                /*
                runBlocking {
                    val job1 = launch {
                        viewModel.getUserRepo(uid)
                    }
                    job1.join()
                    //viewModel.getUserPost()

                    //viewModel.getNumPostUser()
                }
*/
                val user = viewModel.user.value
                println("Dentro RegistrationFragment ${user?.uid}")
                /*
                runBlocking {
                    val job = launch {
                        viewModel.getUserRepo(uid)
                        viewModel.getUserPost()
                        viewModel.getNumPostUser()
                    }
                    job.join()

                }
                 */
                //viewModel.init()

                findNavController().navigate(R.id.action_registrationFragment_to_mainFragment)
                Log.d(TAG, "user registered")
            } else
                Log.d(TAG, "registration failed")
        }
    }

    private fun createUser(): Boolean {
        //Log.d(TAG, "$name, $lastName, $nickName")
        viewModel.apply {
            setName(binding.textInputName.text.toString())
            setLastName(binding.textInputLastname.text.toString())
            setNickName(binding.textInputNickname.text.toString())

        }
        return viewModel.checkUserInfo()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        _binding = null
        super.onDestroy()
    }
}