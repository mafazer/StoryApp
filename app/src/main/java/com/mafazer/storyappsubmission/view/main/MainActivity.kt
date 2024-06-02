package com.mafazer.storyappsubmission.view.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.mafazer.storyappsubmission.view.maps.MapsActivity
import com.mafazer.storyappsubmission.R
import com.mafazer.storyappsubmission.databinding.ActivityMainBinding
import com.mafazer.storyappsubmission.view.adapter.LoadingStateAdapter
import com.mafazer.storyappsubmission.view.adapter.StoryListAdapter
import com.mafazer.storyappsubmission.view.ViewModelFactory
import com.mafazer.storyappsubmission.view.welcome.WelcomeActivity
import com.mafazer.storyappsubmission.view.addstory.AddStoryActivity
import com.mafazer.storyappsubmission.view.detailstory.DetailStoryActivity
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryListAdapter
    private lateinit var addStoryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupRecyclerView()

        addStoryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    storyAdapter.refresh()
                }
            }

        viewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                observeStories()
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshStories()
        }

        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            addStoryLauncher.launch(intent)
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.story_co)
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryListAdapter { storyId ->
            val intent = Intent(this, DetailStoryActivity::class.java)
            intent.putExtra(DetailStoryActivity.EXTRA_STORY_ID, storyId)
            startActivity(intent)
        }
        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter { storyAdapter.retry() }
            )
        }
    }

    private fun observeStories() {
        viewModel.stories.observe(this) { pagingData ->
            lifecycleScope.launch {
                storyAdapter.submitData(pagingData)
                binding.swipeRefreshLayout.isRefreshing = false

                binding.progressBar.visibility = View.GONE
                binding.tvNoData.visibility = View.GONE
            }
        }

        lifecycleScope.launch {
            storyAdapter.loadStateFlow.collect { loadState ->
                when (loadState.refresh) {
                    is LoadState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is LoadState.NotLoading -> {
                        binding.progressBar.visibility = View.GONE

                        if (storyAdapter.itemCount == 0) {
                            binding.tvNoData.visibility = View.VISIBLE
                        } else {
                            binding.tvNoData.visibility = View.GONE
                        }
                    }

                    is LoadState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        val errorMessage = (loadState.refresh as LoadState.Error).error.message
                        Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun refreshStories() {
        storyAdapter.refresh()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }

            R.id.action_logout -> {
                viewModel.logout()
                true
            }

            R.id.action_map -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStories()
    }
}