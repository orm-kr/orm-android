package com.orm.ui.club

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orm.data.model.ClubMember
import com.orm.data.model.club.Club
import com.orm.data.model.club.ClubApprove
import com.orm.data.model.recycler.RecyclerViewButtonItem
import com.orm.databinding.ActivityClubMemberBinding
import com.orm.ui.MainActivity
import com.orm.ui.adapter.ProfileButtonAdapter
import com.orm.viewmodel.ClubViewModel
import com.orm.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClubMemberActivity : AppCompatActivity() {
    private val binding: ActivityClubMemberBinding by lazy {
        ActivityClubMemberBinding.inflate(layoutInflater)
    }

    private val club: Club? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("club", Club::class.java)
        } else {
            intent.getParcelableExtra<Club>("club")
        }
    }

    private val goToMain: Boolean by lazy {
        intent.getBooleanExtra("back", false)
    }

    private val clubViewModel: ClubViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private val rvMemberList: RecyclerView by lazy { binding.rvMemberList }
    private val rvApplicant: RecyclerView by lazy { binding.rvApplicant }

    private lateinit var adapterMemberList: ProfileButtonAdapter
    private lateinit var adapterApplicant: ProfileButtonAdapter

    private var userId: String? = null
    private var membersMap: Map<String, List<ClubMember>?>? = null

    private var clubMembers: List<ClubMember>? = null
    private var applMembers: List<ClubMember>? = null

    private var memberItem: List<RecyclerViewButtonItem>? = null
    private var applItem: List<RecyclerViewButtonItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        userViewModel.user.observe(this) { user ->
            userId = user?.userId
            if (user != null && user.userId != club!!.managerId) {
                binding.cvApplicant.visibility = View.GONE
                binding.rvApplicant.visibility = View.GONE
            }
            checkIfDataReady()
        }

        clubViewModel.getMembers(club!!.id)
        clubViewModel.members.observe(this@ClubMemberActivity) { membersMap ->
            this.membersMap = membersMap
            checkIfDataReady()
        }

        setResult(1, Intent().apply {
            putExtra("clubMember", true)
        }
        )
    }

    private fun checkIfDataReady() {
        if (userId != null && membersMap != null) {
            clubMembers = membersMap!!["members"] ?: emptyList()
            applMembers = membersMap!!["applicants"] ?: emptyList()
            setupAdapterMemberList()
            setupAdapterApplicant()
        }
    }

    private fun setupAdapterMemberList() {
        memberItem = clubMembers?.map { ClubMember.toRecyclerViewButtonItem(it) } ?: emptyList()

        adapterMemberList = ProfileButtonAdapter(memberItem!!)

        adapterMemberList.setType("member")
        adapterMemberList.setUserId(userId.toString())
        adapterMemberList.setManagerId(club!!.managerId)
        adapterMemberList.setItemClickListener(object : ProfileButtonAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                return
            }

            override fun onClickBtnUp(v: View, position: Int) {
                MaterialAlertDialogBuilder(this@ClubMemberActivity)
                    .setTitle("클럽 탈퇴")
                    .setMessage("정말로 ${club?.clubName} 클럽에서 탈퇴하시겠습니까?")
                    .setNegativeButton("취소") { _, _ -> }
                    .setPositiveButton("확인") { dialog, which ->
                        clubViewModel.leaveClubs(club!!.id, memberItem!![position].id!!.toInt())
                        setResult(2)
                        finish()
                    }.show()
            }

            override fun onClickBtnDown(v: View, position: Int) {
                if (memberItem!![position].id == club!!.managerId.toInt()) {
                    return
                }
                MaterialAlertDialogBuilder(this@ClubMemberActivity)
                    .setTitle("회원 추방")
                    .setMessage("정말로 ${memberItem!![position].nickName}님을 추방하시겠습니까?")
                    .setNegativeButton("취소") { _, _ -> }
                    .setPositiveButton("확인") { _, _ ->
                        clubViewModel.dropMember(club!!.id, memberItem!![position].id!!.toInt())
                        adapterMemberList.removeItem(position)
                        memberItem = memberItem?.toMutableList()?.apply {
                            removeAt(position)
                        }
                        clubMembers = clubMembers?.toMutableList()?.apply {
                            removeAt(position)
                        }
                    }.show()
            }
        })

        rvMemberList.adapter = adapterMemberList
        rvMemberList.layoutManager = LinearLayoutManager(this@ClubMemberActivity)
    }

    private fun setupAdapterApplicant() {
        applItem = applMembers?.map { ClubMember.toRecyclerViewButtonItem(it) } ?: emptyList()

        adapterApplicant = ProfileButtonAdapter(applItem!!)

        adapterApplicant.setType("applicant")

        adapterApplicant.setItemClickListener(object : ProfileButtonAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                return
            }

            override fun onClickBtnUp(v: View, position: Int) {
                MaterialAlertDialogBuilder(this@ClubMemberActivity)
                    .setTitle("가입 수락")
                    .setMessage("가입을 수락하시겠습니까?")
                    .setNegativeButton("취소") { _, _ -> }
                    .setPositiveButton("확인") { dialog, _ ->
                        val acceptedMember = applMembers!![position]

                        adapterMemberList.addItem(applItem!![position], clubMembers!!.size)
                        adapterApplicant.removeItem(position)

                        clubViewModel.approveClubs(
                            ClubApprove(
                                club!!.id,
                                applItem!![position].id!!,
                                true
                            )
                        )

                        clubMembers = clubMembers?.toMutableList()?.apply {
                            add(acceptedMember)
                        }

                        memberItem = memberItem?.toMutableList()?.apply {
                            add(ClubMember.toRecyclerViewButtonItem(acceptedMember))
                        }

                        applMembers = applMembers?.toMutableList()?.apply {
                            removeAt(position)
                        }

                        applItem = applItem?.toMutableList()?.apply {
                            removeAt(position)
                        }

                        dialog.dismiss()
                    }
                    .show()
            }

            override fun onClickBtnDown(v: View, position: Int) {
                MaterialAlertDialogBuilder(this@ClubMemberActivity)
                    .setTitle("가입 거절")
                    .setMessage("가입을 거절하시겠습니까?")
                    .setNegativeButton("취소") { _, _ -> }
                    .setPositiveButton("확인") { dialog, which ->
                        clubViewModel.approveClubs(
                            ClubApprove(
                                club!!.id,
                                applItem!![position].id!!,
                                false
                            )
                        )
                        adapterApplicant.removeItem(position)
                        applMembers = applMembers?.toMutableList()?.apply {
                            removeAt(position)
                        }
                        applItem = applItem?.toMutableList()?.apply {
                            removeAt(position)
                        }
                        dialog.dismiss()
                    }
                    .show()
            }
        })

        rvApplicant.adapter = adapterApplicant
        rvApplicant.layoutManager = LinearLayoutManager(this@ClubMemberActivity)
    }

    override fun onPause() {
        if (isFinishing && goToMain) {
            clubViewModel.getClubById(club!!.id)
            clubViewModel.club.observe(this@ClubMemberActivity){
                val intent = Intent(this, ClubDetailActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("club", it)
                    putExtra("back", true)
                }
                startActivity(intent)
            }
        }
        super.onPause()
    }
}
