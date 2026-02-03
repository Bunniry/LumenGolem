package com.bunnir.plugin.jsonassets;

import com.bunnir.plugin.components.LumenGolemComponent;
import com.hypixel.hytale.builtin.adventure.farming.interactions.UseWateringCanInteraction;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class LumenInstruction {
    public static final String METADATA_KEY = "LumenGolem_Instruction";

    public static final BuilderCodec<LumenInstruction> CODEC = BuilderCodec.builder(LumenInstruction.class, LumenInstruction::new)
            .append(
                    new KeyedCodec<>("InstructionVariableName", Codec.STRING),
                    (o, v) -> o.Name = v,
                    o -> o.Name)
            .add()
            .append(
                    new KeyedCodec<>("LogicType", Instruction_Codec.CODEC),
                    (o, v) -> o.ID = v,
                    o -> o.ID)
            .add()
            .append(
                    new KeyedCodec<>("Pointers", BuilderCodec.INT_ARRAY),
                    (o, v) -> o.Pointers = v,
                    o -> o.Pointers)
            .add()
            .build();

    public LumenInstruction() {

    }

    public LumenInstruction(String name, Instruction_Codec id) {
        Name = name;
        ID = id;
    }
    @Override
    public LumenInstruction clone() {
        LumenInstruction rv = new LumenInstruction();
        rv.Name = Name;
        rv.ID = ID;
        rv.Pointers = new int[Pointers.length];
        System.arraycopy(Pointers, 0, rv.Pointers, 0, Pointers.length);
        return rv;
    }

    public String Name;
    public Instruction_Codec ID;
    public int[] Pointers;

    public int RunInstruction(LumenGolemComponent thisGolem, Ref<EntityStore> ent, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if(Pointers == null)
            return -2;
        switch(ID) {
            case EQUALS -> {
                if (Pointers[0] == -1 || Pointers[1] == -1) return -2;
                Object val1 = thisGolem.getValue(Pointers[0]);
                if (val1 == null)
                    thisGolem.setValue(thisGolem.getValue(Pointers[1]) == null, Pointers[2], LumenGolemComponent.Type_Codec.BOOL);
                else
                    thisGolem.setValue(val1.equals(thisGolem.getValue(Pointers[1])), Pointers[2], LumenGolemComponent.Type_Codec.BOOL);
                return Pointers[3];
            }
            case NOTEQUAL -> {
                if (Pointers[0] == -1 || Pointers[1] == -1) return -2;
                Object val1 = thisGolem.getValue(Pointers[0]);
                if (val1 == null)
                    thisGolem.setValue(thisGolem.getValue(Pointers[1]) != null, Pointers[2], LumenGolemComponent.Type_Codec.BOOL);
                else
                    thisGolem.setValue(!val1.equals(thisGolem.getValue(Pointers[1])), Pointers[2], LumenGolemComponent.Type_Codec.BOOL);
                return Pointers[3];
            }
            case LESS -> {
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.FLOAT))
                    if (thisGolem.validateValue(Pointers[1], LumenGolemComponent.Type_Codec.FLOAT)) {
                        thisGolem.setValue((Float) thisGolem.getValue(Pointers[0]) < (Float) thisGolem.getValue(Pointers[1]), Pointers[2], LumenGolemComponent.Type_Codec.BOOL);

                        return Pointers[3];
                    }

                return -2;
            }
            case LESSOREQUAL -> {
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.FLOAT))
                    if (thisGolem.validateValue(Pointers[1], LumenGolemComponent.Type_Codec.FLOAT)) {
                        thisGolem.setValue((Float) thisGolem.getValue(Pointers[0]) <= (Float) thisGolem.getValue(Pointers[1]), Pointers[2], LumenGolemComponent.Type_Codec.BOOL);

                        return Pointers[3];
                    }

                return -2;
            }
            case ADD -> {
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.FLOAT))
                    if (thisGolem.validateValue(Pointers[1], LumenGolemComponent.Type_Codec.FLOAT)) {
                        thisGolem.setValue((Float) thisGolem.getValue(Pointers[0]) + (Float) thisGolem.getValue(Pointers[1]), Pointers[2], LumenGolemComponent.Type_Codec.FLOAT);

                        return Pointers[3];
                    }
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.POS))
                    if (thisGolem.validateValue(Pointers[1], LumenGolemComponent.Type_Codec.POS)) {
                        thisGolem.setValue(Vector3d.add((Vector3d) thisGolem.getValue(Pointers[0]), (Vector3d) thisGolem.getValue(Pointers[1])), Pointers[2], LumenGolemComponent.Type_Codec.FLOAT);

                        return Pointers[3];
                    }

                return -2;
            }
            case SUBTRACT -> {
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.FLOAT))
                    if (thisGolem.validateValue(Pointers[1], LumenGolemComponent.Type_Codec.FLOAT)) {
                        thisGolem.setValue((Float) thisGolem.getValue(Pointers[0]) - (Float) thisGolem.getValue(Pointers[1]), Pointers[2], LumenGolemComponent.Type_Codec.FLOAT);

                        return Pointers[3];
                    }
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.POS))
                    if (thisGolem.validateValue(Pointers[1], LumenGolemComponent.Type_Codec.POS)) {
                        thisGolem.setValue(Vector3d.add((Vector3d) thisGolem.getValue(Pointers[0]), ((Vector3d) thisGolem.getValue(Pointers[1])).negate()), Pointers[2], LumenGolemComponent.Type_Codec.FLOAT);

                        return Pointers[3];
                    }

                return -2;
            }
            case MULTIPLY -> {
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.FLOAT))
                    if (thisGolem.validateValue(Pointers[1], LumenGolemComponent.Type_Codec.FLOAT)) {
                        thisGolem.setValue((Float) thisGolem.getValue(Pointers[0]) * (Float) thisGolem.getValue(Pointers[1]), Pointers[2], LumenGolemComponent.Type_Codec.FLOAT);

                        return Pointers[3];
                    }

                return -2;
            }
            case DIVIDE -> {
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.FLOAT))
                    if (thisGolem.validateValue(Pointers[1], LumenGolemComponent.Type_Codec.FLOAT)) {
                        thisGolem.setValue((Float) thisGolem.getValue(Pointers[0]) / (Float) thisGolem.getValue(Pointers[1]), Pointers[2], LumenGolemComponent.Type_Codec.FLOAT);

                        return Pointers[3];
                    }
                return -2;
            }
            case MODULO -> {
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.FLOAT))
                    if (thisGolem.validateValue(Pointers[1], LumenGolemComponent.Type_Codec.FLOAT)) {
                        thisGolem.setValue((Float) thisGolem.getValue(Pointers[0]) % (Float) thisGolem.getValue(Pointers[1]), Pointers[2], LumenGolemComponent.Type_Codec.FLOAT);

                        return Pointers[3];
                    }
                return -2;
            }
            case ROUND -> {
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.FLOAT))
                    thisGolem.setValue(Math.round((Float) thisGolem.getValue(Pointers[0])), Pointers[1], LumenGolemComponent.Type_Codec.FLOAT);
                else
                    return -2;
                return Pointers[2];
            }
            case IFELSE -> {
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.BOOL)) {
                    if ((Boolean) thisGolem.getValue(Pointers[0])) return Pointers[1];
                } else
                    return -2;
                return Pointers[2];
            }
            case DISTANCE -> {
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.POS) && thisGolem.validateValue(Pointers[1], LumenGolemComponent.Type_Codec.POS))
                    thisGolem.setValue(((Vector3d) thisGolem.getValue(Pointers[0])).distanceTo((Vector3d) thisGolem.getValue(Pointers[1])), Pointers[2], LumenGolemComponent.Type_Codec.FLOAT);
                else
                    return -2;
                return Pointers[3];
            }

            case GOTOBLOCK -> {

                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.POS))
                    thisGolem.TravelToBlock(ent, (Vector3d) thisGolem.getValue(Pointers[0]), store, commandBuffer);
                else
                    return -2;
                return Pointers[1];
            }

            case CHOPBLOCK -> {

                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.POS))
                    thisGolem.setValue(thisGolem.BreakBlock(ent, (Vector3d) thisGolem.getValue(Pointers[0]), 1, store, commandBuffer), Pointers[1], LumenGolemComponent.Type_Codec.BOOL);
                else
                    return -2;
                return Pointers[2];
            }
            case MINEBLOCK -> {

                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.POS))
                    thisGolem.setValue(thisGolem.BreakBlock(ent, (Vector3d) thisGolem.getValue(Pointers[0]), 3, store, commandBuffer), Pointers[1], LumenGolemComponent.Type_Codec.BOOL);
                else
                    return -2;
                return Pointers[2];
            }

            case HARVESTBLOCK -> { //incomplete

                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.POS))
                    thisGolem.setValue(thisGolem.HarvestBlock(ent, (Vector3d) thisGolem.getValue(Pointers[0]), store, commandBuffer), Pointers[1], LumenGolemComponent.Type_Codec.BOOL);
                else
                    return -2;
                return Pointers[2];
            }


            case DUMPALLINCHEST -> { //incomplete

                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.POS))
                    thisGolem.setValue(thisGolem.DumpAllInChest(ent, (Vector3d) thisGolem.getValue(Pointers[0]), store, commandBuffer), Pointers[1], LumenGolemComponent.Type_Codec.BOOL);
                else
                    return -2;
                return Pointers[2];
            }


            case PICKALLFROMCHEST -> { //incomplete

                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.POS))
                    thisGolem.setValue(thisGolem.TakeAllFromChest(ent, (Vector3d) thisGolem.getValue(Pointers[0]), store, commandBuffer), Pointers[1], LumenGolemComponent.Type_Codec.BOOL);
                else
                    return -2;
                return Pointers[2];
            }

            case GETVECTORFLOAT -> {
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.POS)) {
                    Vector3d pos = (Vector3d) thisGolem.getValue(Pointers[0]);
                    thisGolem.setValue((Float)(float)pos.x, Pointers[1], LumenGolemComponent.Type_Codec.FLOAT);
                    thisGolem.setValue((Float)(float)pos.y, Pointers[2], LumenGolemComponent.Type_Codec.FLOAT);
                    thisGolem.setValue((Float)(float)pos.z, Pointers[3], LumenGolemComponent.Type_Codec.FLOAT);
                }
                else
                    return -2;
                return Pointers[4];
            }
            case SETVECTORFLOAT -> {

                if (thisGolem.validateValue(Pointers[1], LumenGolemComponent.Type_Codec.FLOAT))
                    if (thisGolem.validateValue(Pointers[2], LumenGolemComponent.Type_Codec.FLOAT))
                        if (thisGolem.validateValue(Pointers[3], LumenGolemComponent.Type_Codec.FLOAT)){
                            thisGolem.setValue( new Vector3d((Float) thisGolem.getValue(Pointers[1]), (Float) thisGolem.getValue(Pointers[2]), (Float) thisGolem.getValue(Pointers[3])), Pointers[0], LumenGolemComponent.Type_Codec.POS);

                            return Pointers[4];
                        }
                return -2;
            }
            case DROPALLITEMS -> {
                thisGolem.DropAllItems(ent, store, commandBuffer);
                return Pointers[4];
            }
            case GETPOSITION -> {
                TransformComponent component = store.getComponent(ent, TransformComponent.getComponentType());
                thisGolem.setValue(component.getPosition(), Pointers[0], LumenGolemComponent.Type_Codec.POS);

                return Pointers[1];
            }
            case GETBLOCKID -> {
                if (thisGolem.validateValue(Pointers[0], LumenGolemComponent.Type_Codec.POS)) {
                    BlockType blockType = commandBuffer.getExternalData().getWorld().getBlockType(
                            ((Vector3d) thisGolem.getValue(Pointers[0])).toVector3i());
                    if(blockType != null) {
                        thisGolem.setValue(blockType.getId(), Pointers[1], LumenGolemComponent.Type_Codec.STRING);
                        return Pointers[2];
                    }

                }
                return -2;
            }
        }
        return -1;
    }

    public enum Instruction_Codec {
        //Basic Operations
            //Comparisons
        EQUALS((byte) 0),
        NOTEQUAL((byte) 1),
        LESS((byte) 2),
        LESSOREQUAL((byte) 3),
            //Math
        ADD((byte) 4),
        SUBTRACT((byte) 5),
        MULTIPLY((byte) 6),
        DIVIDE((byte) 7),
        ROUND((byte) 8),
        MODULO((byte) 9),
            //ifelse

        IFELSE((byte) 10), //will not implement for
            //Functions

        GOTOBLOCK((byte) 11),
        DISTANCE((byte) 12),
        //ACCESSCHEST((byte) 13), //Autosorting will take 2 more days, and I rather spend the last half-day spamming cosmetics.
        CHOPBLOCK((byte) 13),
        MINEBLOCK((byte) 14),
        HARVESTBLOCK((byte) 15),

        GETVECTORFLOAT((byte) 16),
        SETVECTORFLOAT((byte) 17),

        DUMPALLINCHEST((byte) 18), //Chest Funcs
        PICKALLFROMCHEST((byte) 19),
        DROPALLITEMS((byte) 20),

        //GETCHESTCONTENTS((byte) 21),
        //GETINVENTORYCONTENTS((byte) 22),
        GETPOSITION((byte) 23),

        GETBLOCKID((byte) 24),
        //GETITEMID((byte) 25),
        ; //Yes, it breaks every block. I can't debug everything in 4d, I alr spent the good portion of d1/2 wrangling with code and subsequently giving up.

        private final byte value;

        private Instruction_Codec(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return this.value;
        }

        public static final EnumCodec<Instruction_Codec> CODEC = new EnumCodec(Instruction_Codec.class);
    }

}
