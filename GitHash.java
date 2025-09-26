import java.io.File;
import java.io.IOException;

public class GitHash {

    public static void gitRepoInit() throws IOException {
        GitHash.createDirectoryIfMissing("git");
        GitHash.createDirectoryIfMissing("git/objects");
        GitHash.createFileIfMissing("git/HEAD");
        GitHash.createFileIfMissing("git/INDEX");
        System.out.println("Git Repository Created");
    }

    public static void createDirectoryIfMissing(String path) throws IOException {
        File directoryCreator = new File(path);
        if (!directoryCreator.exists()) {
            directoryCreator.mkdir();
        } else {
            System.out.println(path + " Already Exists");
        }
    }

    public static void createFileIfMissing(String path) throws IOException {
        File fileCreator = new File(path);
        if (!fileCreator.exists()) {
            fileCreator.createNewFile();
        } else {
            System.out.println(path + " Already Exists");
        }
    }

    public static void deleteGitDirectory() throws IOException {
        File indexDeleter = new File("git/INDEX");
        indexDeleter.delete();
        File headDeleter = new File("git/HEAD");
        headDeleter.delete();
        File objectsDeleter = new File("git/objects");
        objectsDeleter.delete();
        File gitDeleter = new File("git");
        gitDeleter.delete();
        System.out.println("Git Repository Deleted");
    }

    public static String generateSHA1Hash(File file) {
        String hex = DigestUtils.sha1Hex(Files.readString(file.toPath()));
        System.out.println(hex);
        return hex;
    }

}
