package io.papermc.paper.world.damagesource;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.papermc.paper.adventure.PaperAdventure;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.minecraft.Optionull;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.FellFromLocation;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record PaperCombatTrackerWrapper(
    net.minecraft.world.damagesource.CombatTracker handle
) implements CombatTracker {

    @Override
    public LivingEntity getEntity() {
        return this.handle.mob.getBukkitEntity();
    }

    @Override
    public List<CombatEntry> getCombatEntries() {
        final List<CombatEntry> combatEntries = new ArrayList<>(this.handle.entries.size());
        this.handle.entries.forEach(combatEntry -> combatEntries.add(new PaperCombatEntryWrapper(combatEntry)));
        return combatEntries;
    }

    @Override
    public void setCombatEntries(final List<CombatEntry> combatEntries) {
        this.handle.entries.clear();
        combatEntries.forEach(combatEntry -> this.handle.entries.add(((PaperCombatEntryWrapper) combatEntry).handle()));
    }

    @Override
    public @Nullable CombatEntry computeMostSignificantFall() {
        final net.minecraft.world.damagesource.CombatEntry combatEntry = this.handle.getMostSignificantFall();
        return combatEntry == null ? null : new PaperCombatEntryWrapper(combatEntry);
    }

    @Override
    public boolean isInCombat() {
        return this.handle.inCombat;
    }

    @Override
    public boolean isTakingDamage() {
        return this.handle.takingDamage;
    }

    @Override
    public int getCombatDuration() {
        return this.handle.getCombatDuration();
    }

    @Override
    public void addCombatEntry(final CombatEntry combatEntry) {
        final net.minecraft.world.damagesource.CombatEntry entry = ((PaperCombatEntryWrapper) combatEntry).handle();
        this.handle.recordDamageAndCheckCombatState(entry);
    }

    @Override
    public Component getDeathMessage() {
        return PaperAdventure.asAdventure(this.handle.getDeathMessage());
    }

    @Override
    public void resetCombatState() {
        this.handle.resetCombatState();
    }

    @Override
    public FallLocationType calculateFallLocationType() {
        final FellFromLocation fallLocation = FellFromLocation.getCurrentFellFromLocation(this.handle().mob);
        return Optionull.map(fallLocation, PaperCombatTrackerWrapper::minecraftToPaper);
    }

    @Override
    public int getLastDamageTime() {
        return this.handle.mob.tickCount - this.handle.lastDamageTime;
    }

    private static final BiMap<FellFromLocation, FallLocationType> FALL_LOCATION_MAPPING = Util.make(() -> {
        final BiMap<FellFromLocation, FallLocationType> map = HashBiMap.create(8);
        map.put(FellFromLocation.GENERIC, FallLocationType.GENERIC);
        map.put(FellFromLocation.LADDER, FallLocationType.LADDER);
        map.put(FellFromLocation.VINES, FallLocationType.VINES);
        map.put(FellFromLocation.WEEPING_VINES, FallLocationType.WEEPING_VINES);
        map.put(FellFromLocation.TWISTING_VINES, FallLocationType.TWISTING_VINES);
        map.put(FellFromLocation.SCAFFOLDING, FallLocationType.SCAFFOLDING);
        map.put(FellFromLocation.OTHER_CLIMBABLE, FallLocationType.OTHER_CLIMBABLE);
        map.put(FellFromLocation.WATER, FallLocationType.WATER);
        return map;
    });

    public static FellFromLocation paperToMinecraft(final FallLocationType fallLocationType) {
        final FellFromLocation fallLocation = FALL_LOCATION_MAPPING.inverse().get(fallLocationType);
        if (fallLocation == null) {
            throw new IllegalArgumentException("Unknown fall location type: " + fallLocationType.id());
        }
        return fallLocation;
    }

    public static FallLocationType minecraftToPaper(final FellFromLocation fallLocation) {
        final FallLocationType fallLocationType = FALL_LOCATION_MAPPING.get(fallLocation);
        if (fallLocationType == null) {
            throw new IllegalArgumentException("Unknown fall location: " + fallLocation.id());
        }
        return fallLocationType;
    }

}
