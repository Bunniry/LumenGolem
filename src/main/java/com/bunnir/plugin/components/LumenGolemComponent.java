package com.bunnir.plugin.components;

import com.bunnir.plugin.LumenGolem;
import com.bunnir.plugin.jsonassets.LumenInstruction;
import com.bunnir.plugin.jsonassets.Vector3dSerializable;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.inventory.SmartMoveItemStack;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainerUtil;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ActionType;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.AddItemInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.RefillContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

public class LumenGolemComponent implements Component<EntityStore> {

    public static ComponentType getComponentType() {
        return LumenGolem.getLumenGolemComponentType();
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        LumenGolemComponent rv = new LumenGolemComponent();

        rv.data = new Object[data.length];
        System.arraycopy(data, 0, rv.data, 0, data.length);

        rv.data_types = new Type_Codec[data_types.length];
        System.arraycopy(data_types, 0, rv.data_types, 0, data_types.length);

        rv.instructions = new LumenInstruction[instructions.length];
        System.arraycopy(instructions, 0, rv.instructions, 0, instructions.length);

        rv.data_names = new String[data_names.length];
        System.arraycopy(data_names, 0, rv.data_names, 0, data_names.length);


        return rv;
    }

    public static final BuilderCodec<LumenGolemComponent> CODEC = ((BuilderCodec.Builder<LumenGolemComponent>)((BuilderCodec.Builder<LumenGolemComponent>)((BuilderCodec.Builder<LumenGolemComponent>)(BuilderCodec.builder(LumenGolemComponent.class, LumenGolemComponent::new)
            .append(
                    new KeyedCodec<>("Memory", new ArrayCodec<>(new ArrayCodec(Codec.BYTE, Byte[]::new), Byte[][]::new)),
                    (o, v) -> {
                        o.data = new Object[v.length];
                        for(int i = 0;i < v.length;i++)
                            o.data[i] = o.deserialize(v[i]);

                    },
                    o -> {
                        Byte[][] bytes = new Byte[o.data.length][];
                        for(int i = 0; i < bytes.length;i++)
                            bytes[i] = o.serialize(i);
                        return bytes;
                    })
            .add()))
            .append(
                    new KeyedCodec<>("VariableNames", new ArrayCodec(Codec.STRING, String[]::new)), //memory locations
                    (o, v) -> o.data_names = v,
                    o -> o.data_names)
            .add())
            .append(
                    new KeyedCodec<>("DataTypes", new ArrayCodec(Type_Codec.CODEC, Type_Codec[]::new)), //memory locations
                    (o, v) -> o.data_types = v,
                    o -> o.data_types)
            .add())
            .append(
                    new KeyedCodec<>("Instructions", new ArrayCodec(LumenInstruction.CODEC, LumenInstruction[]::new)), //memory locations
                    (o, v) -> o.instructions = v,
                    o -> o.instructions)
            .add()
            .build();

    public Object[] data = new Object[0];
    public Type_Codec[] data_types = new Type_Codec[0];
    public String[] data_names = new String[0];
    public LumenInstruction[] instructions = new LumenInstruction[0];

    public Byte[] serialize(int index) { //SUS. AMOGUSS
        ByteArrayOutputStream b = new ByteArrayOutputStream();

        Object item = data[index];
        switch (data_types[index]) {
            case POS -> item = new Vector3dSerializable((Vector3d) item);
        }

        try(ObjectOutputStream o = new ObjectOutputStream(b)){
            o.writeObject(item);
            o.flush();
        } catch (IOException e) {
        }
        byte[] bytes_prim = b.toByteArray();
        Byte[] rv = new Byte[bytes_prim.length];
        for(int i = 0; i < bytes_prim.length; i++)
        {
            rv[i] = bytes_prim[i];
        }
        return rv;
    }

    public Object deserialize(Byte[] bytes) {
        byte[] bytes_prim = new byte[bytes.length];
        for(int i = 0; i < bytes.length; i++)
        {
            bytes_prim[i] = bytes[i];
        }

        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes_prim)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                Object rv = o.readObject();
                if (rv.getClass() == Vector3dSerializable.class)
                    rv = (((Vector3dSerializable) rv).Deserialize());
                return rv;
            } catch (ClassNotFoundException e) {
            }
        } catch (IOException e) {
        }
        return null;
    }

    public void setValue(Object object, int index, Type_Codec type) {
        if(index == -1)
            return;
        data[index] = object;
        data_types[index] = type;
    }

    public boolean validateValue(int index, Type_Codec type) {
        return index > -1 && data_types[index] == type;
    }

    public Object getValue(int index) {
        return data[index];
    }

    String QueuedState = "Idle";

    public void SetNPCState(Ref<EntityStore> ent) {
        Store<EntityStore> store = ent.getStore();
        NPCEntity NPC = store.getComponent(ent, NPCEntity.getComponentType());
        NPC.getRole().getStateSupport().setState(ent, QueuedState, null, store);
    }

    boolean PerformedAction = false;
    boolean DoubleAction = false;
    public void RunAllInstructions(Ref<EntityStore> ent, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        PerformedAction = false;
        DoubleAction = false;
        if(outOfCommision > 0) {
            outOfCommision--;
            return;
        }
        NPCEntity npcComponent;
        npcComponent = store.getComponent(ent, Objects.requireNonNull(NPCEntity.getComponentType()));
        npcComponent.getInventory().setActiveHotbarSlot((byte) 0);
        QueuedState = "Idle";
        if(instructions.length == 0)
            return;
        int[] instRanCount = new int[instructions.length];
        int nextInstruction = 0;
        while(nextInstruction >= 0) { //-1: return, -2: error
            nextInstruction = instructions[nextInstruction].RunInstruction(this, ent, store, commandBuffer);

            if(nextInstruction >= 0) {
                instRanCount[nextInstruction] = instRanCount[nextInstruction] + 1;
                if (instRanCount[nextInstruction] == 30)
                    break;
            }
        }
        SetNPCState(ent);

        TransformComponent transformComponent = store.getComponent(ent, TransformComponent.getComponentType());
        if(nextInstruction == -2 || DoubleAction) {
            outOfCommision = 30;
            ComponentAccessor<EntityStore> componentAccessor = store;
            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = (SpatialResource) componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
            ObjectList<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
            playerSpatialResource.getSpatialStructure().collect(transformComponent.getPosition(), (double) 75.0F, playerRefs);
            ParticleUtil.spawnParticleEffect("Lumen_Error_Smoke", transformComponent.getPosition().getX(), transformComponent.getPosition().getY(), transformComponent.getPosition().getZ(), 0, 0, 0, 1,
                    new Color((byte) 150, (byte) 50, (byte) 50), (Ref) null, playerRefs, componentAccessor);

            SoundUtil.playSoundEvent3d(null, SoundEvent.getAssetMap().getIndex("SFX_Window_Break"), transformComponent.getPosition().getX(), transformComponent.getPosition().getY(), transformComponent.getPosition().getZ(), store);
        }

    }

    Vector3d Velocity = new Vector3d(0, 0, 0);
    int hitCooldown = 10;
    int outOfCommision = 0;
    //Time is running short, have no time to dig through 10000000 lines of code to find API. Enjoy this buggy mess.

    public boolean BreakBlock(Ref<EntityStore> ent, Vector3d value, int tool, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if(PerformedAction) {
            DoubleAction = true;
            return false;
        }

        TransformComponent transformComponent = store.getComponent(ent, TransformComponent.getComponentType());
        if(value.distanceTo(transformComponent.getPosition()) > 1.5)
        {
            TravelToBlock(ent, value, store, commandBuffer);
            return false;
        }
        PerformedAction = true;

        World world = store.getExternalData().getWorld();

        Vector3i targetBlock = value.toVector3i();
        BlockType blockType = world.getBlockType(targetBlock.x, targetBlock.y, targetBlock.z);
        if(blockType.getId().equals("Empty")) {
            QueuedState = "Idle";
            hitCooldown = 10;
            return true;
        }
        StateSupport support = store.getComponent(ent, NPCEntity.getComponentType()).getRole().getStateSupport();
        if(!support.getStateName().equals("Chop.Default"))
            hitCooldown = 10;


        NPCEntity npcComponent;
        npcComponent = store.getComponent(ent, Objects.requireNonNull(NPCEntity.getComponentType()));
        npcComponent.getInventory().setActiveHotbarSlot((byte) tool);

        hitCooldown = hitCooldown - 1;
        if(hitCooldown <= 0) {
            hitCooldown = 10;

            ChunkStore chunkStore = world.getChunkStore();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
            Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);

            BlockHarvestUtils.performBlockDamage(targetBlock, null,
                    npcComponent.getInventory().getItemInHand().getItem().getTool(), 1, 0,
                    chunkReference,
                    commandBuffer, world.getChunkStore().getStore());

        }
        QueuedState = "Chop";
        return false; //Yandere dev! Yandere dev! Whatever, just a 4d modjam, won't be scaled anyway
    }

    public boolean HarvestBlock(Ref<EntityStore> ent, Vector3d value, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if(PerformedAction) {
            DoubleAction = true;
            return false;
        }

        TransformComponent transformComponent = store.getComponent(ent, TransformComponent.getComponentType());
        if(value.distanceTo(transformComponent.getPosition()) > 1.5)
        {
            TravelToBlock(ent, value, store, commandBuffer);
            return false;
        }
        PerformedAction = true;

        World world = store.getExternalData().getWorld();

        Vector3i targetBlock = value.toVector3i();
        BlockType blockType = world.getBlockType(targetBlock.x, targetBlock.y, targetBlock.z);
        String hint = blockType.getInteractionHint();
        if(blockType.getId().equals("Empty") || hint == null ||
                (!hint.equals("server.interactionHints.gather") && !hint.equals("server.interactionHints.harvest"))) {
            QueuedState = "Idle";
            return true;
        }
        ChunkStore chunkStore = world.getChunkStore();
        long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
        Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);

        BlockHarvestUtils.performPickupByInteraction(ent, targetBlock, blockType, 0, chunkReference, store, chunkStore.getStore());
        QueuedState = "Pick";
        outOfCommision = 15;
        return false;
    }

    public static void DepositAll(ItemContainer c, ItemContainer c2) {
        List<ItemStack> itemStacks = new ObjectArrayList();
        itemStacks.addAll(c.dropAllItemStacks());
        ListTransaction<ItemStackTransaction> transaction = c2.addItemStacks(itemStacks);

        for(ItemStackTransaction stackTransaction : transaction.getList()) {
            ItemStack remainder = stackTransaction.getRemainder();
            if (!ItemStack.isEmpty(remainder)) {
                c.addItemStack(remainder);
            }
        }
    }

    public boolean DumpAllInChest(Ref<EntityStore> ent, Vector3d value, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if(PerformedAction) {
            DoubleAction = true;
            return false;
        }

        TransformComponent transformComponent = store.getComponent(ent, TransformComponent.getComponentType());
        if(value.distanceTo(transformComponent.getPosition()) > 1.5)
        {
            TravelToBlock(ent, value, store, commandBuffer);
            return false;
        }
        PerformedAction = true;

        World world = store.getExternalData().getWorld();

        Vector3i targetBlock = value.toVector3i();
        BlockType blockType = world.getBlockType(targetBlock.x, targetBlock.y, targetBlock.z);
        if(blockType.getId().equals("Empty") || blockType.getInteractionHint() == null) {
            QueuedState = "Idle";
            return true;
        }

        if (world.getState(targetBlock.x, targetBlock.y, targetBlock.z, true) instanceof ItemContainerState) {
            NPCEntity npcComponent = store.getComponent(ent, Objects.requireNonNull(NPCEntity.getComponentType()));
            Inventory inventory = npcComponent.getInventory();
            ItemContainerState itemContainerState = (ItemContainerState) world.getState(targetBlock.x, targetBlock.y, targetBlock.z, true);

            SimpleItemContainer container = (SimpleItemContainer) itemContainerState.getItemContainer();
            if(container != null) {


                inventory.getHotbar().removeItemStackFromSlot((byte)1);
                inventory.getHotbar().removeItemStackFromSlot((byte)2);
                inventory.getHotbar().removeItemStackFromSlot((byte)3);

                DepositAll(inventory.getBackpack(), container);
                DepositAll(inventory.getStorage(), container);
                DepositAll(inventory.getHotbar(), container);

                inventory.getHotbar().addItemStackToSlot((short) 1, new ItemStack("Tool_Hatchet_Iron", 1).withIncreasedDurability(Integer.MAX_VALUE));
                inventory.getHotbar().addItemStackToSlot((short) 2, new ItemStack("Tool_Watering_Can", 1).withIncreasedDurability(Integer.MAX_VALUE));
                inventory.getHotbar().addItemStackToSlot((short) 3, new ItemStack("Tool_Pickaxe_Iron", 1).withIncreasedDurability(Integer.MAX_VALUE));
            }


        }

        QueuedState = "Rummage";
        outOfCommision = 15;
        return false;
    }


    public boolean TakeAllFromChest(Ref<EntityStore> ent, Vector3d value, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if(PerformedAction) {
            DoubleAction = true;
            return false;
        }

        TransformComponent transformComponent = store.getComponent(ent, TransformComponent.getComponentType());
        if(value.distanceTo(transformComponent.getPosition()) > 1.5)
        {
            TravelToBlock(ent, value, store, commandBuffer);
            return false;
        }
        PerformedAction = true;

        World world = store.getExternalData().getWorld();

        Vector3i targetBlock = value.toVector3i();
        BlockType blockType = world.getBlockType(targetBlock.x, targetBlock.y, targetBlock.z);
        if(blockType.getId().equals("Empty") || blockType.getInteractionHint() == null) {
            QueuedState = "Idle";
            return true;
        }

        if (world.getState(targetBlock.x, targetBlock.y, targetBlock.z, true) instanceof ItemContainerState) {
            NPCEntity npcComponent = store.getComponent(ent, Objects.requireNonNull(NPCEntity.getComponentType()));
            Inventory inventory = npcComponent.getInventory();
            ItemContainerState itemContainerState = (ItemContainerState) world.getState(targetBlock.x, targetBlock.y, targetBlock.z, true);

            SimpleItemContainer container = (SimpleItemContainer) itemContainerState.getItemContainer();
            if(container != null) {
                DepositAll(container, inventory.getHotbar());
                DepositAll(container, inventory.getStorage());
                DepositAll(container, inventory.getBackpack());
            }


        }

        QueuedState = "Rummage";
        outOfCommision = 15;
        return false;
    }

    public void DropAllItems(Ref<EntityStore> ent, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if(PerformedAction) {
            DoubleAction = true;
            return;
        }
        PerformedAction = true;
        TransformComponent transformComponent = store.getComponent(ent, TransformComponent.getComponentType());

        NPCEntity npcComponent = store.getComponent(ent, Objects.requireNonNull(NPCEntity.getComponentType()));
        Inventory inventory = npcComponent.getInventory();

        inventory.getHotbar().removeItemStackFromSlot((byte)1);
        inventory.getHotbar().removeItemStackFromSlot((byte)2);
        inventory.getHotbar().removeItemStackFromSlot((byte)3);


        List<ItemStack> itemStacks = new ObjectArrayList();
        itemStacks.addAll(inventory.getStorage().dropAllItemStacks());
        itemStacks.addAll(inventory.getHotbar().dropAllItemStacks());
        itemStacks.addAll(inventory.getUtility().dropAllItemStacks());
        itemStacks.addAll(inventory.getBackpack().dropAllItemStacks()); //not armor
        commandBuffer.run((s) -> {
            itemStacks.forEach((o) -> {
                ItemUtils.dropItem(ent, o, store);
            });
        });
        
        inventory.getHotbar().addItemStackToSlot((short) 1, new ItemStack("Tool_Hatchet_Iron", 1).withIncreasedDurability(Integer.MAX_VALUE));
        inventory.getHotbar().addItemStackToSlot((short) 2, new ItemStack("Tool_Watering_Can", 1).withIncreasedDurability(Integer.MAX_VALUE));
        inventory.getHotbar().addItemStackToSlot((short) 3, new ItemStack("Tool_Pickaxe_Iron", 1).withIncreasedDurability(Integer.MAX_VALUE));

        QueuedState = "Rummage";
    }

    public void TravelToBlock(Ref<EntityStore> ent, Vector3d value, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if(PerformedAction) {
            DoubleAction = true;
            return;
        }
        PerformedAction = true;
        TransformComponent transformComponent = store.getComponent(ent, TransformComponent.getComponentType());

        BoundingBox box = store.getComponent(ent, BoundingBox.getComponentType());

        CollisionResult result = new CollisionResult();
        CollisionModule.findBlockCollisionsIterative(store.getExternalData().getWorld(), box.getBoundingBox(), transformComponent.getPosition(), new Vector3d(0, -4, 0), true, result);

        Vector3d diff;

        boolean collided = false;
        boolean tooHigh = true;
        for(int i = 0; i < result.getBlockCollisionCount();i++) {
            if(result.getBlockCollision(i).blockId != Integer.MIN_VALUE) {
                tooHigh = false;
                if(result.getBlockCollision(i).y > transformComponent.getPosition().y - 2)
                    collided = true;
                break;
            }
        }

        Vector3d otherVal = new Vector3d(value);
        diff = otherVal.subtract(transformComponent.getPosition());
        double magnitude = diff.length();
        boolean horizontalMagLarger = (diff.x * diff.x + diff.z * diff.z - diff.y * diff.y > 0);
        if(collided && horizontalMagLarger) {
            diff = new Vector3d(0, 0.015, 0);
        }
        else if (tooHigh && horizontalMagLarger) {
            diff = new Vector3d(0, -0.015, 0);
        }
        else {
            diff = diff.normalize();
            float scale = 0.01f;
            diff.x = diff.x * scale;
            diff.y = diff.y * scale;
            diff.z = diff.z * scale;
        }

        Velocity = Vector3d.add(Velocity, diff);
        Velocity = Velocity.scale(0.95);

        QueuedState = "Walk";
        if(Velocity.length() < 0.001 || magnitude < 0.05)
            Velocity = new Vector3d();
        if(Velocity.length() < 0.01)
            QueuedState = "Idle";
        if(magnitude < 3) {
            float inv_scale = (float) (1 - magnitude / 3);
            Velocity = Velocity.scale(1 - inv_scale * inv_scale);
            QueuedState = "Idle";
        }


        if(Velocity.x != 0 && Velocity.y != 0 && Velocity.z != 0) {
            result = new CollisionResult();
            Vector3d timesTwo = new Vector3d(Velocity);
            timesTwo = Vector3d.add(Velocity, timesTwo.normalize().scale(0.2));
            CollisionModule.findBlockCollisionsShortDistance(store.getExternalData().getWorld(), box.getBoundingBox(), transformComponent.getPosition(), timesTwo, result);

            for (int i = 0; i < result.getBlockCollisionCount(); i++) {
                if (result.getBlockCollision(i).blockId != Integer.MIN_VALUE) { //im done with normals
                    Velocity = Velocity.negate();
                    break;
                }
            }
        }


        NPCEntity npc = store.getComponent(ent, NPCEntity.getComponentType());

        transformComponent.setPosition(Vector3d.add(transformComponent.getPosition(), Velocity));


        if(Velocity.length() > 0.01f) {
            Vector3f rotation = transformComponent.getRotation();
            rotation = Vector3f.lerp(rotation, new Vector3f(0, (float) Math.atan2(-Velocity.x, -Velocity.z), 0), 0.05f);
            transformComponent.setRotation(rotation);

        }
        //transformComponent.setPosition(Vector3d.add(transformComponent.getPosition(), diff));

    }

    public enum Type_Codec {
        BOOL((byte) 0),
        FLOAT((byte) 1),
        STRING((byte) 2),
        //BLOCK((byte) 3),
        POS((byte) 4);

        private final byte value;

        private Type_Codec(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return this.value;
        }

        public static final EnumCodec<Type_Codec> CODEC = new EnumCodec(Type_Codec.class);
    }
}
