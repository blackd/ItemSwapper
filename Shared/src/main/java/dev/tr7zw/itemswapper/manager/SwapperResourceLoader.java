package dev.tr7zw.itemswapper.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import dev.tr7zw.itemswapper.ItemSwapperSharedMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class SwapperResourceLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public SwapperResourceLoader() {
        super(GSON, "itemswapper");

    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
            ProfilerFiller profilerFiller) {
        ItemSwapperSharedMod.instance.getItemGroupManager().reset();
        for (Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            try {
                if (!entry.getKey().getNamespace().equals("itemswapper"))
                    continue;
                Item[] items = getItemArray(entry.getValue(), entry.getKey().getPath().startsWith("wheel"));
                if(items != null) {
                    if (entry.getKey().getPath().startsWith("wheel_primary/")) {
                        ItemSwapperSharedMod.instance.getItemGroupManager().registerCollection(items);
                    }
                    if (entry.getKey().getPath().startsWith("wheel_secondary/")) {
                        ItemSwapperSharedMod.instance.getItemGroupManager().registerSecondaryCollection(items);
                    }
                    if (entry.getKey().getPath().startsWith("list/")) {
                        ItemSwapperSharedMod.instance.getItemGroupManager().registerListCollection(items);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
    }
    
    private Item[] getItemArray(JsonElement json, boolean wheel) {
        if(!json.isJsonArray()) {
            return null;
        }
        List<Item> itemList = new ArrayList<>();
        json.getAsJsonArray().forEach(el -> {
            if(el.isJsonPrimitive()) {
                ResourceLocation resourceLocation = new ResourceLocation(el.getAsString());
                Item item = Registry.ITEM.get(resourceLocation);
                if(item == null) {
                    ItemSwapperSharedMod.LOGGER.warn("Unknown item: " + el.getAsString());
                    if(wheel) {
                        // For unknown items, don't move the rest of the wheel
                        itemList.add(Items.AIR);
                    }
                    return;
                }
                if(!itemList.contains(item) && (!wheel || itemList.size() < 8)) {
                    itemList.add(item);
                }
            }
        });
        while(wheel && itemList.size() < 8) {
            itemList.add(Items.AIR);
        }
        if(!itemList.isEmpty()) {
            return itemList.toArray(new Item[0]);
        }
        return null;
    }
    
}