package com.hrmanager.model;

public class Interview {
    private int id;
    private int application;
    private int applicationId;
    private String type;
    private String date;
    private String time;
    private int duration;
    private String location;
    private String link;
    private String status;
    private String notes;
    private String rejectReason;
    private String candidateName;
    private String jobTitle;
    private String interviewers;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getApplication() { return application; }
    public void setApplication(int application) { this.application = application; }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getInterviewers() { return interviewers; }
    public void setInterviewers(String interviewers) { this.interviewers = interviewers; }
}