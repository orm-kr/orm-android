package com.orm.ui.fragment.club

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orm.data.model.club.Club
import com.orm.data.model.recycler.RecyclerViewBasicItem
import com.orm.databinding.FragmentClubAllBinding
import com.orm.databinding.FragmentClubMeBinding
import com.orm.ui.adapter.ProfileBasicAdapter
import com.orm.ui.club.ClubDetailActivity
import com.orm.viewmodel.ClubViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClubAllFragment : Fragment() {
    private var _binding: FragmentClubAllBinding? = null
    private val binding get() = _binding!!

    private val clubViewModel: ClubViewModel by viewModels()

    private val rvBoard: RecyclerView by lazy { binding.recyclerView }
    private lateinit var adapter: ProfileBasicAdapter

    private val createClubLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val clubChanged = data?.getBooleanExtra("clubChanged", false) ?: false
                if (clubChanged) {
                    clubViewModel.getClubs(isMyClub = false)
                    clubViewModel.clubs.observe(viewLifecycleOwner) { clubs ->
                        if (clubs.isNullOrEmpty()) {
                            binding.emptyView.visibility = View.VISIBLE
                        } else {
                            binding.emptyView.visibility = View.GONE
                        }
                        setupAdapter(clubs)
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentClubAllBinding.inflate(inflater, container, false)
        val root: View = binding.root

        clubViewModel.getClubs(isMyClub = false)
        clubViewModel.clubs.observe(viewLifecycleOwner) { clubs ->
            if (clubs.isNullOrEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
            } else {
                binding.emptyView.visibility = View.GONE
            }
            setupAdapter(clubs)
        }

        clubViewModel.isReady.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) View.GONE else View.VISIBLE
            if (!it) {
                binding.emptyView.visibility = View.GONE
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapter(clubs: List<Club>) {
        adapter =
            ProfileBasicAdapter(clubs.map { Club.toRecyclerViewBasicItem(it) })

        adapter.setItemClickListener(object : ProfileBasicAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val intent = Intent(
                    requireContext(),
                    ClubDetailActivity::class.java
                ).apply {
                    putExtra("club", clubs[position])
                }
                createClubLauncher.launch(intent)
//                startActivity(intent)
            }
        })
        rvBoard.adapter = adapter
        rvBoard.layoutManager = LinearLayoutManager(requireContext())
    }
}