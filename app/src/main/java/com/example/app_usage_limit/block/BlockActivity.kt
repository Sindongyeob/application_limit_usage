package com.example.app_usage_limit.block

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.app_usage_limit.BuildConfig
import com.example.app_usage_limit.R
import com.example.app_usage_limit.quiz.QuizManager
import com.example.app_usage_limit.util.AppUsageManager
import com.example.app_usage_limit.util.UnblockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlockActivity : AppCompatActivity() {

    private lateinit var questionTextView: TextView
    private lateinit var answerEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var hintButton: Button
    private lateinit var infoTextView: TextView
    private lateinit var loadingBar: ProgressBar

    private lateinit var packageName: String
    private lateinit var subject: String
    private lateinit var correctAnswer: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block)

        questionTextView = findViewById(R.id.quiz_question)
        answerEditText = findViewById(R.id.quiz_answer)
        submitButton = findViewById(R.id.btn_submit)
        hintButton = findViewById(R.id.btn_hint)
        infoTextView = findViewById(R.id.info_text)
        loadingBar = findViewById(R.id.loading_bar)

        packageName = intent.getStringExtra("PACKAGE_NAME") ?: ""
        subject = intent.getStringExtra("QUIZ_SUBJECT") ?: ""

        loadQuiz()

        submitButton.setOnClickListener {
            val userAnswer = answerEditText.text.toString().trim()
            if (userAnswer.equals(correctAnswer, ignoreCase = true)) {
                Toast.makeText(this, "정답입니다! 앱 제한이 해제됩니다.", Toast.LENGTH_SHORT).show()

                // ⬇️ 앱 차단 해제
                UnblockManager.unblockApp(this, packageName)

                // ✅ 사용 시간 및 실행 횟수 초기화 추가
                AppUsageManager.resetUsageTime(this, packageName)

                // ⬇️ 원래 앱 실행
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                launchIntent?.let {
                    startActivity(it)
                }

                finish()
            } else {
                infoTextView.text = "오답입니다."
            }
        }

        // 현재 QuizManager는 힌트를 반환하지 않기 때문에 버튼은 비활성화
        hintButton.visibility = View.GONE
    }

    private fun loadQuiz() {
        loadingBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = BuildConfig.OPENAI_API_KEY // 또는 안전하게 불러오기
                val (question, answer) = QuizManager.generateQuiz(apiKey, subject)
                correctAnswer = answer

                withContext(Dispatchers.Main) {
                    loadingBar.visibility = View.GONE
                    questionTextView.text = question
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingBar.visibility = View.GONE
                    questionTextView.text = "퀴즈를 불러오지 못했습니다."
                    Toast.makeText(this@BlockActivity, "오류: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
