package com.example.app_usage_limit.quiz

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.app_usage_limit.BuildConfig
import com.example.app_usage_limit.R
import com.example.app_usage_limit.util.AppLimitStorage
import com.example.app_usage_limit.quiz.QuizManager
import com.example.app_usage_limit.util.UnblockManager
import kotlinx.coroutines.*

class QuizActivity : AppCompatActivity() {

    private lateinit var blockedApp: String
    private lateinit var subject: String

    private lateinit var questionTextView: TextView
    private lateinit var answerEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var retryButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var progressBar: ProgressBar

    private var correctAnswer: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        blockedApp = intent.getStringExtra("blocked_app") ?: ""
        subject = intent.getStringExtra("QUIZ_SUBJECT") ?: "과학"

        questionTextView = findViewById(R.id.question_text)
        answerEditText = findViewById(R.id.answer_input)
        submitButton = findViewById(R.id.submit_button)
        retryButton = findViewById(R.id.retry_button)
        resultTextView = findViewById(R.id.result_text)
        progressBar = findViewById(R.id.loading_bar)

        setupListeners()
        startNewQuiz()
    }

    private fun setupListeners() {
        submitButton.setOnClickListener {
            val userAnswer = answerEditText.text.toString().trim()
            checkAnswer(userAnswer)
        }

        retryButton.setOnClickListener {
            startNewQuiz()
        }

        answerEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitButton.performClick()
                true
            } else false
        }
    }

    private fun startNewQuiz() {
        resetUI()
        generateQuiz(subject)
    }

    private fun resetUI() {
        resultTextView.text = ""
        answerEditText.text.clear()
        answerEditText.isEnabled = false
        submitButton.isEnabled = false
        retryButton.isEnabled = false
        retryButton.visibility = Button.GONE
        progressBar.visibility = ProgressBar.VISIBLE
    }

    private fun checkAnswer(userAnswer: String) {
        if (!submitButton.isEnabled) return

        if (userAnswer.equals(correctAnswer.trim(), ignoreCase = true)) {
            // 퀴즈 정답 처리
            UnblockManager.resetUnblockState(this, blockedApp)

            // 2. 제한 해제 상태로 마킹
            UnblockManager.unblockApp(this, blockedApp)

            // 3. 제한 정보 제거
            AppLimitStorage.removeLimitInfo(this, blockedApp)

            resultTextView.text = "정답입니다! 앱 사용이 다시 가능해졌습니다."
            answerEditText.isEnabled = false
            submitButton.isEnabled = false
            retryButton.isEnabled = false

            Toast.makeText(this, "$blockedApp 제한 해제됨", Toast.LENGTH_SHORT).show()
        } else {
            resultTextView.text = "틀렸습니다. 정답은: $correctAnswer"
            retryButton.visibility = Button.VISIBLE
            retryButton.isEnabled = true
        }
    }

    private fun generateQuiz(keyword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val (question, answer) = QuizManager.generateQuiz(BuildConfig.OPENAI_API_KEY, keyword)

                if (question.isBlank() || answer.isBlank()) {
                    throw IllegalStateException("퀴즈 데이터가 비어 있습니다.")
                }

                correctAnswer = answer

                withContext(Dispatchers.Main) {
                    questionTextView.text = question
                    progressBar.visibility = ProgressBar.GONE
                    submitButton.isEnabled = true
                    answerEditText.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e("QuizActivity", "퀴즈 생성 실패", e)
                withContext(Dispatchers.Main) {
                    questionTextView.text = "퀴즈 생성 실패"
                    resultTextView.text = ""
                    progressBar.visibility = ProgressBar.GONE
                    retryButton.visibility = Button.VISIBLE
                    retryButton.isEnabled = true
                    answerEditText.isEnabled = false

                    Toast.makeText(
                        this@QuizActivity,
                        "퀴즈를 불러오는 데 실패했습니다. 다시 시도하세요.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
