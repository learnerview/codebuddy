package org.codebuddy.core.models;

public class Platform {
    public static final Platform LEETCODE = new Platform("LeetCode");
    public static final Platform CODEFORCES = new Platform("Codeforces");
    public static final Platform CODECHEF = new Platform("CodeChef");
    public static final Platform HACKERRANK = new Platform("HackerRank");
    public static final Platform OTHER = new Platform("Other");

    private final String displayName;

    private Platform(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Platform[] values() {
        return new Platform[] { LEETCODE, CODEFORCES, CODECHEF, HACKERRANK, OTHER };
    }

    public static Platform valueOf(String name) {
        for (Platform p : values()) {
            if (p.displayName.equalsIgnoreCase(name) || p.name().equalsIgnoreCase(name)) {
                return p;
            }
        }
        throw new IllegalArgumentException("No enum constant " + name);
    }

    public String name() {
        return displayName.toUpperCase().replace(" ", "_");
    }
}