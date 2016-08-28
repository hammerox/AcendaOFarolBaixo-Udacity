package com.hammerox.android.acendaofarolbaixo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.hammerox.android.acendaofarolbaixo.data.HistoricContract;
import com.hammerox.android.acendaofarolbaixo.data.HistoricDatabase;
import com.hammerox.android.acendaofarolbaixo.data.HistoricProvider;

/**
 * Created by Mauricio on 22-Aug-16.
 */
public class DatabaseTest extends AndroidTestCase {
    private HistoricDatabase db;
    private String[] columns = new String[]{HistoricContract.COLUMN_DATE, HistoricContract.COLUMN_TIME};

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new HistoricDatabase(context);
    }

    @Override
    public void tearDown() throws Exception {
        db.close();
        super.tearDown();
    }


    public void testAddEntry(){
        ContentValues values = new ContentValues();
        values.put(HistoricContract.COLUMN_DATE, "03/10/1988");
        values.put(HistoricContract.COLUMN_TIME, "11:36");

        long response = db.add(values);

        assertTrue(response > 0);

        Cursor c = db.getHistoric(null, columns, null, null, null);
        assertTrue(c.moveToFirst());

        String date = c.getString(c.getColumnIndex(HistoricContract.COLUMN_DATE));
        assertTrue(date.equals("03/10/1988"));

        ContentResolver resolver = getContext().getContentResolver();
        assertNotNull(resolver);

        Uri uri = resolver.insert(HistoricProvider.CONTENT_URI, values);
        assertNotNull(uri);
        assertTrue(c.moveToFirst());

        c = resolver.query(HistoricProvider.CONTENT_URI, columns, null, null, null);
        int removed = 0;
        if (c.moveToFirst()) {
            String where = HistoricContract.COLUMN_DATE + " LIKE ?";
            String[] args = new String[]{"03/10/1988"};
            removed = resolver.delete(HistoricProvider.CONTENT_URI, where, args);
        }
        assertTrue(removed > 0);
        c.close();
    }
}