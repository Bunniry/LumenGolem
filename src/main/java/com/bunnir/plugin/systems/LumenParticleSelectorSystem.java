package com.bunnir.plugin.systems;

import com.bunnir.plugin.components.LumenGolemComponent;
import com.bunnir.plugin.components.LumenParticleSelectorComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class LumenParticleSelectorSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(float dt, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        LumenParticleSelectorComponent component = (LumenParticleSelectorComponent)archetypeChunk.getComponent(index, LumenParticleSelectorComponent.getComponentType());
        component.Life--;
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if(component.Life <= 0)
            commandBuffer.run((o)->store.removeEntity(ref, RemoveReason.REMOVE));
        else{

            EntityScaleComponent model = (EntityScaleComponent)archetypeChunk.getComponent(index, EntityScaleComponent.getComponentType());
            model.setScale((float)(2.5 * Math.sin((float) component.Life * Math.PI / 60f + Math.PI / 4)));
        }

    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(LumenParticleSelectorComponent.getComponentType());
    }
}
