package com.mauromarod.spaceflightnews.core.domain.repository

interface PerformanceTracer {
    fun newTrace(name: String): AppTrace
}

interface AppTrace {
    fun start()
    fun stop()
    fun putAttribute(key: String, value: String)
    fun putMetric(key: String, value: Long)
}
