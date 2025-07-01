package dev.drawethree.xprison.utils.task;

import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;

import java.util.concurrent.TimeUnit;

public final class RepeatingTask implements Runnable {

    private final Runnable taskAction;
    private final long initialDelay;
    private final TimeUnit initialDelayUnit;
    private final long interval;
    private final TimeUnit intervalUnit;
    private Task task;

    private RepeatingTask(Runnable taskAction, long initialDelay, TimeUnit initialDelayUnit, long interval, TimeUnit intervalUnit) {
        this.taskAction = taskAction;
        this.initialDelay = initialDelay;
        this.initialDelayUnit = initialDelayUnit;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
    }

    @Override
    public void run() {
        this.taskAction.run();
    }

    public void start() {
        stop();
        this.task = Schedulers.async().runRepeating(this, initialDelay, initialDelayUnit, interval, intervalUnit);
    }

    public void stop() {
        if (task != null) {
            task.stop();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Runnable taskAction;
        private long initialDelay = 0;
        private TimeUnit initialDelayUnit = TimeUnit.SECONDS;
        private long interval = 1;
        private TimeUnit intervalUnit = TimeUnit.MINUTES;

        public Builder task(Runnable taskAction) {
            this.taskAction = taskAction;
            return this;
        }

        public Builder initialDelay(long initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        public Builder initialDelayUnit(TimeUnit unit) {
            this.initialDelayUnit = unit;
            return this;
        }

        public Builder interval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder intervalUnit(TimeUnit unit) {
            this.intervalUnit = unit;
            return this;
        }

        public RepeatingTask build() {
            if (taskAction == null) {
                throw new IllegalStateException("Task action must be provided.");
            }
            return new RepeatingTask(taskAction, initialDelay, initialDelayUnit, interval, intervalUnit);
        }
    }
}
