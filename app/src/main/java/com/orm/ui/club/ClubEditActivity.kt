package com.orm.ui.club

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.orm.R
import com.orm.data.model.Mountain
import com.orm.data.model.club.Club
import com.orm.data.model.club.ClubCreate
import com.orm.databinding.ActivityClubEditBinding
import com.orm.ui.fragment.BottomSheetMountainList
import com.orm.util.resizeImage
import com.orm.viewmodel.ClubViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ClubEditActivity : AppCompatActivity(), BottomSheetMountainList.OnMountainSelectedListener {
    private val binding: ActivityClubEditBinding by lazy {
        ActivityClubEditBinding.inflate(layoutInflater)
    }

    private val club: Club? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("club", Club::class.java)
        } else {
            intent.getParcelableExtra<Club>("club")
        }
    }

    private var mountainId: Int = 0
    private val clubViewModel: ClubViewModel by viewModels()

    private var imageFile: File? = null
    private var isDuplicated: Boolean = true
    private var toDefaultImage: Boolean = false

    private lateinit var textInputLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.club = club
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        textInputLayout = binding.tfClubName
        if(club != null) {
            textInputLayout.endIconDrawable?.setColorFilter(ContextCompat.getColor(this, R.color.md_theme_primary), PorterDuff.Mode.SRC_IN)
        } else {
            textInputLayout.endIconDrawable?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
        }

        binding.tfClubMountain.setEndIconOnClickListener {
            Log.d("ClubEditActivity", binding.tfClubMountain.editText?.text.toString())

            val mountainName = binding.tfClubMountain.editText?.text.toString()
            val bottomSheetFragment =
                BottomSheetMountainList.newInstance(mountainName)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }


        mountainId = club?.mountainId?.toInt() ?: 0
        isDuplicated = if (club == null) true else false
        binding.image = club?.imgSrc.toString()

        binding.tfClubName.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(binding.tfClubName.editText?.text.toString() != club?.clubName) {
                    checkDuplicate()
                } else {
                    textInputLayout.endIconDrawable?.setColorFilter(ContextCompat.getColor(this@ClubEditActivity, R.color.md_theme_primary), PorterDuff.Mode.SRC_IN)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        val content = if (club == null) "생성" else "수정"
        binding.btnSign.setOnClickListener {
            if (binding.tfClubName.editText!!.text.isEmpty()) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(content)
                    .setMessage("모임 이름을 입력해주세요.")
                    .setPositiveButton("확인") { _, _ ->
                    }
                    .show()
                return@setOnClickListener
            }
            if (mountainId == 0) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(content)
                    .setMessage("대표 산을 지정해주세요.")
                    .setPositiveButton("확인") { _, _ ->
                    }
                    .show()
                return@setOnClickListener
            }
            if (isDuplicated && binding.tfClubName.editText?.text.toString() != club?.clubName) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(content)
                    .setMessage("모임명 중복확인을 진행해주세요")
                    .setPositiveButton("확인") { _, _ ->
                    }
                    .show()
                return@setOnClickListener
            }
            MaterialAlertDialogBuilder(this)
                .setTitle(content)
                .setMessage("모임을 $content 하시겠습니까?")
                .setNegativeButton("취소") { _, _ -> }
                .setPositiveButton("확인") { dialog, _ ->

                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )

                    val clubCreate = ClubCreate(
                        clubName = binding.tfClubName.editText?.text.toString(),
                        description = binding.tfClubDesc.editText?.text.toString(),
                        mountainId = mountainId
                    )


                    if (club == null) {
                        Log.d("ClubEditActivity create", "club: $club")
                        clubViewModel.createClubs(clubCreate, imageFile)
                    } else {
                        Log.d("ClubEditActivity edit", "club: $club")
                        Log.d(
                            "clubTest",
                            "imageFile ${imageFile.toString()} Default ${toDefaultImage}"
                        )
                        if (imageFile != null) clubViewModel.updateClubs(
                            club!!.id,
                            clubCreate,
                            imageFile
                        )
                        // TODO create empty file bug
                        else if (toDefaultImage) clubViewModel.updateClubs(
                            club!!.id,
                            clubCreate,
                            imageFile
                        )
                        else clubViewModel.updateClubs(club!!.id, clubCreate)
                    }
                    dialog.dismiss()

                    binding.progressBar.visibility = View.VISIBLE
                    clubViewModel.isCreated.observe(this) { clubCreated ->
                        if (clubCreated != null && clubCreated) {
                            binding.progressBar.visibility = View.GONE
                            setResult(Activity.RESULT_OK, Intent().apply {
                                putExtra("clubCreated", true)
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
                            imageFile = null
                            binding.image = null
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
                data?.data?.let {
                    binding.image = it.toString()
                    resizeImage(this, it) { resizedFile ->
                        imageFile = resizedFile
                    }
                }
            }
        }

    private fun showDuplicateCheckDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("중복 체크")
            .setMessage(message)
            .setPositiveButton("확인") { dialog, which -> }
            .show()
    }

    override fun onMountainSelected(mountain: Mountain) {
        binding.tfClubMountain.editText?.setText(mountain.name)
        mountainId = mountain.id
    }

    private fun checkDuplicate(){
        clubViewModel.isOperationSuccessful.removeObservers(this)
        clubViewModel.resetOperationStatus()

        clubViewModel.checkDuplicateClubs(binding.tfClubName.editText?.text.toString())
        clubViewModel.isOperationSuccessful.observe(this) { isDuplicate ->
            isDuplicate?.let {
                Log.d("ClubEditActivity", "isDuplicate: $isDuplicate")
                val message = if (isDuplicate) "생성 불가능한 이름입니다." else "생성 가능한 이름입니다."
                isDuplicated = isDuplicate
                if(isDuplicate) {
                    textInputLayout.endIconDrawable?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                } else {
                    textInputLayout.endIconDrawable?.setColorFilter(ContextCompat.getColor(this, R.color.md_theme_primary), PorterDuff.Mode.SRC_IN)
                }
//                showDuplicateCheckDialog(message)
            }
        }
    }
}
