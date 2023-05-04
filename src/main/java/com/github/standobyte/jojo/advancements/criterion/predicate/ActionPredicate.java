package com.github.standobyte.jojo.advancements.criterion.predicate;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;

public class ActionPredicate {
    public static final ActionPredicate ANY = new ActionPredicate(null);
    @Nullable
    private final Action<?> action;
    
    public ActionPredicate(Action<?> action) {
        this.action = action;
    }
    
    public boolean matches(Action<?> action) {
        if (this == ANY) {
            return true;
        }
        return this.action == null || this.action == action;
    }
    
    public static ActionPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return ANY;
        }
        else {
            JsonObject jsonObject = JSONUtils.convertToJsonObject(json, "JoJo action");
            
            ResourceLocation resLoc = new ResourceLocation(JSONUtils.getAsString(jsonObject, "name"));
            Action<?> action = Optional.ofNullable(((ForgeRegistry<Action<?>>) JojoCustomRegistries.ACTIONS.getRegistry())
                    .getRaw(resLoc)).orElseThrow(() -> {
                        return new JsonSyntaxException("Unknown action '" + resLoc + "'");
                    });
            
            return new ActionPredicate(action);
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonobject = new JsonObject();
            if (action != null) {
                jsonobject.addProperty("name", action.getRegistryName().toString());
            }
            return jsonobject;
        }
    }

}
