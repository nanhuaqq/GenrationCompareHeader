
package cn.mucang.genrationcompareheader;


/**
 * Utility class used to handle flinging within a specified limit.
 */
public abstract class BjDynamics {
    /**
     * The maximum delta time, in milliseconds, between two updates
     */
    private static final int MAX_TIMESTEP = 50;

    /** The current position */
    protected float mPosition;

    /** The current velocity */
    protected float mVelocity;

    /** The time of the last update */
    protected long mLastTime = 0;

    protected float limitEveryScroll = 0;
    private int visibleCount = 0;
    protected  float mOldPosition;

    public float getLimitEveryScroll() {
        return limitEveryScroll;
    }

    public void setLimitEveryScroll(float limitEveryScroll) {
        this.limitEveryScroll = limitEveryScroll;
    }

    public int getVisibleCount() {
        return visibleCount;
    }

    public void setVisibleCount(int visibleCount) {
        this.visibleCount = visibleCount;
    }


    /**
     * Sets the state of the dynamics object. Should be called before starting
     * to call update.
     *
     * @param position The current position.
     * @param velocity The current velocity in pixels per second.
     * @param now The current time
     */
    public void setState(final float position, final float velocity, final long now) {
        mVelocity = velocity;
        mOldPosition = mPosition = position;
        mLastTime = now;
    }

    /**
     * Returns the current position. Normally used after a call to update() in
     * order to get the updated position.
     *
     * @return The current position
     */
    public float getPosition() {
        return mPosition;
    }

    public boolean isAtRest(final float velocityTolerance, final float positionTolerance) {
        final boolean standingStill = Math.abs(mVelocity) < velocityTolerance;
        final boolean withinLimits = (Math.abs(mOldPosition-mPosition) < positionTolerance);
        return standingStill && withinLimits;
    }

    /**
     * Gets the velocity. Unit is in pixels per second.
     *
     * @return The velocity in pixels per second
     */
    public float getVelocity() {
        return mVelocity;
    }


    /**
     * Updates the position and velocity.
     *
     * @param now The current time
     */
    public void update(final long now) {
        int dt = (int)(now - mLastTime);
        if (dt > MAX_TIMESTEP) {
            dt = MAX_TIMESTEP;
        }

        onUpdate(dt);

        mLastTime = now;
    }


    /**
     * Updates the position and velocity.
     *
     * @param dt The delta time since last time
     */
    abstract protected void onUpdate(int dt);
}
