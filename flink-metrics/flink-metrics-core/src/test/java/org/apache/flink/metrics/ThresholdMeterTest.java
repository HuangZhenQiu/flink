package org.apache.flink.metrics;

import org.apache.flink.metrics.ThresholdMeter.ThresholdExceedException;
import org.apache.flink.util.TestLogger;

import org.junit.Test;

import java.time.Duration;

/** Test time stamp based threshold meter. */
public class ThresholdMeterTest extends TestLogger {

    @Test(expected = ThresholdExceedException.class)
    public void testMaximumFailureCheck() {
        ThresholdMeter rater = new ThresholdMeter(5, Duration.ofSeconds(10));

        for (int i = 0; i < 6; i++) {
            rater.markEvent();
        }

        rater.checkAgainstThreshold();
    }

    @Test(expected = ThresholdExceedException.class)
    public void testRateRecordMultipleEvents() throws InterruptedException {
        ThresholdMeter rater = new ThresholdMeter(5, Duration.ofMillis(500));

        for (int i = 0; i < 3; i++) {
            rater.markEvent(2);
            Thread.sleep(150);
        }

        rater.checkAgainstThreshold();
    }

    @Test
    public void testMovingRate() throws InterruptedException {
        ThresholdMeter rater = new ThresholdMeter(5, Duration.ofMillis(500));

        for (int i = 0; i < 6; i++) {
            rater.markEvent();
            Thread.sleep(150);
        }

        rater.checkAgainstThreshold();
    }
}
