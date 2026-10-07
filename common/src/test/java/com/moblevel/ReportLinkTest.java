package com.moblevel;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReportLinkTest {

    private static Map<String, String> diag() {
        Map<String, String> d = new LinkedHashMap<>();
        d.put("MobLevel", "2.1.0");
        d.put("Minecraft", "26.2");
        return d;
    }

    @Test
    public void testPointsAtTheIssueForm() {
        String url = ReportLink.build("cows are invisible", diag());
        assertTrue(url, url.startsWith("https://github.com/facusora01/MobLevel/issues/new?"));
        assertTrue(url, url.contains("title="));
        assertTrue(url, url.contains("body="));
    }

    @Test
    public void testDiagnosticsRideAlong() {
        String url = ReportLink.build("bug", diag());
        assertTrue(url, url.contains("MobLevel") && url.contains("2.1.0"));
        assertTrue(url, url.contains("Minecraft") && url.contains("26.2"));
    }

    @Test
    public void testEverythingIsUrlEncoded() {
        // A player can type anything; none of it may break out of the query string.
        String url = ReportLink.build("a&b=c #1 ñ <script>", diag());
        int query = url.indexOf('?');
        String rest = url.substring(query + 1);
        assertFalse("raw # would truncate the URL at the fragment", rest.contains("#"));
        assertFalse("raw < must not survive", rest.contains("<"));
        // & and = may only appear as the separators the builder itself wrote.
        assertEquals(1, rest.split("&", -1).length - 1);
    }

    @Test
    public void testLongMessageIsTruncated() {
        String url = ReportLink.build("x".repeat(5000), diag());
        assertTrue("URL should stay well under a browser limit: " + url.length(), url.length() < 4000);
    }

    @Test
    public void testTitleUsesTheFirstLineOnly() {
        assertEquals("[In-game report] first", ReportLink.title("first\nsecond"));
    }

    @Test
    public void testTitleFallsBackWhenEmpty() {
        assertEquals("[In-game report] report", ReportLink.title(""));
    }

    @Test
    public void testTitleIsCapped() {
        String title = ReportLink.title("y".repeat(200));
        assertTrue(title, title.length() < 100);
    }

    @Test
    public void testEmptyMessageStillBuilds() {
        String url = ReportLink.build("   ", diag());
        assertTrue(url, url.startsWith("https://github.com/"));
    }
}
