package com.example.artkeeper.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.artkeeper.R
import com.example.artkeeper.adapter.PostAdapter
import com.example.artkeeper.databinding.FragmentMainBinding
import com.example.artkeeper.presentation.MainViewModel
import com.example.artkeeper.presentation.MainViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainFragment : Fragment() {
    companion object {
        const val TAG = "MainFragment"
    }

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!
    private lateinit var recyclerView: RecyclerView
<<<<<<< HEAD
    private val viewModel: PostViewModel by activityViewModels {
        PostViewModelFactory((activity?.application as ArtKeeper).postRepository)
=======
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            (requireActivity().application as ArtKeeper).postRepository
        )
>>>>>>> post
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        Log.d(TAG, "${FirebaseAuth.getInstance().currentUser}")
        if (FirebaseAuth.getInstance().currentUser == null) {
            findNavController().navigate(R.id.login)
        }
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)!!.isGone = false
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding.recyclerViewHome
        val postAdapter = PostAdapter()
        recyclerView.adapter = postAdapter

        viewModel.allPost.observe(viewLifecycleOwner) { post ->
            post.let {
                postAdapter.submitList(it)
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        _binding = null
        super.onDestroy()
    }
}