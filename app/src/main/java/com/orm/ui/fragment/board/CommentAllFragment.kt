package com.orm.ui.fragment.board

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.orm.R
import com.orm.data.model.board.Board
import com.orm.data.model.board.BoardList
import com.orm.data.model.board.Comment
import com.orm.data.model.club.Club
import com.orm.data.model.recycler.RecyclerViewCommentItem
import com.orm.databinding.FragmentCommentAllBinding
import com.orm.ui.adapter.ProfileCommentAdapter
import com.orm.ui.board.BoardDetailActivity
import com.orm.viewmodel.BoardViewModel
import com.orm.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommentAllFragment : Fragment() {
    private var _binding: FragmentCommentAllBinding? = null
    private val binding get() = _binding!!

    private val boardViewModel: BoardViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var adapter: ProfileCommentAdapter

    private val boardId: Int? by lazy {
        arguments?.getInt("boardId")
    }

    private val clubId: Int? by lazy {
        arguments?.getInt("clubId")
    }

    private val board: Board? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("board", Board::class.java)
        } else {
            arguments?.getParcelable<Board>("board")
        }
    }

    private val userId: String? by lazy {
        arguments?.getString("userId")
    }

    private val boardList: BoardList? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("boardList", BoardList::class.java)
        } else {
            arguments?.getParcelable<BoardList>("boardList")
        }
    }

    private val club: Club? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("club", Club::class.java)
        } else {
            arguments?.getParcelable<Club>("club")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCommentAllBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        board?.let {
            updateCommentCount(it.comments.size)  // 초기 댓글 수 설정
            setupAdapter(it.comments, userId)
        }

        boardViewModel.comment.observe(viewLifecycleOwner) { updatedComment ->
            updatedComment?.let {
                val updatedList = adapter.getItems().toMutableList() // 변경된 부분
                val index = updatedList.indexOfFirst { it.commentId == updatedComment.commentId }
                if (index != -1) {
                    updatedList[index] = Comment.toRecyclerViewCommentItem(updatedComment)
                    adapter.submitList(updatedList)
                }
            }
        }

        boardViewModel.board.observe(viewLifecycleOwner) { updatedBoard ->
            updatedBoard?.let {
                updateCommentCount(it.commentCount)
                setupAdapter(it.comments, userId)
            }
        }

        boardViewModel.isOperationSuccessful.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                boardId?.let { boardViewModel.getBoards(it) }
            }
        }

        binding.tfSubmit.setEndIconOnClickListener {
            val comment = binding.tfComment.text.toString().trim()

            if (comment.isNotEmpty()) {
                boardId?.let { boardId ->
                    boardViewModel.createComments(boardId, comment)
                    binding.tfComment.text?.clear()
                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.tfComment.windowToken, 0)
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapter(comments: List<Comment>, currentUserId: String?) {
        adapter = ProfileCommentAdapter(
            comments.map { Comment.toRecyclerViewCommentItem(it) },
            currentUserId ?: "",
            onEditClick = { comment ->
                if (comment.userId.toString() == currentUserId) {
                    showEditBottomSheet(comment)
                }
            },
            onDeleteClick = { comment ->
                if (comment.userId.toString() == currentUserId) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("댓글 삭제")
                        .setMessage("정말로 이 댓글을 삭제하시겠습니까?")
                        .setNegativeButton("취소") { _, _ -> }
                        .setPositiveButton("확인") { dialog, which ->
                            val progressDialog = ProgressDialog(requireContext())
                            progressDialog.setMessage("게시글을 삭제 중입니다...")
                            progressDialog.setCancelable(false)
                            progressDialog.show()
                            boardId?.let { boardId ->
                                boardViewModel.deleteComments(boardId, comment.commentId)
                            }
                            progressDialog.dismiss()
                        }
                        .setNegativeButton("취소", null)
                        .show()


                    }

            }
        )

        binding.recyclerView.adapter = adapter
    }

    @SuppressLint("MissingInflatedId")
    private fun showEditBottomSheet(comment: RecyclerViewCommentItem) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_edit_comment, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // Get references to the views
        val editTextComment = bottomSheetView.findViewById<TextInputEditText>(R.id.tf_edit_comment)
        val editButton = bottomSheetView.findViewById<TextInputLayout>(R.id.tf_edit)

        // Set the current comment content
        editTextComment.setText(comment.content)

        // Set up the click listener for the update button
        editButton.setEndIconOnClickListener {
            val updatedContent = editTextComment.text.toString()
            if (updatedContent.isNotEmpty()) {
                boardId?.let {
                    boardViewModel.updateComments(it, comment.commentId, updatedContent)
                    bottomSheetDialog.dismiss()  // Dismiss the dialog after update
                }
            }
        }
        bottomSheetDialog.show()
    }


    private fun updateCommentCount(count: Int) {
        binding.tvCommentCount.text = "댓글 수 $count"
    }

}
