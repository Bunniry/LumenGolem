package com.bunnir.plugin.goleminterface;

import com.bunnir.plugin.components.LumenGolemComponent;
import com.bunnir.plugin.components.LumenPlayerVectorStorageComponent;
import com.hypixel.hytale.builtin.mounts.interactions.MountInteraction;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;

public class GolemInterfaceSupplier implements OpenCustomUIInteraction.CustomPageSupplier {

    // The CODEC is required, but it can be empty.
    public static final BuilderCodec<GolemInterfaceSupplier> CODEC;

    public GolemInterfaceSupplier() {
    }

    @Nonnull
    @Override
    public CustomUIPage tryCreate(
            Ref<EntityStore> ref,
            ComponentAccessor<EntityStore> componentAccessor,
            @Nonnull PlayerRef playerRef,
            InteractionContext context
    ) {
        Ref<EntityStore> target = context.getTargetEntity();
        LumenGolemComponent golem = (LumenGolemComponent) componentAccessor.getComponent(target, LumenGolemComponent.getComponentType());

        LumenPlayerVectorStorageComponent component = (LumenPlayerVectorStorageComponent) componentAccessor.getComponent(context.getEntity(), LumenPlayerVectorStorageComponent.getComponentType());
        if(component != null && component.vectors.size() > 0)
        {
            int number = golem.data.length + component.vectors.size();

            Object[] data = new Object[number];
            LumenGolemComponent.Type_Codec[] data_types = new LumenGolemComponent.Type_Codec[number];
            String[] data_names = new String[number];

            System.arraycopy(golem.data, 0, data, 0, golem.data.length);
            System.arraycopy(golem.data_types, 0, data_types, 0, golem.data.length);
            System.arraycopy(golem.data_names, 0, data_names, 0, golem.data.length);

            HashMap<String, Boolean> hash = new HashMap<String, Boolean>();

            for(int i = 0; i < golem.data_names.length; i++) {
                hash.put(golem.data_names[i], true);
            }

            int index = 0;
            for(int i = 0; i < component.vectors.size(); i++) {
                data[golem.data_names.length + i] = component.vectors.get(i);
                data_types[golem.data_names.length + i] = LumenGolemComponent.Type_Codec.POS;

                while(true) {
                    index = index + 1;
                    String varName = "SELECTED_" + index;
                    if(!hash.containsKey(varName)) {
                        data_names[golem.data_names.length + i] = varName;
                        break;
                    }
                }
            }

            golem.data = data;
            golem.data_types = data_types;
            golem.data_names = data_names;

            component.vectors = new ArrayList<>();
        }

        return new GolemInterfaceUI(playerRef, CustomPageLifetime.CanDismiss, golem);
    }

    static {
        CODEC = BuilderCodec
                .builder(GolemInterfaceSupplier.class, GolemInterfaceSupplier::new)
                .build();
    }
}