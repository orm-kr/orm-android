package com.orm.ui.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.orm.R
import com.orm.databinding.FragmentGraphBinding

class GraphFragment : Fragment() {
    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!
    private var data: List<Pair<Float, Float>>? = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lineChart: LineChart = view.findViewById(R.id.graph)
        val chartData = data ?: emptyList()
        setDataToChart(lineChart, chartData)
    }

    private fun setDataToChart(lineChart: LineChart, data: List<Pair<Float, Float>>) {
        val entries = data.map { Entry(it.first, it.second) }

        val dataSet = LineDataSet(entries, "Data").apply {
            color = Color.parseColor("#6200EE")  // Purple 500
            valueTextColor = Color.parseColor("#6200EE")  // Match value color to line color
            setDrawCircles(true)
            circleColors = listOf(Color.parseColor("#BB86FC"))  // Light purple for circles
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            lineWidth = 2f
            setDrawFilled(true)
            fillDrawable = ResourcesCompat.getDrawable(resources, R.drawable.gradient_fill, null)
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // x축 설정
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.setLabelCount(5, true)
        xAxis.textColor = Color.parseColor("#B0BEC5")  // Light gray
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}분"
            }
        }

        // y축 설정
        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.axisMinimum = data.minOfOrNull { it.second } ?: 0f
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.gridColor = Color.parseColor("#E0E0E0")  // Light gray for grid lines
        yAxisLeft.gridLineWidth = 1f
        yAxisLeft.setDrawAxisLine(true)
        yAxisLeft.setLabelCount(5, true)
        yAxisLeft.textColor = Color.parseColor("#B0BEC5")  // Light gray
        yAxisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}m"
            }
        }

        lineChart.axisRight.isEnabled = false

        lineChart.legend.isEnabled = false
        lineChart.setTouchEnabled(false)
        lineChart.description.isEnabled = false
        lineChart.setDrawGridBackground(false)
        lineChart.setBackgroundColor(Color.TRANSPARENT)
        lineChart.invalidate()
    }

    companion object {
        fun newInstance(data: List<Pair<Float, Float>>): GraphFragment {
            val fragment = GraphFragment()
            fragment.data = data
            return fragment
        }
    }
}
