package com.bunnir.plugin.components;

import com.bunnir.plugin.LumenGolem;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class LumenParticleSelectorComponent  implements Component<EntityStore> {
    public static ComponentType getComponentType() {
        return LumenGolem.getLumenParticleSelectorComponentType();
    }

    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        return new LumenParticleSelectorComponent();
    }

    public int Life = 30;
    public static final BuilderCodec<LumenParticleSelectorComponent> CODEC =
            BuilderCodec.builder(LumenParticleSelectorComponent.class, LumenParticleSelectorComponent::new)
            .build();
}
