package com.github.standobyte.jojo.client.resources.sprites;

import java.util.stream.Stream;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.client.renderer.texture.SpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

@Deprecated
public class ActionSpriteUploader extends SpriteUploader {
    public ActionSpriteUploader(TextureManager textureManager) {
        super(textureManager, new ResourceLocation("textures/atlas/actions.png"), "action");
    }

    @Override
    protected Stream<ResourceLocation> getResourcesToLoad() {
        return JojoCustomRegistries.ACTIONS.getRegistry().getValues().stream().flatMap(Action::getTexLocationstoLoad);
    }

    public <P extends IPower<P, ?>> TextureAtlasSprite getSprite(Action<P> action, P power) {
        return getSprite(action.getTexture(power));
    }
    
    @Override
    public TextureAtlasSprite getSprite(ResourceLocation texLocation) {
        return super.getSprite(texLocation);
    }
    
    public static ResourceLocation getIcon(Action<?> action) {
        ResourceLocation key = action.getRegistryName();
        return new ResourceLocation(key.getNamespace(), "textures/action/" + key.getPath() + ".png");
    }
}
