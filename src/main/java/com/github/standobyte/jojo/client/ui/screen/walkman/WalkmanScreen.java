package com.github.standobyte.jojo.client.ui.screen.walkman;

import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.WalkmanSoundHandler;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.CassetteSide;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.CassetteTracksSided;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.IndicatorStatus;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.Playlist;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.Track;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.TrackInfo;
import com.github.standobyte.jojo.container.WalkmanItemContainer;
import com.github.standobyte.jojo.item.CassetteRecordedItem;
import com.github.standobyte.jojo.item.WalkmanDataCap;
import com.github.standobyte.jojo.item.WalkmanDataCap.PlaybackMode;
import com.github.standobyte.jojo.item.WalkmanItem;
import com.github.standobyte.jojo.item.cassette.CassetteCap.TrackSourceList;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClWalkmanControlsPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

@SuppressWarnings("deprecation")
public class WalkmanScreen extends ContainerScreen<WalkmanItemContainer> {
    static final ResourceLocation WALKMAN_SCREEN_TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/container/walkman.png");
    private static final ResourceLocation WALKMAN_CASSETTE_TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/container/walkman_cassette.png");

    private PlaybackMode mode;
    
    private ItemStack prevCassetteItem = ItemStack.EMPTY;
    private CassetteTracksSided cassetteTracks = CassetteTracksSided.EMPTY_TRACK_LIST;
    private CassetteSide currentSide;
    private TrackInfo currentTrack;
    private List<Track> tracksToShow;
    private int cassetteGeneration;
    
    private Button playButton;
    private Button flipSideButton;
    private Button stopButton;
    private Button rewindButton;
    private Button fastForwardButton;
    private WalkmanVolumeWheel volumeWheel;
    
    private Button playbackModeSwitch;
    
    private int walkmanId;
    
    public WalkmanScreen(WalkmanItemContainer container, PlayerInventory playerInventory, ITextComponent name) {
        super(container, playerInventory, StringTextComponent.EMPTY);
        imageWidth = 194;
        imageHeight = 224;
    }

    @Override
    protected void init() {
        super.init();
        
        ItemStack walkman = menu.getWalkmanItem();
        WalkmanDataCap walkmanData = WalkmanItem.getWalkmanData(walkman).orElse(null);
        mode = walkmanData.getPlaybackMode();
        walkmanId = walkmanData.getId();
        
        Playlist playlist = WalkmanSoundHandler.getPlaylist(walkmanId);
        if (playlist != null) {
            cassetteTracks = playlist.getAllTracks();
        }
        
        initWidgets();
        volumeWheel.setValue(walkmanData.getVolume());
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        updateCassette();
        updateButtons();
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTick);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
        if (getFocused() != null && isDragging() && mouseButton == 0) {
            getFocused().mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
        }
        return true;
    }
    
    private void updateCassette() {
        ItemStack cassetteItem = getCassetteItem();
        if (!ItemStack.matches(this.prevCassetteItem, cassetteItem)) {
            if (cassetteItem.isEmpty()) {
                setCassetteChanged(CassetteTracksSided.EMPTY_TRACK_LIST);
            }
            else {
                CassetteRecordedItem.getCassetteData(cassetteItem).ifPresent(cap -> {
                    TrackSourceList cassetteSources = cap.getTracks();
                    if (cassetteSources.isBroken()) {
                        setCassetteChanged(CassetteTracksSided.EMPTY_TRACK_LIST);
                    }
                    
                    CassetteTracksSided cassetteTracks = CassetteTracksSided.fromSourceList(cassetteSources);
                    if (!cassetteTracks.matches(this.cassetteTracks)) {
                        setCassetteChanged(cassetteTracks);
                    }
                    
                    currentSide = cap.getSide();
                    cassetteGeneration = cap.getGeneration();
                    
                    List<Track> tracks = cassetteTracks.get(currentSide);
                    setTrack(tracks.isEmpty() ? null : TrackInfo.of(cassetteTracks, currentSide, 
                            MathHelper.clamp(cap.getTrackOn(), 0, tracks.size() - 1)));
                });
            }
            
            this.prevCassetteItem = cassetteItem.copy();
        }
    }
    
    private void setCassetteChanged(CassetteTracksSided newTracks) {
        currentTrack = null;
        tracksToShow = null;
        if (WalkmanSoundHandler.getPlaylist(walkmanId) != null) {
            WalkmanSoundHandler.clearPlaylist();
        }
        this.cassetteTracks = newTracks;
    }
    
    private ItemStack getCassetteItem() {
        return menu.getCassetteItem();
    }
    
    
    
    private void initWidgets() {
        int x = getWindowX();
        int y = getWindowY();
        
        rewindButton = addButton(new WalkmanButton(x + 39, y + 107, 14, 13, 
                button -> {
                    Playlist playlist = WalkmanSoundHandler.getPlaylist(walkmanId);
                    setTrack(playlist.getRewindTrack());
                    playlist.setTrack(currentTrack);
                    playlist.setRewindSoundTicks(40, IndicatorStatus.REWIND);
                    playlist.playCurrentTrack();
                }, 
                () -> {
                    TrackInfo track = WalkmanSoundHandler.getPlaylist(walkmanId).getRewindTrack();
                    if (track != currentTrack) {
                        return new TranslationTextComponent("walkman.button.rewind", tooltipTrackName(track));
                    }
                    else {
                        return new TranslationTextComponent("walkman.button.rewind.start");
                    }
                }, this, 39));
        
        playButton = addButton(new WalkmanButton(x + 58, y + 107, 41, 13, 
                button -> {
                    Playlist playlist = WalkmanSoundHandler.initPlaylist(cassetteTracks, getCassetteItem(), walkmanId);
                    playlist.setVolume(volumeWheel.getValue());
                    playlist.setPlaybackMode(mode);
                    playlist.setTrack(currentTrack);
                    Minecraft.getInstance().getMusicManager().stopPlaying();
                    playlist.playCurrentTrack();
                }, 
                () -> {
                    return new TranslationTextComponent("walkman.button.play", tooltipTrackName(currentTrack));
                }, this, 58));
        
        flipSideButton = addButton(new WalkmanButton(x + 58, y + 107, 41, 13, 
                button -> {
                    Playlist playlist = WalkmanSoundHandler.getPlaylist(walkmanId);
                    setTrack(playlist.getFlipSideTrack());
                    playlist.setTrack(currentTrack);
                    playlist.setRewindSoundTicks(20, null);
                    playlist.playCurrentTrack();
                }, 
                () -> {
                    return new TranslationTextComponent("walkman.button.flip", tooltipTrackName(WalkmanSoundHandler.getPlaylist(walkmanId).getFlipSideTrack()));
                }, this, 58));
        
        fastForwardButton = addButton(new WalkmanButton(x + 104, y + 107, 14, 13, 
                button -> {
                    Playlist playlist = WalkmanSoundHandler.getPlaylist(walkmanId);
                    setTrack(playlist.getFastForwardTrack());
                    playlist.setRewindSoundTicks(40, IndicatorStatus.FAST_FORWARD);
                    playlist.setAndPlayNext();
                }, 
                () -> {
                    Playlist playlist = WalkmanSoundHandler.getPlaylist(walkmanId);
                    if (playlist.stopAfterCurrentTrack()) {
                        return new TranslationTextComponent("walkman.button.fast_forward.end");
                    }
                    else {
                        return new TranslationTextComponent("walkman.button.fast_forward", tooltipTrackName(playlist.getFastForwardTrack()));
                    }
                }, this, 104));
        
        stopButton = addButton(new WalkmanButton(x + 131, y + 107, 14, 13, 
                button -> {
                    WalkmanSoundHandler.getPlaylist(walkmanId).stopPlaying();
                }, 
                () -> {
                    return new TranslationTextComponent("walkman.button.stop");
                }, this, 131));
        
        playbackModeSwitch = addButton(new WalkmanButton(x + 175, y + 107 + mode.ordinal() * 9, 8, 15, 
                button -> {
                    mode = mode.getOpposite();
                    if (mode == PlaybackMode.LOOP) button.y += 9;
                    else                           button.y -= 9;
                    Playlist playlist = WalkmanSoundHandler.getPlaylist(walkmanId);
                    if (playlist != null) {
                        playlist.setPlaybackMode(mode);
                    }
                    PacketManager.sendToServer(ClWalkmanControlsPacket.playbackMode(walkmanId, mode));
                }, 
                () -> {
                    return new TranslationTextComponent("walkman.button.playback_mode." + (mode == PlaybackMode.LOOP ? "loop" : "default"));
                }, this, -1) {
            
            @Override
            protected void renderCustomButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.getTextureManager().bind(WalkmanScreen.WALKMAN_SCREEN_TEXTURE);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                blit(matrixStack, x, y, isHovered() ? 175 : 183, 235, width, height);
            }
        });
        
        volumeWheel = addWidget(new WalkmanVolumeWheel(this, x + 17, y + 61, 11, 37));
    }
    
    private void updateButtons() {
        Playlist playlist = WalkmanSoundHandler.getPlaylist(walkmanId);
        if (playlist == null || !playlist.isPlaying()) {
            playButton.active = currentTrack != null;
            flipSideButton.active = stopButton.active = rewindButton.active = fastForwardButton.active = false;
        }
        else {
            playButton.active = false;
            flipSideButton.active = playlist.getFlipSideTrack() != null;
            stopButton.active = true;
            rewindButton.active = playlist.getRewindTrack() != null;
            fastForwardButton.active = true;
        }
        playButton.visible = !flipSideButton.active;
        flipSideButton.visible = !playButton.visible;
    }
    
    public void setTrack(TrackInfo track) {
        this.currentTrack = track;
        
        if (track != null) {
            CassetteRecordedItem.getCassetteData(getCassetteItem()).ifPresent(cap -> {
                if (cap.getTracks().isBroken()) return;
                
                int trackNumber = track.number;
                List<Track> tracksThisSide = cassetteTracks.get(track.side);
    
                int showFrom = trackNumber - 1;
                int showTo = trackNumber + 2;
                if (showFrom < 0) {
                    showTo -= showFrom;
                    showFrom = 0;
                }
                if (showTo > tracksThisSide.size()) {
                    showFrom = Math.max(0, tracksThisSide.size() - 3);
                    showTo = tracksThisSide.size();
                }
                tracksToShow = tracksThisSide.subList(showFrom, showTo);
            });
        }
    }
    
    private ITextComponent tooltipTrackName(TrackInfo track) {
        return track != null ? track.track.getName().withStyle(TextFormatting.DARK_GREEN, TextFormatting.UNDERLINE)
                : new StringTextComponent("ERROR").withStyle(TextFormatting.RED, TextFormatting.BOLD);
    }
    
    

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(WALKMAN_SCREEN_TEXTURE);
        int windowX = getWindowX();
        int windowY = getWindowY();
        
        volumeWheel.render(matrixStack, mouseX, mouseY, partialTick);
        
        blit(matrixStack, windowX, windowY, 0, 0, imageWidth, imageHeight);

        renderIndicators(matrixStack, partialTick, windowX, windowY);
        renderCassette(matrixStack, partialTick, windowX, windowY);
        RenderSystem.disableBlend();
    }
    
    private int getWindowX() {
        return leftPos;
    }
    
    private int getWindowY() {
        return (height - imageHeight) / 2;
    }
    
    private void renderIndicators(MatrixStack matrixStack, float partialTick, int windowX, int windowY) {
        Playlist playlist = WalkmanSoundHandler.getPlaylist(walkmanId);
        if (playlist != null && playlist.isPlaying()) {
            CassetteSide side = currentSide;
            IndicatorStatus currentOperation = playlist.getIndicatorStatus();

            boolean fwdFlicker = currentOperation == IndicatorStatus.FAST_FORWARD;
            boolean fwdLight = !fwdFlicker;
            boolean revFlicker = currentOperation == IndicatorStatus.REWIND;
            boolean revLight = false;
            
            if (side == CassetteSide.SIDE_B) {
                boolean tmp = fwdLight;
                fwdLight = revLight;
                revLight = tmp;
                tmp = fwdFlicker;
                fwdFlicker = revFlicker;
                revFlicker = tmp;
            }

            minecraft.getTextureManager().bind(WALKMAN_SCREEN_TEXTURE);
            boolean flickerTick = minecraft.player.tickCount % 40 >= 20;
            if (revLight || revFlicker && flickerTick) {
                blit(matrixStack, windowX + 17, windowY + 18, 17, 226, 9, 9);
            }
            if (fwdLight || fwdFlicker && flickerTick) {
                blit(matrixStack, windowX + 17, windowY + 35, 17, 243, 9, 9);
            }
        }
    }
    
    private void renderCassette(MatrixStack matrixStack, float partialTick, int windowX, int windowY) {
        ItemStack cassetteItem = getCassetteItem();
        if (!cassetteItem.isEmpty()) {
            minecraft.getTextureManager().bind(WALKMAN_CASSETTE_TEXTURE);
            blit(matrixStack, windowX + 35, windowY + 7, 0, 0, 150, 95);
            
            Optional<DyeColor> color = CassetteRecordedItem.getCassetteData(cassetteItem).map(cap -> cap.getDye());
            if (color.isPresent()) {
                blit(matrixStack, windowX + 40, windowY + 41, 5, 128 + color.get().ordinal() * 8, 140, 7);
            }
            
            if (currentSide != null) {
                blit(matrixStack, windowX + 47, windowY + 49, 204 + currentSide.ordinal() * 16, 41, 11, 11);
            }
        }
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        ItemStack cassetteItem = getCassetteItem();
        if (!cassetteItem.isEmpty()) {
            if (cassetteItem.hasCustomHoverName()) {
                ITextComponent cassetteName = cassetteItem.getHoverName();
                IReorderingProcessor reorderingProc = cassetteName.getVisualOrderText();
                font.draw(matrixStack, reorderingProc, 110 - font.width(reorderingProc) / 2, 66, 0x404040);
            }
            
            if (tracksToShow != null && !tracksToShow.isEmpty()) {
                int i = 0;
                for (Track track : tracksToShow) {
                    IFormattableTextComponent trackName = track.getName(true);

                    int width = font.width(trackName);
                    if (width > 136) {
                        ITextProperties shortenedTrackName = font.getSplitter().splitLines(trackName, 130, Style.EMPTY).get(0);
                        trackName = new TranslationTextComponent("jojo.textutil.shortened", shortenedTrackName.getString());
                    }

                    if (currentTrack != null && currentTrack.track.equals(track)) {
                        trackName.withStyle(TextFormatting.UNDERLINE);
                    }

                    font.draw(matrixStack, trackName, 42, 13 + i * 9, 0x404040);
                    i++;
                }
            }
        }
    }
    
    void onVolumeChanged(float volume) {
        Playlist playlist = WalkmanSoundHandler.getPlaylist(walkmanId);
        if (playlist != null) {
            playlist.setVolume(volume);
        }
        PacketManager.sendToServer(ClWalkmanControlsPacket.setVolume(walkmanId, volume));
    }
}
