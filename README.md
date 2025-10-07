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

GP-3: Trees / staging folders

**WorkingDirectoryInfo Class:** - (will use WDI for short)

This class stores the file information in a more readable and organized way. These instance variables include:

- fileName: name of the file
- type: tree/blob
- hash: duh
- paths: an array list of all directory paths in order of surface folder to the most nested.

I used two different constructors for convience sake. The only diffence is that one takes a string of paths and splits them up inside the constructor, and the other takes the already split up array. 
    *NOTE* path argument INCLUDES the file/folder name, and is then removed in the constructor

**seperateDirPaths()**
Given a relative Path in String format for the file, seperates the directories into a list using "/" to determine the next folder. includes fileName.

**size()**
Returns the imbedded depth of the blob/tree (like how many folders its in)

**getNestedPath()**
given the level, returns the directory that corresponds to that specific depth within the given WDI

**getClosestRelativePath()**
returns the immediate parent folder

**compareTo()**
returns >0 if the depth of the WDI file is greater (aka more imbedded) and <0 if it is less imbedded. If the depth is the same, instead will return a comparison based on the names of the immediate parent folder via a standard alphabetical compareTo method.

**isSameDepth()**
checks if the WDIs are at the same depth

**isInSameFolder()**
checks if the WDIs are in the same immediate parent folder



!!!! __ADDED GITHASH FUNCTIONALITY__

**stageFolders()**
goes through index and creates tree objects for all folders saved in index

**convertIndexToWorkingDirectory()**
I meant working list here but I'm too lazy to change it now. Creates a priority queue of file paths in WDI format ordered from MOST IMBEDDED to LEAST IMBEDDED (i.e., order of most folders needed to click through before getting to the file). This should also account for sorting them as to be next to other files on the same level but not next to eachother in priority queue (now that I think about it, it might break if you have smth like ./this/docs/cat.txt & ./that/docs/dog.txt bc it only checks one layer... whoops ykw if i wake up early enough I'll try n fix that) returns the priority queue.
    - honestly I wrote this code pretty late but If I were you and this is not fixed I would have WDI store just the path name and then convert to a list when u actually needa grab info to prevent any errors

**convertNestedDirs()**
Using a priority queue of sorted based on file depths, adds folders in the same path to an arraylist and then converts all those folders into a tree object. repeats until Priority Queue is used up and all folders have been stored.

**convertContentsToString()**
takes tree info (generally longer form info that has different lines / multiple data poibts) and turns into a string

**stageWHOLEfolder()**
stages ALL CONTENTS inside a folder NOT RELATED TO INDEX as trees in objects folder. I kinda made this function before i knew what was going on but i was too sad to delete it. you can ignore.

**writeTreeFile()**
creates a filename based on hashed contents and then writes those contents into the a file in the objects folder.


---

(Sam's) NOTES TO SELF:

it may be useful to change some "checker" methods to booleans instead of voids that output their results for a later steps which can easily be done.

output test being run before running it in Terminal

the way indexTester functions it will change the contents of each file every run so if there is a problem specific to a file be sure to denote it


(CJ's) NOTES:

Moved orders of functions around and added some labels

addBLOBToIndex && SHA1-hasher both take Strings as inputs now

ignore stageWHOLEfolder, bad bad code 

lowk it doesn't recognize if there in diff paths but the nearest directory has the same name. weird stuffs gonna happen man idk u gotta fix that sorry