package com.example.weatherchecker;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    public CityListAdapter adapter;
    Toolbar toolbar;
    ListView listview;
    FloatingActionButton fab;
    TextView refreshText;
    Button settingsButton;
    boolean fahrenheight = false;

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        fahrenheight = SP.getBoolean("temperatureScale", false);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        listview = (ListView) findViewById(R.id.weather_list);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        refreshText = (TextView) findViewById(R.id.toolbar_refresh);
        settingsButton = (Button) findViewById(R.id.toolbar_settings);


        adapter = new CityListAdapter(this);
        listview.setAdapter(adapter);
        setSupportActionBar(toolbar);

        initDeleteDialog();
        initAddCityDialog();

        refreshText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < adapter.getCount(); ++i) {
                    adapter.getItem(i).downloadWeather();
                    adapter.notifyDataSetChanged();
                }
                Toast.makeText(MainActivity.this, "Refreshing...", Toast.LENGTH_SHORT).show();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });


        SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase dbReadable = databaseHelper.getReadableDatabase();

        //pull data from database
        Cursor cursor = dbReadable.query("CITIES", new String[] {"NAME"}, null, null, null, null, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            City city = new City(this, name);
            city.downloadWeather();
            adapter.addItem(city);
        }

    }

    public void updateUI() {
        adapter.notifyDataSetChanged();;
    }

    private void initAddCityDialog () {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Enter City or Zip Code");

                // Set up the input
                final EditText input = new EditText(MainActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
                builder.setView(input);


                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = input.getText().toString();
                        City city = new City(MainActivity.this, text);
                        city.downloadWeather();
                        adapter.addItem(city);
                        ContentValues wordValues = new ContentValues();
                        wordValues.put("NAME", text);
                        SQLiteOpenHelper databaseHelper = new DatabaseHelper(MainActivity.this);
                        SQLiteDatabase dbWritable = databaseHelper.getWritableDatabase();
                        dbWritable.insert("CITIES", null, wordValues);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                dialog.show();

            }
        });
    }

    private void initDeleteDialog () {
        listview.setLongClickable(true);
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {

                City city = adapter.getItem(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(String.format("Delete %s?", city.name));

                // Set up the buttons
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.removeItem(position);
                        SQLiteOpenHelper databaseHelper = new DatabaseHelper(MainActivity.this);
                        SQLiteDatabase dbWritable = databaseHelper.getWritableDatabase();

                        Cursor cursor = dbWritable.query("CITIES", new String[] {"_id"}, null, null, null, null, null);
                        cursor.moveToPosition(position);
                        String idNum = cursor.getString(0);
                        dbWritable.delete("CITIES", "_ID = ?", new String[] {idNum});

                    }
                });


                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

                return true;
            }
        });
    }
}