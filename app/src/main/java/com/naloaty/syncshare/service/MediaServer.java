package com.naloaty.syncshare.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.naloaty.syncshare.communication.SimpleServerResponse;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.device.SSDeviceRepository;
import com.naloaty.syncshare.database.media.Album;
import com.naloaty.syncshare.media.Media;
import com.naloaty.syncshare.media.MediaObject;
import com.naloaty.syncshare.media.MediaProvider;
import com.naloaty.syncshare.security.CustomServerSocketFactory;
import com.naloaty.syncshare.security.SecurityManager;
import com.naloaty.syncshare.security.SecurityUtils;
import com.naloaty.syncshare.util.AppUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;
import retrofit2.internal.EverythingIsNonNull;

/**
 * This class represents a server that provides access to media files for trusted devices.
 * @see CommunicationService
 */
public class MediaServer extends SimpleWebServer {

    /*
     * TODO: get rid of SimpleWebServer
     */
    private static final String TAG = "MediaServer";

    private Context mContext;

    /**
     * @param context The Context in which this instance should be created.
     * @param port The port that the server will listen.
     */
    MediaServer(@NonNull Context context, int port) throws Exception {
        super(null, port, new File(Environment.getExternalStorageDirectory().getPath()), true);

        mContext = context;
        makeSecure();
    }

    /**
     * Forces the server to use the https protocol
     */
    private void makeSecure() throws Exception {
        SSLContext sslContext = SecurityUtils.getSSLContext(new SecurityManager(mContext), mContext.getFilesDir());

        if (sslContext != null) {
            SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
            setServerSocketFactory(new CustomServerSocketFactory(factory, null));
        }
        else
            throw new Exception("Cannot start media server in secure mode");
    }

    /**
     * Serves a client requests
     */
    @Override
    public Response serve(IHTTPSession session) {
        //Remove URL arguments
        String uri = session.getUri().trim().replace(File.separatorChar, '/');
        uri = uri.substring(1);
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }

        String[] request = uri.split("/");

        if (request.length < 2)
            return getBadRequestResponse();

        switch (request[0]) {
            case Requests.DEVICE:
                return getDeviceRespond(session, request);

            case Requests.MEDIA:
                return getMediaRespond(session, request);

            default:
                return getBadRequestResponse();
        }
    }

    /**
     * Serves requests about device
     */
    @EverythingIsNonNull
    private Response getDeviceRespond(IHTTPSession session, String[] request) {

        if (Method.POST.equals(session.getMethod())) {
            Map<String, String> map = new HashMap<>();

            try {
                session.parseBody(map);
            }
            catch (IOException e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Internal error: " + e.getMessage());
            }
            catch (ResponseException e) {
                return newFixedLengthResponse(e.getStatus(), MIME_PLAINTEXT, e.getMessage());
            }

            String postParams = map.get("postData");

            if (postParams == null)
                return getBadRequestResponse();

            switch (request[1]) {
                case Requests.INFORMATION:
                    Gson converter = new Gson();
                    SSDevice ssDevice = converter.fromJson(postParams, SSDevice.class);
                    ssDevice.setAccessAllowed(true);

                    SSDeviceRepository repository = new SSDeviceRepository(mContext);
                    repository.publish(ssDevice);

                    SimpleServerResponse resp = new SimpleServerResponse();
                    resp.setDescription("Device added");

                    return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, converter.toJson(resp));

                default:
                    return getNotFoundResponse();
            }
        }
        else if (Method.GET.equals(session.getMethod())) {

            switch (request[1]) {

                case Requests.INFORMATION:
                    SSDevice myDevice = AppUtils.getLocalDevice(mContext);
                    Gson converter = new Gson();
                    String json = converter.toJson(myDevice);

                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, json);

                default:
                    return getNotFoundResponse();
            }
        }

        return getBadRequestResponse();
    }

    /**
     * Serves requests about media files.
     */
    @EverythingIsNonNull
    private Response getMediaRespond(IHTTPSession session, String[] request) {

        if (Method.GET.equals(session.getMethod())) {
            switch (request[1]) {

                /*
                 * Responds albums list
                 */
                case Requests.ALBUMS:
                    try {
                        List<Album> albums = MediaProvider.getSharedAlbums(mContext);
                        Log.i(TAG, String.format("Albums fetched with success. Items count is %d", albums.size()));

                        Gson gson = new Gson();
                        String json = gson.toJson(albums);

                        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, json);
                    }
                    catch (Exception e) {
                        Log.e(TAG, String.format("Cannot fetch albums list. Reason: %s", e.toString()));
                        return getInternalErrorResponse();
                    }


                /*
                 * Responds media list
                 */
                case Requests.MEDIA_LIST:
                    Map<String, List<String>> parameters = session.getParameters();

                    if (!parameters.containsKey(Requests.ALBUM_ID))
                        return getBadRequestResponse();

                    List<String> albumParams = parameters.get(Requests.ALBUM_ID);

                    if (albumParams == null)
                        return getBadRequestResponse();

                    String albumId = albumParams.get(0);

                    try {
                        List<Media> media = MediaProvider.getMediaFromMediaStore(mContext, albumId);
                        Log.i(TAG, String.format("Responding with albums list. Items count is %d", media.size()));

                        Gson gson = new Gson();
                        String json = gson.toJson(media);

                        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, json);
                    }
                    catch (Exception e) {
                        Log.e(TAG, String.format("Cannot respond with albums list. Reason: %s", e.toString()));
                        return getInternalErrorResponse();
                    }

                /*
                 * Responds small-size bitmap thumbnail
                 */
                case Requests.THUMBNAIL:
                    try {
                        MediaObject mediaObject = MediaProvider.getMediaObjectById(mContext, request[2]);
                        Bitmap thumbnail = MediaProvider.getCorrectlyOrientedThumbnail(mContext, mediaObject, false);
                        Log.i(TAG, String.format("Responding thumbnail of %s located by path %s", request[2], mediaObject.getPath()));

                        return getBitmapResponse(thumbnail);
                    }
                    catch (Exception e) {
                        Log.e(TAG, String.format("Cannot respond thumbnail of %s. Reason: %s", request[2], e.toString()));
                        return getInternalErrorResponse();
                    }

                /*
                 * Responds full-size bitmap thumbnail
                 */
                case Requests.FULL_SIZE_IMAGE:
                    try {
                        MediaObject mediaObject = MediaProvider.getMediaObjectById(mContext, request[2]);

                        if (mediaObject.isVideo()) {
                            Bitmap thumb = MediaProvider.getCorrectlyOrientedThumbnail(mContext, mediaObject, true);
                            Log.i(TAG, String.format("Responding full-size thumbnail of video %s located by path %s", request[2], mediaObject.getPath()));

                            return getBitmapResponse(thumb);
                        }

                        Log.i(TAG, String.format("Responding full-size thumbnail of image %s located by path %s", request[2], mediaObject.getPath()));
                        return serveFile(session.getHeaders(), new File(mediaObject.getPath()), getMimeTypeForFile(mediaObject.getPath()));
                    }
                    catch (Exception e) {
                        Log.e(TAG, String.format("Cannot respond full-size thumbnail of %s. Reason: %s", request[2], e.toString()));
                        return getInternalErrorResponse();
                    }

                /*
                 * Responds file
                 */
                case Requests.SERVE_FILE:
                    try {
                        MediaObject mediaObject = MediaProvider.getMediaObjectById(mContext, request[2]);
                        Log.i(TAG, String.format("Responding file %s located by path %s", request[2], mediaObject.getPath()));

                        return serveFile(session.getHeaders(), new File(mediaObject.getPath()), getMimeTypeForFile(mediaObject.getPath()));

                    }
                    catch (Exception e) {
                        Log.e(TAG, String.format("Cannot respond file %s. Reason: %s", request[2], e.toString()));
                        return getInternalErrorResponse();
                    }

                default:
                    return getNotFoundResponse();
            }
        }

        return getBadRequestResponse();
    }
    
    /**
     * Responds by file
     */
    @EverythingIsNonNull
    private Response serveFile(Map<String, String> header, File file, String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath() +
                    file.lastModified() + "" + file.length()).hashCode());

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // Change return code and add Content-Range header when skipping is requested
            long fileLen = file.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                }
                else
                {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    final long dataLen = newLen;
                    FileInputStream fis = new FileInputStream(file) {
                        @Override
                        public int available() {
                            return (int) dataLen;
                        }
                    };
                    fis.skip(startFrom);

                    res = getChunkedResponse(Response.Status.PARTIAL_CONTENT, mime, fis);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" +
                            endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
            else
            {
                if (etag.equals(header.get("if-none-match")))
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                else
                {
                    res = getChunkedResponse(Response.Status.OK, mime, new FileInputStream(file));
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        }
        catch (IOException e) {
            res = newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "Forbidden: Reading file failed");
        }

        return res;
    }

    /**
     * Responds by bitmap
     */
    private Response getBitmapResponse(@NonNull Bitmap bitmap) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapData = bos.toByteArray();
        bos.close();

        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData);
        return getChunkedResponse(Response.Status.OK, MIME_TYPES.get("png"), bs);
    }

    /**
     * Announce that the media server accepts partial content requests
     */
    @EverythingIsNonNull
    private Response getChunkedResponse(Response.Status status, String mimeType, InputStream message) {
        Response res = newChunkedResponse(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    /**
     * Responds by bad request error code (400)
     */
    private Response getBadRequestResponse() {
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                "Error 400, request doesn't match StreamShare requirements");
    }

    /**
     * Responds by internal error code (500)
     */
    private Response getInternalErrorResponse() {
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                "Error 500, request execution error");
    }

    /**
     * Responds by not found error code (404)
     */
    protected Response getNotFoundResponse() {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, requested resource not found");
    }
}