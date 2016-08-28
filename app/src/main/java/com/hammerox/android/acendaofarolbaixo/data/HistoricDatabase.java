package com.hammerox.android.acendaofarolbaixo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

/**
 * Created by Mauricio on 21-Aug-16.
 */
public class HistoricDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HistoricDataBase.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE = "CREATE TABLE " + HistoricContract.TABLE_NAME +
            " (" + HistoricContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            HistoricContract.COLUMN_DATE + " TEXT NOT NULL , " +
            HistoricContract.COLUMN_TIME + " TEXT NOT NULL)";

    private static final String SQL_DROP = "DROP TABLE IS EXISTS " + HistoricContract.TABLE_NAME ;



    public HistoricDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DROP);
        onCreate(sqLiteDatabase);
    }


    public Cursor getHistoric(String id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(HistoricContract.TABLE_NAME);

        if(id != null) {
            sqliteQueryBuilder.appendWhere("_id" + " = " + id);
        }

        Cursor cursor = sqliteQueryBuilder.query(getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return cursor;
    }


    public long add(ContentValues values) throws SQLException {
        long id = getWritableDatabase().insert(HistoricContract.TABLE_NAME, "", values);
        if(id <=0 ) {
            throw new SQLException("Failed to add an image");
        }

        return id;
    }


    public int delete(String id) {
        if(id == null) {
            return getWritableDatabase().delete(HistoricContract.TABLE_NAME, null , null);
        } else {
            return getWritableDatabase().delete(HistoricContract.TABLE_NAME, "_id=?", new String[]{id});
        }
    }


    public int update(String id, ContentValues values) {
        if(id == null) {
            return getWritableDatabase().update(HistoricContract.TABLE_NAME, values, null, null);
        } else {
            return getWritableDatabase().update(HistoricContract.TABLE_NAME, values, "_id=?", new String[]{id});
        }
    }
}
