package com.example.submission.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.core.data.Resource
import com.example.core.ui.MovieAdapter
import com.example.submission.R
import com.example.submission.databinding.ActivityMainBinding
import com.example.submission.detail.DetailMovieActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val homeViewModel: HomeViewModel by viewModel()

    private lateinit var binding: ActivityMainBinding

    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var tvPowerStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val movieAdapter = MovieAdapter()
        movieAdapter.onItemClick = { selectedData ->
            val intent = Intent(this@MainActivity, DetailMovieActivity::class.java)
            intent.putExtra(DetailMovieActivity.EXTRA_DATA, selectedData)
            startActivity(intent)
        }

        homeViewModel.movie.observe(this) { movie ->
            movie?.let { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading(true)
                    is Resource.Success -> {
                        showLoading(false)
                        resource.data?.let { movieAdapter.setListMovies(it) }
                    }

                    is Resource.Error -> {
                        showLoading(false)
                        binding.viewError.root.visibility = View.VISIBLE
                        binding.viewError.tvError.text =
                            resource.message ?: getString(R.string.something_wrong)
                    }
                }
            }
        }

        with(binding.rvMovie) {
            setHasFixedSize(true)
            adapter = movieAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_favorite ->
                try {
                    moveToFavoriteActivity()
                } catch (e: Exception) {
                    Toast.makeText(this, "Module not found", Toast.LENGTH_SHORT).show()
                }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        registerBroadCastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
    }

    private fun moveToFavoriteActivity() {
        Intent(this, Class.forName("com.achmadsm.favorite.FavoriteActivity")).also {
            startActivity(it)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun registerBroadCastReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        tvPowerStatus.text = getString(R.string.power_connected)
                    }

                    Intent.ACTION_POWER_DISCONNECTED -> {
                        tvPowerStatus.text = getString(R.string.power_disconnected)
                    }
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(broadcastReceiver, intentFilter)
    }

}