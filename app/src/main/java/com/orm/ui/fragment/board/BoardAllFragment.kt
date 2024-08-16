package com.orm.ui.fragment.board

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.orm.data.model.board.BoardList
import com.orm.data.model.club.Club
import com.orm.databinding.FragmentBoardAllBinding
import com.orm.ui.adapter.ProfileBoardAdapter
import com.orm.ui.board.BoardDetailActivity
import com.orm.viewmodel.BoardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BoardAllFragment : Fragment() {
    private var _binding: FragmentBoardAllBinding? = null
    private val binding get() = _binding!!
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val boardViewModel: BoardViewModel by viewModels()

    private val rvBoard: RecyclerView by lazy { binding.recyclerView }
    private lateinit var adapter: ProfileBoardAdapter

    private val club: Club? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("club", Club::class.java)
        } else {
            arguments?.getParcelable<Club>("club")
        }
    }

    private lateinit var detailActivityResultLauncher: ActivityResultLauncher<Intent>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBoardAllBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val clubId = club?.id ?: -1

        // ActivityResultLauncher 초기화
        detailActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                refreshData()
            }
        }

        // Fetch board list using clubId
        boardViewModel.getBoardList(clubId)
        boardViewModel.boardList.observe(viewLifecycleOwner) { boardList ->
            Log.e("BoardAllFragment", boardList.toString())
            setupAdapter(boardList, clubId)
        }

        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapter(boardList: List<BoardList>, clubId: Int) {
        Log.d("update123", "update123 : set")

        val reversedList = boardList.reversed()

        // Check if boardList is empty
        if (reversedList.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE

            adapter = ProfileBoardAdapter(reversedList.map { BoardList.toRecyclerViewBoardItem(it) })

            adapter.setItemClickListener(object : ProfileBoardAdapter.OnItemClickListener {
                override fun onClick(v: View, position: Int) {
                    val intent = Intent(
                        requireContext(),
                        BoardDetailActivity::class.java
                    ).apply {
                        putExtra("boardList", reversedList[position])
                        putExtra("club", club)
                    }
                    Log.e("boardAllFragment", "board: ${reversedList[position]}")
                    detailActivityResultLauncher.launch(intent)
                }
            })

            rvBoard.adapter = adapter
            rvBoard.layoutManager = LinearLayoutManager(requireContext())
        }
    }
    fun refreshData() {
        Log.d("refresh", "refresh123 frag")
        val clubId = club?.id ?: -1
        boardViewModel.getBoardList(clubId)
        swipeRefreshLayout.isRefreshing = false
    }
}
