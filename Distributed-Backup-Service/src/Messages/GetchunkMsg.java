package Messages;

import Utils.Utils;

import java.util.regex.Matcher;

public class GetchunkMsg extends Message implements msgGenerator {

    /**
     * Regex used to parse a String containing a 'getchunk' message
     */
    private final static String REGEX_STRING =
            "\\s*?GETCHUNK\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+((\\d){1,6})\\s+?\\r\\n\\r\\n";

    /**
     * The chunk number
     */
    private int chunkNum;

    public GetchunkMsg(String receivedMsg) {
        super(REGEX_STRING);
        Matcher protocolMatch = msgRegex.matcher(receivedMsg);

        if (! protocolMatch.matches()) {
            Utils.showError("Failed to get a Regex match in received message", this.getClass());
            throw new ExceptionInInitializerError();
        }

        protocolVersion = Float.parseFloat(protocolMatch.group(VERSION_GROUP));
        senderID = Integer.parseInt(protocolMatch.group(SENDER_ID_GROUP));
        fileID = protocolMatch.group(FIELD_ID_GROUP);
        chunkNum = Integer.parseInt(protocolMatch.group(CHUNK_NUM_GROUP));
    }

    public GetchunkMsg(float protocolVersion, int senderID, String fileID, int chunkNum, int repDegree) {
        super(protocolVersion, senderID, fileID);
        this.chunkNum = chunkNum;
    }

    @Override
    public String genMsg() {
        return ("GETCHUNK" +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                chunkNum + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF);
    }
}