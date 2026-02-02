package com.bunnir.plugin.components;

import com.bunnir.plugin.LumenGolem;
import com.bunnir.plugin.jsonassets.LumenInstruction;
import com.bunnir.plugin.jsonassets.Vector3dSerializable;
import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTool;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemToolSpec;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthChunk;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.interaction.BlockInteractionUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.BreakBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.DamageEntityInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import java.io.*;
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

    public void RunAllInstructions(Ref<EntityStore> ent, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

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
                    return; //stack overflow error
            }
        }
        SetNPCState(ent);
    }

    Vector3d Velocity = new Vector3d(0, 0, 0);
    int hitCooldown = 5;
    //Time is running short, have no time to dig through 10000000 lines of code to find API. Enjoy this buggy mess.

    public boolean BreakBlock(Ref<EntityStore> ent, Vector3d value, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        TransformComponent transformComponent = store.getComponent(ent, TransformComponent.getComponentType());
        if(value.distanceTo(transformComponent.getPosition()) > 1.5)
        {
            TravelToBlock(ent, value, store, commandBuffer);
            return false;
        }

        World world = store.getExternalData().getWorld();

        Vector3i targetBlock = value.toVector3i();
        BlockType blockType = world.getBlockType(targetBlock.x, targetBlock.y, targetBlock.z);
        if(blockType.getId().equals("Empty")) {
            QueuedState = "Idle";
            hitCooldown = 10;
            return true;
        }


        NPCEntity npcComponent;
        npcComponent = store.getComponent(ent, Objects.requireNonNull(NPCEntity.getComponentType()));
        npcComponent.getInventory().setActiveHotbarSlot((byte) 1);

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

    public void TravelToBlock(Ref<EntityStore> ent, Vector3d value, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
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
        if(Velocity.length() < 0.001)
            Velocity = new Vector3d();
        if(magnitude < 3) {
            float inv_scale = (float) (1 - magnitude / 3);
            Velocity = Velocity.scale(1 - inv_scale * inv_scale);
            QueuedState = "Idle";
        }


        if(Velocity.x != 0 && Velocity.y != 0 && Velocity.z != 0) {
            result = new CollisionResult();
            CollisionModule.findBlockCollisionsShortDistance(store.getExternalData().getWorld(), box.getBoundingBox(), transformComponent.getPosition(), Velocity, result);

            for (int i = 0; i < result.getBlockCollisionCount(); i++) {
                if (result.getBlockCollision(i).blockId != Integer.MIN_VALUE) { //im done with normals
                    Velocity = Velocity.negate();
                    break;
                }
            }
        }


        NPCEntity npc = store.getComponent(ent, NPCEntity.getComponentType());

        transformComponent.setPosition(Vector3d.add(transformComponent.getPosition(), Velocity));


        if(Velocity.length() > 0.1f) {
            transformComponent.setRotation(new Vector3f(0, (float) Math.atan2(-Velocity.x, -Velocity.z), 0));

        }
        //transformComponent.setPosition(Vector3d.add(transformComponent.getPosition(), diff));

    }

    public enum Type_Codec {
        BOOL((byte) 0),
        FLOAT((byte) 1),
        STRING((byte) 2),
        BLOCK((byte) 3),
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
