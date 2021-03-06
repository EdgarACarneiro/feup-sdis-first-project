package Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.FileHandler;

/**
 * Class responsible for splitting a file in chunks
 */
public final class FileManager {

    /**
     * The application workspace, where the backup files will be
     */
    private final static String APPLICATION_WORKSPACE = "user.dir";

    /**
     * The base directory for a directory containing all the peer associated chunks and files
     */
    public final static String BASE_DIRECTORY_NAME = "backup-";

    /**
     * The configuration file name for the configurations regarding backed up files
     */
    public final static String BACKED_UP_FILES_SERIALIZABLE = "BackedUpFiles.ser";

    /**
     * The configuration file name for the configurations regarding chunks stored from other peers
     */
    public final static String CHUNKS_RECORDER_SERIALIZABLE = "ChunksRecorder.ser";

    /**
     * Chunks' size in bytes : 64KBytes (chunk's Body)
     */
    private static final int CHUNKS_SIZE = 64000;

    /**
     * Split the the file (if present in the given filepath) into chunks of maximum size of FileManager.CHUNKS_SIZE Kb
     *
     * @param filePath The file path of the file
     * @return List with the chunks created
     */
    public static ArrayList<byte[]> splitFile(String filePath) {

        Path path = getPath(filePath);
        if (path == null)
            return null;

        ArrayList<byte[]> chunks = new ArrayList<>();
        try {

            byte[] fileData = Files.readAllBytes(path);
            int fileSize = fileData.length;
            int writtenBytes = 0;

            while (writtenBytes < fileSize) {
                chunks.add(Arrays.copyOfRange(fileData, writtenBytes,
                        ((fileSize - writtenBytes) < CHUNKS_SIZE?
                                (writtenBytes += (fileSize - writtenBytes)):
                                (writtenBytes += CHUNKS_SIZE)
                        )
                ));
            }

        } catch (java.io.IOException e) {
            Utils.showError("Unable to handle file bytes", FileManager.class);
        }

        return chunks;
    }

    /**
     * Creates a file from the given chunks, naming it with given fileName and creating it a the given file path
     *
     * @param chunks The chunks that will form the file
     * @param filePath The file's path
     * @param fileName The file's name
     * @return True if the file was successfully created, false otherwise
     */
    public static boolean createFile (HashMap<Integer, byte[]> chunks, String filePath, String fileName) {

        Path path = getPath(filePath);
        if (path == null)
            return false;

        try {
            FileOutputStream out = new FileOutputStream(filePath + "/" + fileName);

            for (int i = 0; i < chunks.size(); ++i) {
                byte[] chunk = chunks.get(i);
                out.write(chunk, 0, chunk.length);
            }
            out.close();

        } catch (java.io.FileNotFoundException e ) {
            Utils.showError("Unable to find to create output file.", FileHandler.class);
            return false;
        } catch (java.io.IOException e) {
            Utils.showError("Unable to output to created file.", FileHandler.class);
            return false;
        }

        return true;
    }

    /**
     * Generates the fileId of a given file, using sha256 over some file peculiarity
     *
     * @param filePath The path of the file to be processed
     * @return The resultant fileID or empty string if  something failed
     */
    public static String genFileID(String filePath) {

        Path path = getPath(filePath);
        if (path == null)
            return "";

        File file = path.toFile();

        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA-256");
            byte[] hashedMessage =  hasher.digest(
                    (file.getName() + file.lastModified()).getBytes("UTF-8")
            );

            // Convert byte[] to String
            StringBuffer hashAsString = new StringBuffer();
            for (byte hashByte : hashedMessage) {
                hashAsString.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }
            return hashAsString.toString();

        } catch (java.security.NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            Utils.showError("failed to apply sha256", FileManager.class);
        }
        return "";
    }

    /**
     * Get the file name from a existing given file path
     *
     * @param filePath The file path representing the file
     * @return The file's name
     */
    public static String getFileName(String filePath) {

        Path path = getPath(filePath);
        if (path == null)
            return "";

        return path.toFile().getName();
    }

    /**
     * Verifies if the file correspondent to a given fileName exists
     *
     * @param filePath The filepath
     * @return returns a Path object if the file exists, null otherwise
     */
    private static Path getPath(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Utils.showError("Given File does not exist", FileManager.class);
            return null;
        }
        return path;
    }

    /**
     * Gets the standard directory name for a given Main.Peer
     *
     * @return the Main.Peer's directory name
     */
    public static String getPeerDirectory(int peerID) {
        return BASE_DIRECTORY_NAME + peerID;
    }

    /**
     * Gets the standard directory name for a given file, in a given Main.Peer
     *
     * @return the File's directory name
     */
    public static String getFileDirectory(int peerID, String fileID) {
        return getPeerDirectory(peerID) + "/" + fileID;
    }

    /**
     * Gets the standard directory name for a given chunk, from a given file, in a given Main.Peer
     *
     * @return the Chunk's directory name
     */
    public static Path getChunkFile(int peerID, String fileID, int chunkNum) {
        return Paths.get(getFileDirectory(peerID, fileID) + "/" + chunkNum);
    }

    /**
     * Getter for all the Files stored in a Peer's backup directory
     *
     * @param peerID The peer identifier
     * @return Array containing all the Files stored in the Peer backup directory
     */
    public static File[] getPeerBackups(int peerID) {
        File dir = new File(System.getProperty(APPLICATION_WORKSPACE));
        File[] directoryListing = dir.listFiles();

        if (directoryListing != null) {
            for (File child : directoryListing) {
                if ( child.getName().equals(getPeerDirectory(peerID)) ) {
                    return new File(dir, child.getName()).listFiles();
                }
            }
        }
        return null;
    }
}