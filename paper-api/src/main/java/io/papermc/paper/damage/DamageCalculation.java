package io.papermc.paper.damage;

import java.util.Collection;
import java.util.Optional;
import net.kyori.adventure.key.Key;

/**
 * A damage calculation is a series of stages that are applied to an initial
 * raw damage value. Each stage contributes a signed change to the damage,
 * which is then passed to the next stage in the calculation.
 *
 * <p>Stages are identified by a {@link Key} and can be inserted or removed
 * from the calculation at any time. The final damage is the sum of the raw
 * damage and all changes contributed by each stage.
 *
 * @see DamageStage
 */
public interface DamageCalculation {

    /**
     * Finds an applicable stage by its identifier.
     *
     * @param key the stage identifier
     * @return the stage, or an empty optional if it does not apply to this hit
     * @throws IllegalArgumentException if the key is null
     */
    Optional<DamageStage> stage(Key key);

    /**
     * Gets the raw damage at the time the event was created.
     *
     * @return the original raw damage
     */
    double getOriginalBaseDamage();

    /**
     * Sets the raw damage and recalculates all later stages.
     *
     * @param damage the new raw damage
     */
    void setBaseDamage(double damage);

    /**
     * Gets the raw damage entering this calculation.
     *
     * @return the current raw damage
     */
    double getBaseDamage();

    /**
     * Gets the damage remaining after all stages have been applied.
     *
     * @return the final damage
     */
    double getFinalDamage();

    /**
     * Gets a snapshot of the stages in calculation order.
     *
     * @return the stages currently in this calculation
     */
    Collection<DamageStage> stages();

    /**
     * Selects the point immediately before an applicable stage. The selected
     * point can then be used to insert one or more plugin adjustments.
     *
     * @param key the identifier of the stage to insert before
     * @return an insertion point before that stage
     * @throws IllegalArgumentException if the key is null or the stage does not apply to this hit
     */
    InsertionPoint before(Key key);
}
