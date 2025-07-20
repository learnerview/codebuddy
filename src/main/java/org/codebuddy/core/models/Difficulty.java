package org.codebuddy.core.models;

public class Difficulty {
    public static final Difficulty EASY = new Difficulty("Easy");
    public static final Difficulty MEDIUM = new Difficulty("Medium");
    public static final Difficulty HARD = new Difficulty("Hard");

    private final String displayName;

    private Difficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Difficulty[] values() {
        return new Difficulty[] { EASY, MEDIUM, HARD };
    }

    public static Difficulty valueOf(String name) {
        for (Difficulty d : values()) {
            if (d.displayName.equalsIgnoreCase(name) || d.name().equalsIgnoreCase(name)) {
                return d;
            }
        }
        throw new IllegalArgumentException("No enum constant " + name);
    }

    public String name() {
        return displayName.toUpperCase();
    }
}