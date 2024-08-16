package com.orm.ui.club

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.orm.R
import com.orm.databinding.ActivityClubBinding
import com.orm.ui.MainActivity
import com.orm.ui.fragment.TabLayoutFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClubActivity : AppCompatActivity() {
    private val binding: ActivityClubBinding by lazy {
        ActivityClubBinding.inflate(layoutInflater)
    }

    private val goToMain: Boolean by lazy {
        intent.getBooleanExtra("back", false)
    }

    private val createClubLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val clubCreated = data?.getBooleanExtra("clubCreated", false) ?: false
                if (clubCreated) {
                    refreshTabLayoutFragment()
                }
            }
        }

    private var selectedTab: Int = 0;
    private var savedState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        savedState = savedInstanceState

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    createClubLauncher.launch(Intent(this, ClubEditActivity::class.java))
                    true
                }

                else -> false
            }
        }

        binding.tvThumbnail.setOnClickListener {
            startActivity(Intent(this, ClubSearchActivity::class.java))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedTab = getSelectedTabFromFragment()
        outState.putInt("selectedTab", selectedTab)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        selectedTab = savedInstanceState.getInt("selectedTab")
    }

    override fun onResume() {
        super.onResume()
        savedState?.let {
            selectedTab = it.getInt("selectedTab")
            Log.d("clubTest", "in resume ${selectedTab}")
        }
        Log.d("clubTest", "savedState ${savedState}")
        refreshTabLayoutFragment(selectedTab)
    }

    private fun refreshTabLayoutFragment(targetTabIndex: Int = 0) {
        Log.d("clubTest", "refresh")
        val newFragment = TabLayoutFragment()
        supportFragmentManager.beginTransaction()
            .replace(binding.info.id, newFragment)
            .commitNow()
        newFragment.selectTab(targetTabIndex)
    }

    private fun getSelectedTabFromFragment(): Int {
        val fragment =
            supportFragmentManager.findFragmentById(binding.info.id) as? TabLayoutFragment
        return fragment?.getSelectedTabIndex() ?: -1
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing && goToMain) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}