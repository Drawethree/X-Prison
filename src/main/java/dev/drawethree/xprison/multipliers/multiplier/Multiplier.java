package dev.drawethree.xprison.multipliers.multiplier;

import java.util.Date;

/**
 * Represents a multiplier that can affect various aspects such as sell price or tokens.
 */
public interface Multiplier {

    /**
     * Gets the current multiplier value.
     *
     * @return the multiplier value
     */
    double getMultiplier();

    /**
     * Sets the multiplier to a new value.
     *
     * @param newValue the new multiplier value to set
     */
    void setMultiplier(double newValue);

    /**
     * Adds a value to the current multiplier.
     *
     * @param addition the value to add to the multiplier
     */
    void addMultiplier(double addition);

    /**
     * Checks if the multiplier is currently active.
     *
     * @return true if active, false otherwise
     */
    boolean isActive();

    /**
     * Resets the multiplier to its default state.
     */
    void reset();

    /**
     * Gets the date when the multiplier ends.
     *
     * @return the end date of the multiplier
     */
    Date getEndDate();
}
