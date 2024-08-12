package org.kulkarni_sampada.travelpal.model;

import java.io.Serializable;
import java.util.List;

public class TravelPlan implements Serializable {
    private String budget;
    private String isTotalBudget;
    private String isPerPersonBudget;
    private String mealsIncluded;
    private String location;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String planID;
    private String title;
    private List<String> placeIDs;

    public TravelPlan() {}

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getMealsIncluded() {
        return mealsIncluded;
    }

    public void setMealsIncluded(String mealsIncluded) {
        this.mealsIncluded = mealsIncluded;
    }

    public String getIsTotalBudget() {
        return isTotalBudget;
    }

    public void setIsTotalBudget(String isTotalBudget) {
        this.isTotalBudget = isTotalBudget;
    }

    public String getIsPerPersonBudget() {
        return isPerPersonBudget;
    }

    public void setIsPerPersonBudget(String isPerPersonBudget) {
        this.isPerPersonBudget = isPerPersonBudget;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getPlanID() {
        return planID;
    }

    public void setPlanID(String planID) {
        this.planID = planID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getPlaceIDs() {
        return placeIDs;
    }

    public void setPlaceIDs(List<String> placeIDs) {
        this.placeIDs = placeIDs;
    }

    @Override
    public String toString() {
        return "TravelPlan{" +
                "budget='" + budget + '\'' +
                ", isTotalBudget='" + isTotalBudget + '\'' +
                ", isPerPersonBudget='" + isPerPersonBudget + '\'' +
                ", mealsIncluded='" + mealsIncluded + '\'' +
                ", location='" + location + '\'' +
                ", startDate='" + startDate + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endDate='" + endDate + '\'' +
                ", endTime='" + endTime + '\'' +
                ", planID='" + planID + '\'' +
                ", title='" + title + '\'' +
                ", placeIDs=" + placeIDs +
                '}';
    }
}
