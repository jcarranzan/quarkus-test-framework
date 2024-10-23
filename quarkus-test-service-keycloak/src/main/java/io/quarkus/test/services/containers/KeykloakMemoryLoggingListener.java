package io.quarkus.test.services.containers;

import java.io.File;
import java.lang.management.ManagementFactory;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import com.sun.management.OperatingSystemMXBean;

public class KeykloakMemoryLoggingListener implements TestExecutionListener {

    private static final long BYTES_IN_KB = 1024;
    private static final long BYTES_IN_MB = BYTES_IN_KB * 1024;

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        logMemoryUsage("Test plan execution started");
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        logMemoryUsage("Test plan execution finished");
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        logMemoryUsage("Execution started: " + testIdentifier.getDisplayName());
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        logMemoryUsage("Execution finished: " + testIdentifier.getDisplayName());
    }

    private void logMemoryUsage(String message) {
        try {
            // JVM Memory Info
            Runtime runtime = Runtime.getRuntime();
            long freeMemory = runtime.freeMemory() / BYTES_IN_MB;
            long totalMemory = runtime.totalMemory() / BYTES_IN_MB;
            long maxMemory = runtime.maxMemory() / BYTES_IN_MB;

            System.out.println("[" + message + "]");
            System.out.println("=== JVM Memory Info ===");
            System.out.println("Free Memory: " + freeMemory + " MB");
            System.out.println("Total Memory: " + totalMemory + " MB");
            System.out.println("Max Memory: " + maxMemory + " MB");

            // System Memory Info
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            long totalPhysicalMemorySize = osBean.getTotalPhysicalMemorySize() / BYTES_IN_MB;
            long freePhysicalMemorySize = osBean.getFreePhysicalMemorySize() / BYTES_IN_MB;

            System.out.println("=== System Memory Info ===");
            System.out.println("Total Physical Memory: " + totalPhysicalMemorySize + " MB");
            System.out.println("Free Physical Memory: " + freePhysicalMemorySize + " MB");

            // Disk Space Info
            File root = new File("/");
            long totalDiskSpace = root.getTotalSpace() / BYTES_IN_MB;
            long freeDiskSpace = root.getFreeSpace() / BYTES_IN_MB;

            System.out.println("=== Disk Space Info ===");
            System.out.println("Total Disk Space: " + totalDiskSpace + " MB");
            System.out.println("Free Disk Space: " + freeDiskSpace + " MB");
            System.out.println("--------------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
