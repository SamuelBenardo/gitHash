import java.util.ArrayList;

public class WorkingDirectoryInfo implements Comparable<WorkingDirectoryInfo> {
    private String fileName;
    private String type;
    private String hash;
    private ArrayList<String> paths;

    //creates a working directory info given the type, hash and string in path format
    public WorkingDirectoryInfo(String type, String hash, String path) {
        this.type = type;
        this.hash = hash;
        paths = seperateDirPaths(path);

        fileName = getNestedPath(size() - 1);
        paths.remove(size() - 1);
    }

    // same thing but w arraylist of dir paths
    public WorkingDirectoryInfo(String type, String hash, ArrayList<String> dirpath) {
        this.type = type;
        this.hash = hash;
        this.fileName = dirpath.remove(dirpath.size() - 1);
        this.paths = dirpath;
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

    public ArrayList<String> getPaths() {
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

    // returns how imbedded the file is
    public int size() {
        return paths.size();
    }

    // gets the directory at that imbedded level
    public String getNestedPath(int index) {
        return paths.get(index);
    }

    // gets the nearest relative path, aka the one that its touching
    public String getClosestRelativePath() {
        if (size() == 0) {
            return ".";
        } 
        return getNestedPath(size() - 1);  
    }

    // Compares WDI by size, and if the size matches returns the one with the alphabetically first path
    @Override
    public int compareTo(WorkingDirectoryInfo other) {
        int size = this.size() - other.size();
        if (size == 0) {
            return this.getClosestRelativePath().compareTo(other.getClosestRelativePath());
        }
        return size;
    }

    // checks if the directories are the same amount of imbedded
    public boolean isSameDepth(WorkingDirectoryInfo other) {
        return this.size() == other.size();
    }

    // checks if the directories are in the same folder
    public boolean isInSameFolder(WorkingDirectoryInfo other) {
        return getClosestRelativePath().equals(other.getClosestRelativePath());
    }
}
