package io.papermc.paper.damage;

import java.util.function.DoubleUnaryOperator;

/**
 * A formula that adjusts the damage of a {@link DamageCalculation}. The formula
 * is a function that takes the current damage as input and returns a signed
 * change to be applied to it. Negative changes reduce damage and positive
 * changes increase it.
 *
 * <p>Plugin stages are inserted into a calculation with a {@link DamageAdjustment}
 * and can be removed at any time. The final damage is the sum of the raw damage
 * and all changes contributed by each stage.
 *
 * @see DamageCalculation
 * @see DamageStage
 */
public interface DamageAdjustment {
    /**
     * Gets the formula that adjusts the damage.
     *
     * @return the damage adjustment formula
     */
    DoubleUnaryOperator formula();
}
