package uk.co.openmoments.hillbagging.database.entities;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "hill")
public class Hill {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "hill_id")
    private int hillId;

    @NonNull
    @ColumnInfo(name = "number")
    private int hillBaggingId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "region")
    private String region;

    @ColumnInfo(name = "area")
    private String area;

    @ColumnInfo(name = "topo_section")
    private String topOSelection;

    @ColumnInfo(name = "county")
    private String county;

    @ColumnInfo(name = "metres")
    private float metres;

    @ColumnInfo(name = "feet")
    private float feet;

    @ColumnInfo(name = "hill_url")
    private String hillURL;

    @NonNull
    @ColumnInfo(name = "latitude")
    private float latitude;

    @NonNull
    @ColumnInfo(name = "longitude")
    private float longitude;

    @Ignore
    private Location hillLocation;

    public Hill(int hillId, int hillBaggingId, String name, String region, String area, String topOSelection, String county, float metres, float feet, String hillURL, float latitude, float longitude) {
        this.hillId = hillId;
        this.hillBaggingId = hillBaggingId;
        this.name = name;
        this.region = region;
        this.area = area;
        this.topOSelection = topOSelection;
        this.county = county;
        this.metres = metres;
        this.feet = feet;
        this.hillURL = hillURL;
        this.latitude = latitude;
        this.longitude = longitude;

        hillLocation = new Location("");
        hillLocation.setLatitude(latitude);
        hillLocation.setLongitude(longitude);
    }

    public int getHillId() {
        return hillId;
    }

    public void setHillId(int hillId) {
        this.hillId = hillId;
    }

    public int getHillBaggingId() {
        return hillBaggingId;
    }

    public void setHillBaggingId(int hillBaggingId) {
        this.hillBaggingId = hillBaggingId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTopOSelection() {
        return topOSelection;
    }

    public void setTopOSelection(String topOSelection) {
        this.topOSelection = topOSelection;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public float getMetres() {
        return metres;
    }

    public void setMetres(float metres) {
        this.metres = metres;
    }

    public float getFeet() {
        return feet;
    }

    public void setFeet(float feet) {
        this.feet = feet;
    }

    public String getHillURL() { return hillURL; }

    public void setHillURL(String hillURL) { this.hillURL = hillURL; }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float calculateDistanceFrom(Location location) {
        return hillLocation.distanceTo(location);
    }
}
