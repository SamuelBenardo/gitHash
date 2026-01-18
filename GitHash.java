import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GitHash {

    public static void gitRepoInit() throws IOException {
        boolean createdSomething = false;
        if (createDirectoryIfMissing("git")) {
            createdSomething = true;
        }
        if (createDirectoryIfMissing("git/objects")) {
            createdSomething = true;
        }
        if (createFileIfMissing("git/HEAD")) {
            createdSomething = true;
        }
        if (createFileIfMissing("git/INDEX")) {
            createdSomething = true;
        }
        
        if (createdSomething) {
            System.out.println("Git Repository Created");
        }
        else {
            System.out.println("Git Repository Already Exists");
        }
    }

    public static boolean createDirectoryIfMissing(String path) throws IOException {
        File directoryCreator = new File(path);
        if (!directoryCreator.exists()) {
            return directoryCreator.mkdir();
        } 
        return false;
    }

    public static boolean createFileIfMissing(String path) throws IOException {
        File fileCreator = new File(path);
        if (!fileCreator.exists()) {
            return fileCreator.createNewFile();
        }
        return false;
    }

    public static void deleteGitDirectory() throws IOException {
        removeRecursively("git", ".");  // deletes ./git and everything inside it

        File gitChecker = new File("git");
        if (!gitChecker.exists()) {
            System.out.println("Git Repository Deleted");
        } 
        else {
            System.out.println("Git Repository Not Deleted");
        }
    }

    public static String generateSHA1Hash(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createBLOBAndAddToObjects(File file) throws IOException {
        String hash = generateSHA1Hash(file);
        File blob = new File("git/objects/" + hash);

        if (blob.exists()) {
            return;
        }

        blob.createNewFile();
        FileOutputStream blobWriter = new FileOutputStream(blob);
        BufferedReader fileCopier = new BufferedReader(new FileReader(file));
        while (fileCopier.ready()) {
            blobWriter.write(fileCopier.read());
        }
        fileCopier.close();
        blobWriter.close();
    }

    public static void deleteBLOBFromObjects(File file) throws IOException {
        String hash = generateSHA1Hash(file);
        File blobDeleter = new File("git/objects/" + hash);
        blobDeleter.delete();
    }

    public static void blobExists(File file) throws IOException {
        String hash = generateSHA1Hash(file);
        File blobChecker = new File("git/objects/" + hash);
        if (blobChecker.exists()) {
            System.out.println("BLOB " + hash + " of File " + file.getName() + " is in objects directory");
        } else {
            System.out.println(
                    "BLOB " + hash + " of File " + file.getName() + " does not exist or is not in objects directory");
        }
    }

    // newline between entries only
    public static void addBLOBEntryToIndex(File file) throws IOException {
        File indexFile = new File("git/INDEX");
        boolean isEmpty = indexFile.length() == 0;
        BufferedWriter entryWriter = new BufferedWriter(new FileWriter(indexFile, true));
        if (!isEmpty) {
            entryWriter.write("\n");
        }

        entryWriter.write(generateSHA1Hash(file) + " " + file.getName());
        entryWriter.close();
    }
    
    public static void cleanObjectsAndINDEX() throws IOException {
        FileWriter overwriter = new FileWriter("git/INDEX");
        overwriter.write("");
        overwriter.close();
        removeRecursively("objects", "git");
        createDirectoryIfMissing("git/objects");
    }

    public static void removeRecursively(String pathName, String parentPathName) throws IOException {
        File file = new File(parentPathName + "/" + pathName);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            file.delete();
        } else {
            for (String childPathName : file.list()) {
                removeRecursively(childPathName, parentPathName + "/" + pathName);
            }
        }
        file.delete();
        return;
    }

}
