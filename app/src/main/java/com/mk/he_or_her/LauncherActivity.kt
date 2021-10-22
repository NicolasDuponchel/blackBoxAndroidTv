package com.mk.he_or_her

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_launcher.*

class LauncherActivity : Activity() {

    private var questionCount = 5
        set(value) {
            field = value
            updateQuestionCount(value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        updateQuestionCount()
    }

    fun startSession(view: View) = startActivity(
        Intent(this, QuestionActivity::class.java).apply {
            putExtra(QuestionActivity.EXTRA_SESSION_QUESTIONS_COUNT, questionCount)
        }
    )

    private fun updateQuestionCount(count: Int = questionCount) {
        questionsCount.text = "$count question${if (count > 1) "s" else ""}"
    }

    fun incrementCount(view: View) {
        questionCount = questionCount.plus(1).coerceAtMost(50)
    }

    fun decrementCount(view: View) {
        questionCount = questionCount.minus(1).coerceAtLeast(1)
    }

}