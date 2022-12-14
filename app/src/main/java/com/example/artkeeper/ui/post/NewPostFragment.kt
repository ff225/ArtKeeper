package com.example.artkeeper.ui.post

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.artkeeper.BuildConfig
import com.example.artkeeper.R
import com.example.artkeeper.databinding.FragmentNewPostBinding
import com.example.artkeeper.presentation.PostViewModel
import com.example.artkeeper.presentation.PostViewModelFactory
import com.example.artkeeper.utils.ArtKeeper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


class NewPostFragment : Fragment(R.layout.fragment_new_post) {

    companion object {
        const val TAG = "NewPost"
    }

    enum class Source {
        CAMERA, GALLERY
    }

    private var imageFromCamera: Uri? = null
    private var _binding: FragmentNewPostBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: PostViewModel by viewModels {
        PostViewModelFactory(
            (requireActivity().application as ArtKeeper).postRepository,
            (requireActivity().application as ArtKeeper).userRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.observe(viewLifecycleOwner) { user ->
            Log.d(TAG, user.nickName)

            Log.d(TAG, user.nameChild!!.isNotEmpty().toString())

            binding.radioGroup.removeAllViews()
            if (binding.radioGroup.isEmpty() && user.nameChild.isNotEmpty()) {
                binding.textviewAddChild.visibility = View.VISIBLE
                binding.radioGroup.visibility = View.VISIBLE
                for (item in user.nameChild) {
                    val radioButton = RadioButton(requireContext())
                    radioButton.apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        id = user.nameChild.indexOf(item)
                        text = item
                    }
                    binding.radioGroup.addView(radioButton)
                }
            } else {
                binding.textviewAddChild.visibility = View.GONE
                binding.radioGroup.visibility = View.GONE
            }

        }

        binding.apply {
            pickFromGallery.setOnClickListener { takePhoto(Source.GALLERY) }
            pickFromCamera.setOnClickListener { takePhoto(Source.CAMERA) }
            shareButton.setOnClickListener { shareAction() }
            cancelButton.setOnClickListener { cancelAction() }


        }

        viewModel.imageUri.observe(viewLifecycleOwner) { image ->
            binding.imageViewPost.setImageURI(image)
            binding.imageViewPost.visibility = if (image != null) View.VISIBLE else View.GONE
        }

        viewModel.description.observe(viewLifecycleOwner) { text ->
            binding.textInputDescription.setText(text)
        }
    }

    private val getPhotoFromGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri.let {
                if (it != null) {
                    Log.d(TAG, it.path.toString())
                    viewModel.setImageUri(saveImageToInternalStorage(it))
                }
            }
        }

    private val getPhotoFromCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                imageFromCamera.let {
                    Log.d(TAG, it?.path.toString())
                    viewModel.setImageUri(saveImageToInternalStorage(it!!))
                }
            }

        }

    private fun takePhoto(photoFrom: Source) {
        when (photoFrom) {
            Source.GALLERY -> {
                Log.d(TAG, "Pick photo from gallery")
                getPhotoFromGallery.launch("image/*")
            }
            else -> {
                Log.d(TAG, "Pick photo from camera")
                lifecycleScope.launchWhenStarted {
                    getTmpFileUri().let { uri ->
                        imageFromCamera = uri
                        getPhotoFromCamera.launch(uri)
                    }

                }
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile =
            File.createTempFile("tmp_image_file", ".jpg", requireContext().cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }

        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.file_provider",
            tmpFile
        )
    }

    private fun cancelAction() {

        if (viewModel.checkPost())
            MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.cancel)
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton(R.string.yes) { dialog, _ ->
                    findNavController().navigate(R.id.action_move_to_home)
                    binding.apply {
                        imageViewPost.visibility = View.GONE
                        textInputDescription.isFocusable = false
                        textInputDescription.isFocusableInTouchMode = true
                        radioGroup.clearCheck()
                    }
                    viewModel.reset()
                    dialog.dismiss()
                }.show()
        else
            findNavController().navigate(R.id.action_move_to_home)


    }

    private fun fromUriToBitmap(uri: Uri) =
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(
                requireContext().contentResolver,
                uri
            )
        )

    private fun saveImageToInternalStorage(imagePath: Uri): Uri {

        val file = File(requireContext().filesDir, "${UUID.randomUUID()}.jpg")
        val bitmap = fromUriToBitmap(imagePath)
        Log.d(TAG, file.path)
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    private fun shareAction() {

        Log.d(TAG, "condivido il post...")
        Log.d(
            TAG,
            binding.radioGroup.findViewById<RadioButton>(binding.radioGroup.checkedRadioButtonId)?.text.toString()
        )
        Log.d(
            TAG,
            (binding.radioGroup.checkedRadioButtonId).toString()
        )
        viewModel.apply {
            setDescription(binding.textInputDescription.text.toString().trim())
            setChildName(binding.radioGroup.findViewById<RadioButton>(binding.radioGroup.checkedRadioButtonId)?.text as String?)
            if (checkPost()) {
                insert()
                findNavController().navigate(R.id.action_move_to_home)
            } else
                Toast.makeText(
                    requireContext(),
                    "È necessario inserire una foto per pubblicare",
                    Toast.LENGTH_LONG
                ).show()

        }

    }


    override fun onDestroy() {
        _binding = null
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }
}