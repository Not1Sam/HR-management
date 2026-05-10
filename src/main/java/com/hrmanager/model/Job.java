package com.hrmanager.model;

import java.util.List;

public class Job {
    public Job() {}

    private int id;
    private String title;
    private String department;
    private String location;
    private String type;
    private String experienceLevel;
    private String salary;
    private String status;
    private int views;
    private String postedAt;
    private String deadline;
    private String description;
    private List<String> missions;
    private List<String> profile;
    private List<String> advantages;
    private List<String> skills;
    private int postedBy;
    private int applicationsCount;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }
    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
    public String getPostedAt() { return postedAt; }
    public void setPostedAt(String postedAt) { this.postedAt = postedAt; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getMissions() { return missions; }
    public void setMissions(List<String> missions) { this.missions = missions; }
    public List<String> getProfile() { return profile; }
    public void setProfile(List<String> profile) { this.profile = profile; }
    public List<String> getAdvantages() { return advantages; }
    public void setAdvantages(List<String> advantages) { this.advantages = advantages; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public int getPostedBy() { return postedBy; }
    public void setPostedBy(int postedBy) { this.postedBy = postedBy; }
    public int getApplicationsCount() { return applicationsCount; }
    public void setApplicationsCount(int applicationsCount) { this.applicationsCount = applicationsCount; }
}