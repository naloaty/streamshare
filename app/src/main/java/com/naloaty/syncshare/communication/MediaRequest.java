package com.naloaty.syncshare.communication;

import com.naloaty.syncshare.service.Requests;
import com.naloaty.syncshare.database.media.Album;
import com.naloaty.syncshare.media.Media;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * This class is used to send and receive all things related to media-files.
 * @see CommunicationHelper
 */
public interface MediaRequest {
    /*
     * media/albums
     */
    @GET(Requests.MEDIA + "/" + Requests.ALBUMS)
    Call<List<Album>> getAlbumsList();

    /*
     * media/medialist?albumId=***
     */
    @GET(Requests.MEDIA + "/" + Requests.MEDIA_LIST)
    Call<List<Media>> getMediaList(@Query(Requests.ALBUM_ID) String albumId);
}
