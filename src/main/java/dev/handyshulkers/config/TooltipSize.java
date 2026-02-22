package dev.handyshulkers.config;

public enum TooltipSize {
	SMALL(16),
	MEDIUM(20),
	LARGE(24);

	public final int slotSize;

	TooltipSize(int slotSize) {
		this.slotSize = slotSize;
	}
}
