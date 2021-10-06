package com.baosystems.icrc.psm.service.scheduler

import io.reactivex.schedulers.TestScheduler

class TestSchedulerProvider(private val scheduler: TestScheduler):
    BaseSchedulerProvider {
    override fun computation() = scheduler
    override fun io() = scheduler
    override fun ui() = scheduler
}