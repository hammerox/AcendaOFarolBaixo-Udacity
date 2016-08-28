package com.hammerox.android.acendaofarolbaixo.others;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.hammerox.android.acendaofarolbaixo.data.HistoricContract;
import com.hammerox.android.acendaofarolbaixo.data.HistoricProvider;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Mauricio on 18-Aug-16.
 */
public class FileManager {

    public static final String FILE_NAME = "preferences";
    public static final String FIRST_TIME = "FIRST_TIME";

    private static DateFormat formatTime = DateFormat.getTimeInstance(DateFormat.SHORT);
    private static DateFormat formatDate = DateFormat.getDateInstance(DateFormat.MEDIUM);


    public static Uri insertDateAndTime(Context context) {
        Date now = new Date();
        String date = formatDate.format(now);
        String time = formatTime.format(now);

        ContentValues values = new ContentValues();
        values.put(HistoricContract.COLUMN_DATE, date);
        values.put(HistoricContract.COLUMN_TIME, time);

        return context.getContentResolver().insert(HistoricProvider.CONTENT_URI, values);
    }
}
