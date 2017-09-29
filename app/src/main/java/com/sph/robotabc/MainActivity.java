package com.sph.robotabc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DragRobotView.OnImgDoubleClickListener {


    private DragRobotView dragRobotView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dragRobotView = (DragRobotView) findViewById(R.id.drag_robot);
        findViewById(R.id.tv_say).setOnClickListener(this);
        dragRobotView.setOnImgDoubleClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dragRobotView.postDelayed(new Runnable() {
            @Override
            public void run() {
                dragRobotView.startImgAnimation(true);
            }
        }, 2000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_say:
                Toast.makeText(this, "onBackClick", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onImgDoubleClick() {
        startActivity(new Intent(this, RobotChatActivity.class));
    }
}
