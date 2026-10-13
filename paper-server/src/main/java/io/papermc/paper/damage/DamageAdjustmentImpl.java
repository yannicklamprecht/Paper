package io.papermc.paper.damage;

import com.google.common.base.Preconditions;
import java.util.function.DoubleUnaryOperator;

/**
 * A fixed or proportional adjustment inserted into a damage calculation.
 * Damage can not be reduced below zero at the insertion point.
 *
 * @see DamageCalculationImpl#before(net.kyori.adventure.key.Key)
 */
public final class DamageAdjustmentImpl implements DamageAdjustment {

    private final DoubleUnaryOperator change;

    private DamageAdjustmentImpl(final DoubleUnaryOperator change) {
        this.change = change;
    }

    /**
     * Creates a fixed signed damage adjustment.
     *
     * @param change the signed damage change
     * @return a flat adjustment
     * @throws IllegalArgumentException if the change is not finite
     */
    public static DamageAdjustmentImpl flat(final double change) {
        Preconditions.checkArgument(Double.isFinite(change), "change must be finite");
        return new DamageAdjustmentImpl(input -> Math.max(0, input + change) - input);
    }

    /**
     * Creates an adjustment that multiplies the damage reaching its insertion
     * point. For example, {@code 0.5} halves the damage.
     *
     * @param multiplier the nonnegative multiplier
     * @return a proportional adjustment
     * @throws IllegalArgumentException if the multiplier is negative or not finite
     */
    public static DamageAdjustmentImpl multiply(final double multiplier) {
        Preconditions.checkArgument(Double.isFinite(multiplier) && multiplier >= 0, "multiplier must be finite and non-negative");
        return new DamageAdjustmentImpl(input -> input * multiplier - input);
    }

    @Override
    public DoubleUnaryOperator formula() {
        return this.change;
    }
}
