package com.uf.togathor.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uf.togathor.R;

public class LeftNavBarListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final int resource;
    private final String[] values;
    private final int[] imageValues;

    public LeftNavBarListAdapter(Context context, int resource, String[] values,
                                 int[] imageValues) {
        super(context, resource, values);
        this.context = context;
        this.values = values;
        this.imageValues = imageValues;
        this.resource = resource;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(resource, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.textLabel);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.textLogo);

        // Customization to textView
        textView.setText(values[position]);
/*        textView.setTypeface(Typeface.createFromAsset(context.getAssets(),
                "fonts/rob_reg.ttf"));*/

        imageView.setImageResource(imageValues[position]);
        return rowView;
    }

}