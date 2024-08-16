package com.orm.ui.fragment

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.orm.R
import com.orm.data.model.User
import com.orm.databinding.FragmentSettingsBinding
import com.orm.ui.LauncherActivity
import com.orm.ui.LoginActivity
import com.orm.util.NetworkUtils
import com.orm.viewmodel.NotificationViewModel
import com.orm.viewmodel.RecordViewModel
import com.orm.viewmodel.TraceViewModel
import com.orm.viewmodel.TrailViewModel
import com.orm.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlin.system.exitProcess

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by viewModels()
    private val recordViewModel: RecordViewModel by viewModels()
    private val traceViewModel: TraceViewModel by viewModels()
    private val trailViewModel: TrailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        userViewModel.getUserInfo()
        userViewModel.user.observe(viewLifecycleOwner) {
            binding.switchPushNotifications.isChecked =
                it!!.pushNotificationsEnabled
        }

        // 푸시 알림 설정
        binding.switchPushNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                userViewModel.updateUserInfo(
                    userViewModel.user.value!!.copy(
                        pushNotificationsEnabled = true
                    )
                )
            } else {
                userViewModel.updateUserInfo(
                    userViewModel.user.value!!.copy(
                        pushNotificationsEnabled = false
                    )
                )
            }
        }

        // 개인 정보 수정
        binding.buttonEditPrivacySettings.setOnClickListener {
            val customLayout = inflater.inflate(R.layout.dialog_edit_privacy_settings, null)
            val ageSlider = customLayout.findViewById<Slider>(R.id.sliderAge)
            val ageValue = customLayout.findViewById<TextView>(R.id.ageValue)
            val genderGroup = customLayout.findViewById<RadioGroup>(R.id.radioGroupGender)
            val proficiencyGroup = customLayout.findViewById<RadioGroup>(R.id.radioGroupProficiency)

            userViewModel.getUserInfo()
            userViewModel.user.observe(viewLifecycleOwner) { user ->
                ageSlider.value = user!!.age.toFloat()
                ageValue.text = user.age.toString()

                val selectedGenderId = when (user.gender.lowercase()) {
                    "male" -> R.id.radioMale
                    "female" -> R.id.radioFemale
                    else -> -1
                }
                if (selectedGenderId != -1) {
                    genderGroup.check(selectedGenderId)
                }

                val selectedProficiencyId = when (user.level) {
                    1 -> R.id.radioBeginner
                    2 -> R.id.radioIntermediate
                    3 -> R.id.radioAdvanced
                    else -> 1
                }
                if (selectedProficiencyId != -1) {
                    proficiencyGroup.check(selectedProficiencyId)
                }
            }

            ageSlider.addOnChangeListener { _, value, _ ->
                ageValue.text = value.toInt().toString()
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("개인 정보 수정")
                .setView(customLayout)
                .setPositiveButton("저장") { dialog, _ ->
                    val selectedAge = ageSlider.value.toInt()
                    val selectedGender = when (genderGroup.checkedRadioButtonId) {
                        R.id.radioMale -> "male"
                        R.id.radioFemale -> "female"
                        else -> "male"
                    }
                    val selectedProficiency = when (proficiencyGroup.checkedRadioButtonId) {
                        R.id.radioBeginner -> 1
                        R.id.radioIntermediate -> 2
                        R.id.radioAdvanced -> 3
                        else -> 1
                    }

                    val updatedUser = User(
                        userId = userViewModel.user.value!!.userId,
                        imageSrc = userViewModel.user.value!!.imageSrc,
                        nickname = userViewModel.user.value!!.nickname,
                        gender = selectedGender,
                        age = selectedAge,
                        level = selectedProficiency,
                        pushNotificationsEnabled = userViewModel.user.value!!.pushNotificationsEnabled
                    )

                    userViewModel.updateUserInfo(updatedUser)
                    Toast.makeText(requireContext(), "개인 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 회원 탈퇴
        binding.buttonDeleteAccount.setOnClickListener {
            if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("회원 탈퇴")
                    .setMessage("인터넷 연결을 확인해주세요.")
                    .setPositiveButton("확인", null)
                    .show()
                return@setOnClickListener
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("회원 탈퇴")
                .setMessage("정말로 탈퇴하시겠습니까?")
                .setPositiveButton("네") { _, _ ->

                    val notificationManager =
                        requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancelAll()

                    notificationViewModel.deleteAllNotifications()
                    recordViewModel.deleteAllRecords()
                    traceViewModel.deleteAllTraces()
                    trailViewModel.deleteAllTrails()
                    userViewModel.deleteUser()

                    clearAppCache()
                    clearAppData()

                    userViewModel.isDeleteUser.observe(viewLifecycleOwner) { isDeleteUser ->
                        if (isDeleteUser) {
                            Toast.makeText(requireContext(), "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT)
                                .show()

                            startActivity(
                                Intent(requireContext(), LauncherActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                            )
                        }
                    }
                }
                .setNegativeButton("아니오", null)
                .show()
        }

        return root
    }

    private fun clearAppCache() {
        try {
            val cacheDir = requireContext().cacheDir
            deleteDir(cacheDir)
        } catch (e: Exception) {
            Log.e("AppClearCache", "Failed to clear app cache", e)
        }
    }

    private fun clearAppData() {
        try {
            val packageName = requireContext().packageName
            val packageManager = requireContext().packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val dataDir = applicationInfo.dataDir
            deleteDir(File(dataDir))
        } catch (e: Exception) {
            Log.e("AppClearData", "Failed to clear app data", e)
        }
    }

    private fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val children = dir.list()
            for (i in children!!.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir.delete()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
