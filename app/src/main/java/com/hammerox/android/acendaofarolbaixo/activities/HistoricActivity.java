package com.hammerox.android.acendaofarolbaixo.activities;

import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.hammerox.android.acendaofarolbaixo.data.HistoricContract;
import com.hammerox.android.acendaofarolbaixo.data.HistoricProvider;
import com.hammerox.android.acendaofarolbaixo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoricActivity extends AppCompatActivity {

    @BindView(R.id.historic_listview) ListView listview;
    @BindView(R.id.historic_emptyview) TextView emptyLayout;

    private SimpleCursorAdapter adapter;
    private String[] columns = new String[]{HistoricContract.COLUMN_DATE, HistoricContract.COLUMN_TIME};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic);
        ButterKnife.bind(this);

        adapter = new SimpleCursorAdapter(getBaseContext(),
                R.layout.adapter_historic,
                null,
                columns,
                new int[] { R.id.item_date , R.id.item_time}, 0);
        listview.setAdapter(adapter);
        listview.setEmptyView(emptyLayout);
        refreshValuesFromContentProvider();
    }

    private void refreshValuesFromContentProvider() {
        CursorLoader cursorLoader = new CursorLoader(getBaseContext(), HistoricProvider.CONTENT_URI,
                null, null, null, HistoricContract._ID + " DESC");
        Cursor c = cursorLoader.loadInBackground();
        adapter.swapCursor(c);
    }
}
