import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;


public class GitHash {

    // Create directories
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




    // SHA 1 && BLOB && INDEX

    public static void add(String filePath) throws IOException {
        File f = new File(filePath);
        createBLOBAndAddToObjects(f);
        addBLOBEntryToIndex(filePath);
    }

    public static String generateSHA1FromString(String contents) throws IOException {
        File temp = new File("temp");
        if (temp.exists()) {
            temp.delete();
        }

        temp.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(temp, true));
        bw.write(contents);
        bw.close();

        return generateSHA1Hash(temp);
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
        blob.createNewFile();
        FileOutputStream blobWriter = new FileOutputStream(blob);
        BufferedReader fileCopier = new BufferedReader(new FileReader(file));
        while (fileCopier.ready()) {
            blobWriter.write(fileCopier.read());
        }
        fileCopier.close();
        blobWriter.close();
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

    public static void addBLOBEntryToIndex(String filePath) throws IOException {
        BufferedWriter entryWriter = new BufferedWriter(new FileWriter("git/INDEX", true));
        entryWriter.write(generateSHA1Hash(new File(filePath)) + " ./" + filePath + "\n");
        entryWriter.close();
    }
    





    // REMOVERS

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

    public static void deleteBLOBFromObjects(File file) throws IOException {
        String hash = generateSHA1Hash(file);
        File blobDeleter = new File("git/objects/" + hash);
        blobDeleter.delete();
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



// TREES

    // saves all folders in index as tree objects
    public static void stageFolders() throws IOException {
        PriorityQueue<WorkingDirectoryInfo> wd = convertIndexToWorkingDirectory();
        convertNestedDirs(wd);
    }

    // converts index to a PriorityQueue sorting from most to least imbedded in dirs. Info stored in Working Directory Function
    public static PriorityQueue<WorkingDirectoryInfo> convertIndexToWorkingDirectory() throws IOException {
        PriorityQueue<WorkingDirectoryInfo> wd = new PriorityQueue<>(Comparator.reverseOrder());
        BufferedReader br = new BufferedReader(new FileReader("./git/INDEX"));
        while (br.ready()) {
            String info = br.readLine();
            wd.add(new WorkingDirectoryInfo("blob", info.substring(0, info.indexOf(" ")), info.substring(info.indexOf(" ") + 1)));
        }
        br.close();

        return wd;
    }   

    // Goes through dirs in priority order (as mentioned above) and converts the contents into object
    public static void convertNestedDirs(PriorityQueue<WorkingDirectoryInfo> wd) throws IOException {
        if (!wd.isEmpty()) {
            ArrayList<WorkingDirectoryInfo> nestedFolders = new ArrayList<>();
            nestedFolders.add(wd.poll());

            WorkingDirectoryInfo currFolder = nestedFolders.get(0);

            while (!wd.isEmpty() && wd.peek().isInSameFolder(currFolder)) {
                nestedFolders.add(wd.poll());
            }

            if (currFolder.size() == -1) {
                writeWorkingListFile(convertContentsToString(nestedFolders));
                return;
            } else {
                String hash = writeTreeFile(convertContentsToString(nestedFolders));
                wd.add(new WorkingDirectoryInfo("tree", hash, currFolder.getStringPath()));
                convertNestedDirs(wd);
            }
        }

    } 

    // converts an arraylist of working directory infos to a string
    public static String convertContentsToString(ArrayList<WorkingDirectoryInfo> treeFiles) {
        StringBuilder sb = new StringBuilder();
        for (WorkingDirectoryInfo file : treeFiles) {
            sb.append(file.getType() + " ");
            sb.append(file.getHash() + " ");
            sb.append(file.getFileName() + "\n");
        }
        return sb.toString().trim();
    }

    // stages all files inside a folder (i did this before i realized how to do the assignment)
    public static String stageWHOLEfolder(File dir) throws IOException {
        if (dir.exists() && dir.isDirectory() ) {

            File[] contents = dir.listFiles();
            String[][] workingList = new String[contents.length][3];


            // stores info in a 2d array
            for (int i = 0; i < contents.length; i++) {
                File f = contents[i];
                if (f.isDirectory() && f.listFiles().length != 0) {
                    workingList[i][0] = "tree";
                    workingList[i][1] = stageWHOLEfolder(f);
                    workingList[i][2] = f.getName();
                } 

                if (!f.isDirectory()) {
                    workingList[i][0] = "blob";
                    workingList[i][1] = generateSHA1Hash(f);
                    workingList[i][2] = f.getName();
                }

            }

            String hash = writeTreeFile(convertContentsToString(workingList));
            return hash;

        } else {
            throw new IOException("Given file is not a directory/cannot be pushed");
        }
    }

    // same as other one but for 2d arrays
    public static String convertContentsToString(String[][] tree) {
        StringBuilder sb = new StringBuilder();
        for (String[] files : tree) {
            if (files[0] != null) {
                sb.append(files[0] + " ");
                sb.append(files[1] + " ");
                sb.append(files[2] + "\n");
            }
        }
        return sb.toString().trim();
    }

    // writes a given string onto a file and names it a hash based on the contents
    public static String writeTreeFile(String contents) throws IOException {
        String hash = generateSHA1FromString(contents);
        File folder = new File("./git/objects/" + hash);
        if (!folder.exists()) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(folder, true));
            bw.write(contents);
            bw.close();
        }

        return hash;

    }

    // writes the final working list to a file named "workingList"
    public static void writeWorkingListFile(String contents) throws IOException {
        File workingListFile = new File("./git/objects/workingList");
        BufferedWriter bw = new BufferedWriter(new FileWriter(workingListFile));
        bw.write(contents);
        bw.close();
    }

    // creates a commit file with author, message, root tree hash, and previous commit
    public static void commit(String author, String message) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("./git/objects/workingList"));
        String rootTreeContent = br.readLine();
        br.close();
        String rootTreeHash = generateSHA1FromString(rootTreeContent);
        String previousCommit = "";
        File headFile = new File("./git/HEAD");
        if (headFile.exists() && headFile.length() > 0) {
            BufferedReader headReader = new BufferedReader(new FileReader(headFile));
            String headContent = headReader.readLine();
            headReader.close();
            if (headContent != null && !headContent.trim().isEmpty()) {
                File previousCommitFile = new File("./git/objects/" + headContent.trim());
                if (previousCommitFile.exists()) {
                    previousCommit = headContent.trim();
                }
            }
        }
        
        // https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html 
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        String currentDate = sdf.format(new Date());
        StringBuilder commitContent = new StringBuilder();
        commitContent.append("tree: ").append(rootTreeHash).append("\n");
        commitContent.append("parent: ").append(previousCommit).append("\n");
        commitContent.append("author: ").append(author).append("\n");
        commitContent.append("date: ").append(currentDate).append("\n");
        commitContent.append("message: ").append(message);
        String commitHash = generateSHA1FromString(commitContent.toString());
        File commitFile = new File("./git/objects/" + commitHash);
        BufferedWriter commitWriter = new BufferedWriter(new FileWriter(commitFile));
        commitWriter.write(commitContent.toString());
        commitWriter.close();
        BufferedWriter headWriter = new BufferedWriter(new FileWriter("./git/HEAD"));
        headWriter.write(commitHash);
        headWriter.close();
    }

}
