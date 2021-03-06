package cn.mucang.genrationcompareheader;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Adapter;
import android.widget.AdapterView;

import java.util.LinkedList;

/**
 * Created by nanhuaqq on 2016/3/24.
 */
public class BjHeaderMenuView extends AdapterView<Adapter>{

    /** 标示一个非法的child index*/
    private static final int INVALID_INDEX = -1;

    /** Distance to drag before we intercept touch events */
    private static final int TOUCH_SCROLL_THRESHOLD = 10;

    /** 在该mode下 children将会添加在最后一个chid的右边*/
    private static final int LAYOUT_MODE_RIGHT = 0;

    /** 在该mode下 children将会添加在第一个child的左边*/
    private static final int LAYOUT_MODE_LEFT = 1;

    /** 用户没有 touch 该list*/
    private static final int TOUCH_STATE_RESTING = 0;

    /** 用户正在点击该list*/
    private static final int TOUCH_STATE_CLICK = 1;
    /** 用户在滚动该list*/
    private static final int TOUCH_STATE_SCROLL = 2;

    private Adapter adapter;

    /** 当前的 触摸状态 */
    private int mTouchState = TOUCH_STATE_RESTING;
    /** 触摸点的 x坐标*/
    private int mTouchStartX;
    /** 触摸点的 y坐标*/
    private int mTouchStartY;

    /**
     * The left of the first item when the touch down event was received
     */
    private int mListLeftStart;

    /** The current left of the first item */
    private int mListLeft;

    /**
     * The offset from the left of the currently first visible item to the left of
     * the first item
     */
    private int mListLeftOffset;

    /** The adaptor position of the first visible item */
    private int mFirstItemPosition = 0;

    /** The adaptor position of the last visible item */
    private int mLastItemPosition;

    /** A list of cached (re-usable) item views */
    private final LinkedList<View> mCachedItemViews = new LinkedList<View>();

    /** Used to check for long press actions */
    private Runnable mLongPressRunnable;

    /** Reusable rect */
    private Rect mRect;

    private int visibleItemCount = 2;

    private int paddingLeftAndRight = 8;

    private VelocityTracker mVelocityTracker;

    private static final int PIXELS_PER_SECOND = 1000;

    public int getPaddingLeftAndRight() {
        return paddingLeftAndRight;
    }

    public void setPaddingLeftAndRight(int paddingLeftAndRight) {
        this.paddingLeftAndRight = paddingLeftAndRight;
    }

    public int getVisibleItemCount() {
        return visibleItemCount;
    }

    public void setVisibleItemCount(int visibleItemCount) {
        this.visibleItemCount = visibleItemCount;
    }


    public BjHeaderMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public Adapter getAdapter() {
        return adapter;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        removeAllViewsInLayout();
        requestLayout();
    }

    @Override
    public View getSelectedView() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setSelection(int position) {
        throw new UnsupportedOperationException("Not supported");
    }

    private int getChildMargin() {
        return paddingLeftAndRight / 2;
    }
    private int getChildLeft(View child) {
        return child.getLeft() - getChildMargin();
    }
    private int getChildRight(View child) {
        return child.getRight() + getChildMargin();
    }
    private int getChildWidth(View child) {
        return child.getMeasuredWidth() + 2 * getChildMargin();
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(event);
                return false;

            case MotionEvent.ACTION_MOVE:
                return startScrollIfNeeded(event);

            default:
                endTouch(0,0);
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (getChildCount() == 0) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if ( mTouchState != TOUCH_STATE_RESTING ){
                    return true;
                }
                startTouch(event);
                break;

            case MotionEvent.ACTION_MOVE:

                if (mTouchState == TOUCH_STATE_CLICK) {
                    startScrollIfNeeded(event);
                }
                if (mTouchState == TOUCH_STATE_SCROLL) {
                    mVelocityTracker.addMovement(event);
//                    mVelocityTracker.computeCurrentVelocity(PIXELS_PER_SECOND);
//                    float velocity = mVelocityTracker.getYVelocity();

                    scrollList((int)event.getX() - mTouchStartX);
                }
                break;

            case MotionEvent.ACTION_UP:
                float velocity = 0;
                if (mTouchState == TOUCH_STATE_CLICK) {
                    clickChildAt((int)event.getX(), (int)event.getY());
                } else if (mTouchState == TOUCH_STATE_SCROLL) {
                    mVelocityTracker.addMovement(event);
                    mVelocityTracker.computeCurrentVelocity(PIXELS_PER_SECOND);
                    velocity = mVelocityTracker.getYVelocity();
                    Log.e("qinqun", "mFirst=>" + mFirstItemPosition);
                    Log.e("qinqun", "mLast=>" + mLastItemPosition);

                    if ( event.getX() - mTouchStartX > 0 ){

//                       if ( mFirstItemPosition == 0 ){
//                           endTouch(mListLeft, (int)( mListLeft - (event.getX() - mTouchStartX) ));
//                        }else{
                           endTouch(mListLeft, (int) ( mListLeft + getWidth() / visibleItemCount - (event.getX() - mTouchStartX)));
//                       }

                    }else {
//                        if ( mLastItemPosition >= adapter.getCount()-1  ){
//                            endTouch(mListLeft, (int)( mListLeft - (event.getX() - mTouchStartX) ));
//                        }else{
                            endTouch(mListLeft, (int) ( mListLeft - getWidth() / visibleItemCount - (event.getX() - mTouchStartX)));
//                        }

                    }


                }

                break;

            default:
                endTouch(0,0);
                break;
        }
        return true;
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right,
                            final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // if we don't have an adapter, we don't need to do anything
        if (adapter == null) {
            return;
        }

        if (getChildCount() == 0) {
            mLastItemPosition = -1;
            fillListRight(mListLeft, 0);
        } else {
            final int offset = mListLeft + mListLeftOffset - getChildLeft(getChildAt(0));
            removeNonVisibleViews(offset);
            fillList(offset);
        }
        positionItems();
        invalidate();
    }

    /**
     * Sets and initializes all things that need to when we start a touch
     * gesture.
     *
     * @param event The down event
     */
    private void startTouch(final MotionEvent event) {

        // save the start place
        mTouchStartX = (int)event.getX();
        mTouchStartY = (int)event.getY();
        mListLeftStart = getChildLeft(getChildAt(0)) - mListLeftOffset;

        // start checking for a long press
        startLongPressCheck();

        // obtain a velocity tracker and feed it its first event
        mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);

        // we don't know if it's a click or a scroll yet, but until we know
        // assume it's a click
        mTouchState = TOUCH_STATE_CLICK;
    }

    /**
     * Resets and recycles all things that need to when we end a touch gesture
     */
    private void endTouch(int fromX,int toX) {

        // recycle the velocity tracker
        mVelocityTracker.recycle();
        mVelocityTracker = null;

        // remove any existing check for longpress
        removeCallbacks(mLongPressRunnable);

        smoothScrollTo(fromX, toX, 100, null);

        //重新定位scroll位置
        int modeResult = Math.abs(mListLeft) % (getWidth()/visibleItemCount);
        int slot = getWidth()/ (visibleItemCount * 2);
        if ( modeResult > slot ){
            smoothScrollTo(mListLeft,mListLeft - (getWidth()/2 -modeResult),50,null);
        }else if ( modeResult < slot ){
            smoothScrollTo(mListLeft,mListLeft + modeResult,50,null);
        }

                // reset touch state
        mTouchState = TOUCH_STATE_RESTING;
    }

    /**
     * Scrolls the list. Takes care of updating rotation (if enabled) and
     * snapping
     *
     * @param scrolledDistance The distance to scroll
     */
    private void scrollList(final int scrolledDistance) {
        mListLeft = mListLeftStart + scrolledDistance;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Posts (and creates if necessary) a runnable that will when executed call
     * the long click listener
     */
    private void startLongPressCheck() {
        // create the runnable if we haven't already
        if (mLongPressRunnable == null) {
            mLongPressRunnable = new Runnable() {
                public void run() {
                    if (mTouchState == TOUCH_STATE_CLICK) {
                        final int index = getContainingChildIndex(mTouchStartX, mTouchStartY);
                        if (index != INVALID_INDEX) {
                            longClickChild(index);
                        }
                    }
                }
            };
        }

        // then post it with a delay
        postDelayed(mLongPressRunnable, ViewConfiguration.getLongPressTimeout());
    }

    /**
     * Checks if the user has moved far enough for this to be a scroll and if
     * so, sets the list in scroll mode
     *
     * @param event The (move) event
     * @return true if scroll was started, false otherwise
     */
    private boolean startScrollIfNeeded(final MotionEvent event) {
        final int xPos = (int)event.getX();
        final int yPos = (int)event.getY();
        if (xPos < mTouchStartX - TOUCH_SCROLL_THRESHOLD
                || xPos > mTouchStartX + TOUCH_SCROLL_THRESHOLD
                || yPos < mTouchStartY - TOUCH_SCROLL_THRESHOLD
                || yPos > mTouchStartY + TOUCH_SCROLL_THRESHOLD) {
            // we've moved far enough for this to be a scroll
            removeCallbacks(mLongPressRunnable);
            mTouchState = TOUCH_STATE_SCROLL;
            return true;
        }
        return false;
    }

    /**
     * Returns the index of the child that contains the coordinates given.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return The index of the child that contains the coordinates. If no child
     *         is found then it returns INVALID_INDEX
     */
    private int getContainingChildIndex(final int x, final int y) {
        if (mRect == null) {
            mRect = new Rect();
        }
        for (int index = 0; index < getChildCount(); index++) {
            getChildAt(index).getHitRect(mRect);
            if (mRect.contains(x, y)) {
                return index;
            }
        }
        return INVALID_INDEX;
    }

    /**
     * Calls the item click listener for the child with at the specified
     * coordinates
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    private void clickChildAt(final int x, final int y) {
        final int index = getContainingChildIndex(x, y);
        if (index != INVALID_INDEX) {
            final View itemView = getChildAt(index);
            final int position = mFirstItemPosition + index;
            final long id = adapter.getItemId(position);
            performItemClick(itemView, position, id);
        }
    }

    /**
     * Calls the item long click listener for the child with the specified index
     *
     * @param index Child index
     */
    private void longClickChild(final int index) {
        final View itemView = getChildAt(index);
        final int position = mFirstItemPosition + index;
        final long id = adapter.getItemId(position);
        final OnItemLongClickListener listener = getOnItemLongClickListener();
        if (listener != null) {
            listener.onItemLongClick(this, itemView, position, id);
        }
    }

    /**
     * Removes view that are outside of the visible part of the list. Will not
     * remove all views.
     *
     * @param offset Offset of the visible area
     */
    private void removeNonVisibleViews(final int offset) {
        // We need to keep close track of the child count in this function. We
        // should never remove all the views, because if we do, we loose track
        // of were we are.
        int childCount = getChildCount();

        // if we are not at the right of the list and have more than one child
        if (mLastItemPosition != adapter.getCount() - 1 && childCount > 1) {
            // check if we should remove any views in the left
            View firstChild = getChildAt(0);
            while (firstChild != null && getChildRight(firstChild) + offset < 0) {
                // remove the left view
                removeViewInLayout(firstChild);
                childCount--;
                mCachedItemViews.addLast(firstChild);
                mFirstItemPosition++;

                // update the list offset (since we've removed the left child)
                mListLeftOffset += getChildWidth(firstChild);

                // Continue to check the next child only if we have more than
                // one child left
                if (childCount > 1) {
                    firstChild = getChildAt(0);
                } else {
                    firstChild = null;
                }
            }
        }

        // if we are not at the left of the list and have more than one child
        if (mFirstItemPosition != 0 && childCount > 1) {
            // check if we should remove any views in the bottom
            View lastChild = getChildAt(childCount - 1);
            while (lastChild != null && getChildLeft(lastChild) + offset > getWidth()) {
                // remove the bottom view
                removeViewInLayout(lastChild);
                childCount--;
                mCachedItemViews.addLast(lastChild);
                mLastItemPosition--;

                // Continue to check the next child only if we have more than
                // one child left
                if (childCount > 1) {
                    lastChild = getChildAt(childCount - 1);
                } else {
                    lastChild = null;
                }
            }
        }
    }

    /**
     * Checks if there is a cached view that can be used
     *
     * @return A cached view or, if none was found, null
     */
    private View getCachedView() {
        if (mCachedItemViews.size() != 0) {
            return mCachedItemViews.removeFirst();
        }
        return null;
    }

    /**
     * Fills the list with child-views
     *
     * @param offset Offset of the visible area
     */
    private void fillList(final int offset) {
        final int rightEdge = getChildRight(getChildAt(getChildCount() - 1));
        fillListRight(rightEdge, offset);

        final int leftEdge = getChildLeft(getChildAt(0));
        fillListLeft(leftEdge, offset);
    }

    /**
     * Starts at the bottom and adds children until we've passed the list bottom
     *
     * @param rightEdge The bottom edge of the currently last child
     * @param offset Offset of the visible area
     */
    private void fillListRight(int rightEdge, final int offset) {
        while (rightEdge + offset < getWidth() && mLastItemPosition < adapter.getCount() - 1) {
            mLastItemPosition++;
            final View newRightchild = adapter.getView(mLastItemPosition, getCachedView(), this);
            addAndMeasureChild(newRightchild, LAYOUT_MODE_RIGHT);
            rightEdge += getChildWidth(newRightchild);
        }
    }

    /**
     * Starts at the top and adds children until we've passed the list top
     *
     * @param leftEdge The top edge of the currently first child
     * @param offset Offset of the visible area
     */
    private void fillListLeft(int leftEdge, final int offset) {
        while (leftEdge + offset > 0 && mFirstItemPosition > 0) {
            mFirstItemPosition--;
            final View newLeftCild = adapter.getView(mFirstItemPosition, getCachedView(), this);
            addAndMeasureChild(newLeftCild, LAYOUT_MODE_LEFT);
            final int childWidth = getChildWidth(newLeftCild);
            leftEdge -= childWidth;

            // update the list offset (since we added a view at the top)
            mListLeftOffset -= childWidth;
        }
    }

    /**
     * Adds a view as a child view and takes care of measuring it
     *
     * @param child The view to add
     * @param layoutMode Either LAYOUT_MODE_ABOVE or LAYOUT_MODE_BELOW
     */
    private void addAndMeasureChild(final View child, final int layoutMode) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(getWidth()/visibleItemCount, LayoutParams.WRAP_CONTENT);
        }
        final int index = layoutMode == LAYOUT_MODE_LEFT ? 0 : -1;
        addViewInLayout(child, index, params, true);

        final int itemWidth = (getWidth()- 2 * visibleItemCount * getChildMargin()) /visibleItemCount;
        child.measure(MeasureSpec.EXACTLY | itemWidth, MeasureSpec.EXACTLY|itemWidth);
    }

    /**
     * Positions the children at the "correct" positions
     */
    private void positionItems() {
        int left = mListLeft + mListLeftOffset + getChildMargin();

        for (int index = 0; index < getChildCount(); index++) {
            final View child = getChildAt(index);

            final int width = getChildWidth(child);
            final int measureWidth = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();
            final int top = (getHeight() - height) / 2;

            child.layout(left, top, left + measureWidth, top + height);
            left += width;
        }
    }

    private SmoothScrollRunnable mCurrentSmoothScrollRunnable;

    private final void smoothScrollTo(int oldScrollValue,int newScrollValue, long duration,
                                      OnSmoothScrollFinishedListener listener) {
        if (null != mCurrentSmoothScrollRunnable) {
            mCurrentSmoothScrollRunnable.stop();
        }

        if (oldScrollValue != newScrollValue) {
            if (null == mScrollAnimationInterpolator) {
                // Default interpolator is a Decelerate Interpolator
                mScrollAnimationInterpolator = new DecelerateInterpolator();
            }
            mCurrentSmoothScrollRunnable = new SmoothScrollRunnable(oldScrollValue, newScrollValue, duration, listener);
            post(mCurrentSmoothScrollRunnable);

        }
    }

    private void setHeaderMenuScroll(int currentX){
        scrollList(currentX - mListLeftStart);
    }

    private Interpolator mScrollAnimationInterpolator = new DecelerateInterpolator();
    final class SmoothScrollRunnable implements Runnable {
        private final Interpolator mInterpolator;
        private final int mScrollToX;
        private final int mScrollFromX;
        private final long mDuration;
        private OnSmoothScrollFinishedListener mListener;

        private boolean mContinueRunning = true;
        private long mStartTime = -1;
        private int mCurrentX = -1;

        public SmoothScrollRunnable(int fromX, int toX, long duration, OnSmoothScrollFinishedListener listener) {
            mScrollFromX = fromX;
            mScrollToX = toX;
            mInterpolator = mScrollAnimationInterpolator;
            mDuration = duration;
            mListener = listener;
        }

        @Override
        public void run() {

            /**
             * Only set mStartTime if this is the first time we're starting,
             * else actually calculate the Y delta
             */
            if (mStartTime == -1) {
                mStartTime = System.currentTimeMillis();
            } else {

                /**
                 * We do do all calculations in long to reduce software float
                 * calculations. We use 1000 as it gives us good accuracy and
                 * small rounding errors
                 */
                long normalizedTime = (1000 * (System.currentTimeMillis() - mStartTime)) / mDuration;
                normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

                final int deltaX = Math.round((mScrollFromX - mScrollToX)
                        * mInterpolator.getInterpolation(normalizedTime / 1000f));
                mCurrentX = mScrollFromX - deltaX;
                setHeaderMenuScroll(mCurrentX);
            }

            // If we're not at the target Y, keep going...
            if (mContinueRunning && mScrollToX != mCurrentX) {
                ViewCompat.postOnAnimation(BjHeaderMenuView.this, this);
            } else {
                if (null != mListener) {
                    mListener.onSmoothScrollFinished();
                }
            }
        }

        public void stop() {
            mContinueRunning = false;
            removeCallbacks(this);
        }
    }

    static interface OnSmoothScrollFinishedListener {
        void onSmoothScrollFinished();
    }
}
