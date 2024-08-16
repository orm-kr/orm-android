package com.orm.ui.trace

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orm.R
import com.orm.data.model.Trace
import com.orm.databinding.ActivityTraceBinding
import com.orm.ui.MainActivity
import com.orm.ui.adapter.ProfileNumberAdapter
import com.orm.viewmodel.TraceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TraceActivity : AppCompatActivity() {
    private val binding: ActivityTraceBinding by lazy {
        ActivityTraceBinding.inflate(layoutInflater)
    }
    private val traceViewModel: TraceViewModel by viewModels()
    private val rvBoard: RecyclerView by lazy { binding.recyclerView }
    private lateinit var adapter: ProfileNumberAdapter

    private val createTraceLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val traceCreated = data?.getBooleanExtra("traceCreated", false) ?: false
                Log.e("TraceActivity", traceCreated.toString())
                if (traceCreated) {
                    traceViewModel.getTraces()
                }
            }

            if (result.resultCode == 1) {
                traceViewModel.getTraces()
                traceViewModel.traces.observe(this@TraceActivity) {
                    if (it.isNullOrEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                    } else {
                        binding.emptyView.visibility = View.GONE
                    }
                    setupAdapter(it!!)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        traceViewModel.getTraces()
        traceViewModel.traces.observe(this@TraceActivity) {
            if (it.isNullOrEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
            } else {
                binding.emptyView.visibility = View.GONE
            }
            setupAdapter(it!!)
        }

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    createTraceLauncher.launch(Intent(this, TraceEditActivity::class.java))
                    true
                }

                else -> false
            }
        }

        this.onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    startActivity(Intent(this@TraceActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
            })
    }

    private fun setupAdapter(traces: List<Trace>) {
        adapter =
            ProfileNumberAdapter(traces.map { Trace.toRecyclerViewNumberItem(it) })

        adapter.setType("trace")

        adapter.setItemClickListener(object : ProfileNumberAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val intent = Intent(
                    this@TraceActivity,
                    TraceDetailActivity::class.java
                ).apply {
                    putExtra("trace", traces[position])
                }
                createTraceLauncher.launch(intent)
            }

            override fun onLongClick(v: View, position: Int) {
                MaterialAlertDialogBuilder(this@TraceActivity)
                    .setTitle("발자국 삭제")
                    .setMessage("발자국을 삭제하시겠습니까?")
                    .setPositiveButton("확인") { _, _ ->
                        traceViewModel.deleteTrace(traces[position])
                    }
                    .setNegativeButton("취소") { _, _ ->
                        // Dialog에서 취소 버튼을 누른 경우에 실행할 코드
                    }
                    .show()
            }
        })
        rvBoard.adapter = adapter
        rvBoard.layoutManager = LinearLayoutManager(this@TraceActivity)
    }
}