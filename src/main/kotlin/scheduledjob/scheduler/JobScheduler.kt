package com.ruiniles.scheduledjob.scheduler

import com.ruiniles.scheduledjob.job.Job

abstract class JobScheduler {
    lateinit var job: Job

    fun register(job: Job) {
        this.job = job
    }

    abstract fun start()
}
