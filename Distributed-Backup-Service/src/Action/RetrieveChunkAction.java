package Action;

import Channel.RestoreChannel;
import Database.ChunksRecorder;
import Messages.ChunkMsg;
import Messages.GetchunkMsg;
import Messages.Message;
import Utils.*;

import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Class representing the Retrieve ChunkAction
 */
public class RetrieveChunkAction extends ActionHasReply {

    /**
     * Maximum time waited to trigger the Retrieve Action.
     */
    private final static int MAX_TIME_TO_SEND = 4000;

    /**
     * The getchunk message that triggered this action
     */
    private GetchunkMsg getchunkMsg;

    /**
     * Boolean indicating if the chunk was indeed stored, and therefore can be retrieved
     */
    private boolean isStored;

    /**
     * The channel used to communicate with other peers, regarding restore information
     */
    private RestoreChannel restoreChannel;

    /**
     * Data Structure to get information from, referent to the Peer stored files' chunks
     */
    private ChunksRecorder peerStoredChunks;

    /**
     * The identifier of the Peer associated to this action
     */
    private int peerID;

    /**
     * Chunks to be resent to the
     */
    private byte[] chunk;

    /**
     * Thread Executor to begin the Scheduled Future
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Schedule Future used to begin the retrieve associated action after a given time
     */
    private ScheduledFuture chunkSender;


    public RetrieveChunkAction (RestoreChannel restoreChannel, ChunksRecorder peerStoredChunks, int peerID, GetchunkMsg requestMsg) {
        this.restoreChannel = restoreChannel;
        this.peerID = peerID;
        getchunkMsg = requestMsg;
        this.peerStoredChunks = peerStoredChunks;

        isStored = getChunk();
    }

    /**
     * Get the chunk bytes to retrieved
     *
     * @return If the chunk wa successfully extracted
     */
    private boolean getChunk() {
        String fileID = getchunkMsg.getFileID();
        int chunkNum = getchunkMsg.getChunkNum();

        if (! peerStoredChunks.hasChunk(fileID, chunkNum))
            return false;

        try {
            this.chunk = Files.readAllBytes(FileManager.getChunkFile(peerID, fileID, chunkNum));
            return true;

        } catch (java.io.IOException e) {
            Utils.showWarning("Failed to get chunk bytes", this.getClass());
        }

        return false;
    }

    @Override
    public void run() {
        if (isStored) {
            restoreChannel.subscribeAction(this);
            chunkSender = scheduler.schedule(() -> {
                try {
                    restoreChannel.sendMessage(
                            new ChunkMsg(getchunkMsg.getProtocolVersion(), peerID,
                                    getchunkMsg.getFileID(), getchunkMsg.getChunkNum(), chunk).genMsg()
                    );
                    restoreChannel.unsubscribeAction(this);
                } catch (ExceptionInInitializerError e) {
                    Utils.showError("Failed to build message, stopping Store action", this.getClass());
                }
            }, new Random().nextInt(MAX_TIME_TO_SEND), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void parseResponse(Message msg) {
        if (! (msg instanceof ChunkMsg))
            return;

        ChunkMsg realMsg = (ChunkMsg) msg;
        if ((realMsg.getFileID().equals(getchunkMsg.getFileID())) &&
            (realMsg.getChunkNum() == getchunkMsg.getChunkNum())) {
            chunkSender.cancel(true);
            restoreChannel.unsubscribeAction(this);
        }
    }
}
