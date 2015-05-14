/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.uf.togathor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.uf.togathor.R;
import com.uf.togathor.model.attendance.Course;
import com.uf.togathor.model.Message;

import java.util.List;

/**
 * WallMessagesAdapter
 * <p/>
 * Adapter class for wall messages.
 */

public class CourseListAdapter extends ArrayAdapter<Course> {

    private final Context context;
    private final int resource;
    private final List<Course> courses;
    ViewHolder holder;

    public CourseListAdapter(Context context, int resource, List<Course> courses) {
        super(context, resource, courses);
        this.context = context;
        this.resource = resource;
        this.courses = courses;
    }

    @Override
    public int getCount() {
        return courses.size();
    }

    @Override
    public Course getItem(int arg0) {
        return courses.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        Course course = courses.get(position);

        if (v == null) {

            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.course_list_item_definition, null);

            holder.courseName = (TextView) v.findViewById(R.id.course_name);
            holder.courseID = (TextView) v.findViewById(R.id.course_id);
            holder.courseInstructor = (TextView) v.findViewById(R.id.course_instructor);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.courseName.setText(course.getName());
        holder.courseID.setText(course.getId());
        holder.courseInstructor.setText(course.getInstructor());

        return v;
    }

    static class ViewHolder {
        TextView courseName;
        TextView courseID;
        TextView courseInstructor;
    }


    private class FullImageClickListener implements View.OnClickListener {

        Message message;

        private FullImageClickListener(Message message) {
            this.message = message;
        }

        @Override
        public void onClick(View v) {
        }
    }
}
