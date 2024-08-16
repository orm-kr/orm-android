package com.orm.ui.trace

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orm.data.model.Trace
import com.orm.databinding.ActivityTraceDetailEditBinding
import com.orm.ui.fragment.map.BasicGoogleMapFragment
import com.orm.viewmodel.TraceViewModel
import com.orm.viewmodel.TrailViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class TraceDetailEditActivity : AppCompatActivity() {
    private val binding: ActivityTraceDetailEditBinding by lazy {
        ActivityTraceDetailEditBinding.inflate(layoutInflater)
    }

    private val traceViewModel: TraceViewModel by viewModels()
    private val trailViewModel: TrailViewModel by viewModels()

    private var imagePath: String? = null
    private var tempImagePath: String? = null
    private var toDefaultImage: Boolean = false;

    private val trace: Trace? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("trace", Trace::class.java)
        } else {
            intent.getParcelableExtra<Trace>("trace")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .commit()
        }

        binding.trace = trace

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        trace?.imgPath?.let {
            val file = File(it)
            if (file.exists()) {
                binding.image = Uri.fromFile(file).toString()
                imagePath = it
            }

        }
        Log.d("traceTest", trace?.imgPath.toString())

        binding.btnSign.setOnClickListener {
            if (binding.tfTraceName.editText!!.text.isEmpty()) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("경고")
                    .setMessage("발자국 이름을 입력하세요.")
                    .setPositiveButton("확인") { _, _ -> }
                    .show()
                return@setOnClickListener
            }

            MaterialAlertDialogBuilder(this)
                .setTitle("수정하기")
                .setMessage("발자국을 수정하시겠습니까?")
                .setNegativeButton("취소") { _, _ -> }
                .setPositiveButton("확인") { dialog, _ ->

                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )

                    tempImagePath?.let { tempPath ->
                        val tempFile = File(tempPath)
                        Log.d("traceTest", "File ${trace?.localId}")

                        val newImageFileName = "trace_image_${trace?.localId}_${System.currentTimeMillis()}.png"
                        val permFile = File(filesDir, newImageFileName)

                        try {
                            tempFile.inputStream().use { input ->
                                permFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            tempFile.delete()
                            imagePath = permFile.absolutePath
                            Log.d("traceTest", "파일이 성공적으로 덮어쓰기 되었습니다: $imagePath")
                        } catch (e: IOException) {
                            Log.e("traceTest", "파일 복사 중 오류 발생", e)
                        }

                        trace?.imgPath?.let { oldImagePath ->
                            val lastFile = File(oldImagePath)
                            if (lastFile.exists()) {
                                lastFile.delete()
                                Log.d("traceTest", "이전 이미지 파일 삭제됨: $oldImagePath")
                            }
                        }
                    }

                    val traceModify = Trace(
                        id = trace?.id,
                        localId = trace?.localId ?: 0,
                        title = binding.tfTraceName.editText?.text.toString(),
                        hikingDate = trace!!.hikingDate,
                        hikingStartedAt = trace!!.hikingStartedAt,
                        hikingEndedAt = trace!!.hikingEndedAt,
                        mountainId = trace!!.mountainId,
                        mountainName = trace!!.mountainName,
                        coordinates = trace!!.coordinates,
                        trailId = trace!!.trailId,
                        maxHeight = trace!!.maxHeight,
                        imgPath = imagePath,
                        recordId = trace!!.recordId,
                        hikingDistance = trace!!.hikingDistance
                    )
                    traceViewModel.createTrace(traceModify)
                    Log.d("traceTest", imagePath.toString())
                    dialog.dismiss()

                    binding.progressBar.visibility = View.VISIBLE
                    // TODO 기본이미지 변경시 바로 반영 안됨
                    traceViewModel.traceCreated.observe(this) { traceCreated ->
                        if (traceCreated) {
                            binding.progressBar.visibility = View.GONE
                            setResult(1, Intent().apply {
                                putExtra("traceModified", true)
                            })
                            finish()
                        }
                    }
                }
                .show()
        }

        var photoSelection: Int = 0
        binding.cvImageUpload.setOnClickListener {
            if (binding.image == null) {
                openGallery()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle("사진 선택")
                    .setSingleChoiceItems(
                        arrayOf("갤러리에서 가져오기", "기본 이미지로 변경"), 0
                    ) { _, which ->
                        photoSelection = which
                    }
                    .setNegativeButton("취소") { _, _ -> }
                    .setPositiveButton("확인") { dialog, _ ->
                        if (photoSelection == 0) {
                            openGallery()
                        } else {
                            tempImagePath = null
                            binding.image = null
                            imagePath = null
                            toDefaultImage = true
                        }
                    }.show()
            }
        }
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImageLauncher.launch(gallery)
    }

    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    val tempFile = File(cacheDir, "selected_image.png")
                    try {
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            FileOutputStream(tempFile).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        tempImagePath = tempFile.absolutePath
                        Log.d("traceTest", "tempPath ${tempImagePath.toString()}")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    binding.image = uri.toString()
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        // 임시 파일 삭제
        tempImagePath?.let {
            val tempFile = File(it)
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }
}
