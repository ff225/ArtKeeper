package com.example.artkeeper.ui.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.artkeeper.R
import com.example.artkeeper.adapter.PostAdapter
import com.example.artkeeper.databinding.FragmentProfileBinding
import com.example.artkeeper.presentation.ProfileViewModel
import com.example.artkeeper.presentation.ProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.utils.ShareAction
import com.google.android.material.bottomnavigation.BottomNavigationView


class ProfileFragment : Fragment() {

    private val TAG: String = javaClass.simpleName
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!


    private lateinit var recyclerView: RecyclerView
    private val viewModel by navGraphViewModels<ProfileViewModel>(R.id.profile) {
        ProfileViewModelFactory(
            (requireActivity().application as ArtKeeper).userRepository,
            (requireActivity().application as ArtKeeper).postRepository,
            (requireActivity().application as ArtKeeper).workManager
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)!!.isGone = false
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding.recyclerViewProfile
        val adapter = PostAdapter(PostAdapter.PostListener { post, position, option ->
            Log.d(TAG, "position: $position")
            if (option == "remove") {
                viewModel.deletePost(post)
                recyclerView.adapter!!.notifyItemRemoved(position)
            }
            if (option == "share") {
                val path = ShareAction().getTmpFileUri(requireContext(), post.imagePath)
                val shareIntent =
                    ShareCompat.IntentBuilder(requireActivity()).apply {
                        setType("image/jpg")
                        setText(post.description.toString())
                        post.sketchedBy?.let {
                            setText("Disegnato da: $it \n${post.description.toString()}")
                        }
                        addStream(path)

                    }.createChooserIntent()
                startActivity(shareIntent)
            }
        })
        recyclerView.adapter = adapter
        adapter.menu = R.menu.options_menu_profile

        observer(adapter)
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToSettingsFragment())
        }
    }

    private fun observer(adapter:PostAdapter) {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            val imageUri: Uri = user.photo.toUri()
            Glide.with(binding.imageProfile.context)
                .load(imageUri)
                .into(binding.imageProfile)
            binding.apply {
                tvName.text = user.firstName
                tvLastName.text = user.lastName
                tvUsername.text = user.nickName

                viewModel.pendingRequestFrom.observe(viewLifecycleOwner) { pendingReq ->
                    if (pendingReq.isNotEmpty())
                        buttonNotification.setOnClickListener {
                            Log.d(TAG, pendingReq.toString())
                            findNavController().navigate(
                                ProfileFragmentDirections.actionProfileFragmentToPendingRequestFragment(
                                    null,
                                    pendingReq.toTypedArray()
                                )
                            )
                        }
                }

                viewModel.followers.observe(viewLifecycleOwner) { followers ->
                    if (followers.isNotEmpty()) {
                        buttonFollower.setOnClickListener {
                            findNavController().navigate(
                                ProfileFragmentDirections.actionProfileFragmentToPendingRequestFragment(
                                    "Follower",
                                    followers.toTypedArray()
                                )
                            )
                        }
                    }
                }
            }
            adapter.nickName = user.nickName

        }

        viewModel.postUser.observe(viewLifecycleOwner)
        { post ->
            Log.d(TAG, post.size.toString())
            binding.tvNPost.text = getString(R.string.num_post, post.size.toString())
            post.let {
                adapter.submitList(post)
            }
        }
    }


    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}