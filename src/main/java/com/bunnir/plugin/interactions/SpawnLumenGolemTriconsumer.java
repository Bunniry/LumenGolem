package com.bunnir.plugin.interactions;

import com.bunnir.plugin.components.LumenGolemComponent;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerItemEntityPickupSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

public class SpawnLumenGolemTriconsumer implements TriConsumer<NPCEntity, Holder<EntityStore>, Store<EntityStore>> {
    @Override
    public void accept(NPCEntity npcEntity, Holder<EntityStore> entityStoreHolder, Store<EntityStore> entityStoreStore) {

        entityStoreHolder.addComponent(LumenGolemComponent.getComponentType(), new LumenGolemComponent());
    }
}
