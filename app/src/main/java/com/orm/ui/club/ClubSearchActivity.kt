package com.orm.ui.club

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orm.data.model.club.Club
import com.orm.databinding.ActivityClubSearchBinding
import com.orm.ui.adapter.ProfileBasicAdapter
import com.orm.viewmodel.ClubViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClubSearchActivity : AppCompatActivity() {
    private val binding: ActivityClubSearchBinding by lazy {
        ActivityClubSearchBinding.inflate(layoutInflater)
    }
    private val clubViewModel: ClubViewModel by viewModels()
    private val rvBoard: RecyclerView by lazy { binding.recyclerView }
    private lateinit var adapter: ProfileBasicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        clubViewModel.isReady.observe(this@ClubSearchActivity) {
            binding.progressBar.visibility = if (it) View.GONE else View.VISIBLE
        }

        // 클럽 데이터를 관찰하는 관찰자 등록
        clubViewModel.clubs.observe(this@ClubSearchActivity) { clubs ->
            if (clubs.isEmpty()) {
                showDialog()
            } else {
                setupRecyclerView(clubs)
            }
        }

        binding.svClub.isSubmitButtonEnabled = true
        binding.svClub.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(name: String?): Boolean {
                // 검색 요청을 보냄
                clubViewModel.getClubs(name.toString())
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }

        })
    }

    private fun setupRecyclerView(clubs: List<Club>) {
        adapter = ProfileBasicAdapter(clubs.map { Club.toRecyclerViewBasicItem(it) })
        adapter.setItemClickListener(object : ProfileBasicAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val intent = Intent(this@ClubSearchActivity, ClubDetailActivity::class.java).apply {
                    putExtra("club", clubs[position])
                }
                startActivity(intent)
            }
        })
        rvBoard.adapter = adapter
        rvBoard.layoutManager = LinearLayoutManager(this@ClubSearchActivity)
    }

    private fun showDialog() {
        MaterialAlertDialogBuilder(this@ClubSearchActivity)
            .setTitle("검색 결과")
            .setMessage("검색 결과가 없습니다.")
            .setPositiveButton("확인") { _, _ -> }
            .show()
    }
}
