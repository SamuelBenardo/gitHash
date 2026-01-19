---

Introduction: \***\*IMPORTANT\*\***

READ this to EFFICIENTLY NAVIGATE the README.md for easily accesible information. The following text is organized by GP section, similarly to the directions. Each section is broken up into two parts, Methods and Testing. Methods explains the way that actual code works, starting with broad methods, and gradually working through each method call within each method. If its any help, you can think of it as depth first search. These methods are found in GitHash.java. Testing explains how I tested the methods in GitHash.java and how that code works. This is found in GitHashTester.java. Methods are **bolded** where their explanation first begins.

---

GP-2.1

_Methods_:

**gitRepoInit()** is the overarching method that inializes the git folder within the root Repository. It is wrapped in a try catch because otherwise I get an error. Something about throwing IOExceptions idk.

gitRepoInit() calls on createDirectoryIfMissing() and createFileIfMissing() with arguments containing the pathnames of the files (the word "files" in this case describes both directories and files) that might need to be created.

gitRepoInit() will output the message "Git Repository Created" after having done so.

**createDirectoryIfMissing()** and **createFileIfMissing()** have nearly the same functionality except one creates directories and the other files

createDirectoryIfMissing() and createFileIfMissing() intializes a File object with the path paramater. Then they check if the file already exists using .exists() of the File class. ONLY If NOT then it creates said file using their respective methods, again of the File class. If a file already exists, they will instead output a message like "git/INDEX Already Exists".

Convienientely, gitRepoInit() initializes from root to leaf, in other words, if a parent directory like git exists, but the INDEX file does not, gitRepoInit() will not create a new git directory, but will create the INDEX file inside of the already existing git directory.

**deleteGitDirectory()** is solely for testing purposes. In order to test multiple times if gitRepoInit() works, the files it creates need to be deleted. As opposed to doing so manually, deleteGitDirectory() deletes all of the files at once.

deleteGitDirectory() works in the opposite direction as gitRepoInit(), it first deletes HEAD, objects, INDEX, then the parent diretory, git. I did this to avoid having to delete recursively. For each and every different file, a file object is initialized with the file's path name, then the file object calls .delete() to delete the file. Interestingly the delete() method checks first if the path is valid before attemtping to delete, meaning if for example INDEX does not exist, deleteGitDirectory() will not break even though .delete() is called on INDEX.

_Testing_:

**gitRepoInitTester()** tests for all of the following cases: no files already exist; the git, parent folder, exists, but none of its contents; the git folder and some but not all of its contents already exist; all files exist.

First, all pre-existing files before the test are deleted. Then each test is carried out, by first individually calling the create methods for each folder, for example GitHash.createDirectoryIfMissing("git"), then running the entire gitRepoInit(), and finally deleting all of the files again for the next test. Tests are seperated by one empty line, hence the System.out.println().

How do I read and understand the Terminal after running the Tester? Because the method createDirectoryIfMissing() outputs the message "\_\_\_ Already Exists" when a File already exists

---

NOTES TO SELF:

it may be useful to change some "checker" methods to booleans instead of voids that output their results for a later steps which can easily be done.

output test being run before running it in Terminal

the way indexTester functions it will change the contents of each file every run so if there is a problem specific to a file be sure to denote it





RK Part 2 Notes:
For Milestone 3.0, I added the ability to back up a directory by creating a tree object that represents the contents of a folder.

What I added:
- A method to take a directory path and recursively process everything inside it.
- For each file the program:
  - hashes the file contents using SHA1 method provided
  - creates a BLOB object in git/objects/ if it doesn’t already exist
  - adds a blob entry into the directory’s tree content
- For each subdirectory the program:
  - recursively creates a tree for that subdirectory
  - adds a tree entry into the parent directory’s tree content
- The final tree content is hashed saved into git/objects/ as a tree object and the method returns the tree’s SHA1 hash.

Milestone 3.1: Recursive tree generation + working list
For Milestone 3.1, I changed the process to build trees using a “working list” approach that starts from the index.

What I added:
- A method to stage a directory so that git/INDEX contains only file (BLOB) entries using relative paths.
- A method to convert the git/INDEX file into a separate working list file where each line begins with blob.
- A method to generate tree objects bottom-up by:
  - reading the working list
  - prioritizing deeper directories first
  - creating tree objects for each subdirectory and then the root
  - appending tree entries into the working list as each directory is processed

How I tested
I used a tester class (GitHashMilestone31Tester) that creates a small sample folder with a subfolder and two files runs the 3.1 steps and checks that:
- the index and working list are created and updated correctly
- blob objects are created for the files
- tree objects are created for the directories
- the generated tree content matches what the directory structure should produce
