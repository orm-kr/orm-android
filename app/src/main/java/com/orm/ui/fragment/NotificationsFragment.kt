package com.orm.ui.fragment

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orm.data.model.Notification
import com.orm.databinding.FragmentNotificationsBinding
import com.orm.ui.adapter.ProfileNotificationAdapter
import com.orm.viewmodel.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val notificationViewModel: NotificationViewModel by viewModels()

    private val rvBoard: RecyclerView by lazy { binding.recyclerView }
    private lateinit var adapter: ProfileNotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            notificationViewModel.getAllNotifications()
            notificationViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
//                if (notifications.isNullOrEmpty()) {
//                    binding.emptyView.visibility = View.VISIBLE
//                    binding.recyclerView.visibility = View.GONE
//                    return@observe
//                }
                setupAdapter(notifications)
            }

            binding.btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("알림 삭제")
                    .setMessage("모든 알림을 삭제하시겠습니까?")
                    .setNegativeButton("취소") { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("삭제") { dialog, which ->
                        notificationViewModel.deleteAllNotifications()
                        binding.count = 0
                        dialog.dismiss()
                    }
                    .setNegativeButton("취소", null)
                    .show()


            }

        } catch (e: Exception) {
            Log.e("notification", "notification list", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapter(notifications: List<Notification>) {
        if (_binding == null) return
//
//        binding.emptyView.visibility = View.GONE
//        binding.recyclerView.visibility = View.VISIBLE
//
        val reversedNotifications = notifications.reversed()

        adapter = ProfileNotificationAdapter(reversedNotifications.map {
            Notification.toRecyclerViewNotificationItem(it)
        })

        adapter.setItemClickListener(object : ProfileNotificationAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
            }

            override fun onLongClick(v: View, position: Int) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("알림 삭제")
                    .setMessage("알림을 삭제하시겠습니까?")
                    .setPositiveButton("확인") { _, _ ->
                        notificationViewModel.deleteNotification(notifications[notifications.count() - position - 1])
                    }
                    .setNegativeButton("취소") { _, _ ->
                        // Dialog에서 취소 버튼을 누른 경우에 실행할 코드
                    }
                    .show()
            }
        })

        binding.count = reversedNotifications.count()

        rvBoard.adapter = adapter
        rvBoard.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }
}
