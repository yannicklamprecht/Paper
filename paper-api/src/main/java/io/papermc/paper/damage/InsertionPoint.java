package io.papermc.paper.damage;

import net.kyori.adventure.key.Key;

/**
 * An insertion point for adding a new stage to a damage calculation.
 * <p>
 * Insertion points are obtained from {@link DamageCalculation#before(Key)}. They allow adjustments to be added
 * before an existing stage in the calculation.
 *
 * @see DamageCalculation#before(Key)
 */
public interface InsertionPoint {


    /**
     * Inserts an adjustment before the selected stage. Adjustments added
     * through this insertion point run in insertion order. The identifier
     * must be unique within this calculation and may later be used with
     * {@link DamageCalculation#stage(Key)} or {@link DamageCalculation#before(Key)}.
     *
     * @param key        the unique identifier for this adjustment
     * @param adjustment the fixed or proportional damage adjustment
     * @return the inserted, mutable stage
     * @throws IllegalArgumentException if the key or adjustment is null, or
     *                                  the identifier is already in use
     */
    DamageStage add(Key key, DamageAdjustment adjustment);
}
