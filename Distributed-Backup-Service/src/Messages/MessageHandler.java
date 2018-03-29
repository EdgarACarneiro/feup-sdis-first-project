package Messages;

import Action.StoreAction;
import Channel.ControlChannel;
import ThreadPool.ThreadPool;
import Utils.Utils;

public class MessageHandler {

    /**
     * The maximum length of a possible message type
     */
    private final static int MAXIMUM_TYPE_SIZE = 8;

    public static Message messageInterpreter(String receivedMsg) {
        String trimmedMsg = receivedMsg.trim();
        String[] temp = trimmedMsg.substring(0, MAXIMUM_TYPE_SIZE).split(" ");

        try {
            switch (temp[0].trim()) {
                case "PUTCHUNK":
                    return new PutchunkMsg(trimmedMsg);
                case "STORED":
                    return new StoredMsg(trimmedMsg);
                case "GETCHUNK":
                    return new GetchunkMsg(trimmedMsg);
                case "CHUNK":
                    return new ChunkMsg(trimmedMsg);
                case "DELETE":
                    return new DeleteMsg(trimmedMsg);
                case "REMOVED":
                    return new RemovedMsg(trimmedMsg);
                default:
                    Utils.showWarning("Unrecognizable message type. Discarding it.", MessageHandler.class);
                    return null;
            }
        } catch (ExceptionInInitializerError e) {
            Utils.showWarning("Unrecognizable message type. Discarding it.", MessageHandler.class);
            return null;
        }
    }

    public static void messageHandler(ControlChannel controlChannel, ThreadPool threadPool, int peerID, Message message) {
        if (message == null || peerID == message.getSenderID())
            return;

        System.out.println("RECEIVED MESSAGE: " + message);

        if (message instanceof PutchunkMsg) {
            threadPool.executeThread(
                    new StoreAction(controlChannel, peerID, (PutchunkMsg) message)
            );
        }
    }
}
