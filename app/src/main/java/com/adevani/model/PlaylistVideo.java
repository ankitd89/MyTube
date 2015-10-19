package com.adevani.model;

import com.google.api.client.util.DateTime;

import java.math.BigInteger;

/**
 * Created by ankitdevani on 10/18/15.
 */
public class PlaylistVideo {
    private String id;
    private String title;
    private BigInteger viewCount;
    private DateTime publishedDate;
    private String thumbnailURL;

    public String getPlaylistItemVideoId() {
        return playlistItemVideoId;
    }

    public void setPlaylistItemVideoId(String playlistItemVideoId) {
        this.playlistItemVideoId = playlistItemVideoId;
    }

    private String playlistItemVideoId;

    public BigInteger getViewCount() {
        return viewCount;
    }

    public void setViewCount(BigInteger viewCount) {
        this.viewCount = viewCount;
    }

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
