package com.bunnir.plugin.interactions;

import com.bunnir.plugin.components.LumenParticleSelectorComponent;
import com.bunnir.plugin.components.LumenPlayerVectorStorageComponent;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventItemMerging;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;

public class SelectVectorInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<SelectVectorInteraction> CODEC = BuilderCodec.builder(
                    SelectVectorInteraction.class, SelectVectorInteraction::new, SimpleBlockInteraction.CODEC
            )
            .documentation("Selects a Vector.")
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

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("VectorSelected");
        Model model = Model.createScaledModel(modelAsset, 2.0f);

        Vector3d displayPos = Vector3d.add( targetBlock.toVector3d(), new Vector3d(0.5f, 0.05f, 0.5f));
        Vector3d pos = Vector3d.add( targetBlock.toVector3d(), new Vector3d(0.5f, 0.5f, 0.5f));

        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(displayPos, new Vector3f(0, 0, 0)));
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
        holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(2));
        holder.addComponent(LumenParticleSelectorComponent.getComponentType(), new LumenParticleSelectorComponent());

        holder.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));
        holder.ensureComponent(UUIDComponent.getComponentType());
        commandBuffer.run((o) -> {commandBuffer.addEntity(holder, AddReason.SPAWN);});

        LumenPlayerVectorStorageComponent component = (LumenPlayerVectorStorageComponent) commandBuffer.getComponent(context.getEntity(), LumenPlayerVectorStorageComponent.getComponentType());
        if(component == null)
            component = (LumenPlayerVectorStorageComponent) commandBuffer.addComponent(context.getEntity(), LumenPlayerVectorStorageComponent.getComponentType());
        component.vectors.add(pos);
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
