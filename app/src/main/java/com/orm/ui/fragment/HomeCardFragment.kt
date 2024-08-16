package com.orm.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.orm.R
import com.orm.databinding.FragmentHomeCardBinding

class HomeCardFragment : Fragment() {
    private var _binding: FragmentHomeCardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeCardBinding.inflate(inflater, container, false)
        val root = binding.root

        val title = arguments?.getString("title") ?: "Default Title"
        val subtitle = arguments?.getString("subtitle") ?: "Default Subtitle"
//        val backGround = arguments?.getInt("background") ?: R.drawable.sample

        binding.tvTitle.text = title
        binding.tvSubTitle.text = subtitle
//        root.setBackgroundResource(backGround)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(title: String, subtitle: String): HomeCardFragment {
            val fragment = HomeCardFragment()
            val args = Bundle()
            args.putString("title", title)
            args.putString("subtitle", subtitle)
//            args.putInt("background", backGround)
            fragment.arguments = args
            return fragment
        }
    }
}