package io.papermc.paper.damage;

import com.google.common.base.Preconditions;
import java.util.function.DoubleUnaryOperator;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * One stage in an entity damage calculation. A stage contributes a signed
 * change to its input: negative changes reduce damage and positive changes
 * increase it. Its output becomes the input of the next stage in the calculation.
 *
 * @see DamageCalculationImpl
 */
public final class DamageStageImpl implements DamageStage{

    private final DamageCalculationImpl calculation;
    private final Key key;
    private final DoubleUnaryOperator formula;
    private @Nullable DoubleUnaryOperator edits;
    private final double originalInput;
    private final double originalChange;
    double input;
    double change;

    DamageStageImpl(final DamageCalculationImpl calculation, final Key key, final DoubleUnaryOperator formula, final double originalInput, final double originalChange) {
        this.calculation = calculation;
        this.key = key;
        this.formula = formula;
        this.originalInput = originalInput;
        this.originalChange = originalChange;
        this.input = originalInput;
        this.change = originalChange;
    }


    @Override
    public Key key() {
        return this.key;
    }

    @Override
    public double getOriginalInput() {
        return this.originalInput;
    }

    @Override
    public double getOriginalChange() {
        return this.originalChange;
    }

    @Override
    public double getInput() {
        this.calculation.resolve();
        return this.input;
    }

    @Override
    public double getChange() {
        this.calculation.resolve();
        return this.change;
    }


    @Override
    public double getOutput() {
        this.calculation.resolve();
        return this.input + this.change;
    }

    @Override
    public void setChange(final double change) {
        this.edits = ignored -> change;
        this.calculation.invalidate();
    }


    @Override
    public void multiplyChange(final double multiplier) {
        Preconditions.checkArgument(Double.isFinite(multiplier) && multiplier >= 0, "multiplier must be finite and non-negative");
        final DoubleUnaryOperator edit = change -> change * multiplier;
        this.edits = this.edits == null ? edit : this.edits.andThen(edit);
        this.calculation.invalidate();
    }

    @Override
    public double calculate(final double input) {
        final double change = this.formula.applyAsDouble(input);
        return this.edits == null ? change : this.edits.applyAsDouble(change);
    }

    /**
     * Calculates the stage's unmodified vanilla change for a supplied input.
     *
     * @param input the damage entering the stage
     * @return the signed vanilla change
     */
    @ApiStatus.Internal
    public double calculateVanilla(final double input) {
        return this.formula.applyAsDouble(input);
    }
}
