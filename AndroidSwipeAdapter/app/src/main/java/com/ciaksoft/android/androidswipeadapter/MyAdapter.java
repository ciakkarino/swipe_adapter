package com.ciaksoft.android.androidswipeadapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Nunzio on 09/02/2016.
 */
public class MyAdapter extends SwipeRowAdapter<MyAdapter.MyHolder>{

    public static class MyHolder extends SwipeRowAdapter.SwipeRowAdapterHolder{

        private TextView tv;

        public MyHolder(View itemView, SwipeRowAdapter adapter, SwipeAdapterCallback listener) {
            super(itemView, adapter, listener);
            tv = (TextView) itemView.findViewById(R.id.tv_example);
        }

        public void bindView(String text){
            tv.setText(text);
        }
    }

    private ArrayList<String> mList;

    public MyAdapter(Context context, SwipeAdapterCallback listener, LinearLayoutManager lm, ArrayList<String> list) {
        super(context, listener, lm);
        mList = list;
    }

    @Override
    protected MyHolder onCreateSwipeViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_row,parent,false);
        MyHolder holder = new MyHolder(v,this,this.mSwipeAdapterCallback);
        return holder;
    }

    @Override
    protected void onBindSwipeViewHolder(MyHolder holder, int position) {
        holder.bindView(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    protected int getSwipeRowContainerId() {
        return R.id.container;
    }

    @Override
    protected int getSwipeRowRightSelectActionId() {
        return R.id.v_right;
    }

    @Override
    protected int getSwipeRowLeftSelectActionId() {
        return R.id.v_left;
    }

    @Override
    protected int getSwipeBottomViewId() {
        return R.id.v_bottom;
    }

    @Override
    protected int getSelectedLineColorId() {
        return R.color.colorAccent;
    }

    @Override
    protected boolean isSwipeLeftEnabled(int listPosition) {
        return true;
    }

    @Override
    protected boolean isSwipeRightEnabled(int listPosition) {
        return true;
    }
}
