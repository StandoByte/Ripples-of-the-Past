package com.github.standobyte.jojo.network;

import java.nio.ByteBuffer;
import java.util.OptionalInt;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class BatchReceiver {
    private OptionalInt batchesCount = OptionalInt.empty();
    private Int2ObjectMap<ByteBuffer> batchesReceived = new Int2ObjectArrayMap<>();
    

    public ByteBuffer receiveYou(BatchSender.Batch receivedBatch) {
        if (receivedBatch.batchStart != 0 || receivedBatch.batchSize != receivedBatch.dataBatch.length) {
            throw new IllegalArgumentException();
        }
        return receiveBatch(receivedBatch.dataBatch, receivedBatch.batchIndex, receivedBatch.isLastBatch);
    }
    
    /**
     * @return If all the batches have been received, returns a merged ByteBuffer with all the data, null otherwise
     */
    public ByteBuffer receiveBatch(byte[] batch, int batchIndex, boolean isLastBatch) {
        if (batchesReceived.containsKey(batchIndex)) {
            throw new IllegalStateException(String.format("Already have a batch with index %d", batchIndex));
        }
        
        if (isLastBatch) {
            if (batchesCount.isPresent()) {
                throw new IllegalStateException(String.format("Already set the number of batches as %d, tried to set it to %d", batchesCount.getAsInt(), batchIndex + 1));
            }
            batchesCount = OptionalInt.of(batchIndex + 1);
            if (batchesReceived.keySet().stream().anyMatch(key -> key > batchIndex)) {
                throw new IllegalStateException(String.format("Batches with index higher than the last one (%d) are present", batchIndex));
            }
        }
        else if (batchesCount.isPresent() && batchIndex >= batchesCount.getAsInt()) {
            throw new IllegalStateException(String.format("Received a batch with index higher than the last one (%d > %d)", batchIndex, batchesCount.getAsInt() - 1));
        }
        
        ByteBuffer wrapper = ByteBuffer.wrap(batch);
        batchesReceived.put(batchIndex, wrapper);
        
        if (batchesCount.isPresent() && batchesCount.getAsInt() == batchesReceived.size()) {
            int fullSize = batchesReceived.values().stream().mapToInt(ByteBuffer::capacity).sum();
            ByteBuffer fullBuf = ByteBuffer.allocate(fullSize);
            for (int i = 0; i < batchesCount.getAsInt(); i++) {
                fullBuf.put(batchesReceived.get(i));
            }
            fullBuf.flip();
            return fullBuf;
        }
        else {
            return null;
        }
    }
    
    
    
    public static byte[] byteBufferToArray(ByteBuffer buf) {
        byte[] arr;
        if (buf.hasArray()) {
            arr = buf.array();
        }
        else {
            buf.position(0);
            arr = new byte[buf.limit()];
            buf.get(arr);
        }
        return arr;
    }
}
