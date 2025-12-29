package com.ruiniles.support

import com.ruiniles.scheduledjob.scheduler.JobScheduler

class TestJobScheduler : JobScheduler() {
    override fun start() {}

    fun runJob() {
        job.run()
    }
}
