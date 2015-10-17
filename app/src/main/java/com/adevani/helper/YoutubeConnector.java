package com.adevani.helper;

import android.content.Context;
import android.util.Log;

import com.adevani.model.Video;
import com.adevani.mytube.MainActivity;
import com.adevani.mytube.R;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankitdevani on 10/17/15.
 */
public class YoutubeConnector {

    public static final String TAG = YoutubeConnector.class.getSimpleName();
    private YouTube youtube;
    private YouTube.Search.List query;
    public static final String KEY = "AIzaSyD7DB-rO5WHOHPj0EfxT8qtB-SYf3udbG8";

    public YoutubeConnector(Context context) {

        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {}
        })
                .setApplicationName(context.getString(R.string.app_name)).build();

        try{
            query = youtube.search().list("id,snippet");
            query.setOauthToken(MainActivity.FETCH_TOKEN);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/publishedAt,snippet/thumbnails/default/url)");
        }catch(IOException e){
            Log.d(TAG, "Could not initialize: " + e);
        }
    }

    public List<Video> search(String keywords){
        query.setQ(keywords);
        try{
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            List<Video> items = new ArrayList<Video>();
            for(SearchResult result:results){
                Video item = new Video();
                item.setTitle(result.getSnippet().getTitle());
                item.setPublishedDate(result.getSnippet().getPublishedAt());
                //item.setViewCount(result.getSt);
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getId().getVideoId());
                items.add(item);
            }
            return items;
        }catch(IOException e){
            Log.d(TAG, "Could not search: "+e);
            return null;
        }
    }
}
