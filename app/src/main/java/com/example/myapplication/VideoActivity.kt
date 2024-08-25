package com.example.myapplication

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.myapplication.databinding.ActivityVideoBinding
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import com.google.firebase.storage.OnProgressListener


class VideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoBinding
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityVideoBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        progressDialog = ProgressDialog(this)


        binding.videoView.isVisible = false
        binding.select.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.type = "video/*"
            videoLauncher.launch(intent)

        }


    }

    val videoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                if (it.data != null) {
                    /// progress bar
                    progressDialog.setTitle("Uploading Video")
                    progressDialog.show()

                    val ref = Firebase.storage.reference.child(
                        "Video/" + System.currentTimeMillis() + "." + getFileType(it.data!!.data!!)
                    )
                    ref.putFile(it.data!!.data!!)
                        .addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener {
                                Firebase.database.reference.child("Video").push()
                                    .setValue(it.toString())
                                progressDialog.dismiss()  // dismiss the progess bar
//                            binding.imageView.setImageURI(it)    // if i want to show image in imageview we can use this code
                                Toast.makeText(this, "Video Uploaded", Toast.LENGTH_SHORT).show()
                                binding.select.isVisible = false
                                binding.videoView.isVisible = true
                                val mc = MediaController(this)
//                               binding.videoView.setMediaController(mc)
                                mc.setAnchorView(binding.videoView)

                                binding.videoView.setVideoURI(it)
                                binding.videoView.setMediaController(mc)
                                binding.videoView.start()
                                // if video finish video delete
                                binding.videoView.setOnCompletionListener {
                                    ref.delete().addOnSuccessListener {
                                        Toast.makeText(this, "Video Deleted", Toast.LENGTH_SHORT)
                                            .show()
                                        binding.select.isVisible = true
                                        binding.videoView.isVisible = false
                                    }

                                }

                            }

                        }

                        /// see how much video is uploaded
                        .addOnProgressListener{
                                val value = (it.bytesTransferred / it.totalByteCount) * 100
                                progressDialog.setMessage("Uploaded"+ value.toString() + "%")

                            }



                }


            }
        }

    private fun getFileType(data: Uri): String? {
        val mimeType = MimeTypeMap.getSingleton()
        return mimeType.getExtensionFromMimeType(contentResolver.getType(data))


    }
}