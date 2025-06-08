package com.example.app_usage_limit.block

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.app_usage_limit.R

class SubjectInputActivity : AppCompatActivity() {

    private lateinit var subjectEditText: EditText
    private lateinit var proceedButton: Button
    private lateinit var packageName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_input)

        subjectEditText = findViewById(R.id.subject_input)
        proceedButton = findViewById(R.id.btn_proceed)

        packageName = intent.getStringExtra("PACKAGE_NAME") ?: ""

        proceedButton.setOnClickListener {
            val subject = subjectEditText.text.toString().trim()

            if (subject.isEmpty()) {
                Toast.makeText(this, "주제를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, BlockActivity::class.java).apply {
                putExtra("PACKAGE_NAME", packageName)
                putExtra("QUIZ_SUBJECT", subject)
            }
            startActivity(intent)
            finish()
        }
    }
}
