package com.example.weatherchecker;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CityListAdapter extends BaseAdapter {
    private List<City> cities = new ArrayList<City>();
    protected MainActivity context;
    protected CityListAdapter(MainActivity _context) {
        context = _context;
    }

    String temperature;

    @Override
    public int getCount () {
        return cities.size();
    }
    @Override
    public City getItem(int position) {
        return cities.get(position);
    }
    @Override
    public long getItemId(int position) {
        return cities.get(position).hashCode();
    }

    public void removeItem(int position) {
        cities.remove(position);
        notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.list_item, container, false);
        }
        ((TextView) convertView.findViewById(R.id.name))
                .setText(cities.get(position).name);
        ((TextView) convertView.findViewById(R.id.text))
                .setText(cities.get(position).text);
        if (context.fahrenheight)
            temperature = cities.get(position).temperature_f + " °F";
        else
            temperature = cities.get(position).temperature_c + " °C";

        ((TextView) convertView.findViewById(R.id.temperature))
                .setText(temperature);

        if (cities.get(position).image != null) {
            ImageView image = (ImageView) convertView.findViewById(R.id.imageView);
            if (image != null) {
                image.setImageBitmap(cities.get(position).image);
            }
        }
        return convertView;
    }

    public void addItem(City city) {
        cities.add(city);
        notifyDataSetChanged();
    }

    public void removeAll() {
        cities = new ArrayList<City>();
    }
}