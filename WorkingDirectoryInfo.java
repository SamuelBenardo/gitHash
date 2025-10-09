import java.util.ArrayList;

public class WorkingDirectoryInfo implements Comparable<WorkingDirectoryInfo> {
    private String type;
    private String hash;
    private String stringPath;
    private String fileName;
    private int size;

    //creates a working directory info given the type, hash and string in path format
    public WorkingDirectoryInfo(String type, String hash, String path) {
        this.type = type;
        this.hash = hash;

        int fileIndex = path.lastIndexOf("/");
        fileName = path.substring(fileIndex + 1);
        stringPath = path.substring(0, fileIndex);

        size = findDepth(stringPath);
    }

    // finds how many folders the given path is nested in
    public static int findDepth(String path) {
        if (path.lastIndexOf("/") == -1) {
            return -1;
        }
        return seperateDirPaths(path).size();
    }

    // splits a relative path string by the / and returns an array w/all info seperated
    public static ArrayList<String> seperateDirPaths(String path) {
        ArrayList<String> paths = new ArrayList<>();
        int dirStart = 0;
        int nextDir = path.indexOf("/");
        while (nextDir + 1 != dirStart) {
            paths.add(path.substring(dirStart, nextDir));
            dirStart = nextDir + 1;
            nextDir = dirStart + path.substring(dirStart).indexOf("/");
        }
        paths.add(path.substring(dirStart));
        return paths;
    }

    public String getType() {
        return type;
    }

    public String getHash() {
        return hash;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStringPath() {
        return stringPath;
    }

    // returns how imbedded the file is
    public int size() {
        return size;
    }

    // Compares WDI by size, and if the size matches returns the one with the alphabetically first path
    @Override
    public int compareTo(WorkingDirectoryInfo other) {
        int size = this.size() - other.size();
        if (size == 0) {
            return this.getStringPath().compareTo(other.getStringPath());
        }
        return size;
    }

    // checks if the directories are the same amount of imbedded
    public boolean isSameDepth(WorkingDirectoryInfo other) {
        return this.size() == other.size();
    }

    // checks if the directories are in the same folder
    public boolean isInSameFolder(WorkingDirectoryInfo other) {
        return this.getStringPath().equals(other.getStringPath());
    }
}
