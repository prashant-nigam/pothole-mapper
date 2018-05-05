package project.saurabh.entity;

/**
 * Created by Prashant on 8/15/2017.
 */

public class Potholes {

    private int Id;
    private String latitude;
    private String longitude;
    private String picLocation;
    private String severity;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPicLocation() {
        return picLocation;
    }

    public void setPicLocation(String picLocation) {
        this.picLocation = picLocation;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
