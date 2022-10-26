package com.example.artkeeper.ui.post

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.artkeeper.BuildConfig
import com.example.artkeeper.R
import com.example.artkeeper.databinding.FragmentNewPostBinding
import com.example.artkeeper.utils.ArtKeeper
import com.example.artkeeper.viewmodel.PostViewModel
import com.example.artkeeper.viewmodel.PostViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


private const val nameFragment = "NewPost"

class NewPost : Fragment() {

    enum class Source {
        CAMERA, GALLERY
    }

    private var imageFromCamera: Uri? = null
    private var _binding: FragmentNewPostBinding? = null
    private val binding
        get() = _binding!!
    private val viewModel: PostViewModel by activityViewModels {
        PostViewModelFactory((activity?.application as ArtKeeper).database.postDao())
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

        binding.pickFromGallery.setOnClickListener { takePhoto(Source.GALLERY) }
        binding.pickFromCamera.setOnClickListener { takePhoto(Source.CAMERA) }
        binding.shareButton.setOnClickListener { shareAction() }
        binding.cancelButton.setOnClickListener { cancelAction() }

        viewModel.description.observe(viewLifecycleOwner) { text ->
            binding.textInputDescription.setText(text)
        }

        viewModel.image.observe(viewLifecycleOwner) { image ->
            Log.d("$nameFragment - imageIsNull", (image == null).toString())
            binding.imageViewPost.setImageURI(image)
            binding.shareButton.isEnabled = image != null
            binding.imageViewPost.visibility = if (image != null) View.VISIBLE else View.GONE

        }
    }

    private val getPhotoFromGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri.let {
                if (it != null)
                    viewModel.setImage(saveImageToInternalStorage(it))
            }
        }

    private val getPhotoFromCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                imageFromCamera.let {
                    Log.d("$nameFragment - photoPathFromCamera", it?.path.toString())
                    viewModel.setImage(saveImageToInternalStorage(it!!))
                }
            }

        }

    private fun takePhoto(photoFrom: Source) {
        when (photoFrom) {
            Source.GALLERY -> {
                Log.d("$nameFragment - Source Gallery", "Pick photo from gallery")
                getPhotoFromGallery.launch("image/*")
            }
            else -> {
                Log.d("$nameFragment - Source Camera", "Pick photo from camera")
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
        val tmpFile = File.createTempFile("tmp_image_file", ".jpg").apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    private fun cancelAction() {

        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.cancel)
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(R.string.yes) { dialog, _ ->
                binding.imageViewPost.visibility = View.GONE
                binding.textInputDescription.isFocusable = false
                binding.textInputDescription.isFocusableInTouchMode = true
                viewModel.reset()
                dialog.dismiss()
            }.show()
    }

    private fun fromUriToBitmap(uri: Uri) =
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(
                requireContext().contentResolver,
                uri
            )
        )

    private fun saveImageToInternalStorage(imagePath: Uri): Uri {
        var file = requireContext().getDir(getString(R.string.folder_image), Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        val bitmap = fromUriToBitmap(imagePath)
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
        //if (imageUri == null)
        //  addImageToPost()
        //else {
        //viewModel.setImage(saveImageToInternalStorage(imageUri!!))
        //viewModel.setDescription(binding.textInputDescription.text.toString())
        Log.d("$nameFragment - shareAction()", "condivido il post...")
        viewModel.setDescription(binding.textInputDescription.text.toString())
        viewModel.sharePost()
        findNavController().navigate(R.id.action_move_to_home)
        //}
    }

    /*
    private fun addImageToPost() {
        MaterialAlertDialogBuilder(requireContext())
            .apply {
                setTitle("Devi aggiungere una foto per poter pubblicare")
                setItems(R.array.choice, DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        0 -> {
                            takePhoto(Source.CAMERA)
                            dialog.dismiss()
                        }
                        1 -> {
                            takePhoto(Source.GALLERY)
                            dialog.dismiss()
                        }
                    }
                })

            }.show()
    }
     */

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}