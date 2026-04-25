package entities;

public enum ReactionType {
    LIKE("👍"),
    LOVE("❤"),
    HAHA("😂"),
    WOW("😮"),
    SAD("😢");

    private final String emoji;

    ReactionType(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }

    /** Safe parse — returns null instead of throwing if value is unknown. */
    public static ReactionType fromString(String value) {
        if (value == null) return null;
        for (ReactionType t : values()) {
            if (t.name().equalsIgnoreCase(value)) return t;
        }
        return null;
    }
}
