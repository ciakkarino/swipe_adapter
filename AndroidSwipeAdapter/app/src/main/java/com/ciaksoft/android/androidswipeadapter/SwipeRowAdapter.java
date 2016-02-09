package com.ciaksoft.android.androidswipeadapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Nunzio on 09/02/2016.
 */
public abstract class SwipeRowAdapter<T extends SwipeRowAdapter.SwipeRowAdapterHolder> extends RecyclerView.Adapter<SwipeRowAdapter.SwipeRowAdapterHolder>{

    /**
     * Utility class with direction constants
     */
    public static class SwipeDirection{
        public final static int RIGHT = 1;
        public final static int LEFT = 2;
    }

    public static class SwipeRowAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener{

        public int sliderCounter;
        public float currentX;
        public boolean ignoreClick;
        public int tollerance;

        //adaper
        SwipeRowAdapter mHolderAdapter;
        //listener
        SwipeAdapterCallback mHolderListener;


        public SwipeRowAdapterHolder(View itemView,SwipeRowAdapter adapter,SwipeAdapterCallback listener) {
            super(itemView);
            mHolderAdapter = adapter;
            mHolderListener = listener;
            if(mHolderAdapter.getOnTouchRowPanelId() > 0){
                itemView.findViewById(mHolderAdapter.getOnTouchRowPanelId()).setOnTouchListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            if (!ignoreClick) {
                mHolderListener.onRowItemClicked(v,getAdapterPosition());
            } else {
                ignoreClick = false;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            this.ignoreClick = true;
            if (sliderCounter <= tollerance && sliderCounter >= -tollerance) {
                return mHolderListener.onRowItemLongClicked(v, getAdapterPosition());
            } else {
                return false;
            }
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return mHolderAdapter.listenOnTouch(view, motionEvent, getAdapterPosition(), this);
        }


        public void clearMotionValue() {
            sliderCounter = 0;
            currentX = 0;
        }
    }

    /**
     * Base callback for Abstract total adapter
     */
    public interface SwipeAdapterCallback {

        /**
         * Called on click on row item
         *
         * @param clickedView
         * @param listPosition
         */
        void onRowItemClicked(View clickedView, int listPosition);

        /**
         * Called on click on row item
         *
         * @param clickedView
         * @param listPosition
         * @return
         */
        boolean onRowItemLongClicked(View clickedView, int listPosition);

        /**
         * Called on swipe completed
         * @param direction Contstant in SwipeDirection
         * @param listPosition the position in the list
         */
        void onSwipeComplete(int direction, int listPosition);

        /**
         * Called on swipe in progress
         * @param swipeDirection
         * @param swipeContainer
         * @param swipeBottom
         */
        void onSwipeProgress(int swipeDirection, View swipeContainer, View swipeBottom);

    }

    protected Context mContext;
    protected int trasparent;
    protected SwipeTouchSupport mSwipeTouchSupport;
    protected SwipeAdapterCallback mSwipeAdapterCallback;
    protected LinearLayoutManager mLayouManager;

    public SwipeRowAdapter(Context context, SwipeAdapterCallback listener, LinearLayoutManager lm) {
        mContext = context;
        mSwipeAdapterCallback = listener;
        mLayouManager = lm;
        mSwipeTouchSupport = new SwipeTouchSupport();
        trasparent = retrieveColor(android.R.color.transparent);
    }

    @TargetApi(23)
    private int retrieveColor(int colorId){
        if (android.os.Build.VERSION.SDK_INT >= 23){
            return mContext.getResources().getColor(android.R.color.transparent, mContext.getTheme());
        }else{
            return mContext.getResources().getColor(android.R.color.transparent);
        }
    }

    //OVERRIDABLE METHODS****************************************************************************

    /**
     * You must override this method if you want move your row with on touch
     *
     * @return
     */
    protected int getOnTouchRowPanelId() {
        return 0;
    }

    /**
     * You must override this method if you want move your row with on touch
     *
     * @return
     */
    protected int getSwipeRowContainerId() {
        return 0;
    }

    /**
     * You must override this method if you want show select action view after row scroll to right
     *
     * @return
     */
    protected int getSwipeRowRightSelectActionId() {
        return 0;
    }


    /**
     * You must override this method if you want show select action view after row scroll to left
     *
     * @return
     */
    protected int getSwipeRowLeftSelectActionId() {
        return 0;
    }


    /**
     * You must override this method if you want manage scroll bottom view
     *
     * @return
     */
    protected int getSwipeBottomViewId() {
        return 0;
    }

    /**
     * Return select line hover color in this method
     *
     * @return
     */
    protected int getSelectedLineColorId() {
        return 0;
    }

    /**
     * Return false if you want lock left scroll
     *
     * @return
     */
    protected boolean isSwipeLeftEnabled(int listPosition) {
        return true;
    }

    /**
     * Return false if you want lock right scroll
     *
     * @return
     */
    protected boolean isSwipeRightEnabled(int listPosition) {
        return true;
    }


    //*************FINAL METHOD**********************


    @Override
    public SwipeRowAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return this.onCreateSwipeViewHolder(parent,viewType);
    }

    @Override
    public void onBindViewHolder(SwipeRowAdapterHolder holder, int position) {
        onBindSwipeViewHolder((T)holder,position);
    }

    /**
     * Analog or onCreateViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    protected abstract T onCreateSwipeViewHolder(ViewGroup parent, int viewType);

    /**
     * Analog of onBindViewHolder
     * @param holder
     * @param position
     */
    protected abstract void onBindSwipeViewHolder(T holder, int position);


    /**
     * Listen SwipeAdapterCallback
     *
     * @param listener
     */
    public void setSwipeCallback(SwipeAdapterCallback listener) {
        this.mSwipeAdapterCallback = listener;
    }

    /**
     * @param v
     * @param event
     * @return
     */
    public boolean listenOnTouch(View v, MotionEvent event, int listPosition, SwipeRowAdapterHolder listener) {
        if (getSwipeRowContainerId() == 0 || mSwipeTouchSupport == null) {
            return false;
        }
        boolean toReturn = true;
        try {
            //Get touched child
            int diff = listPosition;
            if (listPosition != this.mLayouManager.findFirstVisibleItemPosition()) {
                diff = listPosition - this.mLayouManager.findFirstVisibleItemPosition();
            }
            View child = this.mLayouManager.getChildAt(diff);
            //*************
            float tmpX = event.getX(event.getActionIndex());
            float moveDifference = 0;
            //get swipe container
            View container = child.findViewById(getSwipeRowContainerId());
            //get swipe bottom view
            View bottomScroll = child.findViewById(getSwipeBottomViewId());
            //get select action view
            View rowTouchPanel = child.findViewById(getOnTouchRowPanelId());
            View scrollLeftSelectActionView = child.findViewById(getSwipeRowLeftSelectActionId());
            View scrollRightSelectActionView = child.findViewById(getSwipeRowRightSelectActionId());
            if ((scrollLeftSelectActionView != null && scrollLeftSelectActionView.getVisibility() == View.VISIBLE) ||
                    (scrollRightSelectActionView != null && scrollRightSelectActionView.getVisibility() == View.VISIBLE)) {
                toReturn = true;
                float yOffset = 0;
                int counter = 0;
                while (counter < diff) {
                    View c = this.mLayouManager.getChildAt(counter);
                    yOffset += (c.getHeight());
                    if (c.getY() < 0) {
                        yOffset += (this.mLayouManager.getChildAt(counter).getY());
                    }
                    counter++;
                }
                MotionEvent newEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getX(), event.getY() - yOffset, event.getMetaState());//container.getHeight()/2
                if (scrollLeftSelectActionView.getVisibility() == View.VISIBLE) {
                    if(!scrollLeftSelectActionView.dispatchTouchEvent(newEvent)){
                        if(event.getActionMasked() == MotionEvent.ACTION_UP){
                            mSwipeTouchSupport.onSwipeStopped(listPosition,container,false);
                        }
                    }
                } else {
                    if(!scrollRightSelectActionView.dispatchTouchEvent(newEvent)){
                        if(event.getActionMasked() == MotionEvent.ACTION_UP){
                            mSwipeTouchSupport.onSwipeStopped(listPosition,container,false);
                        }
                    }
                }
                newEvent.recycle();
            } else {
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        listener.ignoreClick = false;
                        listener.currentX = tmpX;
                        if (getSelectedLineColorId() != 0) {
                            rowTouchPanel.setBackgroundColor(getSelectedLineColor());
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moveDifference = tmpX - listener.currentX;
                        listener.currentX = tmpX;
                        if ((listener.sliderCounter + moveDifference < 0 && isSwipeLeftEnabled(listPosition)) ||
                                (listener.sliderCounter + moveDifference > 0 && isSwipeRightEnabled(listPosition))) {
                            listener.sliderCounter += moveDifference;
                            if (listener.sliderCounter > 0) {
                                mSwipeAdapterCallback.onSwipeProgress(SwipeDirection.RIGHT,container, bottomScroll);
                            } else {
                                mSwipeAdapterCallback.onSwipeProgress(SwipeDirection.LEFT,container, bottomScroll);
                            }
                            int tollerance = (int) ((float) v.getWidth() / 100.0f * 5);
                            listener.tollerance = tollerance;
                            if ((listener.sliderCounter > tollerance) || (listener.sliderCounter < (-tollerance)) || container.getX() != 0) {
                                if (container.getX() == 0) {
                                    mSwipeTouchSupport.onSwipeStarted(listPosition, v, listener);
                                    toReturn = false;
                                }
                                container.setX(listener.sliderCounter);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        rowTouchPanel.setBackgroundColor(trasparent);
                        if (container.getX() != 0) {
                            listener.ignoreClick = true;
                        }
                        if (listener.sliderCounter > ((v.getWidth() / 10.0f) * 4.0f)) {
                            listener.clearMotionValue();
                            mSwipeTouchSupport.onRightSwipe(listPosition, container);
                            listener.ignoreClick = false;
                        } else if (listener.sliderCounter < -((v.getWidth() / 10.0f) * 4.0f)) {
                            listener.clearMotionValue();
                            mSwipeTouchSupport.onLeftSwipe(listPosition, container);
                            listener.ignoreClick = false;
                        } else {
                            listener.clearMotionValue();
                            mSwipeTouchSupport.onSwipeStopped(listPosition, container, false);
                        }
                        toReturn = false;
                        break;
                    default:
                        toReturn = false;
                        break;
                }
                container.dispatchTouchEvent(event);
            }
            return toReturn;
        } catch (Exception e) {
            e.printStackTrace();
            if(listener != null){
                listener.clearMotionValue();
            }
            v.setX(0);
            mSwipeTouchSupport.onSwipeStopped(-1, null, true);
            return false;
        }
    }

    /**
     * Return Color of selected line
     *
     * @return
     */
    private int getSelectedLineColor() {
        if (getSelectedLineColorId() != 0) {
            return retrieveColor(getSelectedLineColorId());
        }
        return 0;
    }

    /**
     * Get scroll container
     *
     * @param position
     * @return
     */
    public View getScrollContainerByPosition(int position) {
        int diff = position;
        if (position != mLayouManager.findFirstVisibleItemPosition()) {
            diff = position - mLayouManager.findFirstVisibleItemPosition();
        }
        View child = mLayouManager.getChildAt(diff).findViewById(getSwipeRowContainerId());
        return child;
    }


    /**
     * Show selectScrollActionView
     *
     * @param position
     */
    public View showAndGetSelectScrollLeftActionView(int position) {
        int diff = position;
        if (position != mLayouManager.findFirstVisibleItemPosition()) {
            diff = position - mLayouManager.findFirstVisibleItemPosition();
        }
        View child = mLayouManager.getChildAt(diff).findViewById(getSwipeRowLeftSelectActionId());
        child.setVisibility(View.VISIBLE);
        return child;
    }

    /**
     * Show selectScrollActionView
     *
     * @param position
     */
    public View hideAndGetSelectScrollLeftActionView(int position) {
        int diff = position;
        if (position != mLayouManager.findFirstVisibleItemPosition()) {
            diff = position - mLayouManager.findFirstVisibleItemPosition();
        }
        View child = mLayouManager.getChildAt(diff).findViewById(getSwipeRowLeftSelectActionId());
        child.setVisibility(View.GONE);
        return child;
    }


    /**
     * Show selectScrollActionView
     *
     * @param position
     */
    public View showAndGetSelectScrollRightActionView(int position) {
        int diff = position;
        if (position != mLayouManager.findFirstVisibleItemPosition()) {
            diff = position - mLayouManager.findFirstVisibleItemPosition();
        }
        View child = mLayouManager.getChildAt(diff).findViewById(getSwipeRowRightSelectActionId());
        child.setVisibility(View.VISIBLE);
        return child;
    }

    /**
     * Show selectScrollActionView
     * NB. if this view is visible row scroll don't work
     *
     * @param position
     */
    public View hideAndGetSelectScrollRightActionView(int position) {
        int diff = position;
        if (position != mLayouManager.findFirstVisibleItemPosition()) {
            diff = position - mLayouManager.findFirstVisibleItemPosition();
        }
        View child = mLayouManager.getChildAt(diff).findViewById(getSwipeRowRightSelectActionId());
        child.setVisibility(View.GONE);
        return child;
    }

    /**
     * Signal to {@link SwipeTouchSupport} that ScrollRow listen finished
     */
    public void cancelRowScrollListenMode() {
        if (mSwipeTouchSupport != null) {
            mSwipeTouchSupport.cancelRowSwipeMode();
        }
    }


    /**
     * Utility class that implements onTouch listener and provide all functions to manage the row swipe animation
     *
     * @author Nunzio
     */
    public class SwipeTouchSupport{


        private int scrollRowPosition;
        private SwipeRowAdapterHolder mSwipeViewHolder;
        private boolean scrollRowMode;
        private ScrollAnimationHandler handler;


        public SwipeTouchSupport() {
            handler = new ScrollAnimationHandler();
        }

        /**
         * Signal swipe start
         * @param position
         * @param row
         * @param holder
         */
        public void onSwipeStarted(int position, View row, SwipeRowAdapterHolder holder) {
            scrollRowMode = true;
            scrollRowPosition = position;
            mSwipeViewHolder = holder;
        }

        /**
         * Signal swipe stop
         * @param position
         * @param container
         * @param withoutAnimation
         */
        public void onSwipeStopped(int position, View container, boolean withoutAnimation) {
            if (container != null && container.getX() != 0) {
                new RowAnimationStoppedThread(position, container.getX(), handler, withoutAnimation).start();
            } else {
                cancelRowSwipeMode();
            }
        }

        /**
         * Cancel row swipe
         */
        private void cancelRowSwipeMode() {
            scrollRowMode = false;
            scrollRowPosition = 0;
            mSwipeViewHolder = null;
        }

        /**
         * Manage swipe animation
         * @param position
         * @param container
         */
        public void onLeftSwipe(int position, View container) {
            new RowAnimationScrollThread(container.getX(), container.getWidth(), handler, position).start();
        }

        /**
         * Manage swipe animation
         * @param position
         * @param container
         */
        public void onRightSwipe(int position, View container) {
            new RowAnimationScrollThread(container.getX(), container.getWidth(), handler, position).start();
        }

        /**
         * Manage left swipe completed animation
         * @param position
         */
        private void onLeftSwipeComplete(int position) {
            if (getSwipeRowLeftSelectActionId() == 0) {
                cancelRowSwipeMode();
            } else {
                showAndGetSelectScrollLeftActionView(position);
            }
            mSwipeAdapterCallback.onSwipeComplete(SwipeDirection.LEFT, position);
        }

        /**
         * Manage right swipe completed animation
         * @param position
         */
        private void onRightSwipeComplete(int position) {
            if (getSwipeRowRightSelectActionId() == 0) {
                cancelRowSwipeMode();
            } else {
                showAndGetSelectScrollRightActionView(position);
            }
            mSwipeAdapterCallback.onSwipeComplete(SwipeDirection.RIGHT, position);
        }


        /**
         * Handler that manage the scroll animations
         */
        public class ScrollAnimationHandler extends Handler {

            private final String TAG = ScrollAnimationHandler.class.getSimpleName();

            public final static int SCROLL_STOPPED_CODE = 1;
            public final static int SCROLL_ANIMATION_CONTINUE = 2;
            public final static int SCROLL_ANIMATION_FINISH = 3;
            public final static int SCROLL_STOPPED_FINISH = 4;

            @Override
            public void handleMessage(Message msg) {
                try {
                    int position = 0;
                    int diff = 0;
                    View child = null;
                    float offset = 0;
                    float childX = 0;
                    float x = 0;
                    switch (msg.what) {
                        case SCROLL_STOPPED_CODE:
                            position = msg.getData().getInt("position");
                            diff = position;
                            if (position != mLayouManager.findFirstVisibleItemPosition()) {
                                diff = position - mLayouManager.findFirstVisibleItemPosition();
                            }
                            child = mLayouManager.getChildAt(diff).findViewById(getSwipeRowContainerId());
                            if (getSwipeRowLeftSelectActionId() != 0) {
                                hideAndGetSelectScrollLeftActionView(position);
                            }
                            if (getSwipeRowRightSelectActionId() != 0) {
                                hideAndGetSelectScrollRightActionView(position);
                            }
                            offset = msg.getData().getFloat("offset");
                            childX = child.getX();
                            x = 0.0f;
                            x = childX + offset;
                            if (offset > 0 && x > 0) {
                                x = 0;
                            } else if (offset < 0 && x < 0) {
                                x = 0;
                            }
                            child.setX(x);
                            break;
                        case SCROLL_STOPPED_FINISH:
                            if (msg.getData().getBoolean("close")) {
                                position = msg.getData().getInt("position");
                                diff = position;
                                if (position != mLayouManager.findFirstVisibleItemPosition()) {
                                    diff = position - mLayouManager.findFirstVisibleItemPosition();
                                }
                                child = mLayouManager.getChildAt(diff).findViewById(getSwipeRowContainerId());
                                child.setX(0);
                            }
                            cancelRowSwipeMode();
                            break;
                        case SCROLL_ANIMATION_CONTINUE:
                            position = msg.getData().getInt("position");
                            diff = position;
                            if (position != mLayouManager.findFirstVisibleItemPosition()) {
                                diff = position - mLayouManager.findFirstVisibleItemPosition();
                            }
                            child = mLayouManager.getChildAt(diff).findViewById(getSwipeRowContainerId());
                            offset = msg.getData().getFloat("offset");
                            childX = child.getX();
                            x = 0.0f;
                            x = childX + offset;
                            child.setX(x);
                            break;
                        case SCROLL_ANIMATION_FINISH:
                            position = msg.getData().getInt("position");
                            diff = position;
                            if (position != mLayouManager.findFirstVisibleItemPosition()) {
                                diff = position - mLayouManager.findFirstVisibleItemPosition();
                            }
                            child = mLayouManager.getChildAt(diff).findViewById(getSwipeRowContainerId());
                            offset = msg.getData().getFloat("offset");
                            if (child.getX() > 0) {
                                child.setX(child.getWidth());
                                onRightSwipeComplete(position);
                            } else if (child.getX() < 0) {
                                child.setX(-child.getWidth());
                                onLeftSwipeComplete(position);
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "animation handler-> Exception during end animation");
                    e.printStackTrace();
                }
            }

        }

        /**
         * Scroll animation thread
         */
        private class RowAnimationScrollThread extends Thread {

            private final String TAG = RowAnimationScrollThread.class.getSimpleName();

            private float currentX;
            private float width;
            private ScrollAnimationHandler h;
            private int position;


            public RowAnimationScrollThread(float currentX, float width, ScrollAnimationHandler h, int position) {
                super();
                this.currentX = currentX;
                this.width = width;
                this.h = h;
                this.position = position;
            }


            @Override
            public void run() {
                float offset = 0;
                float distance = 0;
                float sign = 1;
                if (currentX > 0) {
                    distance = this.width - this.currentX;
                } else {
                    distance = this.width + this.currentX;
                    sign = -1;
                }
                offset = (distance) * 10.0f / 300.0f;
                Bundle data = new Bundle();
                data.putFloat("offset", sign * offset);
                data.putInt("position", this.position);
                while (distance >= 0.0f) {
                    distance -= offset;
                    Message msg = Message.obtain(h, ScrollAnimationHandler.SCROLL_ANIMATION_CONTINUE);
                    msg.setData(data);
                    msg.sendToTarget();
                    try {
                        sleep(5);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "RowAnimationThread-> Exception during close animation: " + e.getMessage());
                    }
                }
                Message msg = Message.obtain(h, ScrollAnimationHandler.SCROLL_ANIMATION_FINISH);
                msg.setData(data);
                msg.sendToTarget();
            }


        }

        /**
         * Stop animation thread
         */
        private class RowAnimationStoppedThread extends Thread {

            private final String TAG = RowAnimationStoppedThread.class.getSimpleName();

            private float currentX;
            private ScrollAnimationHandler h;
            private int position;
            private boolean withoutAnimation;

            public RowAnimationStoppedThread(int position, float currentX, ScrollAnimationHandler h, boolean withoutAnimation) {
                super();
                this.currentX = currentX;
                this.h = h;
                this.position = position;
                this.withoutAnimation = withoutAnimation;
            }

            @Override
            public void run() {
                if (withoutAnimation) {
                    Bundle data = new Bundle();
                    data.putBoolean("close", true);
                    data.putInt("position", this.position);
                    Message msg = Message.obtain(h, ScrollAnimationHandler.SCROLL_STOPPED_FINISH);
                    msg.setData(data);
                    msg.sendToTarget();
                } else {
                    float offset = 0;
                    float distance = 0;
                    float sign = 1;
                    if (currentX > 0) {
                        distance = this.currentX;
                        sign = -1;
                    } else {
                        distance = -this.currentX;
                    }
                    offset = (distance * 5.0f) / 300.0f;
                    Bundle data = new Bundle();
                    data.putFloat("offset", sign * offset);
                    data.putBoolean("close", false);
                    data.putInt("position", this.position);
                    while (distance >= 0.0f) {
                        distance -= offset;
                        Message msg = Message.obtain(h, ScrollAnimationHandler.SCROLL_STOPPED_CODE);
                        msg.setData(data);
                        msg.sendToTarget();
                        try {
                            sleep(5);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "RowAnimationThread-> Exception during close animation: " + e.getMessage());
                        }
                    }
                    Message msg = Message.obtain(h, ScrollAnimationHandler.SCROLL_STOPPED_FINISH);
                    msg.setData(data);
                    msg.sendToTarget();
                }
            }

        }


    }

}
