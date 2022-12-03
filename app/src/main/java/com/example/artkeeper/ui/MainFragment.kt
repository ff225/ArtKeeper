package com.example.artkeeper.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.artkeeper.R
import com.example.artkeeper.adapter.PostAdapter
import com.example.artkeeper.data.model.Post
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


    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            (requireActivity().application as ArtKeeper).postRepository
        )
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
        val postAdapter = PostAdapter(object : PostAdapter.OptionsMenuClickListener {
            override fun onOptionsMenuClicked(post: Post, position: Int) {
                performOptionsMenuClick(post, position)
            }

        })
        recyclerView.adapter = postAdapter

        viewModel.allPost.observe(viewLifecycleOwner) { post ->
            post.let {
                postAdapter.submitList(it)
            }
        }
    }

    private fun performOptionsMenuClick(post: Post, position: Int) {

        val popupMenu = PopupMenu(
            requireContext(),
            binding.recyclerViewHome[position].findViewById(R.id.textViewOptions)
        )

        popupMenu.inflate(R.menu.options_menu_home)

        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.share_post -> {
                        Log.d(TAG, post.id.toString())

                        return true
                    }
                }
                return false
            }
        })
        popupMenu.show()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        _binding = null
        super.onDestroy()
    }
}