package com.moblevel;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.SharedConstants;
import com.moblevel.platform.Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

// Asks Modrinth for the newest release matching this loader and MC version and,
// if it is newer than the installed one, tells the player in chat with a
// clickable download link. Runs once per game session, off the main thread,
// and stays completely silent on any failure (offline, API change, timeout).
// Each loader calls check() when the client joins a world.
public class UpdateChecker {
    private static final String PROJECT_PAGE = "https://modrinth.com/mod/mob-level/versions";

    private static final AtomicBoolean CHECKED = new AtomicBoolean(false);

    public static void check() {
        if (!CHECKED.compareAndSet(false, true)) return;

        String current = Services.PLATFORM.modVersion();
        String apiUrl = "https://api.modrinth.com/v2/project/mob-level/version"
            + "?loaders=%5B%22" + Services.PLATFORM.loaderName() + "%22%5D"
            + "&game_versions=%5B%22" + SharedConstants.getCurrentVersion().getName() + "%22%5D";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            // Modrinth API guidelines ask for an identifying User-Agent.
            .header("User-Agent", "facusora01/MobLevel/" + current)
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();

        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {
                if (response.statusCode() != 200) return;
                try {
                    // Modrinth returns versions newest-first.
                    JsonArray versions = JsonParser.parseString(response.body()).getAsJsonArray();
                    if (versions.isEmpty()) return;
                    String latest = versions.get(0).getAsJsonObject().get("version_number").getAsString();
                    if (VersionCompare.isNewer(latest, current)) {
                        Minecraft.getInstance().execute(() -> notifyPlayer(latest, current));
                    }
                } catch (Exception ignored) {
                    // Never break the game over an update notice.
                }
            });
    }

    private static void notifyPlayer(String latest, String current) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        Component message = Component.literal("[MobLevel] ").withStyle(ChatFormatting.GOLD)
            .append(Component.literal("Update available: " + latest
                + " (you have " + current + "). ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("[Download]").withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.OpenUrl(java.net.URI.create(PROJECT_PAGE)))));

        player.displayClientMessage(message, false);
    }
}
