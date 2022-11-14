package com.example.artkeeper.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.artkeeper.R
import com.example.artkeeper.adapter.PostAdapter
import com.example.artkeeper.databinding.FragmentProfileBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private val viewModel: ProfileViewModel by activityViewModels {
        ProfileViewModelFactory(
            (activity?.application as ArtKeeper).userRepository,
            (activity?.application as ArtKeeper).postRepository
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding.recyclerViewProfile
        val adapter = PostAdapter()
        recyclerView.adapter = adapter

        viewModel.user.observe(viewLifecycleOwner) {
            binding.tvName.text = it?.firstName
            binding.tvLastName.text = it?.lastName
            binding.tvUsername.text = it?.nickName
        }

        viewModel.getNumPostUser().observe(viewLifecycleOwner) {
            binding.tvNPost.text = getString(R.string.num_post, it.toString())
        }

        viewModel.getUserPost().observe(viewLifecycleOwner) { post ->
            post.let {
                adapter.submitList(post)
            }
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}