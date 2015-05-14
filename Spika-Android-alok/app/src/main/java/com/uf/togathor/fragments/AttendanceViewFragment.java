package com.uf.togathor.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.uf.togathor.modules.attendance.AttendanceActivity;
import com.uf.togathor.R;
import com.uf.togathor.adapters.CourseListAdapter;
import com.uf.togathor.model.attendance.Course;
import com.uf.togathor.model.OnAnimationFinishListener;
import com.uf.togathor.uitems.Animations;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alok on 1/16/2015.
 */
public class AttendanceViewFragment extends Fragment implements OnAnimationFinishListener {

    private View rootView;
    private ListView courseListView;
    private List<Course> courseList;
    private CourseListAdapter courseListAdapter;

    private View tempView;
    private int position;
    private Animations animations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_attendance, container, false);
        courseListView = (ListView) rootView.findViewById(R.id.list_of_courses);
        courseList = getListOfCourses();

        courseListAdapter = new CourseListAdapter(getActivity(), R.layout.course_list_item_definition, courseList);
        courseListView.setAdapter(courseListAdapter);
        courseListView.setOnItemClickListener(new CourseInformationListener());
        courseListView.setItemsCanFocus(true);

        animations = new Animations(getActivity(), this);

        return rootView;
    }

    private List<Course> getListOfCourses() {

        List<Course> coursesLocal = new ArrayList<>();
        Course course = new Course();

        course.setName("Cyber Physical Systems");
        course.setId("EEL6935");
        course.setInstructor("Dr. Andy Li");
        coursesLocal.add(course);

        course = new Course();
        course.setName("Parallel Computer Architecture");
        course.setId("EEL6763");
        course.setInstructor("Dr. Alan D George");
        coursesLocal.add(course);

        course = new Course();
        course.setName("Virtual Computers");
        course.setId("EEL6892");
        course.setInstructor("Dr. Renato Figueiredo");
        coursesLocal.add(course);

        course = new Course();
        course.setName("Distributed Computing");
        course.setId("EEL6935");
        course.setInstructor("Dr. Jose Fortes");
        coursesLocal.add(course);

        return coursesLocal;
    }

    @Override
    public void animationFinished() {
        Intent intent = new Intent(getActivity(), AttendanceActivity.class);
        intent.putExtra("course_name", courseList.get(position).getName());
        intent.putExtra("course_id", courseList.get(position).getId());
        intent.putExtra("course_instructor", courseList.get(position).getInstructor());
        startActivityForResult(intent, 2048);
    }

    private class CourseInformationListener implements AdapterView.OnItemClickListener  {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(getResources().getBoolean(R.bool.isLollipop))    {
                tempView = view;
                animations.setupScene(view);
                animations.raiseView(100.0f);
                AttendanceViewFragment.this.position = position;
            }
            else {
                animationFinished();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(tempView != null)
            tempView.setElevation(5.0f);
    }
}
