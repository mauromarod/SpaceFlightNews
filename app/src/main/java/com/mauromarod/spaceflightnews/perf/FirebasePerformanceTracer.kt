package com.mauromarod.spaceflightnews.perf

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.mauromarod.spaceflightnews.core.domain.repository.AppTrace
import com.mauromarod.spaceflightnews.core.domain.repository.PerformanceTracer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePerformanceTracer @Inject constructor(
    private val performance: FirebasePerformance,
) : PerformanceTracer {

    override fun newTrace(name: String): AppTrace =
        FirebaseAppTrace(performance.newTrace(name))
}

private class FirebaseAppTrace(private val trace: Trace) : AppTrace {
    override fun start() = trace.start()
    override fun stop() = trace.stop()
    override fun putAttribute(key: String, value: String) = trace.putAttribute(key, value)
    override fun putMetric(key: String, value: Long) = trace.putMetric(key, value)
}
