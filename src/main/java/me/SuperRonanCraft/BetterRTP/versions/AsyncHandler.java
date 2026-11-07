package me.SuperRonanCraft.BetterRTP.versions;

import com.tcoded.folialib.impl.ServerImplementation;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import org.bukkit.entity.Entity;

public class AsyncHandler {

    public static void async(Runnable runnable) {
        getFolia().runAsync(task -> runnable.run());
    }

    public static void sync(Runnable runnable) {
        getFolia().runNextTick(task -> runnable.run());
    }

    /** Runs on the thread that owns {@code location} (Folia: its region; Paper: the main thread next tick). */
    public static void syncAtLocation(org.bukkit.Location location, Runnable runnable) {
        getFolia().runAtLocation(location, task -> runnable.run());
    }

    /** Paper/Folia World#getChunkAtAsync(Location); absent on Spigot (the plugin compiles against spigot-api 1.8). */
    private static final java.lang.reflect.Method CHUNK_AT_ASYNC = findChunkAtAsync();

    private static java.lang.reflect.Method findChunkAtAsync() {
        try {
            return org.bukkit.World.class.getMethod("getChunkAtAsync", org.bukkit.Location.class);
        } catch (NoSuchMethodException spigot) {
            return null;
        }
    }

    /**
     * Loads the chunk at {@code location} without blocking. Calls World#getChunkAtAsync directly: the bundled PaperLib
     * no longer recognises Paper/Folia 26.x and falls back to a synchronous load, which Folia rejects off the owning
     * region. The future may complete on any thread, so callers hop with {@link #syncAtLocation} before reading blocks.
     */
    @SuppressWarnings("unchecked")
    public static java.util.concurrent.CompletableFuture<org.bukkit.Chunk> chunkAt(org.bukkit.Location location) {
        if (CHUNK_AT_ASYNC != null) {
            try {
                return (java.util.concurrent.CompletableFuture<org.bukkit.Chunk>) CHUNK_AT_ASYNC.invoke(location.getWorld(), location);
            } catch (ReflectiveOperationException fallback) {
                // fall through to PaperLib
            }
        }
        return io.papermc.lib.PaperLib.getChunkAtAsync(location);
    }

    /** Async teleport through FoliaLib (Entity#teleportAsync on Paper/Folia; PaperLib's fallback teleports synchronously). */
    public static java.util.concurrent.CompletableFuture<Boolean> teleport(Entity entity, org.bukkit.Location location) {
        return getFolia().teleportAsync(entity, location);
    }

    public static void syncAtEntity(Entity entity, Runnable runnable) {
        getFolia().runAtEntity(entity, task -> runnable.run());
    }

    public static WrappedTask asyncLater(Runnable runnable, long ticks) {
        return getFolia().runLaterAsync(runnable, ticks);
    }
    public static WrappedTask syncLater(Runnable runnable, long ticks) {
        return getFolia().runLater(runnable, ticks);
    }

    private static ServerImplementation getFolia() {
        return BetterRTP.getInstance().getFoliaHandler().get();
    }
}
