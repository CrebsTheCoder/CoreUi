package org.crebs.coreui.minestom;

import net.minestom.server.MinecraftServer;
import net.minestom.server.dialog.*;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerCustomClickEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.registry.HolderSet;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.crebs.coreui.CoreUiRuntime;
import org.crebs.coreui.menu.CoreMenu;
import org.crebs.coreui.platform.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class MinestomCoreUi implements AutoCloseable {
    private final Object lock = new Object();
    private final ThreadLocal<Boolean> inRuntime = ThreadLocal.withInitial(() -> false);
    private final EventNode<Event> events = EventNode.all("coreui");
    private final CoreUiRuntime runtime;
    private final Task expiry;
    private boolean closed;

    public MinestomCoreUi() {
        runtime = new CoreUiRuntime(new Platform(), Logger.getLogger("CoreUi"));
        events.addListener(PlayerCustomClickEvent.class, event -> {
            if (event.getKey().namespace().equals("coreui")) {
                execute(ui -> {
                    if (event.getPlayer().isOnline()) ui.handleClick(new MinestomViewer(event.getPlayer()), event.getKey());
                });
            }
        });
        events.addListener(PlayerDisconnectEvent.class,
            event -> execute(ui -> ui.disconnect(new MinestomViewer(event.getPlayer()))));
        MinecraftServer.getGlobalEventHandler().addChild(events);
        expiry = MinecraftServer.getSchedulerManager().buildTask(() -> run(runtime::expireSessions))
            .delay(TaskSchedule.tick(200)).repeat(TaskSchedule.tick(200)).schedule();
    }

    public CompletableFuture<Void> open(Player player, CoreMenu menu) {
        return execute(ui -> {
            if (player.isOnline()) ui.open(new MinestomViewer(player), menu);
        });
    }

    public CompletableFuture<Void> refresh(Player player) {
        return execute(ui -> {
            if (player.isOnline()) ui.refresh(new MinestomViewer(player));
        });
    }

    public CompletableFuture<Void> close(Player player) {
        return execute(ui -> ui.close(new MinestomViewer(player)));
    }

    /** Runs menu operations on the scheduler. Player world changes need the player's own scheduler. */
    public CompletableFuture<Void> execute(Consumer<CoreUiRuntime> action) {
        Objects.requireNonNull(action);
        CompletableFuture<Void> result = new CompletableFuture<>();
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            synchronized (lock) {
                if (closed) {
                    result.completeExceptionally(new IllegalStateException("CoreUi is closed"));
                    return;
                }
                try {
                    run(() -> action.accept(runtime));
                    result.complete(null);
                } catch (RuntimeException exception) {
                    result.completeExceptionally(exception);
                }
            }
        });
        return result;
    }

    private void run(Runnable action) {
        synchronized (lock) {
            if (closed) return;
            boolean previous = inRuntime.get();
            inRuntime.set(true);
            try { action.run(); }
            finally { inRuntime.set(previous); }
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            if (closed) return;
            expiry.cancel();
            MinecraftServer.getGlobalEventHandler().removeChild(events);
            try { run(runtime::close); }
            finally { closed = true; }
        }
    }

    private final class Platform implements MenuPlatform {
        public void show(MenuViewer viewer, RenderedMenu menu) {
            DialogMetadata metadata = new DialogMetadata(menu.title(), menu.externalTitle(), true, false,
                DialogAfterAction.NONE, List.of(new DialogBody.PlainMessage(menu.content(), 511)), List.of());
            viewer.unwrap(Player.class).showDialog(new Dialog.DialogList(metadata,
                new HolderSet.Direct<Dialog>(List.of()), null, 1, 1));
        }

        public void close(MenuViewer viewer) { viewer.unwrap(Player.class).closeDialog(); }
        public long tick(MenuViewer viewer) {
            var instance = viewer.unwrap(Player.class).getInstance();
            return instance == null ? 0 : instance.getWorldAge();
        }
        public void schedule(MenuViewer viewer, Runnable action, long delayTicks) {
            MinecraftServer.getSchedulerManager().buildTask(() -> run(action))
                .delay(TaskSchedule.tick(Math.toIntExact(delayTicks))).schedule();
        }
        public void requireThread() {
            if (!inRuntime.get()) throw new IllegalStateException("Use MinestomCoreUi.execute for menu operations");
        }
    }
}
