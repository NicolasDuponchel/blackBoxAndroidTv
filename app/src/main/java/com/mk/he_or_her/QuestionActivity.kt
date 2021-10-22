package com.mk.he_or_her

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_questions.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

@ExperimentalSerializationApi
class QuestionActivity : Activity() {

    private val disposables = CompositeDisposable()

    private var wantedQuestionCount = 0
    private var currentIndex = 0
    private var questions = emptyList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions)

        wantedQuestionCount = intent.getIntExtra(EXTRA_SESSION_QUESTIONS_COUNT, 5)
        service
            .questions()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { loader.isVisible = true }
            .doOnSuccess { loader.isVisible = false }
            .subscribeBy(
                onError = {
                    Log.e("QuestionActivity", "ERROR failed to get questions", it)
                    displayedQuestion.text = it.message
                },
                onSuccess = { questionsResult ->
                    Log.d("QuestionActivity", "SUCCESS result[${questionsResult.count()}]=$questionsResult")
                    wantedQuestionCount = wantedQuestionCount.coerceAtMost(questionsResult.count())
                    questions = questionsResult
                        .shuffled()
                        .take(wantedQuestionCount)
                        .map { it }
                        .plus("Un petit mot pour la fin ?")
                    Log.d("QuestionActivity", "questions=$questions")
                    updateUi()
                    nextButton.requestFocus()
                }
            )
            .addTo(disposables)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    fun previousButtonClicked(view: View) {
        currentIndex = currentIndex.minus(1).coerceAtLeast(0)
        updateUi()
    }

    fun nextButtonClicked(view: View) {
        if (currentIndex == questions.count() - 1) finish()
        else {
            currentIndex = currentIndex.plus(1).coerceAtMost(questions.count() - 1)
            updateUi()
        }
    }

    private fun updateUi() {
        previousButton.isVisible = currentIndex > 0
        nextButton.isVisible = true
        nextButton.text = resources.getString(if (currentIndex == questions.count() - 1) R.string.endSession else R.string.nextQuestion)
        displayedQuestion.text = questions[currentIndex]
    }

    companion object {
        const val EXTRA_SESSION_QUESTIONS_COUNT = "com.mk.extra.EXTRA_SESSION_QUESTIONS_COUNT"
    }

    private val service by lazy { JokeApiServiceFactory.createService() }

}


interface ApiService {
    @GET("raw")
    fun questions(): Single<Questions>
}

object JokeApiServiceFactory {
    @ExperimentalSerializationApi
    fun createService(): ApiService = Retrofit.Builder()
        .baseUrl("https://gist.githubusercontent.com/NicolasDuponchel/ec6c319bd1b0d686d7f8bbcf057ffa5e/")
        .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(ApiService::class.java)

}