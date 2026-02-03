package com.bunnir.plugin.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Objects;

public class SpawnLumenGolemInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<SpawnLumenGolemInteraction> CODEC = BuilderCodec.builder(
                    SpawnLumenGolemInteraction.class, SpawnLumenGolemInteraction::new, SimpleBlockInteraction.CODEC
            )
            .documentation("Spawns a Lumen Golem.")
            .build();

    @Override
    protected void interactWithBlock(
            World world,
            CommandBuffer<EntityStore> commandBuffer,
            InteractionType type,
            InteractionContext context,
            ItemStack itemInHand,
            Vector3i targetBlock,
            CooldownHandler cooldownHandler) {

            Ref<EntityStore> ref = context.getEntity();
            HeadRotation headRotation = commandBuffer.getComponent(ref, HeadRotation.getComponentType());
            float yaw = headRotation.getRotation().getYaw();
            Vector3d disp = new Vector3d(0.5, 0, 0.5);

            switch (context.getClientState().blockFace) {
                case Up -> disp.y = disp.y + 1;
                case Down -> disp.y = disp.y - 1;
                case North -> disp.z = disp.z - 1;
                case South -> disp.z = disp.z + 1;
                case East -> disp.x = disp.x + 1;
                case West -> disp.x = disp.x - 1;
            }
            Vector3f rot = headRotation.getRotation();
            rot.y = rot.y + (float)Math.PI;

            commandBuffer.run((s) -> {

                Pair<Ref<EntityStore>, NPCEntity> lumenBot = NPCPlugin.get().spawnEntity(
                        world.getEntityStore().getStore(), NPCPlugin.get().getIndex("Lumen_Bot"),
                        Vector3d.add(targetBlock.toVector3d(), disp),
                        rot, null, new SpawnLumenGolemTriconsumer(), null);

                setupNPCInventory(lumenBot.first(), commandBuffer.getStore());
            });
    }
    public void setupNPCInventory(Ref<EntityStore> npcRef, Store<EntityStore> store) {
        // Retrieve the NPCEntity component to access inventory settings
        NPCEntity npcComponent;
        npcComponent = store.getComponent(npcRef, Objects.requireNonNull(NPCEntity.getComponentType()));

        if (npcComponent == null)
            return;
        // Initialize inventory size (e.g., 3 rows, 9 columns, 0 offset)
        npcComponent.setInventorySize(10, 30, 0);

        // Add items to the initialized inventory
        addItemsToNPCInventory(npcComponent.getInventory());
    }
    /**
     * Adds specific items and armor to the NPC's inventory.
     */
    public void addItemsToNPCInventory(Inventory inventory) {
        // Add a Thorium Mace to the first slot of the hotbar
        inventory.getHotbar().addItemStackToSlot((short) 1, new ItemStack("Tool_Hatchet_Iron", 1).withIncreasedDurability(Integer.MAX_VALUE));
        inventory.getHotbar().addItemStackToSlot((short) 2, new ItemStack("Tool_Watering_Can", 1).withIncreasedDurability(Integer.MAX_VALUE));
        inventory.getHotbar().addItemStackToSlot((short) 3, new ItemStack("Tool_Pickaxe_Iron", 1).withIncreasedDurability(Integer.MAX_VALUE));

        // Equip a Thorium Helmet using the InventoryHelper

        // Set the active hotbar slot to the weapon
    }

    @Override
    protected void simulateInteractWithBlock(
            @NonNullDecl InteractionType interactionType,
            @NonNullDecl InteractionContext interactionContext,
            @NullableDecl ItemStack itemStack,
            @NonNullDecl World world,
            @NonNullDecl Vector3i vector3i) {

    }
}
