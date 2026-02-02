package com.bunnir.plugin.interactions;

import com.bunnir.plugin.components.LumenParticleSelectorComponent;
import com.bunnir.plugin.components.LumenPlayerVectorStorageComponent;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class ClearVectorInteraction extends SimpleInteraction {
    @Nonnull
    public static final BuilderCodec<ClearVectorInteraction> CODEC = BuilderCodec.builder(
                    ClearVectorInteraction.class, ClearVectorInteraction::new, ClearVectorInteraction.CODEC
            )
            .documentation("Clears vectors.")
            .build();

    protected final void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        InteractionSyncData clientState = context.getClientState();

        assert clientState != null;

        if (!firstRun) {
            context.getState().state = clientState.state;
        } else {
            Ref<EntityStore> playerReference = context.getEntity();
            CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

            LumenPlayerVectorStorageComponent component = (LumenPlayerVectorStorageComponent) commandBuffer.getComponent(playerReference, LumenPlayerVectorStorageComponent.getComponentType());
            if(component != null)
                component.vectors = new ArrayList<>();
            super.tick0(firstRun, time, type, context, cooldownHandler);
        }
    }

    protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {

    }

    public String toString() {
        return "LumenOpenGolemInteraction{}";
    }
}
