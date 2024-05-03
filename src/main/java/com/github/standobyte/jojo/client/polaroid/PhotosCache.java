package com.github.standobyte.jojo.client.polaroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.system.MemoryUtil;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.item.polaroid.ClPhotoSender;
import com.github.standobyte.jojo.network.BatchReceiver;
import com.github.standobyte.jojo.network.BatchSender;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClPhotoAssignIdPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClPhotoRequestPacket;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.FolderName;

public class PhotosCache {
    private static Map<UUID, Long2ObjectMap<PhotoHolder>> photosCache = new HashMap<>();
    private static Map<UUID, SendPhotoToServer> toSend = new HashMap<>();
    
    
    static void queueToSendToServer(NativeImage photo, NativeImage highQuality, int giveToPlayer) {
        UUID tmpUuid = UUID.randomUUID();
        toSend.put(tmpUuid, new SendPhotoToServer(tmpUuid, photo, highQuality, giveToPlayer));
        
        SendPhotoToServer send = toSend.get(tmpUuid);
        PacketManager.sendToServer(new ClPhotoAssignIdPacket(send.tmpUuid, send.giveItemToPlayer));
    }
    
    public static void tick() {
        Iterator<SendPhotoToServer> senders = toSend.values().iterator();
        while (senders.hasNext()) {
            SendPhotoToServer sender = senders.next();
            if (sender.tick()) {
                sender.close();
                senders.remove();
            }
        }
        
        photosCache.values().forEach(photos -> photos.values().forEach(PhotoHolder::tick));
    }
    
    
    static PhotoHolder cacheImage(UUID serverId, long photoId, NativeImage image) {
        PhotoInstance photo = PhotoInstance.create(image, serverId, photoId);
        PhotoHolder photoHolder = new PhotoHolder(serverId, photoId, photo);
        photosCache.computeIfAbsent(serverId, id -> new Long2ObjectOpenHashMap<>()).put(photoId, photoHolder);
        return photoHolder;
    }
    
    public static void assignImageId(UUID serverId, long photoId, UUID usedTmpId, boolean saveToFile) {
        SendPhotoToServer sent = toSend.get(usedTmpId);
        if (sent != null) {
            PhotoHolder photo = cacheImage(serverId, photoId, sent.photo);
            if (saveToFile) {
                photo.saveToFile();
            }
            sent.sendPhotoToServer(serverId, photoId);
        }
    }

    public static final FolderName PHOTO_DIR = new FolderName("jojo_polaroid");
    private static File getPhotosFolder(UUID serverId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isLocalServer()) {
            return mc.getSingleplayerServer().getWorldPath(PHOTO_DIR).toFile();
        }
        else {
            return new File(mc.gameDirectory, PHOTO_DIR.getId() + "/" + serverId.toString());
        }
    }
    
    private static File makePhotoFile(File folder, long photoId) {
        folder.mkdirs();
        return new File(folder, photoId + ".png");
    }
    
    
    public static PhotoHolder getOrCreatePhotoHolder(UUID serverId, long photoId) {
        PhotoHolder photo = photosCache.computeIfAbsent(serverId, id -> new Long2ObjectOpenHashMap<>())
                .computeIfAbsent(photoId, id -> new PhotoHolder(serverId, id));
        return photo;
    }
    
    @Nullable
    public static PhotoInstance getOrTryLoadPhoto(UUID serverId, long photoId) {
        if (serverId == null) return null;
        PhotoHolder photo = getOrCreatePhotoHolder(serverId, photoId);
        photo.tryLoad();
        return photo.photoInstance;
    }
    
    public static PhotoHolder.Status getCacheStatus(UUID serverId, long photoId) {
        if (serverId == null) return null;
        PhotoHolder photo = getOrCreatePhotoHolder(serverId, photoId);
        return photo.status;
    }
    
    public static void onLogOut(UUID serverId) {
        if (serverId != null) {
            Long2ObjectMap<PhotoHolder> photos = photosCache.get(serverId);
            if (photos != null) {
                Iterator<PhotoHolder> iter = photos.values().iterator();
                while (iter.hasNext()) {
                    PhotoHolder photo = iter.next();
                    if (photo.status == PhotoHolder.Status.FAILED) {
                        iter.remove();
                    }
                }
            }
        }
    }
    
    
    private static class SendPhotoToServer {
        private final UUID tmpUuid;
        private final NativeImage photo;
        private final NativeImage highQuality;
        private final int giveItemToPlayer;
        
        private ClPhotoSender photoSender = null;
        
        private SendPhotoToServer(UUID tmpUuid, NativeImage photo, NativeImage highQuality, int giveItemToPlayer) {
            this.tmpUuid = tmpUuid;
            this.photo = photo;
            this.highQuality = highQuality;
            this.giveItemToPlayer = giveItemToPlayer;
        }
        
        public void sendPhotoToServer(UUID serverId, long photoId) {
            try {
                byte[] data = photo.asByteArray();
                this.photoSender = new ClPhotoSender(data, serverId, photoId);
                // TODO send the HQ version too
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public boolean tick() {
            if (photoSender != null) {
                photoSender.sendNext();
                return photoSender.finishedSending();
            }
            return false;
        }
        
        void close() {
            highQuality.close();
        }
    }
    
    
    private static NativeImage largeImageFromHeapBuf(ByteBuffer input) throws IOException {
        input.rewind();
        ByteBuffer offHeapBuf = MemoryUtil.memAlloc(input.capacity());
        offHeapBuf.put(input);
        offHeapBuf.rewind();
        NativeImage image = NativeImage.read(offHeapBuf);
        return image;
    }
    
    
    public static class PhotoHolder {
        private final UUID serverUuid;
        private final long photoId;
        @Nullable private PhotoInstance photoInstance;
        @Nonnull private Status status;
        private BatchReceiver dataReceive = null;
        private ByteBuffer fullDataReceived = null;
        private int failedRetryTick;
        
        private PhotoHolder(UUID serverUuid, long photoId) {
            this.serverUuid = serverUuid;
            this.photoId = photoId;
            this.status = Status.EMPTY;
        }
        
        private PhotoHolder(UUID serverUuid, long photoId, PhotoInstance photoInstance) {
            this(serverUuid, photoId);
            if (photoInstance != null) {
                this.photoInstance = photoInstance;
                this.status = Status.CACHED;
            }
        }
        
        private void tryLoad() {
            switch (status) {
            case EMPTY:
                status = Status.LOADING_FILE;
                File photoFile = makePhotoFile(getPhotosFolder(serverUuid), photoId);
                if (photoFile.isFile()) {
                    try (InputStream inputstream = new FileInputStream(photoFile)) {
                        NativeImage nativeImage = NativeImage.read(inputstream);
                        PhotoInstance photo = PhotoInstance.create(nativeImage, serverUuid, photoId);
                        this.photoInstance = photo;
                        status = Status.CACHED;
                    } catch (Throwable throwable) {
                        JojoMod.getLogger().error("Could not load photo {}_{}", serverUuid.toString(), photoId);
                        photoFile.delete();
                        requestFromServer();
                    }
                }
                else {
                    requestFromServer();
                }
                break;
            case RECEIVED_FULL_FROM_SERVER:
                fullDataReceived.rewind();
                try {
                    NativeImage image = largeImageFromHeapBuf(fullDataReceived);
                    photoInstance = PhotoInstance.create(image, serverUuid, photoId);
                    status = Status.CACHED;
                    saveToFile();
                } catch (Exception e) {
                    e.printStackTrace();
                    setFailed();
                }
                break;
            default:
                break;
            }
        }
        
        private void requestFromServer() {
            status = Status.REQUESTED_FROM_SERVER;
            dataReceive = new BatchReceiver();
            PacketManager.sendToServer(new ClPhotoRequestPacket(photoId));
        }
        
        public void readBatchFromPacket(BatchSender.Batch data) {
            switch (status) {
            case REQUESTED_FROM_SERVER:
            case RECEIVING_FROM_SERVER:
            case FAILED:
                if (data != null) {
                    status = Status.RECEIVING_FROM_SERVER;
                    ByteBuffer fullPhoto = dataReceive.receiveYou(data);
                    if (fullPhoto != null) {
                        if (fullPhoto.capacity() > 0) {
                            fullDataReceived = fullPhoto;
                            status = Status.RECEIVED_FULL_FROM_SERVER;
                        }
                        else {
                            setFailed();
                        }
                    }
                }
                else {
                    setFailed();
                }
                break;
            default:
                break;
            }
        }
        
        public void saveToFile() {
            if (photoInstance != null) {
                File folder = getPhotosFolder(serverUuid);
                File photoFile = makePhotoFile(folder, photoId);
                try {
                    photoInstance.image.writeToFile(photoFile);
                } catch (IOException e) {
                    JojoMod.getLogger().error("Failed to save polaroid photo", e);
                }
            }
        }
        
        private void setFailed() {
            dataReceive = new BatchReceiver();
            status = Status.FAILED;
//            failedRetryTick = 100;
        }
        
        void tick() {
//            if (status == Status.FAILED && --failedRetryTick <= 0) {
//                status = Status.EMPTY;
//            }
        }
        
        public enum Status {
            EMPTY,
            LOADING_FILE,
            REQUESTED_FROM_SERVER,
            RECEIVING_FROM_SERVER,
            RECEIVED_FULL_FROM_SERVER,
            CACHED,
            FAILED
        }
    }
    
    public static class PhotoInstance {
        final NativeImage image;
        final DynamicTexture texture;
        final RenderType renderType;
        
        public static PhotoInstance create(NativeImage image, UUID serverUuid, long photoId) {
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation path = Minecraft.getInstance().textureManager.register(
                    String.format("jojo_photo%d_%d", serverUuid.hashCode(), photoId), texture);
            RenderType renderType = RenderType.text(path);
            return new PhotoInstance(image, texture, renderType);
        }
        
        private PhotoInstance(NativeImage image, DynamicTexture texture, RenderType renderType) {
            this.image = image;
            this.texture = texture;
            this.renderType = renderType;
        }
    }
}
