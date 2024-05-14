package com.github.standobyte.jojo.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;

public abstract class BatchSender {
    protected final byte[] data;
    protected final int maxPayloadSize;
    protected boolean finishedSending = false;
    private int batchesCount;
    private int batchToSend = 0;
    private int dataIndex = 0;
    
    public BatchSender(byte[] data) {
        this(data, 32767 - 128);
    }
    
    public BatchSender(byte[] data, int maxPayloadSize) {
        this.data = data;
        this.maxPayloadSize = maxPayloadSize;
        this.batchesCount = (data.length - 1) / maxPayloadSize + 1;
    }
    
    public void sendAll() {
        if (data.length < maxPayloadSize) {
            sendBatch(new Batch(0, true, data));
        }
        else {
            while (!finishedSending) {
                sendNext();
            }
        }
    }

    public void sendNext() {
        int batchSize = Math.min(maxPayloadSize, data.length - dataIndex);
        Batch batch = new Batch(batchToSend, batchToSend == batchesCount - 1, data, dataIndex, batchSize);
        sendBatch(batch);
        dataIndex += batchSize;
        batchToSend++;
        finishedSending = batchToSend == batchesCount;
    }
    
    public boolean finishedSending() {
        return finishedSending;
    }
    
    protected abstract void sendBatch(Batch batch);
    
    
    
    public static class Batch {
        public final int batchIndex;
        public final boolean isLastBatch;
        public final byte[] dataBatch;
        public final int batchStart;
        public final int batchSize;
        
        public Batch(int batchIndex, boolean isLastBatch, byte[] dataBatch) {
            this(batchIndex, isLastBatch, dataBatch, 0, dataBatch.length);
        }
        
        public Batch(int batchIndex, boolean isLastBatch, byte[] dataBatch, int batchStart, int batchSize) {
            this.batchIndex = batchIndex;
            this.isLastBatch = isLastBatch;
            this.dataBatch = dataBatch;
            this.batchStart = batchStart;
            this.batchSize = batchSize;
        }
        
        public void toBuf(PacketBuffer buf) {
            buf.writeVarInt(batchIndex);
            buf.writeBoolean(isLastBatch);
            buf.writeInt(batchSize);
            buf.writeBytes(dataBatch, batchStart, batchSize);
        }
        
        public static Batch fromBuf(PacketBuffer buf) {
            int batchIndex = buf.readVarInt();
            boolean isLastBatch = buf.readBoolean();
            int batchSize = buf.readInt();
            ByteBuf batchRead = buf.readBytes(batchSize);
            return new Batch(batchIndex, isLastBatch, toArr(batchRead));
        }
        
        public static byte[] toArr(ByteBuf byteBuf) {
            if (byteBuf.hasArray()) {
                byte[] array = byteBuf.array();
                return array;
            } else {
                byte[] array = new byte[byteBuf.capacity()];
                byteBuf.getBytes(0, array);
                return array;
            }
        }
    }
}
