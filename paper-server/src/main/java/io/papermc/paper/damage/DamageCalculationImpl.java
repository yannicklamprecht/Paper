package io.papermc.paper.damage;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;

/**
 * The ordered, mutable damage calculation for this entity damage event.
 * <p>
 * Changes to a stage cause later stages to recalculate when their values are
 * next read. Some vanilla stages are present only for certain damage types.
 * Use {@link #before(Key)} to add a plugin adjustment at a specific point.
 *
 * @see org.bukkit.event.entity.EntityDamageEvent#getDamageCalculation()
 */
public final class DamageCalculationImpl implements DamageCalculation{

    private final List<DamageStageImpl> stages = new ArrayList<>();
    private final double originalBaseDamage;
    private double originalAmount;
    private double baseDamage;
    private double finalDamage;
    private boolean dirty;

    /**
     * Creates a calculation with the raw damage supplied by the server.
     *
     * @param baseDamage the original raw damage
     */
    @ApiStatus.Internal
    public DamageCalculationImpl(final double baseDamage) {
        this.originalBaseDamage = baseDamage;
        this.originalAmount = baseDamage;
        this.baseDamage = baseDamage;
        this.finalDamage = baseDamage;
    }

    /**
     * Adds a vanilla stage while constructing an event's calculation.
     *
     * @param key            the stage identifier
     * @param originalChange the signed change calculated by vanilla
     * @param formula        the vanilla calculation for a new input
     * @return the added stage
     * @throws IllegalArgumentException if the key is null, already used, or the formula is null
     */
    @ApiStatus.Internal
    public DamageStageImpl addStage(final Key key, final double originalChange, final DoubleUnaryOperator formula) {
        Preconditions.checkArgument(key != null, "key cannot be null");
        Preconditions.checkArgument(formula != null, "formula cannot be null");
        Preconditions.checkArgument(this.stage(key).isEmpty(), "Damage stage already exists: %s", key);
        final DamageStageImpl stage = new DamageStageImpl(this, key, formula, this.originalAmount, originalChange);
        this.stages.add(stage);
        this.originalAmount += originalChange;
        if (this.dirty) {
            this.resolve();
        } else {
            this.finalDamage = this.originalAmount;
        }
        return stage;
    }

    /**
     * Adds a vanilla stage and calculates its original change from the current
     * original input. This method is for constructing an event's calculation.
     *
     * @param key     the stage identifier
     * @param formula the vanilla calculation for a stage input
     * @return the added stage
     * @throws IllegalArgumentException if the key is null, already used, or the formula is null
     */
    @ApiStatus.Internal
    public DamageStageImpl addStage(final Key key, final DoubleUnaryOperator formula) {
        Preconditions.checkArgument(formula != null, "formula cannot be null");
        return this.addStage(key, formula.applyAsDouble(this.originalAmount), formula);
    }


    @Override
    public double getOriginalBaseDamage() {
        return this.originalBaseDamage;
    }

   @Override
    public double getBaseDamage() {
        return this.baseDamage;
    }


    @Override
    public void setBaseDamage(final double damage) {
        this.baseDamage = damage;
        this.invalidate();
    }

    @Override
    public double getFinalDamage() {
        this.resolve();
        return this.finalDamage;
    }


    @Override
    public List<DamageStage> stages() {
        return List.copyOf(this.stages);
    }

    @Override
    public Optional<DamageStage> stage(final Key key) {
        Preconditions.checkArgument(key != null, "key cannot be null");
        for (final DamageStageImpl stage : this.stages) {
            if (stage.key().equals(key)) {
                return Optional.of(stage);
            }
        }
        return Optional.empty();
    }


    @Override
    public InsertionPointImpl before(final Key key) {
        final DamageStageImpl anchor = (DamageStageImpl) this.stage(key).orElseThrow(() -> new IllegalArgumentException("Damage stage is not applicable: " + key));
        return new InsertionPointImpl(anchor);
    }

    void invalidate() {
        this.dirty = true;
    }

    void resolve() {
        if (!this.dirty) {
            return;
        }
        double amount = this.baseDamage;
        for (final DamageStageImpl stage : this.stages) {
            stage.input = amount;
            stage.change = stage.calculate(amount);
            amount += stage.change;
        }
        this.finalDamage = amount;
        this.dirty = false;
    }

    /**
     * A position before an applicable stage in this calculation.
     */
    public final class InsertionPointImpl implements InsertionPoint {

        private final DamageStageImpl anchor;

        private InsertionPointImpl(final DamageStageImpl anchor) {
            this.anchor = anchor;
        }


        @Override
        public DamageStage add(final Key key, final DamageAdjustment adjustment) {
            Preconditions.checkArgument(key != null, "key cannot be null");
            Preconditions.checkArgument(adjustment != null, "adjustment cannot be null");
            Preconditions.checkArgument(DamageCalculationImpl.this.stage(key).isEmpty(), "Damage stage already exists: %s", key);
            DamageCalculationImpl.this.resolve();
            final double input = this.anchor.getInput();
            final DoubleUnaryOperator formula = adjustment.formula();
            final DamageStageImpl stage = new DamageStageImpl(DamageCalculationImpl.this, key, formula, input, formula.applyAsDouble(input));
            DamageCalculationImpl.this.stages.add(DamageCalculationImpl.this.stages.indexOf(this.anchor), stage);
            DamageCalculationImpl.this.invalidate();
            return stage;
        }
    }
}
