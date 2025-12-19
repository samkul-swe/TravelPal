package org.kulkarni_sampada.travelpal.models;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class Cost {
    private double amountPerPerson;
    private String currency;
    private CostType costType;
    private String notes;

    // Cost Type Enum
    public enum CostType {
        FREE("free", "Free"),
        PAID_ENTRANCE("paid_entrance", "Entrance Fee"),
        MEAL("meal", "Meal"),
        TRANSPORTATION("transportation", "Transportation"),
        OTHER("other", "Other");

        private final String value;
        private final String displayName;

        CostType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static CostType fromValue(String value) {
            for (CostType type : CostType.values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return OTHER;
        }
    }

    // Constructors
    public Cost() {
        this.currency = "USD";
        this.costType = CostType.FREE;
        this.amountPerPerson = 0.0;
    }

    public Cost(double amountPerPerson) {
        this();
        this.amountPerPerson = amountPerPerson;
        this.costType = amountPerPerson == 0 ? CostType.FREE : CostType.OTHER;
    }

    public Cost(double amountPerPerson, CostType costType) {
        this();
        this.amountPerPerson = amountPerPerson;
        this.costType = costType;
    }

    public Cost(double amountPerPerson, String currency, CostType costType) {
        this(amountPerPerson, costType);
        this.currency = currency;
    }

    // Getters and Setters
    public double getAmountPerPerson() {
        return amountPerPerson;
    }

    public void setAmountPerPerson(double amountPerPerson) {
        this.amountPerPerson = amountPerPerson;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public CostType getCostType() {
        return costType;
    }

    public void setCostType(CostType costType) {
        this.costType = costType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Helper methods
    public boolean isFree() {
        return amountPerPerson == 0.0 || costType == CostType.FREE;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedAmount() {
        if (isFree()) {
            return "Free";
        }

        switch (currency) {
            case "USD":
                return String.format("$%.2f", amountPerPerson);
            case "EUR":
                return String.format("€%.2f", amountPerPerson);
            case "GBP":
                return String.format("£%.2f", amountPerPerson);
            case "JPY":
                return String.format("¥%.0f", amountPerPerson);
            default:
                return String.format("%s %.2f", currency, amountPerPerson);
        }
    }

    public String getFormattedAmountWithType() {
        String amount = getFormattedAmount();
        if (isFree()) {
            return amount;
        }
        return String.format("%s (%s)", amount, costType.getDisplayName());
    }

    public double getTotalForGroup(int groupSize) {
        return amountPerPerson * groupSize;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedTotalForGroup(int groupSize) {
        if (isFree()) {
            return "Free";
        }

        double total = getTotalForGroup(groupSize);

        switch (currency) {
            case "USD":
                return String.format("$%.2f total", total);
            case "EUR":
                return String.format("€%.2f total", total);
            case "GBP":
                return String.format("£%.2f total", total);
            case "JPY":
                return String.format("¥%.0f total", total);
            default:
                return String.format("%s %.2f total", currency, total);
        }
    }

    public String getCostEmoji() {
        if (isFree()) {
            return "🆓";
        } else if (amountPerPerson < 10) {
            return "💵";
        } else if (amountPerPerson < 50) {
            return "💰";
        } else {
            return "💎";
        }
    }

    public String getPriceRange() {
        if (isFree()) {
            return "Free";
        } else if (amountPerPerson < 10) {
            return "$";
        } else if (amountPerPerson < 30) {
            return "$$";
        } else if (amountPerPerson < 75) {
            return "$$$";
        } else {
            return "$$$$";
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Cost{" +
                "amount=" + getFormattedAmount() +
                ", type=" + costType.getDisplayName() +
                (notes != null ? ", notes='" + notes + '\'' : "") +
                '}';
    }
}
