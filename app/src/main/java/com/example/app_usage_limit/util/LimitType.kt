package com.example.app_usage_limit.util

enum class LimitType {
    TIME_LIMIT,     // 사용 시간 기반 제한
    TIME_OF_DAY,    // 특정 시각 제한
    USAGE_COUNT     // 사용 횟수 제한
}
