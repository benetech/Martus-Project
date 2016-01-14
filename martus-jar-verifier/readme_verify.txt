Martus(tm) Software readme_verify.txt
-------------------------------------

Benetech continues to improve the Martus application. 
Check http://www.martus.org for the latest version, 
and follow the instructions on the web site to validate the 
downloaded software using the SHA-1 sum program.  

In the future, Benetech may choose to distribute an updated 
version of the martus.jar file.  You should not trust the authenticity 
of any martus.jar file that is separately delivered to you, until 
you have verified it using the procedure that follows.  Do not trust 
any alternative verification procedures provided to you by other means.

Use only the verification program found on an authentic Martus CD. 
The instructions below assume the verification program is located 
in the “Verify” directory of a Martus CD.  If you have placed the 
verification program on a floppy disk or hard disk drive, you will need 
to alter these instructions accordingly.

 
* In Windows:

Open an MS-DOS prompt window and type the following three lines, 
substituting your own CD-ROM drive letter for “d” and the path 
to the .jar file you’re verifying:

d:
cd  \verify
ven  d:\path-to-file\martus.jar

OR, from the Start menu, choose Run and then type:

d:\verify\ven  d:\path-to-file\martus.jar  

(where d is the letter assigned to your CD-ROM and path-to-file is the path 
to the .jar file you’re verifying).	

If the line “Martus JAR verified.” appears on your screen, the .jar file is legitimate.


* In Mac OS:

Place the new .jar file in the existing Martus folder on your hard drive, 
and then run the verifier: 

Open the Terminal application window, and then type in the following commands:

cd  /Volumes
ls
cd  Martus (then hit the Tab key to auto-complete the folder name)
cd  verify
java  -cp  .  JarVerifier  /Library/Java/Martus/martus.jar

If the line “Martus JAR verified.” appears on your screen, the .jar file is legitimate.

Note: If you have Java 1.5 or later installed, the JarVerifier may not work correctly, and may incorrectly claim that a jar file is invalid when it is actually valid.  If you have questions, please email help@martus.org.


* In Linux:

Place the new .jar file in the existing ~/.Martus/ directory, and then run 
the verifier. The following code can be pasted into a bash or sh shell (although 
you may need to be signed in as 'root' to run the mount command):

  mount=/mnt/cdrom
  [ -d $mount ] || mount=/cdrom		# for Debian and etc
  [ -d $mount/[vV]erify ] || mount -r $mount
  cd $mount/[vV]erify && java JarVerifier ~/.Martus/martus.jar

If the line “ Martus JAR verified.” appears on your screen, then  the .jar file is legitimate.

If you see “ bash: java: No such file or directory” then you need to add java's 
'bin' directory to your PATH.  See step 1 of the Linux install procedure.

If you see “Error loading class JarVerifier: Bad major version number” then you need 
to install a newer version of java, and/or put the latest version of java earlier 
in your PATH.  See the step 1 of the Linux install procedure.

Note: If you have Java 1.5 or later installed, the JarVerifier may not work correctly, and may incorrectly claim that a jar file is invalid when it is actually valid.  If you have questions, please email help@martus.org.