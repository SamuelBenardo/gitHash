import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GitHashMilestone3Tester {

    public static void main(String[] args) throws IOException {
        System.out.println("-------Milestone 3 Tester-------");

        GitHash.deleteGitDirectory();
        GitHash.gitRepoInit();

        // Make a simple folder structure to test:
        // m3testRoot/
        //   README.md
        //   Hello.txt
        //   scripts/
        //       Notes.txt

        String root = "m3testRoot";
        String scripts = root + "/scripts";

        deleteFolder(new File(root));

        new File(root).mkdir();
        new File(scripts).mkdir();

        File readme = new File(root + "/README.md");
        File hello = new File(root + "/Hello.txt");
        File notes = new File(scripts + "/Notes.txt");

        writeFile(readme, "This is a README.\nSecond line.");
        writeFile(hello, "Hello world!");
        writeFile(notes, "This is a text file inside the scripts folder.");

        System.out.println("\nCalling addDirectory(\"" + root + "\")...");
        String rootTreeHash = GitHash.addDirectory(root);
        System.out.println("Returned tree hash: " + rootTreeHash);

        File rootTreeObject = new File("git/objects/" + rootTreeHash);
        printCheck("Root tree object exists in git/objects", rootTreeObject.exists());

        String readmeHash = GitHash.generateSHA1Hash(readme);
        String helloHash = GitHash.generateSHA1Hash(hello);
        String notesHash = GitHash.generateSHA1Hash(notes);

        printCheck("README blob exists", new File("git/objects/" + readmeHash).exists());
        printCheck("Hello blob exists", new File("git/objects/" + helloHash).exists());
        printCheck("Notes.txt blob exists", new File("git/objects/" + notesHash).exists());

        System.out.println("\nChecking git/INDEX");
        String indexText = readFileAsText(new File("git/INDEX"));

        printCheck("INDEX has README entry", hasLine(indexText, "blob " + readmeHash + " " + root + "/README.md"));

        printCheck("INDEX has Hello entry", hasLine(indexText, "blob " + helloHash + " " + root + "/Hello.txt"));

        printCheck("INDEX has Notes.txt entry", hasLine(indexText, "blob " + notesHash + " " + root + "/scripts/Notes.txt"));

        String scriptsTreeHash = findTreeHashInIndex(indexText, root + "/scripts");
        printCheck("INDEX has scripts tree entry", scriptsTreeHash != null);

        if (scriptsTreeHash != null) {
            File scriptsTreeObject = new File("git/objects/" + scriptsTreeHash);
            printCheck("Scripts tree object exists", scriptsTreeObject.exists());

            String scriptsTreeText = readFileAsText(scriptsTreeObject);
            printCheck("Scripts tree file includes Notes.txt (name only)", hasLine(scriptsTreeText, "blob " + notesHash + " Notes.txt"));

            printCheck("Scripts tree file does NOT end with a newline", !fileEndsWithNewline(scriptsTreeObject));
        }

        System.out.println("\nChecking root tree object content");
        String rootTreeText = readFileAsText(rootTreeObject);

        printCheck("Root tree has README.md line (name only)", hasLine(rootTreeText, "blob " + readmeHash + " README.md"));

        printCheck("Root tree has Hello.txt line (name only)", hasLine(rootTreeText, "blob " + helloHash + " Hello.txt"));

        if (scriptsTreeHash != null) {
            printCheck("Root tree has scripts line (name only)", hasLine(rootTreeText, "tree " + scriptsTreeHash + " scripts"));
        }

        printCheck("INDEX does NOT end with a newline", !fileEndsWithNewline(new File("git/INDEX")));

        printCheck("Root tree object does NOT end with a newline", !fileEndsWithNewline(rootTreeObject));

        deleteFolder(new File(root));

        System.out.println("\n-------Done-------");
    }


    private static void writeFile(File f, String text) throws IOException {
        FileWriter fw = new FileWriter(f, false);
        fw.write(text);
        fw.close();
    }

    private static String readFileAsText(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String text = "";
        String line;
        boolean firstLine = true;

        while ((line = br.readLine()) != null) {
            if (!firstLine) {
                text += "\n";
            }
            text += line;
            firstLine = false;
        }
        br.close();
        return text;
    }

    private static boolean hasLine(String text, String target) {
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

    private static String findTreeHashInIndex(String indexText, String path) {
        String current = "";
        for (int i = 0; i < indexText.length(); i++) {
            char c = indexText.charAt(i);
            if (c == '\n') {
                String hash = getTreeHashFromLine(current, path);
                if (hash != null) {
                    return hash;
                }
                current = "";
            } 
            else {
                current += c;
            }
        }
        return getTreeHashFromLine(current, path);
    }

    private static String getTreeHashFromLine(String line, String path) {
        if (!line.startsWith("tree ")) {
            return null;
        }

        int firstSpaceAfterTree = line.indexOf(' ', 5);
        if (firstSpaceAfterTree == -1) {
            return null;
        }

        String hash = line.substring(5, firstSpaceAfterTree);
        String rest = line.substring(firstSpaceAfterTree + 1);

        if (rest.equals(path)) {
            return hash;
        }
        return null;
    }

    private static boolean fileEndsWithNewline(File file) throws IOException {
        if (!file.exists()) {
            return false;
        }

        FileReader fr = new FileReader(file);
        int lastChar = -1;
        int ch;

        while ((ch = fr.read()) != -1) {
            lastChar = ch;
        }
        fr.close();

        return lastChar == '\n';
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

    private static void printCheck(String message, boolean ok) {
        if (ok) {
            System.out.println("[PASS] " + message);
        }
        else {
            System.out.println("[FAIL] " + message);
        }
    }
}
