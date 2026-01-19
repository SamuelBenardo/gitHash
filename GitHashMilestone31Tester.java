import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GitHashMilestone31Tester {

    public static void main(String[] args) throws IOException {
        System.out.println("-------Milestone 3.1 Tester-------");

        GitHash.deleteGitDirectory();
        GitHash.gitRepoInit();

        // Build a folder to test:
        // m31root/
        //   A.txt
        //   sub/
        //      B.txt
        String root = "m31root";
        String subDir = root + "/sub";

        deleteFolder(new File(root));
        new File(root).mkdir();
        new File(subDir).mkdir();

        File a = new File(root + "/A.txt");
        File b = new File(subDir + "/B.txt");

        writeFile(a, "AAA");
        writeFile(b, "BBB");

        GitHash.cleanObjectsAndINDEX();

        System.out.println("\n---3.1A: Stage directory to INDEX (blobs only)---");
        GitHash.stageDirectoryToIndexBlobsOnly(root);

        File indexFile = new File("git/INDEX");
        printCheck("INDEX exists", indexFile.exists());
        printCheck("INDEX not empty", indexFile.length() > 0);
        printCheck("INDEX contains NO tree lines", !fileContainsLineStartingWith(indexFile, "tree "));

        String indexText = readWholeFile(indexFile);
        String aHash = GitHash.generateSHA1Hash(a);
        String bHash = GitHash.generateSHA1Hash(b);

        printCheck("INDEX contains A.txt as <hash> <path>", containsExactLine(indexText, aHash + " " + root + "/A.txt"));
        printCheck("INDEX contains B.txt as <hash> <path>", containsExactLine(indexText, bHash + " " + root + "/sub/B.txt"));

        System.out.println("\n---3.1B: Write working list from INDEX---");
        String workingListPath = "git/WORKING_LIST.txt";
        GitHash.writeWorkingListFromIndex(workingListPath);

        File workingFile = new File(workingListPath);
        printCheck("WORKING_LIST exists", workingFile.exists());
        printCheck("WORKING_LIST not empty", workingFile.length() > 0);

        String workingBefore = readWholeFile(workingFile);
        printCheck("WORKING_LIST contains blob A line", containsExactLine(workingBefore, "blob " + aHash + " " + root + "/A.txt"));
        printCheck("WORKING_LIST contains blob B line", containsExactLine(workingBefore, "blob " + bHash + " " + root + "/sub/B.txt"));

        System.out.println("\n---3.1C: Generate trees from working list---");
        String rootTreeHash = GitHash.generateTreesFromWorkingList(workingListPath, root);
        System.out.println("Root tree hash: " + rootTreeHash);

        printCheck("Root tree hash length is 40", rootTreeHash != null && rootTreeHash.length() == 40);

        File rootTreeObj = new File("git/objects/" + rootTreeHash);
        printCheck("Root tree object exists in git/objects", rootTreeObj.exists());

        String workingAfter = readWholeFile(workingFile);
        printCheck("WORKING_LIST now contains at least one tree line", fileContainsLineStartingWith(workingFile, "tree "));

        String subTreeHash = findTreeHashForPath(workingAfter, root + "/sub");
        printCheck("WORKING_LIST contains tree entry for " + root + "/sub", subTreeHash != null);

        if (subTreeHash != null) {
            File subTreeObj = new File("git/objects/" + subTreeHash);
            printCheck("Sub tree object exists in git/objects", subTreeObj.exists());

            String subTreeText = readWholeFile(subTreeObj);
            printCheck("Sub tree object contains 'blob <hash> B.txt' (name only)", containsExactLine(subTreeText, "blob " + bHash + " B.txt"));

            printCheck("Sub tree object does NOT end with newline", !fileEndsWithNewline(subTreeObj));
        }

        String rootTreeText = readWholeFile(rootTreeObj);
        printCheck("Root tree object contains 'blob <hash> A.txt' (name only)", containsExactLine(rootTreeText, "blob " + aHash + " A.txt"));

        if (subTreeHash != null) {
            printCheck("Root tree object contains 'tree <hash> sub' (name only)", containsExactLine(rootTreeText, "tree " + subTreeHash + " sub"));
        }

        printCheck("INDEX does NOT end with newline", !fileEndsWithNewline(indexFile));
        printCheck("WORKING_LIST does NOT end with newline", !fileEndsWithNewline(workingFile));
        printCheck("Root tree object does NOT end with newline", !fileEndsWithNewline(rootTreeObj));

        printCheck("A blob object exists", new File("git/objects/" + aHash).exists());
        printCheck("B blob object exists", new File("git/objects/" + bHash).exists());

        deleteFolder(new File(root));

        System.out.println("\n------Done------");
    }

    private static void writeFile(File f, String text) throws IOException {
        FileWriter fw = new FileWriter(f, false);
        fw.write(text);
        fw.close();
    }

    private static String readWholeFile(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String out = "";
        String line;
        boolean first = true;

        while ((line = br.readLine()) != null) {
            if (!first) out += "\n";
            out += line;
            first = false;
        }
        br.close();
        return out;
    }

    private static boolean containsExactLine(String text, String target) {
        String current = "";
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                if (current.equals(target)) {
                    return true;
                }
                current = "";
            }
            else {
                current += c;
            }
        }
        return current.equals(target);
    }

    private static boolean fileContainsLineStartingWith(File f, String prefix) throws IOException {
        if (!f.exists() || f.length() == 0){
            return false;
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        boolean found = false;

        while ((line = br.readLine()) != null) {
            if (line.startsWith(prefix)) {
                found = true;
            }
        }
        br.close();
        return found;
    }

    private static String findTreeHashForPath(String workingText, String targetPath) {
        String current = "";
        for (int i = 0; i < workingText.length(); i++) {
            char c = workingText.charAt(i);
            if (c == '\n') {
                String h = treeHashFromLine(current, targetPath);
                if (h != null) return h;
                current = "";
            }
            else {
                current += c;
            }
        }
        return treeHashFromLine(current, targetPath);
    }

    private static String treeHashFromLine(String line, String targetPath) {
        if (!line.startsWith("tree ")) {
            return null;
        }

        int firstSpace = -1;
        for (int i = 5; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
                firstSpace = i;
                i = line.length();
            }
        }

        if (firstSpace == -1) {
            return null;
        }

        String hash = line.substring(5, firstSpace);
        String path = line.substring(firstSpace + 1);

        if (path.equals(targetPath)) {
            return hash;
        }
        return null;
    }

    private static boolean fileEndsWithNewline(File f) throws IOException {
        if (!f.exists() || f.length() == 0) {
            return false;
        }

        FileReader fr = new FileReader(f);
        int last = -1;
        int ch;

        while ((ch = fr.read()) != -1) {
            last = ch;
        }
        fr.close();
        return last == '\n';
    }

    private static void deleteFolder(File f) {
        if (f == null || !f.exists()) {
            return;
        }

        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    deleteFolder(children[i]);
                }
            }
        }
        f.delete();
    }

    private static void printCheck(String msg, boolean ok) {
        if (ok) {
            System.out.println("[PASS] " + msg);
        }
        else {
            System.out.println("[FAIL] " + msg);
        }
    }
}