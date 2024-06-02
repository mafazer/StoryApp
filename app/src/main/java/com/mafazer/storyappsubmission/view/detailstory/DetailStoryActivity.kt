package com.mafazer.storyappsubmission.view.detailstory

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.mafazer.storyappsubmission.R
import com.mafazer.storyappsubmission.databinding.ActivityDetailStoryBinding
import com.mafazer.storyappsubmission.data.Result
import com.mafazer.storyappsubmission.view.ViewModelFactory
import com.mafazer.storyappsubmission.view.formatDate

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding
    private lateinit var ivDetailPhoto: ImageView
    private lateinit var cardView: CardView
    private val viewModel: DetailStoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ivDetailPhoto = binding.ivDetailPhoto
        cardView = binding.detailCardView

        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        if (storyId != null) {
            viewModel.getDetailStory(storyId).observe(this) { result ->
                binding.apply {
                    when (result) {
                        is Result.Loading -> {
                            progressBar.visibility = View.VISIBLE
                            tvError.visibility = View.GONE
                            tvNoData.visibility = View.GONE
                        }

                        is Result.Success -> {
                            progressBar.visibility = View.GONE

                            val story = result.data
                            tvDetailName.text = story.name
                            tvDetailCreatedAt.text = getString(
                                R.string.posted_date_placeholder,
                                formatDate(story.createdAt)
                            )
                            tvDetailDescription.text = story.description

                            Glide.with(this@DetailStoryActivity)
                                .load(story.photoUrl)
                                .into(ivDetailPhoto)

                            startAnimation()
                        }

                        is Result.Error -> {
                            progressBar.visibility = View.GONE

                            tvError.visibility = View.VISIBLE
                            tvError.text = result.error
                        }

                        else -> {
                            tvNoData.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun startAnimation() {
        val fadeInAnimation = ObjectAnimator.ofFloat(ivDetailPhoto, View.ALPHA, 0f, 1f)
        fadeInAnimation.duration = 1000

        val cardViewAnimation = ObjectAnimator.ofFloat(cardView, View.ALPHA, 0f, 1f)
        cardViewAnimation.duration = 500
        cardViewAnimation.startDelay = 100

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(fadeInAnimation, cardViewAnimation)
        animatorSet.start()
    }

    companion object {
        const val EXTRA_STORY_ID = "extra_story_id"
    }
}