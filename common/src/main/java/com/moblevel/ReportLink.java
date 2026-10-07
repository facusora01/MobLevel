package com.moblevel;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Builds the prefilled "new issue" link that /moblevel report hands the player.
 *
 * Nothing is sent from the game: the player opens the link and submits it themselves,
 * so there is no endpoint to run, no secret to embed in the jar, and the report is
 * only ever filed by someone who saw exactly what it says.
 *
 * Free of Minecraft types so it can be unit tested, like the other helpers.
 */
public final class ReportLink {

    private static final String ISSUE_URL = "https://github.com/facusora01/MobLevel/issues/new";

    /** A browser will carry far more, but a wall of text makes a worse report, not a better one. */
    static final int MAX_MESSAGE = 500;
    private static final int MAX_TITLE = 70;

    private ReportLink() {
    }

    /**
     * @param message     what the player typed
     * @param diagnostics ordered label -> value pairs appended under the message
     */
    public static String build(String message, Map<String, String> diagnostics) {
        String text = clean(message);
        StringBuilder body = new StringBuilder();
        body.append(text.isEmpty() ? "_(no description given)_" : text);
        body.append("\n\n---\n\nCollected automatically:\n\n");
        for (Map.Entry<String, String> entry : diagnostics.entrySet()) {
            body.append("- ").append(entry.getKey()).append(": `").append(entry.getValue()).append("`\n");
        }
        return ISSUE_URL + "?title=" + encode(title(text)) + "&body=" + encode(body.toString());
    }

    static String title(String message) {
        String firstLine = message.isEmpty() ? "report" : message.split("\\R", 2)[0].trim();
        if (firstLine.isEmpty()) {
            firstLine = "report";
        }
        if (firstLine.length() > MAX_TITLE) {
            firstLine = firstLine.substring(0, MAX_TITLE - 1).trim() + "…";
        }
        return "[In-game report] " + firstLine;
    }

    // Commands arrive as a single line, but trim and cap it so one player cannot paste a book.
    static String clean(String message) {
        if (message == null) {
            return "";
        }
        String text = message.trim();
        if (text.length() > MAX_MESSAGE) {
            text = text.substring(0, MAX_MESSAGE - 1).trim() + "…";
        }
        return text;
    }

    private static String encode(String value) {
        // URLEncoder is form encoding: it turns a space into +, which is right for a query
        // string, but leaves ~ and * alone. GitHub accepts both, so no further escaping.
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
