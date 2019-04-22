package com.joshuahalvorson.safeyoutube.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ContentDetails {

    @SerializedName("duration")
    @Expose
    private String duration;
    @SerializedName("dimension")
    @Expose
    private String dimension;
    @SerializedName("definition")
    @Expose
    private String definition;
    @SerializedName("caption")
    @Expose
    private String caption;
    @SerializedName("licensedContent")
    @Expose
    private Boolean licensedContent;
    @SerializedName("projection")
    @Expose
    private String projection;

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Boolean getLicensedContent() {
        return licensedContent;
    }

    public void setLicensedContent(Boolean licensedContent) {
        this.licensedContent = licensedContent;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

}
