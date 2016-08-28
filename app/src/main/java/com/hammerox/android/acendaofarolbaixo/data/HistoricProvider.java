package com.hammerox.android.acendaofarolbaixo.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Mauricio on 21-Aug-16.
 */
public class HistoricProvider extends ContentProvider {


    public static final String PROVIDER_NAME = "hammerox.android.acendaofarolbaixo";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/historic");
    private static final int HISTORIC = 1;
    private static final int HISTORIC_ID = 2;
    private static final UriMatcher uriMatcher = getUriMatcher();
    private static UriMatcher getUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "historic", HISTORIC);
        uriMatcher.addURI(PROVIDER_NAME, "historic/#", HISTORIC_ID);
        return uriMatcher;
    }

    private HistoricDatabase historicDatabase = null;


    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case HISTORIC:
                return "vnd.android.cursor.dir/vnd.com." + PROVIDER_NAME + ".provider.historic";
            case HISTORIC_ID:
                return "vnd.android.cursor.item/vnd.com." + PROVIDER_NAME + ".provider.historic";

        }
        return null;
    }


    @Override
    public boolean onCreate() {
        Context context = getContext();
        historicDatabase = new HistoricDatabase(context);
        return true;
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        String id = null;
        if(uriMatcher.match(uri) == HISTORIC_ID) {
            //Query is for one single image. Get the ID from the URI.
            id = uri.getPathSegments().get(1);
        }
        return historicDatabase.getHistoric(id, strings, s, strings1, s1);
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        try {
            long id = historicDatabase.add(contentValues);
            Uri returnUri = ContentUris.withAppendedId(CONTENT_URI, id);
            return returnUri;
        } catch(Exception e) {
            return null;
        }
    }


    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        String id = null;
        if(uriMatcher.match(uri) == HISTORIC_ID) {
            //Update is for one single image. Get the ID from the URI.
            id = uri.getPathSegments().get(1);
        }

        return historicDatabase.update(id, contentValues);
    }


    @Override
    public int delete(Uri uri, String s, String[] strings) {
        String id = null;
        if(uriMatcher.match(uri) == HISTORIC_ID) {
            //Delete is for one single image. Get the ID from the URI.
            id = uri.getPathSegments().get(1);
        }

        return historicDatabase.delete(id);
    }
}