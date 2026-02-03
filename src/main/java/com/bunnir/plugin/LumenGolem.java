package com.bunnir.plugin;

import com.bunnir.plugin.components.LumenGolemComponent;
import com.bunnir.plugin.components.LumenParticleSelectorComponent;
import com.bunnir.plugin.components.LumenPlayerVectorStorageComponent;
import com.bunnir.plugin.goleminterface.GolemInterfaceSupplier;
import com.bunnir.plugin.interactions.ClearVectorInteraction;
import com.bunnir.plugin.interactions.GiveGolemItemInteraction;
import com.bunnir.plugin.interactions.SelectVectorInteraction;
import com.bunnir.plugin.interactions.SpawnLumenGolemInteraction;
import com.bunnir.plugin.systems.LumenGolemInitializer;
import com.bunnir.plugin.systems.LumenGolemPickUp;
import com.bunnir.plugin.systems.LumenGolemSystem;
import com.bunnir.plugin.systems.LumenParticleSelectorSystem;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import javax.annotation.Nonnull;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class LumenGolem extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private ComponentType lumenPlayerVectorStorageComponentType;

    public LumenGolem(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
        instance = this;
    }
    static LumenGolem instance;
    ComponentType<EntityStore, LumenGolemComponent> lumenGolemComponentType;
    ComponentType<EntityStore, LumenParticleSelectorComponent> lumenParticleSelectorComponentType;

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        //this.getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));


        this.getCodecRegistry(Interaction.CODEC).register("SpawnLumenGolemInteraction", SpawnLumenGolemInteraction.class, SpawnLumenGolemInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("GiveLumenGolemItemInteraction", GiveGolemItemInteraction.class, GiveGolemItemInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("SelectVectorInteraction", SelectVectorInteraction.class, SelectVectorInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("ClearVectorInteraction", ClearVectorInteraction.class, ClearVectorInteraction.CODEC);

        lumenGolemComponentType = this.getEntityStoreRegistry().registerComponent(
                LumenGolemComponent.class, "LumenGolemComponent", LumenGolemComponent.CODEC);
        lumenParticleSelectorComponentType = this.getEntityStoreRegistry().registerComponent(
                LumenParticleSelectorComponent.class, "LumenParticleSelectorComponent", LumenParticleSelectorComponent.CODEC);
        lumenPlayerVectorStorageComponentType = this.getEntityStoreRegistry().registerComponent(
                LumenPlayerVectorStorageComponent.class, "LumenPlayerVectorStorageComponent", LumenPlayerVectorStorageComponent.CODEC);
        //I used PutComponent, I used this nonsense, yet data is not persisted.
        // That's it, I'm using the spawnentity prespawn to force the component in.

        this.getEntityStoreRegistry().registerSystem(new LumenGolemSystem());
        this.getEntityStoreRegistry().registerSystem(new LumenGolemInitializer());

        this.getEntityStoreRegistry().registerSystem(new LumenParticleSelectorSystem());
        this.getEntityStoreRegistry().registerSystem(new LumenGolemPickUp(ItemComponent.getComponentType(), LumenGolemComponent.getComponentType(), EntityModule.get().getEntitySpatialResourceType()));

        this.getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC).register("LumenGolemUI", GolemInterfaceSupplier.class, GolemInterfaceSupplier.CODEC);

    }

    @Override
    protected void start() {
    }

    public static ComponentType getLumenGolemComponentType() { return instance.lumenGolemComponentType; }

    public static ComponentType getLumenParticleSelectorComponentType() {
        return instance.lumenParticleSelectorComponentType;
    }

    public static ComponentType getLumenPlayerVectorStorageComponentType() {
        return instance.lumenPlayerVectorStorageComponentType;
    }
}