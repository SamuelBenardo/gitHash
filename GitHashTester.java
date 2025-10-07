import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GitHashTester {

    public static void main(String[] args) throws IOException {

        GitHash.cleanObjectsAndINDEX();
        GitHash.add("goofy_ahh/wow.txt");
        GitHash.add("goofy_ahh/BEANS/dawg.txt");
        GitHash.add("goofy_ahh/goofy_nahhh/snog/dog.txt");
        GitHash.stageFolders();

        // PriorityQueue<WorkingDirectoryInfo> q = GitHash.convertIndexToWorkingDirectory();
        // System.out.println(q.poll().size());
        // System.out.println(q.poll().size());
        // System.out.println(q.poll().size());

        // GitHash.writeDirContents(new File("./goofy_ahh"));
        // System.out.println();
        // GitHash.gitRepoInit();
        // System.out.println();
        // testGenerateSHA1Hash();
        // System.out.println();
        // indexMethodsTester(5, 20);


    }

    public static void gitRepoInitTester() throws IOException {
        GitHash.deleteGitDirectory();
        areAllFilesDeleted();
        System.out.println();

        GitHash.gitRepoInit();
        areAllFilesInitialized();
        GitHash.deleteGitDirectory();
        areAllFilesDeleted();
        System.out.println();

        GitHash.createDirectoryIfMissing("git");
        GitHash.gitRepoInit();
        areAllFilesInitialized();
        GitHash.deleteGitDirectory();
        areAllFilesDeleted();
        System.out.println();

        GitHash.createDirectoryIfMissing("git");
        GitHash.createDirectoryIfMissing("git/objects");
        GitHash.gitRepoInit();
        areAllFilesInitialized();
        GitHash.deleteGitDirectory();
        areAllFilesDeleted();
        System.out.println();

        GitHash.gitRepoInit();
        GitHash.gitRepoInit();
        areAllFilesInitialized();
        GitHash.deleteGitDirectory();
        areAllFilesDeleted();
    }

    public static void areAllFilesInitialized() {
        File gitFile = new File("git");
        File objectsFile = new File("git/objects");
        File headFile = new File("git/HEAD");
        File indexFile = new File("git/INDEX");
        if (gitFile.exists() && objectsFile.exists() && headFile.exists() && indexFile.exists()) {
            System.out.println("All Files Are Initialized");
        } else {
            System.out.println("Some Files Missing");
        }
    }

    public static void areAllFilesDeleted() {
        File gitFile = new File("git");
        File objectsFile = new File("git/objects");
        File headFile = new File("git/HEAD");
        File indexFile = new File("git/INDEX");
        if (!(gitFile.exists() || objectsFile.exists() || headFile.exists() || indexFile.exists())) {
            System.out.println("All Files Are Deleted");
        } else {
            System.out.println("Some Files Still Exist");
        }
    }

    public static void testGenerateSHA1Hash() throws IOException {
        File testFileHash = new File("testFileHash.txt");
        testFileHash.createNewFile();
        FileWriter testFileHashWriter = new FileWriter(testFileHash);
        testFileHashWriter.write("I believe in you. Keep coding!");
        testFileHashWriter.close();
        System.out.println(
                "From SHA1 website should output:\n657ea366b820a51f36ff13e8236f574e7cfb5e81\nMethod outputs:\n"
                        + GitHash.generateSHA1Hash(testFileHash));
    }

    public static void doBLOBMethodsWork() throws IOException {
        File testFileBLOB = new File("testFileHash.txt");
        testFileBLOB.createNewFile();
        FileWriter testFileBLOBWriter = new FileWriter(testFileBLOB);
        testFileBLOBWriter.write("Gott hilft denen, die sich selbst helfen.");
        testFileBLOBWriter.close();
        GitHash.blobExists(testFileBLOB);
        GitHash.createBLOBAndAddToObjects(testFileBLOB);
        GitHash.blobExists(testFileBLOB);
        // GitHash.deleteBLOBFromObjects(testFileBLOB);
        // GitHash.blobExists(testFileBLOB);
    }

    public static void indexMethodsTester(int numFiles, int fileLength) throws IOException {
        for (int i = 0; i < numFiles; i++) {
            File newTestFile = new File("testFile" + i + ".txt");
            newTestFile.createNewFile();
            FileWriter testFileWriter = new FileWriter(newTestFile);
            String fileContents = "";
            for (int j = 0; j < fileLength; j++) {
                fileContents += (char) (Math.random() * 94 + 33);
            }
            testFileWriter.write(fileContents);
            testFileWriter.close();
            GitHash.createBLOBAndAddToObjects(newTestFile);
            GitHash.addBLOBEntryToIndex(newTestFile.getName());
            doIndexEntriesMatchActualFiles(newTestFile);
        }
    }

    public static void doIndexEntriesMatchActualFiles(File newTestFile) throws IOException {

    }
}
