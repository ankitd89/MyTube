package com.adevani.model;

import com.google.api.client.util.DateTime;

/**
 * Created by ankitdevani on 10/17/15.
 */
public class Video {

    private String id;
    private String title;
    //private long viewCount;
    private DateTime publishedDate;
    private String thumbnailURL;

   /* public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }*/

    public DateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(DateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
