package com.bytemaniak.wov.interfaces;

import net.minecraft.entity.data.TrackedData;

import java.util.Optional;
import java.util.UUID;

public interface AngryAt {
    TrackedData<Optional<UUID>> wov$getTrackedData();
    Optional<UUID> wov$getAngryAt();
}
