package com.bunnir.plugin.systems;

import com.bunnir.plugin.LumenGolem;
import com.bunnir.plugin.components.LumenGolemComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class LumenGolemSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(float dt, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        LumenGolemComponent component = (LumenGolemComponent)archetypeChunk.getComponent(index, LumenGolemComponent.getComponentType());
        component.RunAllInstructions(archetypeChunk.getReferenceTo(index), store, commandBuffer);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(LumenGolemComponent.getComponentType());
    }
}
