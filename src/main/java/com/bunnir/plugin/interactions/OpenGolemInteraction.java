package com.bunnir.plugin.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class OpenGolemInteraction extends SimpleInteraction {
    public static final int MAX_ADVENTURE_PLACEMENT_RANGE_SQUARED = 36;
    @Nonnull
    public static final BuilderCodec<OpenGolemInteraction> CODEC = BuilderCodec.builder(
                    OpenGolemInteraction.class, OpenGolemInteraction::new, SimpleInteraction.CODEC
            )
            .documentation("Modifies the innards of a golem.")
            .build();

    protected final void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        InteractionSyncData clientState = context.getClientState();

        assert clientState != null;

        if (!firstRun) {
            context.getState().state = clientState.state;
        } else {
            Ref<EntityStore> playerReference = context.getEntity();
            CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

            assert commandBuffer != null;

            World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();

            super.tick0(firstRun, time, type, context, cooldownHandler);
        }
    }

    protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {

    }

    public String toString() {
        return "LumenOpenGolemInteraction{}";
    }
}
