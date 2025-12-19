package org.kulkarni_sampada.travelpal.models;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.List;

public class Activity {
    private String id;
    private String name;
    private ActivityCategory category;
    private String description;
    private Location location;
    private TimeSlot timeSlot;
    private Cost cost;
    private List<String> tags;
    private boolean weatherDependent;
    private boolean bookingRequired;
    private String bookingUrl;
    private boolean studentDiscountAvailable;
    private Double studentDiscountAmount;
    private Accessibility accessibility;
    private List<String> dietaryOptions;
    private String recommendedExperience; // e.g., "Best at sunset", "Book in advance"
    private boolean isTransportOption; // True if this is a travel option between places

    // State management (not stored in Firebase, calculated dynamically)
    private transient ActivityState state;
    private transient int travelTimeFromPrevious; // in minutes
    private transient boolean needsEarlyDeparture; // True if user needs to leave before trip start time

    // Activity Category Enum
    public enum ActivityCategory {
        OUTDOOR("outdoor", "Outdoor"),
        CULTURAL("cultural", "Cultural"),
        FOOD_LUNCH("food_lunch", "Lunch"),
        FOOD_DINNER("food_dinner", "Dinner"),
        ENTERTAINMENT("entertainment", "Entertainment"),
        SHOPPING("shopping", "Shopping"),
        NIGHTLIFE("nightlife", "Nightlife"),
        RELAXATION("relaxation", "Relaxation");

        private final String value;
        private final String displayName;

        ActivityCategory(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static ActivityCategory fromValue(String value) {
            for (ActivityCategory category : ActivityCategory.values()) {
                if (category.value.equalsIgnoreCase(value)) {
                    return category;
                }
            }
            return OUTDOOR; // Default
        }
    }

    // Activity State Enum
    public enum ActivityState {
        AVAILABLE,      // Can be selected
        SELECTED,       // User has chosen this
        DISABLED,       // Generic disabled state
        TIME_CONFLICT,  // Overlaps with selected activity
        BUDGET_EXCEED   // Would exceed budget
    }

    // Constructors
    public Activity() {
        this.tags = new ArrayList<>();
        this.dietaryOptions = new ArrayList<>();
        this.state = ActivityState.AVAILABLE;
        this.travelTimeFromPrevious = 0;
    }

    public Activity(String id, String name, ActivityCategory category) {
        this();
        this.id = id;
        this.name = name;
        this.category = category;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActivityCategory getCategory() {
        return category;
    }

    public void setCategory(ActivityCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Cost getCost() {
        return cost;
    }

    public void setCost(Cost cost) {
        this.cost = cost;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isWeatherDependent() {
        return weatherDependent;
    }

    public void setWeatherDependent(boolean weatherDependent) {
        this.weatherDependent = weatherDependent;
    }

    public boolean isBookingRequired() {
        return bookingRequired;
    }

    public void setBookingRequired(boolean bookingRequired) {
        this.bookingRequired = bookingRequired;
    }

    public String getBookingUrl() {
        return bookingUrl;
    }

    public void setBookingUrl(String bookingUrl) {
        this.bookingUrl = bookingUrl;
    }

    public boolean isStudentDiscountAvailable() {
        return studentDiscountAvailable;
    }

    public void setStudentDiscountAvailable(boolean studentDiscountAvailable) {
        this.studentDiscountAvailable = studentDiscountAvailable;
    }

    public Double getStudentDiscountAmount() {
        return studentDiscountAmount;
    }

    public void setStudentDiscountAmount(Double studentDiscountAmount) {
        this.studentDiscountAmount = studentDiscountAmount;
    }

    public Accessibility getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(Accessibility accessibility) {
        this.accessibility = accessibility;
    }

    public List<String> getDietaryOptions() {
        return dietaryOptions;
    }

    public void setDietaryOptions(List<String> dietaryOptions) {
        this.dietaryOptions = dietaryOptions;
    }

    public ActivityState getState() {
        return state;
    }

    public void setState(ActivityState state) {
        this.state = state;
    }

    public int getTravelTimeFromPrevious() {
        return travelTimeFromPrevious;
    }

    public void setTravelTimeFromPrevious(int travelTimeFromPrevious) {
        this.travelTimeFromPrevious = travelTimeFromPrevious;
    }

    public String getRecommendedExperience() {
        return recommendedExperience;
    }

    public void setRecommendedExperience(String recommendedExperience) {
        this.recommendedExperience = recommendedExperience;
    }

    public boolean isTransportOption() {
        return isTransportOption;
    }

    public void setTransportOption(boolean transportOption) {
        isTransportOption = transportOption;
    }

    public boolean needsEarlyDeparture() {
        return needsEarlyDeparture;
    }

    public void setNeedsEarlyDeparture(boolean needsEarlyDeparture) {
        this.needsEarlyDeparture = needsEarlyDeparture;
    }

    // Helper methods
    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    public double getEffectiveCost() {
        if (cost == null) return 0.0;
        if (studentDiscountAvailable && studentDiscountAmount != null) {
            return studentDiscountAmount;
        }
        return cost.getAmountPerPerson();
    }

    public boolean isFoodActivity() {
        return category == ActivityCategory.FOOD_LUNCH ||
                category == ActivityCategory.FOOD_DINNER;
    }

    public boolean isFree() {
        return cost != null && cost.getAmountPerPerson() == 0.0;
    }

    public String getCategoryIcon() {
        switch (category) {
            case OUTDOOR: return "🏞️";
            case CULTURAL: return "🏛️";
            case FOOD_LUNCH:
            case FOOD_DINNER: return "🍽️";
            case ENTERTAINMENT: return "🎭";
            case SHOPPING: return "🛍️";
            case NIGHTLIFE: return "🌃";
            case RELAXATION: return "🧘";
            default: return "📍";
        }
    }

    /**
     * Get early departure message for display
     */
    @SuppressLint("DefaultLocale")
    public String getEarlyDepartureMessage() {
        if (!needsEarlyDeparture || travelTimeFromPrevious <= 0 || timeSlot == null) {
            return "";
        }

        try {
            java.time.LocalTime activityStart = timeSlot.getStartTime();
            if (activityStart == null) return "";

            java.time.LocalTime departureTime = activityStart.minusMinutes(travelTimeFromPrevious);

            return String.format("⚠️ Leave %d min early (Depart at %s)",
                    travelTimeFromPrevious,
                    departureTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a")));
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category=" + category.getDisplayName() +
                ", cost=" + (cost != null ? "$" + cost.getAmountPerPerson() : "Free") +
                ", timeSlot=" + (timeSlot != null ? timeSlot.getFormattedRange() : "N/A") +
                ", state=" + state +
                '}';
    }
}