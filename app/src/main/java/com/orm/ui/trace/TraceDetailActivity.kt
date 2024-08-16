package com.orm.ui.trace

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.orm.R
import com.orm.data.model.Trace
import com.orm.databinding.ActivityTraceDetailBinding
import com.orm.ui.PhotoViewerActivity
import com.orm.ui.fragment.GraphFragment
import com.orm.ui.fragment.map.BasicGoogleMapFragment
import com.orm.ui.fragment.table.TextTableFragment
import com.orm.viewmodel.RecordViewModel
import com.orm.viewmodel.TraceViewModel
import com.orm.viewmodel.TrailViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TraceDetailActivity : AppCompatActivity() {
    private val binding: ActivityTraceDetailBinding by lazy {
        ActivityTraceDetailBinding.inflate(layoutInflater)
    }
    private val traceViewModel: TraceViewModel by viewModels()
    private val trailViewModel: TrailViewModel by viewModels()
    private val recordViewModel: RecordViewModel by viewModels()

    private val createTraceLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val traceCreated = data?.getBooleanExtra("traceCreated", false) ?: false
                if (traceCreated) {
                    traceViewModel.getTrace(trace!!.localId)
                    traceViewModel.trace.observe(this@TraceDetailActivity) {
                        trace = it
                        binding.trace = trace
                        if (trace!!.trailId == -1 || trace!!.trailId == null) {
                            binding.cvMap.visibility = View.GONE
                        } else {
                            trailViewModel.getTrail(trace!!.trailId!!)
                            trailViewModel.trail.observe(this@TraceDetailActivity) {
                                val fragment =
                                    supportFragmentManager.findFragmentById(binding.fcvMap.id) as? BasicGoogleMapFragment
                                fragment?.updatePoints(it.trailDetails)
                            }
                            binding.cvMap.visibility = View.VISIBLE
                        }
                        if (trace!!.mountainId == -1) {
                            binding.cvMap.visibility = View.GONE
                        }
                    }
                }
            }

            if (result.resultCode == 1) {
                val data: Intent? = result.data
                val traceModified = data?.getBooleanExtra("traceModified", false) ?: false
                if (traceModified) {
                    traceViewModel.getTrace(trace!!.localId)
                    traceViewModel.trace.observe(this@TraceDetailActivity) {
                        trace = it
                        binding.trace = trace
                    }
                }
            }
        }

    private var trace: Trace? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        trace = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("trace", Trace::class.java)
        } else {
            intent.getParcelableExtra<Trace>("trace")
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fcvMap.id, BasicGoogleMapFragment())
                .commit()

            if (trace!!.recordId != null) {
                supportFragmentManager.beginTransaction()
                    .replace(binding.fcvMapTrack.id, BasicGoogleMapFragment())
                    .commit()

                supportFragmentManager.beginTransaction()
                    .replace(binding.fcvTable.id, TextTableFragment.newInstance(trace!!))
                    .commit()

                recordViewModel.getRecord(trace!!.recordId!!)
                recordViewModel.record.observe(this@TraceDetailActivity) {
                    if (it.coordinate.isNullOrEmpty()) {
                        binding.cvMapTrack.visibility = View.GONE
                        binding.cvGraph.visibility = View.GONE
                        return@observe
                    }

                    val firstTime = it.coordinate.first().time!!.toFloat()
                    val adjustedCoordinates = it.coordinate.map { pair ->
                        Pair(
                            (pair.time!!.toFloat() - firstTime) / 60000,
                            pair.altitude!!.toFloat()
                        )
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(
                            binding.fragmentGraph.id,
                            GraphFragment.newInstance(adjustedCoordinates)
                        )
                        .commit()
                }
            } else {
                binding.cvMapTrack.visibility = View.GONE
                binding.cvGraph.visibility = View.GONE
                binding.cvImageUpload.visibility = View.GONE
            }
        }

        binding.trace = trace

        binding.topAppBar.setNavigationOnClickListener {
            setResult(1)
            onBackPressedDispatcher.onBackPressed()
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    val intent: Intent = if (trace?.recordId == null) {
                        Intent(this, TraceEditActivity::class.java)
                    } else {
                        Intent(this, TraceDetailEditActivity::class.java)
                    }

                    createTraceLauncher.launch(intent.apply {
                        putExtra("trace", trace)
                    })
                    true
                }

                else -> false
            }
        }

        this.onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    setResult(1)
                    finish()
                }
            })

        // 측정 기록이 없는 경우 측정 테이블 안보임
        // 측정 완료한 경우 측정 버튼 안보임
        if (trace!!.recordId == null) {
            binding.fcvTable.visibility = View.GONE
            binding.divider.visibility = View.GONE
            binding.cvTraceResult.visibility = View.GONE
        } else {
            binding.btnStart.visibility = View.GONE
        }

        binding.cvImageUpload.setOnClickListener {
            val intent = Intent(this, PhotoViewerActivity::class.java)
            intent.putExtra("IMAGE_URL", trace?.imgPath)
            startActivity(intent)
        }

        // 측정 시작 버튼
        binding.btnStart.setOnClickListener {
            val intent = Intent(this, TraceMeasureActivity::class.java)
            intent.putExtra("trace", trace)
            startActivity(intent)
        }

        // 등산로 선택하지 않은 경우 지도 안보임
        if (trace!!.trailId == -1 || trace!!.trailId == null) {
            binding.cvMap.visibility = View.GONE
        } else {
            trailViewModel.getTrail(trace!!.trailId!!)
            trailViewModel.trail.observe(this@TraceDetailActivity) {
                val fragment =
                    supportFragmentManager.findFragmentById(binding.fcvMap.id) as? BasicGoogleMapFragment
                fragment?.updatePoints(it.trailDetails)
            }
        }

        if (trace!!.recordId != null) {
            recordViewModel.getRecord(trace!!.recordId!!)
            recordViewModel.record.observe(this@TraceDetailActivity) {
                val fragment =
                    supportFragmentManager.findFragmentById(binding.fcvMapTrack.id) as? BasicGoogleMapFragment

                val points = it.coordinate ?: emptyList()
                fragment?.updatePoints(points.reversed())
            }
        }

        binding.transparentImage1.setOnTouchListener { v, event ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.scrollView.requestDisallowInterceptTouchEvent(true)
                    false
                }

                MotionEvent.ACTION_UP -> {
                    binding.scrollView.requestDisallowInterceptTouchEvent(false)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    binding.scrollView.requestDisallowInterceptTouchEvent(true)
                    false
                }

                else -> true
            }
        }

        binding.transparentImage2.setOnTouchListener { v, event ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.scrollView.requestDisallowInterceptTouchEvent(true)
                    false
                }

                MotionEvent.ACTION_UP -> {
                    binding.scrollView.requestDisallowInterceptTouchEvent(false)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    binding.scrollView.requestDisallowInterceptTouchEvent(true)
                    false
                }

                else -> true
            }
        }
    }
}