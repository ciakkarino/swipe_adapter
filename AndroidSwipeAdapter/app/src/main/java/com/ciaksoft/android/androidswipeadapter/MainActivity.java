package com.ciaksoft.android.androidswipeadapter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SwipeRowAdapter.SwipeAdapterCallback{

    private RecyclerView mRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRv = (RecyclerView) findViewById(R.id.rv_exaple);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinearLayoutManager lm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRv.setLayoutManager(lm);
        ArrayList<String> mList = new ArrayList<>();
        for(int i = 0; i<50; i++){
            mList.add("Element " + i+1);
        }
        MyAdapter adapter = new MyAdapter(this,this,lm,mList);
        mRv.setAdapter(adapter);
    }

    @Override
    public void onRowItemClicked(View clickedView, int listPosition) {

    }

    @Override
    public boolean onRowItemLongClicked(View clickedView, int listPosition) {
        return false;
    }

    @Override
    public void onSwipeComplete(int direction, int listPosition) {

    }

    @Override
    public void onSwipeProgress(int swipeDirection, View swipeContainer, View swipeBottom) {

    }
}
