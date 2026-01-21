package com.purantis.tameableocelots;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedList;
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Stream;

public class TameableOcelotsConfig extends Config {

    public static TameableOcelotsConfig config = ConfigApiJava.registerAndLoadConfig(TameableOcelotsConfig::new, RegisterType.BOTH);
    public static List<Item> OCELOT_TAMING_ITEMS = List.of();
    public static List<Item> OCELOT_BREEDING_ITEMS = List.of();
    public ValidatedList<Identifier> ocelotTamingItems = new ValidatedList<>(List.of(
            Identifier.tryParse("salmon")
    ), ValidatedIdentifier.ofRegistry(Identifier.tryParse("salmon"), Registries.ITEM));
    public ValidatedList<Identifier> ocelotBreedingItems = new ValidatedList<>(List.of(
            Identifier.tryParse("cod")
    ), ValidatedIdentifier.ofRegistry(Identifier.tryParse("cod"), Registries.ITEM));

    @ValidatedFloat.Restrict(min = 0f, max = 1f, type = ValidatedNumber.WidgetType.SLIDER)
    public boolean ocelotsAttackWithOwner = false;

    public boolean ocelotsTameDirectly = true;

    public boolean ocelotsCanIgnoreMobGriefingRule = false;

    public float ocelotsTamingChance = 0.3f;

    public boolean untamedOcelotsCanBeTempted = false;

    public boolean convertToCat = false;

    public TameableOcelotsConfig() {
        super(Identifier.of(TameableOcelots.MOD_ID, "config"));
    }

    public static Stream<ItemStack> getFoxTamingItemStacks() {
        Stream<ItemStack> foxTamingItemStacks = Stream.<ItemStack>builder().build();
        for (Item item : OCELOT_TAMING_ITEMS
        ) {
            foxTamingItemStacks = Stream.concat(foxTamingItemStacks, Stream.of(new ItemStack(item)));
        }
        return foxTamingItemStacks;
    }

    public static Stream<ItemStack> getFoxBreedingItemStacks() {
        Stream<ItemStack> foxBreedingItemStacks = Stream.<ItemStack>builder().build();
        for (Item item : OCELOT_BREEDING_ITEMS
        ) {
            foxBreedingItemStacks = Stream.concat(foxBreedingItemStacks, Stream.of(new ItemStack(item)));
        }
        return foxBreedingItemStacks;
    }

    public static Stream<ItemStack> getFoxTemptingItemStacks() {
        return Stream.concat(getFoxTamingItemStacks(), getFoxBreedingItemStacks());
    }

    public static void init() {
        OCELOT_TAMING_ITEMS = TameableOcelotsConfig.config.ocelotTamingItems.stream().map(Registries.ITEM::get).toList();
        OCELOT_BREEDING_ITEMS = TameableOcelotsConfig.config.ocelotBreedingItems.stream().map(Registries.ITEM::get).toList();
    }

    @Override
    public void onUpdateClient() {
        OCELOT_TAMING_ITEMS = TameableOcelotsConfig.config.ocelotTamingItems.stream().map(Registries.ITEM::get).toList();
        OCELOT_BREEDING_ITEMS = TameableOcelotsConfig.config.ocelotBreedingItems.stream().map(Registries.ITEM::get).toList();
    }
}