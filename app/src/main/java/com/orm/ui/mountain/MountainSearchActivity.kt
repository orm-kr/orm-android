package com.orm.ui.mountain

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orm.data.model.Mountain
import com.orm.databinding.ActivityMountainSearchBinding
import com.orm.ui.adapter.ProfileBasicAdapter
import com.orm.util.NetworkUtils
import com.orm.viewmodel.MountainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MountainSearchActivity : AppCompatActivity() {
    private val binding: ActivityMountainSearchBinding by lazy {
        ActivityMountainSearchBinding.inflate(layoutInflater)
    }
    private val mountainViewModel: MountainViewModel by viewModels()
    private val rvBoard: RecyclerView by lazy { binding.recyclerView }
    private lateinit var adapter: ProfileBasicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.emptyView.visibility = View.GONE

        mountainViewModel.fetchMountainsTop()
        mountainViewModel.mountains.observe(this@MountainSearchActivity) {
            setupAdapter(it!!)
        }

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        mountainViewModel.isLoading.observe(this) {
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        binding.svMountain.isSubmitButtonEnabled = true
        binding.svMountain.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(name: String?): Boolean {

                mountainViewModel.fetchMountainByName(name.toString())
                mountainViewModel.mountains.observe(this@MountainSearchActivity) {
                    if (it.isNullOrEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                    } else {
                        binding.emptyView.visibility = View.GONE
                    }
                    setupAdapter(it!!)
                }
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }

        })
    }

    private fun setupAdapter(mountains: List<Mountain>) {
        adapter =
            ProfileBasicAdapter(mountains.map { Mountain.toRecyclerViewBasicItem(it) })

        adapter.setItemClickListener(object : ProfileBasicAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val intent = Intent(
                    this@MountainSearchActivity,
                    MountainDetailActivity::class.java
                ).apply {
                    putExtra("mountain", mountains[position])
                }
                startActivity(intent)
            }
        })
        rvBoard.adapter = adapter
        rvBoard.layoutManager = LinearLayoutManager(this@MountainSearchActivity)
    }
}