package com.example.artkeeper.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.artkeeper.R
import com.example.artkeeper.adapter.PostAdapter
import com.example.artkeeper.databinding.FragmentProfileBinding
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.viewmodel.PostViewModel
import com.example.artkeeper.viewmodel.PostViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private val viewModel: PostViewModel by activityViewModels { PostViewModelFactory((activity?.application as ArtKeeper).database.postDao()) }


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

        lifecycle.coroutineScope.launch(Dispatchers.IO) {
            viewModel.getNumOfPost("Francesco").collect {
                binding.tvNPost.text = getString(R.string.num_post, it.toString())
            }
        }

        lifecycle.coroutineScope.launch(Dispatchers.IO) {
            viewModel.getUserPosts("Francesco").collect { posts ->
                adapter.submitList(posts)
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