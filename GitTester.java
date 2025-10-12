import java.io.IOException;

public class GitTester {

    public static void main(String args[]) throws IOException {
        
        GitHash.deleteGitDirectory();
        
        GitWrapper gw = new GitWrapper();
        gw.init();
        
        gw.add("myProgram/hello.txt");
        gw.add("myProgram/inner/world.txt");
        
        String commitHash = gw.commit("John Doe", "Initial commit");
        System.out.println("Commit created with hash: " + commitHash);
        
        gw.add("myProgram/hello.txt");
        System.out.println("Duplicate add attempt - should not add again");
        
        System.out.println("Git functionality test completed");
    }
    
}