package com.bunnir.plugin.components;

import com.bunnir.plugin.LumenGolem;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;

public class LumenPlayerVectorStorageComponent implements Component<EntityStore> {
    public static ComponentType getComponentType() {
        return LumenGolem.getLumenPlayerVectorStorageComponentType();
    }

    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        return new LumenPlayerVectorStorageComponent();
    }

    public int Life = 30;
    public static final BuilderCodec<LumenPlayerVectorStorageComponent> CODEC =
            BuilderCodec.builder(LumenPlayerVectorStorageComponent.class, LumenPlayerVectorStorageComponent::new)
            .build();

    public ArrayList<Vector3d> vectors = new ArrayList<>();
}
