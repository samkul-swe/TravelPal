package org.kulkarni_sampada.travelpal.models;

import androidx.annotation.NonNull;

public class Accessibility {
    private boolean wheelchairAccessible;
    private DifficultyLevel difficultyLevel;
    private String accessibilityNotes;

    // Difficulty Level Enum
    public enum DifficultyLevel {
        EASY("easy", "Easy", "Suitable for all fitness levels"),
        MODERATE("moderate", "Moderate", "Requires some physical activity"),
        CHALLENGING("challenging", "Challenging", "Requires good fitness level");

        private final String value;
        private final String displayName;
        private final String description;

        DifficultyLevel(String value, String displayName, String description) {
            this.value = value;
            this.displayName = displayName;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public static DifficultyLevel fromValue(String value) {
            for (DifficultyLevel level : DifficultyLevel.values()) {
                if (level.value.equalsIgnoreCase(value)) {
                    return level;
                }
            }
            return EASY; // Default
        }
    }

    // Constructors
    public Accessibility() {
        this.wheelchairAccessible = false;
        this.difficultyLevel = DifficultyLevel.EASY;
    }

    public Accessibility(boolean wheelchairAccessible, DifficultyLevel difficultyLevel) {
        this.wheelchairAccessible = wheelchairAccessible;
        this.difficultyLevel = difficultyLevel;
    }

    // Getters and Setters
    public boolean isWheelchairAccessible() {
        return wheelchairAccessible;
    }

    public void setWheelchairAccessible(boolean wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getAccessibilityNotes() {
        return accessibilityNotes;
    }

    public void setAccessibilityNotes(String accessibilityNotes) {
        this.accessibilityNotes = accessibilityNotes;
    }

    // Helper methods
    public String getWheelchairAccessibilityIcon() {
        return wheelchairAccessible ? "♿" : "⚠️";
    }

    public String getWheelchairAccessibilityText() {
        return wheelchairAccessible ? "Wheelchair Accessible" : "Not Wheelchair Accessible";
    }

    public String getDifficultyIcon() {
        switch (difficultyLevel) {
            case EASY:
                return "🟢";
            case MODERATE:
                return "🟡";
            case CHALLENGING:
                return "🔴";
            default:
                return "⚪";
        }
    }

    public String getFormattedDifficulty() {
        return String.format("%s %s", getDifficultyIcon(), difficultyLevel.getDisplayName());
    }

    public String getAccessibilitySummary() {
        StringBuilder summary = new StringBuilder();

        summary.append(getWheelchairAccessibilityIcon())
                .append(" ")
                .append(getWheelchairAccessibilityText());

        summary.append(" | ")
                .append(getFormattedDifficulty());

        if (accessibilityNotes != null && !accessibilityNotes.isEmpty()) {
            summary.append("\nNote: ").append(accessibilityNotes);
        }

        return summary.toString();
    }

    public boolean isSuitableFor(DifficultyLevel maxDifficulty) {
        if (maxDifficulty == null) return true;

        // Easy < Moderate < Challenging
        return difficultyLevel.ordinal() <= maxDifficulty.ordinal();
    }

    @NonNull
    @Override
    public String toString() {
        return "Accessibility{" +
                "wheelchairAccessible=" + wheelchairAccessible +
                ", difficultyLevel=" + difficultyLevel.getDisplayName() +
                (accessibilityNotes != null ? ", notes='" + accessibilityNotes + '\'' : "") +
                '}';
    }
}
