package com.orm.ui.club

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orm.R
import com.orm.data.model.RequestMember
import com.orm.data.model.club.Club
import com.orm.data.model.club.ClubCreate
import com.orm.databinding.ActivityClubDetailBinding
import com.orm.ui.MainActivity
import com.orm.ui.PhotoViewerActivity
import com.orm.ui.mountain.MountainDetailActivity
import com.orm.ui.board.BoardActivity
import com.orm.ui.board.BoardEditActivity
import com.orm.viewmodel.ClubViewModel
import com.orm.viewmodel.MountainViewModel
import com.orm.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@OptIn(ExperimentalBadgeUtils::class)
class ClubDetailActivity : AppCompatActivity() {
    private val binding: ActivityClubDetailBinding by lazy {
        ActivityClubDetailBinding.inflate(layoutInflater)
    }

    private val userViewModel: UserViewModel by viewModels()
    private val clubViewModel: ClubViewModel by viewModels()
    private val mountainViewModel: MountainViewModel by viewModels()

    private var club: Club? = null

    private var badgeDrawable: BadgeDrawable? = null

    private val goToMain: Boolean by lazy {
        intent.getBooleanExtra("back", false)
    }

    private val createClubLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val clubCreated = data?.getBooleanExtra("clubCreated", false) ?: false
                if (clubCreated && club != null) {
                    clubViewModel.getClubById(club!!.id)
                    clubViewModel.club.observe(this) {
                        club = it
                        binding.club = it
                    }

                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra("clubChanged", true)
                    })
                }
            }

            if (result.resultCode == 1) {
                val data: Intent? = result.data
                val clubMember = data?.getBooleanExtra("clubMember", false) ?: false
                if (clubMember) {
                    clubViewModel.getClubById(club!!.id)
                    clubViewModel.club.observe(this) {
                        club = it
                        binding.club = it
                    }
                }
            }

            if (result.resultCode == 2) {
                finish()
            }
        }

    @OptIn(ExperimentalBadgeUtils::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        club = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("club", Club::class.java)
        } else {
            intent.getParcelableExtra<Club>("club")
        }

        binding.club = club
        Log.d("clubTest", "club ${binding.club}")
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.cvThumbnail.setOnClickListener {
            val intent = Intent(this, PhotoViewerActivity::class.java)
            intent.putExtra("IMAGE_URL", club?.imgSrc)
            startActivity(intent)
        }

        binding.tvDescription.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("설명")
                .setMessage(club?.description)
                .setPositiveButton("확인") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.btnMember.setOnClickListener {
            if (club?.isMember == true) {
                createClubLauncher.launch(Intent(this, ClubMemberActivity::class.java).apply {
                    putExtra("club", club)
                })
            }
        }
        badgeDrawable = BadgeDrawable.create(this)
        binding.btnMember.post {
            if (club!!.applicantCount > 0) {
                badgeDrawable?.number = club!!.applicantCount
                badgeDrawable?.verticalOffset = 50
                badgeDrawable?.horizontalOffset = 44
                badgeDrawable?.backgroundColor = getColor(R.color.md_theme_error)
                badgeDrawable?.isVisible = true
            } else {
                badgeDrawable?.isVisible = false
            }
            BadgeUtils.attachBadgeDrawable(badgeDrawable!!, binding.btnMember)
            binding.btnMember.invalidate()
        }

        userViewModel.user.observe(this) {
            if (it != null && it.userId == club?.managerId) {
                binding.btnEdit.visibility = View.VISIBLE
            } else {
                binding.btnEdit.visibility = View.INVISIBLE
            }
        }

        binding.btnEdit.setOnClickListener {
            createClubLauncher.launch(
                Intent(this, ClubEditActivity::class.java).apply {
                    putExtra("club", club)
                }
            )
        }

        binding.tfClubMountain.setOnClickListener {
            moveToMountainDetail()
        }

        binding.tfClubMountainField.setOnClickListener {
            moveToMountainDetail()
        }

        binding.btnSign.setOnClickListener {
            if (club?.isMember == true) {
                // TODO : 채팅 서비스
                val intent = Intent(this, BoardActivity::class.java)
                intent.putExtra("club", club)
                startActivity(intent)
            } else if (club?.isApplied == false) {
                val input = EditText(this).apply {
                    hint = "자기소개를 입력해주세요."
                }

                MaterialAlertDialogBuilder(this)
                    .setView(input)
                    .setTitle("가입")
                    .setMessage("${club?.clubName} 모임에 가입하시겠습니까?")
                    .setNegativeButton("취소") { _, _ -> }
                    .setPositiveButton("확인") { dialog, which ->
                        clubViewModel.applyClubs(
                            RequestMember(
                                clubId = club!!.id,
                                introduction = input.text.toString(),
                                userId = userViewModel.user.value!!.userId.toInt()
                            )
                        )
                        Log.d("clubTest", "가입")
                        binding.progressBar.visibility = View.VISIBLE
                        dialog.dismiss()
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        )
                        clubViewModel.isLoading.observe(this) {
                            if (!it) {
                                binding.progressBar.visibility = View.GONE
                                finish()
                            }
                        }

                    }
                    .show()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle("가입 신청 취소")
                    .setMessage("${club?.clubName} 모임 가입을 취소하시겠습니까?")
                    .setNegativeButton("취소") { _, _ -> }
                    .setPositiveButton("확인") { dialog, which ->
                        clubViewModel.cancelApply(clubId = club!!.id)
                        binding.progressBar.visibility = View.VISIBLE
                        dialog.dismiss()
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        )
                        clubViewModel.isLoading.observe(this) {
                            if (!it) {
                                binding.progressBar.visibility = View.GONE
                                finish()
                            }
                        }
                    }
                    .show()
            }
        }

        setResult(1, Intent().apply {
            putExtra("clubChanged", true)
        })
    }

    private fun moveToMountainDetail() {
        mountainViewModel.fetchMountainById(club?.mountainId!!.toInt(), false)
        mountainViewModel.mountain.observe(this) {
            val intent = Intent(
                this@ClubDetailActivity,
                MountainDetailActivity::class.java
            ).apply {
                putExtra("mountain", it)
            }
            startActivity(intent)

            mountainViewModel.mountain.removeObservers(this)
        }
    }

    override fun onResume() {
        super.onResume()
        clubViewModel.club.observe(this) {
            if (it?.applicantCount!! > 0) {
                badgeDrawable?.isVisible = true
                badgeDrawable?.number = it.applicantCount
            } else {
                badgeDrawable?.isVisible = false
            }
            BadgeUtils.attachBadgeDrawable(badgeDrawable!!, binding.btnMember)
            binding.btnMember.invalidate()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing && goToMain) {
            val intent = Intent(this, ClubActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("back", true)
            }
            startActivity(intent)
        }
    }
}