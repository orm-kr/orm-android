package com.orm.ui.trace

import com.orm.ui.fragment.BottomSheetMountainList
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orm.data.model.Mountain
import com.orm.data.model.Point
import com.orm.data.model.Trace
import com.orm.data.model.Trail
import com.orm.databinding.ActivityTraceEditBinding
import com.orm.ui.fragment.map.BasicGoogleMapFragment
import com.orm.util.NetworkUtils
import com.orm.viewmodel.MountainViewModel
import com.orm.viewmodel.TraceViewModel
import com.orm.viewmodel.TrailViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class TraceEditActivity : AppCompatActivity(), BottomSheetMountainList.OnMountainSelectedListener {
    private val binding: ActivityTraceEditBinding by lazy {
        ActivityTraceEditBinding.inflate(layoutInflater)
    }

    private val trace: Trace? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("trace", Trace::class.java)
        } else {
            intent.getParcelableExtra<Trace>("trace")
        }
    }

    private val trailIndex: Int by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getIntExtra("trailIndex", 0)
        } else {
            intent.getIntExtra("trailIndex", 0)
        }
    }

    private val mountain: Mountain? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("mountain", Mountain::class.java)
        } else {
            intent.getParcelableExtra<Mountain>("mountain")
        }
    }

    private var mountainId: Int = -1
    private var mountainName: String = "선택된 산 없음"
    private var trails: List<Trail> = emptyList()

    private val traceViewModel: TraceViewModel by viewModels()
    private val mountainViewModel: MountainViewModel by viewModels()
    private val trailViewModel: TrailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fcvMap.id, BasicGoogleMapFragment())
                .commit()
        }

        if (trace != null) {
            if (trace!!.trailId != null) {
                trailViewModel.getTrail(trace!!.trailId!!)
            }
            trailViewModel.trail.observe(this@TraceEditActivity) {
                updateMapFragment(it.trailDetails)
            }
            mountainId = trace!!.mountainId
            mountainName = if (mountainId == -1) "" else trace!!.mountainName!!
            binding.mountainName = mountainName
        }

        binding.trace = trace

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if ((trace != null && trace!!.trailId == -1) || trace == null) {
            if (mountain == null) {
                binding.cvMap.visibility = View.INVISIBLE
                binding.cvTrails.visibility = View.INVISIBLE
            } else if (mountain != null) {
                binding.mountainName = mountain!!.name
                loadMountainTrails(mountain!!.id)
            }
        } else {
            if (trace!!.mountainId != -1) {
                if (NetworkUtils.isNetworkAvailable(this)) {
                    loadMountainTrails(trace!!.mountainId)
                } else {
                    binding.cvMap.visibility = View.INVISIBLE
                }
            } else {
                binding.cvMap.visibility = View.INVISIBLE
                binding.cvTrails.visibility = View.INVISIBLE
            }
        }

        mountainViewModel.isLoading.observe(this) {
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        binding.tfTraceMountain.setEndIconOnClickListener {
            val mountainName = binding.tfTraceMountain.editText?.text.toString()
            val bottomSheetFragment = BottomSheetMountainList.newInstance(mountainName)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.tfTraceMountain.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mountainId = -1
                mountainName = "선택된 산 없음"
            }

            override fun afterTextChanged(s: Editable?) {
                mountainId = -1
                mountainName = "선택된 산 없음"
            }
        })

        binding.btnSign.setOnClickListener {

            binding.btnSign.isEnabled = false

            if (binding.tfTraceName.editText!!.text.isEmpty()) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("경고")
                    .setMessage("발자국 이름을 입력하세요.")
                    .setPositiveButton("확인") { _, _ ->
                        binding.btnSign.isEnabled = true
                    }
                    .setOnDismissListener {
                        binding.btnSign.isEnabled = true
                    }
                    .show()
                return@setOnClickListener
            }

            val content = if (trace != null) "수정" else "생성"
            MaterialAlertDialogBuilder(this)
                .setTitle("${content}하기")
                .setMessage("발자국을 $content 하시겠습니까?")
                .setNegativeButton("취소") { _, _ -> }
                .setPositiveButton("확인") { dialog, which ->

                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )

                    val selectedTrailIndex = binding.spinnerTrails.selectedItemPosition
                    var selectedTrail = if (selectedTrailIndex != AdapterView.INVALID_POSITION) {
                        trails[selectedTrailIndex]
                    } else {
                        null
                    }

                    if (selectedTrail == null && trace?.trailId != null) {
                        if (trace?.mountainId == mountainId) {
                            trailViewModel.getTrail(trace!!.trailId!!)
                            trailViewModel.trail.observe(this@TraceEditActivity) {
                                selectedTrail = it
                            }
                        }
                    }

                    val traceCreate = Trace(
                        localId = trace?.localId ?: 0,
                        id = trace?.id,
                        title = binding.tfTraceName.editText?.text.toString(),
                        hikingDate = binding.tfDate.editText?.text.toString(),
                        mountainId = mountainId,
                        mountainName = mountainName,
                        trailId = selectedTrail?.id,
                        coordinates = selectedTrail?.trailDetails,
                    )

                    traceViewModel.createTrace(traceCreate)
                    if (selectedTrail != null) {
                        trailViewModel.createTrail(selectedTrail!!)
                    }

                    dialog.dismiss()

                    binding.progressBar.visibility = View.VISIBLE
                    traceViewModel.traceCreated.observe(this) { traceCreated ->
                        if (traceCreated) {
                            binding.progressBar.visibility = View.GONE
                            setResult(Activity.RESULT_OK, Intent().apply {
                                putExtra("traceCreated", true)
                            })
                            if (mountain == null) {
                                finish()
                            } else {
                                startActivity(
                                    Intent(
                                        this@TraceEditActivity,
                                        TraceActivity::class.java
                                    ).apply {
                                        flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    })
                            }
                        }
                    }
                }
                .show()
        }

        mountainViewModel.isLoading.observe(this) {
            if (it) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }

        val today = MaterialDatePicker.todayInUtcMilliseconds()

        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("등산 날짜 선택")
            .setSelection(today) // 기본 선택 날짜를 오늘로 설정
            .setCalendarConstraints(constraintsBuilder.build())
            .setTextInputFormat(SimpleDateFormat("yyyy-MM-dd"))
            .build()

        picker.addOnPositiveButtonClickListener {
            val selectedDateInMillis = it
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = sdf.format(Date(selectedDateInMillis))
            binding.tfDate.editText?.setText(dateString)
        }

        picker.addOnPositiveButtonClickListener {
            val selectedDateInMillis = it
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = sdf.format(Date(selectedDateInMillis))
            binding.tfDate.editText?.setText(dateString)
        }

        binding.tfDate.setOnClickListener {
            val existingPicker = supportFragmentManager.findFragmentByTag("trace_date")
            if (existingPicker == null) {
                picker.show(supportFragmentManager, "trace_date")
            }
        }

        binding.tfDateField.setOnClickListener {
            val existingPicker = supportFragmentManager.findFragmentByTag("trace_date")
            if (existingPicker == null) {
                picker.show(supportFragmentManager, "trace_date")
            }
        }
    }

    private fun updateMapFragment(points: List<Point>) {
        val fragment =
            supportFragmentManager.findFragmentById(binding.fcvMap.id) as? BasicGoogleMapFragment
        fragment?.updatePoints(points)
    }

    private fun setupTrailSpinner(trails: List<Trail>) {
        val spinner = findViewById<Spinner>(binding.spinnerTrails.id)
        val trailNames = trails.map { it.distance.toString() + "km" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trailNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long,
            ) {
                val selectedTrail = trails[position]
                Log.e("MountainDetailActivity", selectedTrail.trailDetails.toString())
                updateMapFragment(selectedTrail.trailDetails)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadMountainTrails(mountainKey: Int) {

        mountainViewModel.fetchMountainById(mountainKey)
        mountainViewModel.mountain.observe(this@TraceEditActivity) { it ->
            mountainId = it!!.id
            mountainName = it.name
            binding.mountainName = mountainName
            if (it.trails?.size != 0) {
                setupTrailSpinner(it.trails!!)
                this.trails = it.trails
                if (trace != null && trace!!.trailId != null) {
                    val index = findTrailIndex(it.trails, trace!!.trailId!!)
                    binding.spinnerTrails.setSelection(index)
                } else {
                    binding.spinnerTrails.setSelection(trailIndex)
                }
            } else {
                binding.cvMap.visibility = View.INVISIBLE
                binding.cvTrails.visibility = View.INVISIBLE
            }
        }
    }

    fun findTrailIndex(trails: List<Trail>, key: Int): Int {
        return trails.indexOfFirst { it.id == key }
    }

    override fun onMountainSelected(mountain: Mountain) {
        binding.tfTraceMountain.editText?.setText(mountain.name)
        mountainId = mountain.id
        mountainName = mountain.name

        mountainViewModel.fetchMountainById(mountainId)
        mountainViewModel.mountain.observe(this@TraceEditActivity) { it ->
            it?.trails?.let { trails ->
                setupTrailSpinner(trails)
                this.trails = trails
            }
            if (this.trails.isNotEmpty()) {
                binding.cvMap.visibility = View.VISIBLE
                binding.cvTrails.visibility = View.VISIBLE
            }
        }
    }
}
