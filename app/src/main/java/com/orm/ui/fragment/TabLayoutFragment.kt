package com.orm.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.orm.R
import com.orm.databinding.FragmentTabLayoutBinding
import com.orm.ui.fragment.club.ClubAllFragment
import com.orm.ui.fragment.club.ClubApplyFragment
import com.orm.ui.fragment.club.ClubMeFragment

class TabLayoutFragment : Fragment() {
    private var _binding: FragmentTabLayoutBinding? = null
    private val binding get() = _binding!!

    private var selectedTabIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTabLayoutBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val tabLayout = binding.tabLayout

        // 처음 Fragment 설정
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction().replace(R.id.main_view, ClubMeFragment())
                .commitNow()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedTabIndex = tab.position
                Log.e("tab position", tab.position.toString())
                when (tab.position) {
                    0 -> {
                        replaceFragment(ClubMeFragment())
                    }

                    1 -> {
                        replaceFragment(ClubAllFragment())
                    }

                    2 -> {
                        replaceFragment(ClubApplyFragment())
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return root
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_view, fragment)
        fragmentTransaction.commitNow()
    }

    fun selectTab(index: Int) {
        binding.tabLayout.getTabAt(index)?.select()
    }

    fun getSelectedTabIndex(): Int {
        return selectedTabIndex
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
