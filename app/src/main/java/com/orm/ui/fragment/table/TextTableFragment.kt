package com.orm.ui.fragment.table

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.orm.data.model.Trace
import com.orm.databinding.FragmentTextTableBinding
import com.orm.util.getTimeDifferenceFormatted
import com.orm.util.timestampToTimeString
import java.util.Locale

class TextTableFragment : Fragment() {
    private var _binding: FragmentTextTableBinding? = null
    private val binding get() = _binding!!

    private var trace: Trace? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            trace = it.getParcelable(ARG_TRACE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTextTableBinding.inflate(inflater, container, false)
        val root: View = binding.root

        trace?.let {
            binding.tvStart.text =
                timestampToTimeString(it.hikingStartedAt ?: 0, "yy-MM-dd HH:mm")
            binding.tvEnd.text = timestampToTimeString(it.hikingEndedAt ?: 0, "yy-MM-dd HH:mm")
            binding.tvMaxHeight.text = String.format(Locale.KOREA, "%.1f m", it.maxHeight)
            binding.tvDistance.text = String.format(Locale.KOREA, "%.0f m", it.hikingDistance)
            binding.tvDuration.text = getTimeDifferenceFormatted(
                it.hikingStartedAt ?: 0,
                it.hikingEndedAt ?: 0
            )

        } ?: run {
            binding.tvStart.text = "N/A"
            binding.tvEnd.text = "N/A"
            binding.tvDuration.text = "N/A"
            binding.tvMaxHeight.text = "N/A"
            binding.tvDistance.text = "N/A"
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "TRACE_TABLE"
        private const val ARG_TRACE = "TRACE"

        fun newInstance(trace: Trace): TextTableFragment {
            val fragment = TextTableFragment()
            val args = Bundle().apply {
                putParcelable(ARG_TRACE, trace)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
