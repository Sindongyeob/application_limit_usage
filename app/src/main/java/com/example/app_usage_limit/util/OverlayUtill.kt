package com.example.app_usage_limit.util

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.*
import android.widget.Button
import android.widget.TextView
import com.example.app_usage_limit.R


object OverlayUtil {

    fun showOverlayView(context: Context, usedSeconds: Long) {
        if (!Settings.canDrawOverlays(context)) return

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(context)
        val overlayView = inflater.inflate(R.layout.overlay_block_screen, null)

        // 누적 사용 시간 분, 초 계산
        val minutes = usedSeconds / 60
        val seconds = usedSeconds % 60

        // 메시지 표시 텍스트뷰 찾기
        val messageTextView = overlayView.findViewById<TextView>(R.id.message_text)
        messageTextView.text = "${minutes}분 ${seconds}초 사용하셨습니다."

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )


        wm.addView(overlayView, params)


        //확인 누르면 제거
        val confirmButton = overlayView.findViewById<Button>(R.id.confirm_button)
        confirmButton.setOnClickListener {
            try {
                wm.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}