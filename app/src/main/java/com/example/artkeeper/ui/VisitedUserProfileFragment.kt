package com.example.artkeeper.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.artkeeper.R
import com.example.artkeeper.adapter.PostAdapter
import com.example.artkeeper.databinding.FragmentProfileBinding
import com.example.artkeeper.presentation.VisitedUserProfileViewModel
import com.example.artkeeper.presentation.VisitedUserProfileViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.utils.Resource
import com.example.artkeeper.utils.ShareAction

class VisitedUserProfileFragment : Fragment(R.layout.fragment_profile) {

    private val TAG = javaClass.simpleName
    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding
        get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private val viewModel: VisitedUserProfileViewModel by viewModels {
        VisitedUserProfileViewModelFactory(
            (requireActivity().application as ArtKeeper).userRepository,
            (requireActivity().application as ArtKeeper).postRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("VisitedUserProfileFragment", arguments?.getString("uid_user").toString())

        recyclerView = binding.recyclerViewProfile

        val adapter = PostAdapter(PostAdapter.PostListener { post, position, option ->
            Log.d(TAG, "postId: ${post.id}")
            Log.d(TAG, "position: $position")

            if (option.equals("share")) {
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
        adapter.menu = R.menu.options_menu_home

        viewModel.getInfoUser(arguments?.getString("uid_user").toString())
            .observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Loading -> {
                        binding.tvNPost.visibility = View.INVISIBLE
                        binding.btnSettings.visibility = View.INVISIBLE
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        adapter.nickName = result.data.nickName.toString()
                        binding.apply {
                            tvName.text = result.data.firstName
                            tvLastName.text = result.data.lastName
                            tvUsername.text = result.data.nickName

                            Log.d(TAG, result.data.photoUser!!.toUri().toString())
                            Glide.with(imageProfile.context)
                                .load(result.data.photoUser!!.toUri())
                                .format(DecodeFormat.PREFER_RGB_565)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .error(R.drawable.ic_baseline_settings_24)
                                .into(imageProfile)
                            btnSettings.text = "Segui"
                            btnSettings.visibility = View.INVISIBLE

                        }
                    }
                    is Resource.Failure -> {
                        binding.tvNPost.visibility = View.INVISIBLE
                        binding.btnSettings.visibility = View.INVISIBLE
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                }
            }
        viewModel.getPostUser(arguments?.getString("uid_user").toString())
            .observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        binding.progressBar2.visibility = View.GONE
                        binding.tvNPost.visibility = View.VISIBLE
                        binding.tvNPost.text =
                            getString(R.string.num_post, result.data.size.toString())
                        adapter.submitList(result.data)
                    }
                    is Resource.Failure -> {

                    }
                }
            }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}