package com.bunnir.plugin.interactions;

import com.bunnir.plugin.components.LumenGolemComponent;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainerUtil;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.PlaceBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import java.util.List;

public class GiveGolemItemInteraction extends SimpleInteraction {
    public static final int MAX_ADVENTURE_PLACEMENT_RANGE_SQUARED = 36;
    @Nonnull
    public static final BuilderCodec<GiveGolemItemInteraction> CODEC = BuilderCodec.builder(
                    GiveGolemItemInteraction.class, GiveGolemItemInteraction::new, SimpleInteraction.CODEC
            )
            .documentation("Gives golem item.")
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
            Ref<EntityStore> target = context.getTargetEntity();
            if(target == null)
            {
                context.getState().state = InteractionState.Failed;
                return;
            }
            LumenGolemComponent golem = (LumenGolemComponent) commandBuffer.getComponent(target, LumenGolemComponent.getComponentType());
            if(golem != null) {
                Inventory inventory = commandBuffer.getComponent(target, NPCEntity.getComponentType()).getInventory();

                context.getState().state = InteractionState.Finished;

                context.getHeldItemContainer().removeItemStackFromSlot((short)context.getHeldItemSlot(), context.getHeldItem(), 1);

                List<ItemStack> itemStacks = new ObjectArrayList();
                itemStacks.addAll(inventory.getArmor().dropAllItemStacks()); //not armor
                commandBuffer.run((s) -> {
                    itemStacks.forEach((o) -> {
                        ItemUtils.dropItem(target, o, commandBuffer);
                    });
                });
                InventoryHelper.useArmor(inventory.getArmor(), context.getHeldItem().getItemId());

                return;
            }

            context.getState().state = InteractionState.Failed;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        }
        context.getState().state = InteractionState.Failed;
    }

    protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {

    }

    public String toString() {
        return "LumenOpenGolemInteraction{}";
    }
}
