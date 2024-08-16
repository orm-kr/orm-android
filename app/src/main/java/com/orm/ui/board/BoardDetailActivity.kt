package com.orm.ui.board

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orm.R
import com.orm.data.model.board.Board
import com.orm.data.model.board.BoardList
import com.orm.data.model.club.Club
import com.orm.databinding.ActivityBoardDetailBinding
import com.orm.ui.MainActivity
import com.orm.ui.PhotoViewerActivity
import com.orm.ui.fragment.board.CommentAllFragment
import com.orm.viewmodel.BoardViewModel
import com.orm.viewmodel.ClubViewModel
import com.orm.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.util.regex.Pattern

@AndroidEntryPoint
class BoardDetailActivity : AppCompatActivity() {
    private val binding: ActivityBoardDetailBinding by lazy {
        ActivityBoardDetailBinding.inflate(layoutInflater)
    }
    private val boardViewModel: BoardViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val clubViewModel: ClubViewModel by viewModels()

    private var isContentModified = false
    private var currentBoard: Board? = null
    private var processedContent: String = ""
    private lateinit var editActivityResultLauncher: ActivityResultLauncher<Intent>

    private val club: Club? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("club", Club::class.java)
        } else {
            intent.getParcelableExtra<Club>("club")
        }
    }

    private val boardList: BoardList? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("boardList", BoardList::class.java)
        } else {
            intent.getParcelableExtra<BoardList>("boardList")
        }
    }

    private val goToMain: Boolean by lazy {
        intent.getBooleanExtra("back", false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val boardId = boardList?.boardId ?: -1
        val clubId = club?.id ?: -1

        // Initialize ActivityResultLauncher for editing
        editActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val refresh = result.data?.getBooleanExtra("refresh", false) ?: false
                if (refresh) {
                    isContentModified = true
                    refreshData()
                }
            }
        }

        // Observe user info
        userViewModel.user.observe(this, Observer { user ->
            val userId = user?.userId
            checkPermissions(userId)

            // Fetch board details
            boardViewModel.getBoards(boardId)

            boardViewModel.board.observe(this, Observer { board ->
                if (board != null) {
                    currentBoard = board
                    binding.board = board
                    displayContent(board.content, board)

                    val commentFragment = CommentAllFragment().apply {
                        arguments = Bundle().apply {
                            putParcelable("board", board)
                            putInt("boardId", boardId)
                            putInt("clubId", clubId)
                            putParcelable("boardList", boardList)
                            putParcelable("club", club)
                            userId?.let { putString("userId", it) } // Pass userId as a string
                        }
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.info, commentFragment)
                        .commit()
                }
            })
        })

        binding.topAppBar.setNavigationOnClickListener {
            setResult(RESULT_OK, Intent().putExtra("refresh", true))
            finish()
        }

        boardViewModel.isOperationSuccessful.observe(this, Observer { isSuccess ->
            if (isSuccess == true) {
                // 삭제가 성공적으로 완료된 후에 setResult와 finish를 호출
                setResult(RESULT_OK, Intent().putExtra("refresh", true))
                finish()
            }
        })

        binding.tvDelete.setOnClickListener {

            MaterialAlertDialogBuilder(this)
                .setTitle("게시글 삭제")
                .setMessage("정말로 이 게시글을 삭제하시겠습니까?")
                .setNegativeButton("취소") { _, _ -> }
                .setPositiveButton("확인") { dialog, which ->
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("게시글을 삭제 중입니다...")
                    progressDialog.setCancelable(false)
                    progressDialog.show()
                    boardViewModel.deleteBoards(boardList?.boardId ?: -1)
                    progressDialog.dismiss()
                }
                .setNegativeButton("취소", null)
                .show()

        }



        // 수정 버튼 클릭 리스너 설정
        binding.tvUpdate.setOnClickListener {
            currentBoard?.let { board ->
                val intent = Intent(this, BoardEditActivity::class.java).apply {
                    putExtra("clubId", club?.id)
                    putExtra("title", board.title)
                    putExtra("content", processedContent)
                    putExtra("boardId", board.boardId)
                }
                editActivityResultLauncher.launch(intent)
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setResult(RESULT_OK, Intent().putExtra("refresh", true))
                finish()
            }
        })



    }

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    private fun displayContent(content: String, board: Board) {
        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(this, "imageListener")

        Log.d("detail", "detail22 :CONTENT $content")
        val pattern = Pattern.compile("<img src=\"(.*?)\"")
        val matcher = pattern.matcher(content)

        var result = content
        while (matcher.find()) {
            val contentUrl = matcher.group(1) // content:// 경로

            // URI 디코딩
            val decodedContentUrl = URLDecoder.decode(contentUrl, "UTF-8")

            // 디코딩된 파일 이름 추출
            val fileName = decodedContentUrl.substringAfterLast("/")

//            val fileName = contentUrl.substringAfterLast("/")
            val imgSrc = board.imgSrcs.find { it.imgSrc.contains(fileName) }?.imgSrc

            if (imgSrc != null) {
                result = result.replace(contentUrl, imgSrc, ignoreCase = false)
                Log.d("detail", "detail22 :contentUrl $contentUrl")
                Log.d("detail", "detail22 :imgSrc $imgSrc")
            }
        }

        val js = """
        <script type="text/javascript">
            function registerImageClickListener() {
                var imgs = document.getElementsByTagName('img');
                for (var i = 0; i < imgs.length; i++) {
                    imgs[i].onclick = function() {
                        window.imageListener.onImageClick(this.src);
                    }
                }
            }
            document.addEventListener("DOMContentLoaded", registerImageClickListener);
        </script>
    """
        result += js

        Log.d("detail", "detail22 :result $result")
        processedContent = result
        webView.loadData(result, "text/html", "UTF-8")
    }


        private fun checkPermissions(userId: String?) {
        val boardUserId = boardList?.userId
        val clubManagerId = club?.managerId

        if (userId == boardUserId.toString() || userId == clubManagerId) {
            binding.tvUpdate.visibility = View.VISIBLE
            binding.tvDelete.visibility = View.VISIBLE
        } else {
            binding.tvUpdate.visibility = View.GONE
            binding.tvDelete.visibility = View.GONE
        }
    }

    private fun refreshData() {
        val boardId = boardList?.boardId ?: -1
        boardViewModel.getBoards(boardId)
    }

    @android.webkit.JavascriptInterface
    fun onImageClick(imgSrc: String) {
        Log.d("WebView", "Image clicked: $imgSrc")
        val intent = Intent(this, PhotoViewerActivity::class.java).apply {
            putExtra("IMAGE_URL", imgSrc)
        }
        startActivity(intent)
    }

//    override fun onBackPressed() {
//        super.onBackPressed()
//        setResult(RESULT_OK, Intent().putExtra("refresh", true))
//        finish()
//    }

    override fun onPause() {
        if (isFinishing && goToMain) {
            clubViewModel.getClubById(club!!.id)
            clubViewModel.club.observe(this@BoardDetailActivity) {
                val intent = Intent(this, BoardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("club", it)
                    putExtra("back", true)
                }
                startActivity(intent)
            }
        }
        super.onPause()
    }
}