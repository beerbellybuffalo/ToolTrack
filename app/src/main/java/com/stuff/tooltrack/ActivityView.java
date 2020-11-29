package com.stuff.tooltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ActivityView extends AppCompatActivity {

    LinearLayout linearLayout;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refData = database.getReference();
    DataSnapshot currentDatabase;

    HashMap<String, Tool> toolMap;
    HashMap<String, Rack> rackMap;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        //initialize variables
        toolMap = new HashMap<String, Tool>();
        rackMap = new HashMap<String, Rack>();

        linearLayout = findViewById(R.id.linearLayoutMain);

        Context context = getApplicationContext();

        user = new User(context);

        //invalid credentials
        if(user.checkValidCredentials()){
            finish();
        }

        //listen for tool changes
        ValueEventListener changeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentDatabase = dataSnapshot;
                updateRacks(dataSnapshot.child("racks"));
                updateTools(dataSnapshot.child("tools"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //do stuff like log errors or what not
            }
        };
        refData.addValueEventListener(changeListener);


    }

    @Override
    protected void onPause() {
        super.onPause();

        //lock all the racks
        for(Rack rack: rackMap.values()){
            rack.lock(user);
        }

    }

    //initialize and update the rack layouts
    public void updateRacks(DataSnapshot rackSnapshot){

        for(DataSnapshot rackData: rackSnapshot.getChildren()) {

            String key = rackData.getKey();
            Rack rack;

            if (rackMap.containsKey(key)) {
                rack = rackMap.get(key);
            } else {
                //create a new rack
                rack = new Rack(refData, rackData, LayoutInflater.from(this).inflate(R.layout.rack_view, null), "racks");
                rackMap.put(key, rack);

                //put rack layout inside main layout
                linearLayout.addView(rack.getView());
            }

            rack.update(rackData, user);

        }

    }

    //initialize and update the tools layouts
    public void updateTools(DataSnapshot toolSnapshot){

        for(DataSnapshot toolData: toolSnapshot.getChildren()){

            String key = toolData.getKey();
            Tool tool;

            if(toolMap.containsKey(key)) {
                tool = toolMap.get(key);
            }
            else{
                //create a new tool
                tool = new Tool(refData, toolData, LayoutInflater.from(this).inflate(R.layout.tool_view, null), "tools");
                toolMap.put(key, tool);

                //put tool layout inside the rack layout
                rackMap.get(tool.getRack()).addTool(tool);

            }


            tool.update(toolData, user);

        }

    }


    public void onToolEditButtonPressed(View v){
        toolMap.get(v.getTag()).displayEditPopup(this);
    }

    public void onRackLockButtonPressed(View v){
        rackMap.get(v.getTag()).toggleLocked(user);
    }

    public void onRackEditButtonPressed(View v){
        rackMap.get(v.getTag()).displayEditPopup(this);
    }





}