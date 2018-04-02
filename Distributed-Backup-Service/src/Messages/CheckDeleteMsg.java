package Messages;

import Utils.Utils;

import java.util.regex.Matcher;

public class CheckDeleteMsg extends Message implements msgGenerator {

    /**
     * Regex used to parse a String containing a 'remove' message
     */
    private final static String REGEX_STRING =
            "\\s*?CHECKDELETE\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+?\\r\\n\\r\\n";

    public CheckDeleteMsg(String receivedMsg) {
        super(REGEX_STRING);
        Matcher protocolMatch = msgRegex.matcher(receivedMsg);

        if (! protocolMatch.matches()) {
            Utils.showError("Failed to get a Regex match in received message", this.getClass());
            throw new ExceptionInInitializerError();
        }

        protocolVersion = Float.parseFloat(protocolMatch.group(VERSION_GROUP));
        senderID = Integer.parseInt(protocolMatch.group(SENDER_ID_GROUP));
        fileID = protocolMatch.group(FIELD_ID_GROUP);
    }

    public CheckDeleteMsg(float protocolVersion, int senderID, String fileID) {
        super(protocolVersion, senderID, fileID);
    }

    @Override
    public byte[] genMsg() {
        return ("CHECKDELETE" + " " +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF).getBytes();
    }
}