package com.bunnir.plugin.systems;

import com.bunnir.plugin.components.LumenGolemComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class LumenGolemInitializer extends RefSystem<EntityStore> {
    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void onEntityAdded(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl AddReason addReason, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc != null && npc.getRole().getRoleName().equals("Lumen_Bot")) {
            commandBuffer.run(o -> {
                Interactions interactions = (Interactions) commandBuffer.getComponent(ref, Interactions.getComponentType());
                if (interactions == null) {
                    interactions = new Interactions();
                    commandBuffer.putComponent(ref, Interactions.getComponentType(), interactions);
                }

                for (InteractionType interactionType : InteractionType.values()) {
                    if (interactions.getInteractionId(interactionType) != null && !interactions.getInteractionId(interactionType).equals("OpenGolem")) {
                        interactions.setInteractionId(interactionType, (String) null);
                    }
                }

                String rootInteractionId = "OpenGolem";
                interactions.setInteractionId(InteractionType.Use, rootInteractionId);
                String hintText = "server.interactionHints.Lumen_OpenGolem";
                interactions.setInteractionHint(hintText);
            });
        }
    }

    @Override
    public void onEntityRemove(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl RemoveReason removeReason, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

    }
}
