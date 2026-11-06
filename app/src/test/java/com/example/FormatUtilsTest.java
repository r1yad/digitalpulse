package com.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.utils.FormatUtils;

import org.junit.Test;

public class FormatUtilsTest {

    @Test
    public void testFormatDuration() {
        assertEquals("0m", FormatUtils.formatDuration(0));
        assertEquals("5m", FormatUtils.formatDuration(5 * 60 * 1000));
        assertEquals("2h 15m", FormatUtils.formatDuration((2 * 3600 + 15 * 60) * 1000));
    }

    @Test
    public void testFormatBytes() {
        assertEquals("500 B", FormatUtils.formatBytes(500));
        assertTrue(FormatUtils.formatBytes(1024).contains("KB"));
        assertTrue(FormatUtils.formatBytes(1024 * 1024 * 5).contains("MB"));
    }

    @Test
    public void testFormatSpeed() {
        String speed = FormatUtils.formatSpeed(1024 * 100);
        assertNotNull(speed);
        assertTrue(speed.endsWith("/s"));
    }
}
