Logiciel Martus™ readme_verify_fr.txt
-------------------------------------

Benetech améliore constamment l’application Martus. Allez sur http://www.martus.org pour récupérer la dernière version et suivez les instructions sur le site web pour valider le logiciel téléchargé à l’aide du programme  SHA-1 sum .

A l’avenir, Benetech choisira peut-être de distribuer une version mise à jour du fichier martus.jar. Ne faites confiance à aucun fichier martus.jar qui vous serait livré séparément, tant que vous n’avez pas vérifié son authenticité à l’aide de la procédure ci-après. Ne faites confiance à aucune autre procédure de vérification qui vous aurait été fournie par d’autres moyens. 

N’utilisez que le programme de vérification fourni sur un CD Martus authentique. Les instructions ci-dessous assument que le programme de vérification se situe dans le répertoire "Verify" (vérifier) d’un CD Martus. Si vous avez placé le programme de vérification sur une disquette ou sur le disque dur, il vous faudra modifier ces instructions en conséquence.


* Sous Windows :

Ouvrez une fenêtre d’invite MS-DOS et saisissez les trois lignes suivantes, en remplaçant la lettre "d" par celle de votre propre CD-ROM ainsi que le chemin d’accès au fichier .jar que vous vérifiez :

d:
cd\verify
ven d:\chemin-fichier\martus.jar

OU BIEN, à partir du menu Démarrer, choisissez Exécuter, puis saisissez :

d:\verify\ven  d:\chemin-fichier\martus.jar  

(d étant la lettre correspondant à votre CD-ROM et chemin-fichier étant le chemin menant au fichier .jar que vous vérifiez).	

Si la ligne "Martus JAR verified" (Martus JAR vérifié) s’affiche à l’écran, le fichier .jar est légitime.


* Sous Mac OS :

Placez le nouveau fichier .jar dans le dossier Martus existant sur votre disque dur, puis exécutez le programme de vérification : 

Ouvrez la fenêtre d’application du Terminal, puis saisissez les commandes suivantes :

cd  /Volumes
ls
cd  Martus (puis tapez la touche Tab pour remplir automatiquement le nom du dossier)
cd  verify
java  -cp  .  JarVerifier  /Library/Java/Martus/martus.jar

Si la ligne "Martus JAR verified" (Martus JAR vérifié) s’affiche à l’écran, le fichier .jar est légitime.


* Sous Linux :

Placez le nouveau fichier .jar dans le répertoire ~/.Martus/ existant, puis exécutez le programme de vérification. Le code suivant peut être collé dans un bash ou sh shell (bien qu’il vous faille peut-être vous trouver à la racine ("root") pour exécuter la commande mount):

  mount=/mnt/cdrom
  [ -d $mount ] || mount=/cdrom		# for Debian and etc
  [ -d $mount/[vV]erify ] || mount -r $mount	
  cd $mount/[vV]erify && java JarVerifier ~/.Martus/martus.jar

Si la ligne "Martus JAR verified" (Martus JAR vérifié) s’affiche à l’écran, le fichier .jar est légitime.  

Si vous lisez "bash: java: No such file or directory" (bash: java: Fichier ou Répertoire inexistant) vous devrez alors ajouter le répertoire 'bin' de java à votre CHEMIN D’ACCES (PATH). Voir étape 1 de la procédure d’installation Linux.   

Si vous lisez "Error loading class JarVerifier : Bad major version number" (Erreur de chargement class JarVerifier : Mauvais numéro de version principale) vous devrez alors installer une version plus récente de java, et/ou placer la dernière version de java plus en amont de votre chemin (PATH). Voir étape 1 de la procédure d’installation Linux.
