package com.moblevel;

import org.junit.Test;
import static org.junit.Assert.*;

public class VersionCompareTest {

    @Test
    public void testHigherCoreIsNewer() {
        assertTrue(VersionCompare.isNewer("2.2.0", "2.1.0"));
        assertTrue(VersionCompare.isNewer("3.0.0", "2.9.9"));
        assertTrue(VersionCompare.isNewer("2.1.1", "2.1.0"));
    }

    @Test
    public void testSameOrOlderIsNotNewer() {
        assertFalse(VersionCompare.isNewer("2.1.0", "2.1.0"));
        assertFalse(VersionCompare.isNewer("2.0.9", "2.1.0"));
    }

    @Test
    public void testStableBeatsPrerelease() {
        assertTrue(VersionCompare.isNewer("2.1.0", "2.1.0-beta"));
        assertFalse(VersionCompare.isNewer("2.1.0-beta", "2.1.0"));
    }

    @Test
    public void testGameVersionSuffixIsNotPartOfTheCore() {
        // Modrinth versions are normally published as "<mod>+<game>". The suffix must
        // not leak into the comparison: "2.1.0+26.2" once parsed as 2.1.26, so every
        // client running 2.1.0 was told an update was available on every login.
        assertFalse(VersionCompare.isNewer("2.1.0+26.2", "2.1.0"));
        assertFalse(VersionCompare.isNewer("2.1.0+1.21.8", "2.1.0"));
        assertFalse(VersionCompare.isNewer("2.1.0+1.20.1", "2.1.0"));
    }

    @Test
    public void testSuffixedVersionsStillCompareByCore() {
        assertTrue(VersionCompare.isNewer("2.2.0+26.2", "2.1.0+26.2"));
        assertFalse(VersionCompare.isNewer("2.1.0+26.2", "2.2.0+26.2"));
    }
}
