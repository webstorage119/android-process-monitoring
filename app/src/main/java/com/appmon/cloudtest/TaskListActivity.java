package com.appmon.cloudtest;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TaskListActivity extends AppCompatActivity {

    TaskListAdapter mTaskAdapter;
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // represents reference to "taskList" Database entity
        final DatabaseReference root = mDatabase.child("users").child(uid).child("taskList");
        // attach adapter to list
        ArrayList<TaskInfo> tasks = new ArrayList<>();
        final ListView taskList = (ListView) findViewById(R.id.taskList);
        mTaskAdapter = new TaskListAdapter(this,R.layout.layout_task_tem, tasks);
        mTaskAdapter.setOnClickListener(new OnTaskItemClickListener() {
            @Override
            public void onClick(int pos, TaskInfo task) {
                root.child(Integer.toString(pos)).child("checked").setValue(task.isChecked());
            }
        });
        taskList.setAdapter(mTaskAdapter);
        // handle data changes
        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // In this prototype we can add element from firebase console
                mTaskAdapter.add(
                        new TaskInfo(
                        dataSnapshot.child("text").getValue().toString(),
                        dataSnapshot.child("checked").getValue().toString().contentEquals("true")
                ));
                Log.w("Firebase", "Item addded!");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mTaskAdapter.setChecked(Integer.parseInt(dataSnapshot.getKey()),
                        dataSnapshot.child("checked").getValue().toString().contentEquals("true"));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // Button to force upload data entirely
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < mTaskAdapter.itemsCount(); i++) {
                    TaskInfo ti = mTaskAdapter.get(i);
                    root.child(Integer.toString(i)).child("text").setValue(ti.getText());
                    root.child(Integer.toString(i)).child("checked").setValue(ti.isChecked());
                }
                Toast.makeText(TaskListActivity.this, "Data have been sent", Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

}