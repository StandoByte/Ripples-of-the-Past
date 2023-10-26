package com.github.standobyte.jojo.client.playeranim.playeranimator.anim;

import com.github.standobyte.jojo.client.playeranim.playeranimator.PlayerAnimatorInstalled.AnimLayerHandler;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TestAnimLayer extends AnimLayerHandler {

    public TestAnimLayer(ResourceLocation id) {
        super(id);
    }

    @Override
    public ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null);
    }
    
    @Override
    public boolean isForgeEventHandler() {
        return true;
    }
    
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
//        if (event.getMessage().getString().contains("anim start")) {
//            testAnim((AbstractClientPlayerEntity) Minecraft.getInstance().level.getPlayerByUUID(event.getSenderUUID()), true);
//        }
//        else if (event.getMessage().getString().contains("anim end")) {
//            testAnim((AbstractClientPlayerEntity) Minecraft.getInstance().level.getPlayerByUUID(event.getSenderUUID()), false);
//        }
    }

    @SubscribeEvent
    public void preRender(RenderPlayerEvent.Pre event) {
        preventCrouch((AbstractClientPlayerEntity) event.getPlayer(), event.getRenderer());
    }
    
    private void testAnim(AbstractClientPlayerEntity player, boolean enabled) {
//        if (enabled) {
//            setAnimFromName(player, new ResourceLocation(JojoMod.MOD_ID, "glider_hold"));
//        }
//        else {
//            fadeOutAnim((AbstractClientPlayerEntity) player, AbstractFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
//        }
    }
    
}
