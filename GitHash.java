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
import java.util.ArrayList;
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



    // 3.0
    public static String addDirectory(String directoryPath) throws IOException {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Not a valid directory: " + directoryPath);
        }

        return buildTreeAndStore(dir, directoryPath);
    }


    private static String buildTreeAndStore(File dir, String dirLabel) throws IOException {
        File[] children = dir.listFiles();
        if (children == null) {
            return storeTreeObjectContent("");
        }
    
        java.util.ArrayList<String> treeLines = new java.util.ArrayList<>();
    
        for (File child: children) {
            String name = child.getName();
    
            if (!name.equals("git")) {
                String childPathLabel = dirLabel + "/" + name;
    
                if (child.isDirectory()) {
                    String subTreeHash = buildTreeAndStore(child, childPathLabel);
                    treeLines.add("tree " + subTreeHash + " " + name);
                    addTreeEntryToIndex(subTreeHash, childPathLabel);
                }
                else {
                    createBLOBAndAddToObjects(child);
                    String blobHash = generateSHA1Hash(child);
                    treeLines.add("blob " + blobHash + " " + name);
                    addBlobEntryToIndex(blobHash, childPathLabel);
                }
            }
        }
    
        String treeContent = "";
        for (int i = 0; i < treeLines.size(); i++) {
            treeContent += treeLines.get(i);
            if (i < treeLines.size() - 1) {
                treeContent += "\n";
            }
        }

        return storeTreeObjectContent(treeContent);
    }


    private static String storeTreeObjectContent(String treeContent) throws IOException {
        File tempTreeFile = new File("git/TEMP_TREE.txt");
        if (!tempTreeFile.exists()) {
            tempTreeFile.createNewFile();
        }
    
        FileWriter tempWriter = new FileWriter(tempTreeFile, false);
        tempWriter.write(treeContent);
        tempWriter.close();
    
        String treeHash = generateSHA1Hash(tempTreeFile);
    
        File treeObject = new File("git/objects/" + treeHash);
        if (!treeObject.exists()) {
            treeObject.createNewFile();
            FileWriter objWriter = new FileWriter(treeObject, false);
            objWriter.write(treeContent);
            objWriter.close();
        }
    
        tempTreeFile.delete();
    
        return treeHash;
    }


    private static void addBlobEntryToIndex(String hash, String path) throws IOException {
        addTypedEntryToIndex("blob", hash, path);
    }

    private static void addTreeEntryToIndex(String hash, String path) throws IOException {
        addTypedEntryToIndex("tree", hash, path);
    }

    private static void addTypedEntryToIndex(String type, String hash, String path) throws IOException {
        File indexFile = new File("git/INDEX");
        boolean isEmpty = indexFile.length() == 0;
    
        BufferedWriter entryWriter = new BufferedWriter(new FileWriter(indexFile, true));
        if (!isEmpty) {
            entryWriter.write("\n");
        }
        entryWriter.write(type + " " + hash + " " + path);
        entryWriter.close();
    }


    //3.1
    public static void stageDirectoryToIndexBlobsOnly(String directoryPath) throws IOException {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Not a valid directory: " + directoryPath);
        }
    
        stageDirectoryHelper(dir, directoryPath);
    }

    private static void stageDirectoryHelper(File dir, String rootLabel) throws IOException {
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
    
        for (File child: children) {
            String name = child.getName();
    
            if (!name.equals("git")) {
                String childPath = rootLabel + "/" + name;
    
                if (child.isDirectory()) {
                    stageDirectoryHelper(child, childPath);
                }
                else {
                    createBLOBAndAddToObjects(child);
                    String hash = generateSHA1Hash(child);
                    addBlobPathEntryToIndex(hash, childPath);
                }
            }
        }
    }

    private static void addBlobPathEntryToIndex(String hash, String relativePath) throws IOException {
        File indexFile = new File("git/INDEX");
        boolean isEmpty = indexFile.length() == 0;
    
        BufferedWriter writer = new BufferedWriter(new FileWriter(indexFile, true));
        if (!isEmpty) {
            writer.write("\n");
        }
        writer.write(hash + " " + normalizeRelativePath(relativePath));
        writer.close();
    }

    private static String normalizeRelativePath(String path) {
        path = path.trim();
    
        while (path.startsWith("./")) {
            path = path.substring(2);
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }


    //objective B
    public static void writeWorkingListFromIndex(String workingListFilePath) throws IOException {
        File indexFile = new File("git/INDEX");
        File workingFile = new File(workingListFilePath);

        FileWriter clear = new FileWriter(workingFile, false);
        clear.write("");
        clear.close();
    
        if (indexFile.exists() && indexFile.length() > 0) {
            BufferedReader br = new BufferedReader(new FileReader(indexFile));
            String line;
            boolean firstOutLine = true;
    
            while ((line = br.readLine()) != null) {
                line = line.trim();
    
                if (line.length() > 0) {
                    String[] parts = line.split(" ", 2);
    
                    if (parts.length == 2) {
                        String sha1 = parts[0];
                        String path = normalizeRelativePath(parts[1]);
    
                        BufferedWriter bw = new BufferedWriter(new FileWriter(workingFile, true));
                        if (!firstOutLine) {
                            bw.write("\n");
                        }
                        bw.write("blob " + sha1 + " " + path);
                        bw.close();
    
                        firstOutLine = false;
                    }
                }
            }
    
            br.close();
        }
    }


    //objective C
    public static String generateTreesFromWorkingList(String workingListFilePath, String rootDirPath) throws IOException {
        rootDirPath = normalizeRelativePath(rootDirPath);
    
        ArrayList<WLItem> items = readWorkingListItems(workingListFilePath);
    
        ArrayList<String> dirs = new ArrayList<>();
        addUniqueDir(dirs, rootDirPath);
    
        for (int i = 0; i < items.size(); i++) {
            WLItem it = items.get(i);
            if (it.type.equals("blob")) {
                String parent = parentDir(it.path);

                while (parent.length() > 0) {
                    addUniqueDir(dirs, parent);
                    if (parent.equals(rootDirPath)) {
                        parent = "";
                    }
                    else {
                        parent = parentDir(parent);
                    }
                }
            }
        }
    
        sortDirsByDepthDescending(dirs);
    
        ArrayList<String> dirKeys = new ArrayList<>();
        ArrayList<String> dirVals = new ArrayList<>();
    
        for (int d = 0; d < dirs.size(); d++) {
            String dirPath = dirs.get(d);
    
            ArrayList<String> treeLines = new ArrayList<>();
    
            for (int i = 0; i < items.size(); i++) {
                WLItem it = items.get(i);
                if (it.type.equals("blob")) {
                    String p = parentDir(it.path);
                    if (dirPath.equals(p)) {
                        treeLines.add("blob " + it.hash + " " + nameOnly(it.path));
                    }
                }
            }
    
            for (int i = 0; i < dirs.size(); i++) {
                String maybeChild = dirs.get(i);
                if (isDirectChildDir(dirPath, maybeChild)) {
                    String childHash = getMappedHash(dirKeys, dirVals, maybeChild);
                    if (childHash != null) {
                        treeLines.add("tree " + childHash + " " + nameOnly(maybeChild));
                    }
                }
            }
 
            String treeContent = "";
            for (int i = 0; i < treeLines.size(); i++) {
                treeContent += treeLines.get(i);
                if (i < treeLines.size() - 1) {
                    treeContent += "\n";
                }
            }
    
            String treeHash = storeTreeObjectContent(treeContent);
    
            dirKeys.add(dirPath);
            dirVals.add(treeHash);
    
            appendWorkingListEntry(workingListFilePath, "tree " + treeHash + " " + dirPath);
    
 
            items.add(new WLItem("tree", treeHash, dirPath));
        }
    
        return getMappedHash(dirKeys, dirVals, rootDirPath);
    }


    private static class WLItem {
        String type;
        String hash;
        String path;
    
        WLItem(String type, String hash, String path) {
            this.type = type;
            this.hash = hash;
            this.path = path;
        }
    }

    private static ArrayList<WLItem> readWorkingListItems(String workingListFilePath) throws IOException {
        ArrayList<WLItem> items = new ArrayList<>();
        File f = new File(workingListFilePath);
    
        if (f.exists() && f.length() > 0) {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
    
            while ((line = br.readLine()) != null) {
                line = line.trim();
    
                if (line.length() > 0) {
                    String[] parts = line.split(" ", 3);
                    if (parts.length == 3) {
                        items.add(new WLItem(parts[0], parts[1], normalizeRelativePath(parts[2])));
                    }
                }
            }
    
            br.close();
        }
        return items;
    }

    private static void addUniqueDir(ArrayList<String> dirs, String dirPath) {
        boolean exists = false;
        for (int i = 0; i < dirs.size(); i++) {
            if (dirs.get(i).equals(dirPath)) {
                exists = true;
            }
        }
        if (!exists) {
            dirs.add(dirPath);
        }
    }

    private static void sortDirsByDepthDescending(ArrayList<String> dirs) {
        for (int i = 0; i < dirs.size(); i++) {
            int best = i;
            for (int j = i + 1; j < dirs.size(); j++) {
                if (depth(dirs.get(j)) > depth(dirs.get(best))) {
                    best = j;
                }
            }
            String temp = dirs.get(i);
            dirs.set(i, dirs.get(best));
            dirs.set(best, temp);
        }
    }

    private static void appendWorkingListEntry(String workingListFilePath, String entry) throws IOException {
        File f = new File(workingListFilePath);
        boolean isEmpty = (!f.exists() || f.length() == 0);
    
        BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
        if (!isEmpty) {
            bw.write("\n");
        }
        bw.write(entry);
        bw.close();
    }

    private static String getMappedHash(ArrayList<String> keys, ArrayList<String> vals, String key) {
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).equals(key)) {
                return vals.get(i);
            }
        }
        return null;
    }

    private static String parentDir(String path) {
        int lastSlash = -1;
    
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                lastSlash = i;
            }
        }
    
        if (lastSlash == -1) {
            return "";
        }
        return path.substring(0, lastSlash);
    }

    private static String nameOnly(String path) {
        int lastSlash = -1;
    
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                lastSlash = i;
            }
        }
    
        if (lastSlash == -1) {
            return path;
        }
        return path.substring(lastSlash + 1);
    }

    private static int depth(String dirPath) {
        int count = 0;
        for (int i = 0; i < dirPath.length(); i++) {
            if (dirPath.charAt(i) == '/') {
                count++;
            }
        }
        return count;
    }

    private static boolean isDirectChildDir(String parentDir, String childDir) {
        if (childDir.equals(parentDir)) {
            return false;
        }
    
        String prefix = parentDir + "/";
        if (!childDir.startsWith(prefix)) {
            return false;
        }
    
        String rest = childDir.substring(prefix.length());
    
        int slashPos = -1;
        for (int i = 0; i < rest.length(); i++) {
            if (rest.charAt(i) == '/') {
                slashPos = i;
            }
        }
        return slashPos == -1;
    }

    

}
