package io.papermc.paper.damage;

import net.kyori.adventure.key.Key;

/**
 * One stage in an entity damage calculation. A stage contributes a signed
 * change to its input: negative changes reduce damage and positive changes
 * increase it. Its output becomes the input of the next stage in the calculation.
 *
 * @see DamageCalculation
 */
public interface DamageStage {

    /**
     * Gets this stage's identifier.
     *
     * @return the stage identifier
     */
    Key key();

    /**
     * Gets the damage entering this stage when the event was created. For a
     * plugin stage, this is its input when it was inserted.
     *
     * @return the original input
     */
    double getOriginalInput();

    /**
     * Gets this stage's original signed change. For a plugin stage, this is
     * the change its adjustment produced when it was inserted.
     *
     * @return the original signed change
     */
    double getOriginalChange();

    /**
     * Gets the damage after this stage has been applied.
     *
     * @return the current input plus this stage's change
     */
    double getOutput();

    /**
     * Replaces this stage's change with a fixed signed amount. This also
     * replaces previous edits to this stage, including edits by earlier event
     * listeners (e.g. from other plugins). This recalculates all later stages.
     *
     * @param change the signed damage change
     */
    void setChange(double change);

    /**
     * Gets the current damage entering this stage, including changes to all
     * preceding stages.
     *
     * @return the current input
     */
    double getInput();

    /**
     * Gets the current signed change contributed by this stage.
     *
     * @return the current signed change
     */
    double getChange();

    /**
     * Multiplies this stage's signed change, including edits made by earlier
     * event listeners. For example, {@code 0.5} halves the magnitude of a
     * reduction.
     *
     * @param multiplier the nonnegative multiplier
     * @throws IllegalArgumentException if the multiplier is negative or not finite
     */
    void multiplyChange(double multiplier);

    /**
     * Calculates the output of this stage for a given input.
     *
     * @param input the input damage
     * @return the output damage
     */
    double calculate(double input);
}
