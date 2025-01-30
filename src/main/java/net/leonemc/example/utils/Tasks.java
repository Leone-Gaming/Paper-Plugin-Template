package net.leonemc.example.utils;

import net.leonemc.example.ExamplePlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import java.util.concurrent.TimeUnit;

public class Tasks {

    public static void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(ExamplePlugin.getInstance(), runnable);
    }

    public static void runSync(Runnable runnable) {
        Bukkit.getScheduler().runTask(ExamplePlugin.getInstance(), runnable);
    }

    public static void runSyncLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(ExamplePlugin.getInstance(), runnable, delay);
    }

    public static void runSyncTimer(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(ExamplePlugin.getInstance(), runnable, delay, period);
    }

    public static void runSyncTimerAsync(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(ExamplePlugin.getInstance(), runnable, delay, period);
    }

    public static BukkitTask runTaskLater(Runnable run, long delay, TimeUnit unit) {
        return Bukkit.getScheduler().runTaskLater(ExamplePlugin.getInstance(), run, unit.toSeconds(delay) * 20L);
    }

    public static BukkitTask runTaskTimerAsync(Runnable run, long start, long repeat) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(ExamplePlugin.getInstance(), run, start, repeat);
    }

    public static int scheduleSyncRepeatingTask(Runnable run, long start, long repeat) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(ExamplePlugin.getInstance(), run, start, repeat);
    }

    public static void runAsyncLater(Runnable run, long after) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(ExamplePlugin.getInstance(), run, after);
    }
}
