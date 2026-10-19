package com.moblevel;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.ClickEvent;

import java.net.URI;

// One jar covers several Minecraft releases, so the few calls whose signature changed between
// them go through reflection: a direct call would load fine here and throw NoSuchMethodError there.
public final class Compat {
    private Compat() {}

    // ClickEvent became an interface with record subclasses in 1.21.5.
    public static ClickEvent openUrl(String url) {
        try {
            return (ClickEvent) Class.forName("net.minecraft.network.chat.ClickEvent$OpenUrl")
                .getConstructor(URI.class).newInstance(URI.create(url));
        } catch (ReflectiveOperationException e) {
            return new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        }
    }

    // WorldVersion.getName() was renamed name() in 1.21.6.
    public static String gameVersion() {
        Object version = SharedConstants.getCurrentVersion();
        try {
            return (String) version.getClass().getMethod("name").invoke(version);
        } catch (ReflectiveOperationException e) {
            return SharedConstants.getCurrentVersion().getName();
        }
    }
}
