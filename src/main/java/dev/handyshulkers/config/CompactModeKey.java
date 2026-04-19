package dev.handyshulkers.config;

/**
 * Modifier key that toggles compact-mode tooltip view while held.
 *
 * NONE disables the hotkey entirely: compact mode is controlled solely by
 * {@code defaultCompactMode} and cannot be toggled from the inventory.
 */
public enum CompactModeKey {
	NONE,
	SHIFT,
	CTRL,
	ALT
}
