Martus(tm) Version Logiciel 5.2.0 README_fr.txt
---------------------------------------------

Table des Mati�res :
A. Modifications de la version 3.2 � la version 3.3     (08-2008)
B. Modifications de la version 3.1 � la version 3.2     (09-2007)
C. Modifications de la version 3.0 � la version 3.1     (04-2007)
D. Modifications de la version 2.9 � la version 3.0     (09-2006)
E. Modifications de la version 2.8.1 � la version 2.9   (03-2006)
F. Modifications de la version 2.8 � la version 2.8.1   (11-2005)
G. Modifications de la version 2.7.2 � la version 2.8   (09-2005)
H. Modifications de la version 2.7 � la version 2.7.2   (08-2005)
I. Modifications de la version 2.6 � la version 2.7     (04-2005)
J. Modifications de la version 2.5 � la version 2.6     (02-2005)
K. Modifications de la version 2.0.1 � la version 2.5   (11-2004)
L. Modifications de la version 2.0. � la version 2.0.1  (08-2004)
M. Modifications de la version 1.5. � la version 2.0    (07-2004)
N. Modifications de la version 1.2.1 � la version 1.5   (02-2004)
O. Modifications de la version 1.2. � la version 1.2.1  (12-2003)
P. Modifications de la version 1.0.3 � la version 1.2   (09-2003)
Q. Modifications de la version 1.0.2 � la version 1.0.3 (05-2003)
R. Modifications de la version 1.0.1 � la version 1.0.2 (02-2003)
S. Modifications de la version 1.0. � la version 1.0.1  (01-2003)
T. Instructions particuli�res de mise � jour de Winsock 
   en cas de probl�me d'ouverture du logiciel Martus sous 
   Windows 95 :

Voir http://www.martus.org pour tous renseignements sur le 
logiciel Martus.

Voir le Guide de l'Utilisateur Martus pour les instructions 
d'installation du logiciel Martus. 

Pour toute traduction mise � jour de la documentation de 
l'utilisateur, ainsi que la documentation des versions 
pr�c�dentes en diverses langues, suivez le lien 
http://www.martus.org/downloads/.  

A. Modifications de la version 3.2 � la version 3.3     

Cette publication est disponible en anglais mais aussi en 
d'autres langues, comme indiqu� � la page t�l�chargement du
logiciel sur le site http://www.martus.org .  
Les autres langues seront disponibles par la suite, comme 
packs de langues, � cette m�me page (http://www.martus.org/downloads).

- Ajout d�une fonction qui permet de verrouiller plusieurs 
  Brouillons simultan�ment. Pour cela, allez dans Editer > 
  Verrouiller le(s)communiqu�(s).
- Nouvelle fonction qui permet d�ajouter un Si�ge � un groupe 
  De communiqu�s ou � un dossier de communiqu�s. Pour cela, allez 
  dans Editer > Mettre � jour l�acc�s du Si�ge. Cette option sera 
  gris�e sauf si au moins un communiqu� est s�lectionn� et un Si�ge 
  est configur�. Martus affichera une barre de progression pendant
  la mise � jour et vous permettra d�annuler l�op�ration si vous 
  le souhaitez. Les brouillons de communiqu�s seront mis � jour 
  pour correspondre aux nouvelles informations du Si�ge. Pour les 
  communiqu�s verrouill�s, Martus g�n�rera automatiquement une 
  nouvelle version verrouill�e de chaque communiqu�.
- Ajout d�une fonction qui permet d�afficher plusieurs types
  d�images en pi�ce jointe dans Martus lors de la pr�visualisation,
  la cr�ation et l��dition de communiqu�s, ainsi que dans l�aper�u 
  des d�tails de communiqu�.
- Plusieurs r�parations mineures de bogues, clarifications et 
  nettoyages Sur les �crans de l'utilisateur.

REMARQUE (option disponible � partir de Martus 3.1, mais pas encore incluse dans le Guide de l�utilisateur) :  
Il existe un m�canisme suppl�mentaire qui permet d�acc�l�rer le chargement et la navigation dans Martus lorsqu'un compte poss�de un
grand nombre de communiqu�s. Vous pouvez ajouter " --folders-unsorted"
� la fin de la commande dans votre raccourci Martus se trouvant sur 
le bureau. Cette option permet de ne pas trier les dossiers lorsque 
vous les chargez au d�part dans Martus (car le tri peut prendre du 
temps avec un grand nombre de communiqu�s). Vous pouvez toujours 
cliquer sur un en-t�te de colonne dans la liste de pr�visualisation 
des communiqu�s pour trier le dossier, si vous le souhaitez, mais 
l'ajout de cette option vous permettra de gagner du temps au 
d�marrage et lors de la saisie de nouveaux dossiers dans Martus.


B. Modifications de la version 3.1 � la version 3.2     

- Nouvelle fonction d'affichage des communiqu�s qui vous permet de :
  1) masquer/afficher des champs dans les communiqu�s (longs champs
  qui prennent beaucoup de place), 2) cr�er des sections dans vos
  communiqu�s que vous pouvez �galement masquer/afficher, 3) afficher
  des grilles dans une vue "�tendue" (et possibilit� d'ajouter des
  rang�es � partir de cette vue), 4) ins�rer des champs les uns
  � la suite des autres sur une rang�e, 5) r�duire la taille par 
  d�faut des grilles pour gagner de la place.
- Ajout d'une fonction qui permet d'exiger que certains champs
  soient renseign�s avant l'enregistrement d'un communiqu�. 
- Ajout de la fonction de cr�ation de listes d�roulantes guid�es
  par les donn�es, o� les valeurs d'un champ d�roulant sont bas�es
  sur des donn�es qui ont �t� saisies ailleurs dans un champ de
  grille du communiqu�.
- Ajout d'une bo�te de dialogue indiquant l'�tat de progression de
  la recherche et la possibilit� d'annuler des recherches.
- Am�lioration des performances de recherche et des
  autres op�rations relatives aux communiqu�s, en particulier
  pour les utilisateurs poss�dant un grand nombre de communiqu�s. 
- Correction de plusieurs probl�mes : 1) les param�tres de
  configuration (p. ex des Si�ges) n'�taient pas enregistr�s pour
  les utilisateurs dont les communiqu�s avaient subi un grand
  nombre de personnalisations, 2) erreurs lors du glisser-d�poser
  de communiqu�s pour leur envoi/extraction �/� partir d'un serveur.
- Am�lioration des messages destin�s aux utilisateurs (p. ex.
  des message d'erreur lors de la personnalisation).
- Plusieurs r�parations mineures de bogues, clarifications 
  et nettoyages sur les �crans de l'utilisateur.


C. Modifications de la version 3.0 � la version 3.1

- Nouvelle fonction qui notifie les Si�ges si des
  communiqu�s de terrain sont � extraire. Pour l'activer dans
  votre compte Martus, allez dans Options > Pr�f�rences, puis
  s�lectionnez "Rechercher automatiquement les nouveaux communiqu�s
  de bureau de terrain". Environ une fois par heure, un message
  appara�t dans la barre d'�tat (coin inf�rieur gauche de l'�cran)
  indiquant que Martus recherche les nouveaux communiqu�s de bureau
  de terrain. Un autre message s'affiche dans la barre d'�tat si des
  communiqu�s de bureau de terrain sont � extraire. A ce stade, vous
  pouvez aller dans le menu Serveur pour charger l'�cran Extraire.  
- Ajout de la fonction de tri des communiqu�s dans l'�cran Extraire
  en cliquant sur les en-t�tes de colonnes. La fonction de tri
  est uniquement croissante.
- Am�lioration des performances et correction des probl�mes de m�moire
  possibles lors de la connexion et la d�connexion d'utilisateurs
  poss�dant un grand nombre de communiqu�s. 
- R�solution des probl�mes introduits dans la version 3.0, � savoir : 
  1) Les champs des fourchettes de dates qui n'apparaissent pas sur
  les rapports sous forme de page
  2) La personnalisation de la partie priv�e qui est perdue lors de
  la fermeture de Martus
  3) Les probl�mes de s�lection de fichiers sur un Mac (p.ex. lors de
  la configuration de si�ges, l'ajout de fichiers en pi�ce jointe
  � des communiqu�s, la restauration d'une cl� et l'importation d'un
  mod�le personnalis�)
  4) L'affichage incoh�rent de la date en Arabe entre les d�tails de
  communiqu� et les d�comptes sommaires 
- Plusieurs r�parations mineures de bogues, clarifications 
  et nettoyages sur les �crans de l'utilisateur.


D. Modifications de la version 2.9 � la version 3.0     

- Ajout de la fonction de Rapport. Les rapports affichent les
  r�sultats des communiqu�s qui correspondent � un certain
  crit�re de recherche. Ils peuvent �tre imprim�s ou enregistr�s
  dans un fichier. Les rapports peuvent contenir des sous-ensembles
  de champs de communiqu� et �tre formatt�s sous forme de tableau, 
  avec une rang�e pour chaque communiqu�.
  Les rapports peuvent �tre group�s et tri�s selon plusieurs champs,
  avec un d�compte sommaire des communiqu�s pour chaque groupement.
- Ajout de la fonction de personnalisation du format de la partie 
  inf�rieure/priv�e des communiqu�s.
- Nouvelle fonction d'Importation qui permet aux utilisateurs
  d'importer des donn�es �lectroniques dans le format de communiqu�
  Martus, comprenant � la fois le texte et les fichiers joints.
  La fonction d'Exportation a �t� �galement mise � jour pour
  correspondre � la structure d'Importation et permet d�sormais
  d'exporter des pi�ces jointes.
- Am�lioration de la fonction de Recherche, comprenant la possibilit�
  de rechercher dans les colonnes particuli�res d'une grille
  personnalis�e, la fusion de champs similaires dans la liste de
  s�lection des champs, et des clarifications si plusieurs champs
  poss�dent les m�mes �tiquettes.
- Modification de l'affichage de l'ann�e par d�faut dans la date des
  communiqu�s en "Inconnue", au lieu de l'ann�e en cours.
- Am�lioration significative des performances pour les compteS
  poss�dant un grand nombre de communiqu�s et sp�cifiquement lors du 
  chargement de l'�cran d'extraction.    
  Messages d'�tat suppl�mentaires aux utilisateurs lors d'op�rations
  potentiellement longues.
- Am�lioration de la personnalisation, comprenant les messages
  suppl�mentaires aux utilisateurs et l'affichage de longues
  �tiquettes de champs personnalis�s sur plusieurs lignes.


E. Modifications de la version 2.8.1 � la version 2.9

- A partir de la version 2.6 du Serveur (Mars 2006), acc�l�ration 
  de plusieurs op�rations majeures client/serveur. De fa�on pr�cise, 
  les op�rations suivantes sont d�sormais plus rapides : 
  t�l�chargement/envoi de communiqu�s, extraction de vos propres
  communiqu�s ou communiqu�s de bureau, initialisation lors de la
  connection au serveur avec votre compte.
- Am�lioration du fonctionnement des comptes comportant un grand
  nombre de communiqu�s. Acc�l�ration des actions suivantes :  
  affichage des dossiers/classement/d�placement des communiqu�s, etc.
- Modification des op�rations d'extraction qui appara�tront d�sormais
  en t�ches de fond (de la m�me mani�re que l'envoi de communiqu�s
  � un serveur), afin que vous puissiez continuer de travailler 
  dans Martus sans attendre la fin de l'extraction.  Lorsqu'elle
  est termin�e, les communiqu�s sont affich�s dans le dossier
  appropri� "Extraits". Pour annuler une extraction, revenez � la
  bo�te de dialogue "Extraire".
- Am�lioration des messages transmis � l'utilisateur sur le statut
  des serveurs.
- Ajout de la fonction de recherche dans les colonnes d'une grille
  (au lieu de la totalit� du texte de la grille) lors de la
  sp�cification d'un champ dans une Recherche Avanc�e, et ajout
  de l'option de recherche uniquement dans la derni�re version
  des communiqu�s.
- R�solution de probl�mes introduits dans les versions 2.8 et 2.8.1.
  De fa�on pr�cise, localisation des r�sultats de recherche
  incorrects dans les champs d�roulants comportant des espaces
  dans les valeurs choisies, et incorporation de balises de  
  personnalisation dans la liste des champs de recherche o�
  les �tiquettes �taient laiss�es vides (p.ex. pour les en-t�tes
  de section) afin qu'il n'y ait aucune valeur vide dans la liste
  de champs. 
- Ajout de la fonction d'insertion et de suppression de rang�es
  dans les grilles personnalis�es et l'�cran de recherche.
- Utilisation de tout l'espace �cran disponible lors de l'affichage
  de donn�es de communiqu� et de la bo�te de dialogue Info Contact.
- D�placement de l'option "Renvoyer Communiqu�s" sous le menu
  du serveur (pour les Si�ges qui sauvegardent les communiqu�s sur
  les serveurs pour les bureaux qui n'ont pas acc�s � internet).
- Suppression des messages trompeurs "Tous les communiqu�s n'ont
  pas �t� extraits", qui apparaissaient lorsqu'un compte Si�ge
  n'avait pas la permission d'afficher les anciennes versions
  de certains communiqu�s.
- Plusieurs mises � jour concernant les pr�f�rences de date :  
  localisation des formats de date, choix de formats additionnels,
  modifications effectu�es pour afficher correctement les dates
  tha� et perses (et convertir celles pr�c�demment saisies).
  Les dates perses utilisent un algorithme arithm�tique bien connu
  pour calculer les ann�es bissextiles. Cr�ation �galement d'un
  outil d'aide au diagnostique des param�tres de date.  
- Modification pour aider les programmes de traitement de textes
  � afficher correctement les accents dans les fichiers rapport
  en html.
- Ajout du kurde dans la liste d�roulante des langues disponibles.
  Si vous avez besoin d'aide pour l'affichage des caract�res kurdes
  dans Martus, veuillez contacter help@martus.org .
- Impl�mentation initiale d'un outil d'importation de donn�es
  pour permettre la conversion de fichiers �lectroniques
  (format .csv ou .xml) au format de communiqu�s Martus.
  Cette version initiale prend en charge l'importation de tous
  les types de champs, except�es les grilles personnalis�es
  et les pi�ces jointes. Su vous avez besoin d'aide/instructions
  sur l'ex�cution de cet utilitaire, veuillez contacter  
  help@martus.org .
- Plusieurs r�parations mineures de bogues, clarifications 
  et nettoyages sur les �crans de l'utilisateur.


F. Modifications de la version 2.8 � la version 2.8.1

Nous avons not� l�apparition d�un probl�me dans la version 
2.8.1 lorsque vous effectuez une recherche dans les champs 
d�roulants personnalis�s qui contiennent des espaces dans 
leurs choix d�roulants. Actuellement, Martus retrouvera 
correctement des communiqu�s si vous recherchez des valeurs 
dans le menu d�roulant en utilisant � Tous les Champs � et 
� contient �. Le probl�me appara�tra uniquement si vous 
effectuez une recherche dans un champs d�roulant sp�cifique. 
Ce probl�me sera r�gl� dans la prochaine �dition du logiciel, 
mais en attendant, si vous personnalisez vos communiqu�s nous 
vous conseillons de cr�er des choix d�roulants qui ne 
contiennent pas d'espace entre les mots (par contre, si vous 
le d�sirez, vous pouvez utiliser des tirets entre les mots.) 

- R�solution d'un probl�me introduit dans la version 2.8 o�
  les dates et les fourchettes de dates ayant des valeurs 
  ant�rieures au 1er janvier 1970 n'�taient pas correctement
  affich�es ni m�moris�es.
- Int�gration des traductions en tha�landais et en russe de 
  l'interface utilisateur de la version 2.8 du logiciel


G. Modifications de la version 2.7.2 � la version 2.8

Cette publication est disponible en anglais mais aussi en 
d'autres langues, comme indiqu� � la page t�l�chargement du
logiciel sur le site http://www.martus.org .  
Les autres langues seront disponibles par la suite, comme 
packs de langues, � cette m�me page 
(http://www.martus.org/downloads).

- Ajout de la fonction cr�ation de colonnes de la grille de
  types diff�rents (listes d�roulantes, cases � cocher, 
  dates et fourchettes de dates)
- La fonction de Recherche Avanc�e permet d�sormais � 
  l'utilisateur de sp�cifier des champs particuliers pour 
  la recherche (incluant les champs personnalis�s), en plus
  de la recherche dans la totalit� des communiqu�s. La 
  recherche peut combiner l'utilisation de diff�rents champs 
  et/ou options.
- Am�lioration des fonctionnalit�s d'impression afin de 
  pouvoir imprimer � la fois plusieurs communiqu�s 
  s�lectionn�s.
- Ajout de la fonction sauvegarde du/des communiqu�(s) dans 
  un fichier html, avec l'option d'inclure ou non les 
  donn�es priv�es
- Cr�ation d'une nouvelle option "Organiser les dossiers" 
  dans le menu pour permettre � l'utilisateur de mettre les 
  dossiers dans n'importe quel ordre.
- Ajout de messages d'avertissement � l'encontre de 
  l'utilisateur si la version de la traduction n'est pas la 
  m�me que celle du logiciel, et affichage de la date de 
  tout pack de langue existant dans la bo�te de dialogue 
  A propos de
- Affichage du logo Martus � l'ex�cution du programme afin 
  que l'utilisateur sache qu'il est en train de d�marrer
- Mise � jour des biblioth�ques de cryptage afin de pouvoir 
  utiliser Bouncy Castle Java Cryptography Extension
- Am�liorations suppl�mentaires dans l'affichage et 
  l'impression des langues se lisant de droite � gauche
- Int�gration des polices syst�me afin d'afficher � partir 
  du menu certaines langues (comme le n�palais)
- Modification du comportement des communiqu�s afin que les 
  modifications des brouillons comprennent les nouveaux 
  param�tres de personnalisation et de configuration Si�ge.
- A partir de la version 2.4 du Serveur Martus, ajout d'une 
  fonction qui permet � l'utilisateur du Client Martus de 
  recevoir des nouvelles du serveur de sauvegarde Martus 
  lorsqu'il se connecte (� savoir des messages � propos des 
  nouvelles versions disponibles en t�l�chargement, ou de la
  dur�e d'indisponibilit� lors de la maintenance du serveur)
- Modification de la m�thode de v�rification des fichiers 
  t�l�charg�s de MD5 � SHA1
- Mise � jour de la documentation utilisateur en anglais 
  (Guide de D�marrage Rapide et Guide de l'Utilisateur)
- Plusieurs r�parations mineures de bogues, clarifications 
  et nettoyages sur les �crans de l'utilisateur.


H. Modifications de la version 2.7 � la version 2.7.2

- Remplacement de la traduction incompl�te/inexacte en 
  n�palais de la version 2.0.1 de l'interface utilisateur du 
  logiciel Martus par la mise � jour d'un nouveau pack de 
  langue n�palaise � la page http://www.martus.org/downloads.   
  Ce pack de langue inclut les traductions en n�palais de 
  l'interface utilisateur du logiciel (utilisable pour les 
  versions 2.5 et ult�rieures, et comportant 90 % des cha�nes 
  traduites en n�palais), de l'aide int�gr�e (version 2.0.1), 
  du Guide de D�marrage Rapide (version 2.0.1), du Guide de 
  l'Utilisateur (version 2.0.1), et du fichier Lisez-moi
  (partiellement traduit jusqu'� la version 2.6).

Sous Windows, pour ex�cuter le logiciel Martus en n�palais 
pour la version 2.7.2 et les versions pr�c�dentes, afin que 
les menus puissent s'afficher correctement, vous devez 
effectuer une petite modification dans la commande utilis�e 
pour d�marrer Martus (� partir de l'invite de commande et de 
n'importe quel raccourci ou alias du bureau/Menu D�marrer 
cr�� lors de l'installation).  

Pour l'ex�cuter � partir de la ligne de commande, allez dans 
le r�pertoire Martus et entrez : 
C:\Martus\bin\javaw.exe -Dswing.useSystemFontSettings=false -jar C:\Martus\Martus.jar 

Pour modifier vos raccourcis, faites un clic droit sur 
l'ic�ne, choisissez Propri�t�s, et changez la Cible vers :
C:\Martus\bin\javaw.exe -Dswing.useSystemFontSettings=false -jar C:\Martus\Martus.jar 


I. Modifications de la version 2.6 � la version 2.7

Cette publication est disponible en anglais mais aussi en 
d'autres langues, comme indiqu� � la page t�l�chargement du
logiciel sur le site http://www.martus.org .  
Les autres langues seront disponibles par la suite, comme 
packs de langues, � cette m�me page 
(http://www.martus.org/downloads).

- Ajout de la fonction cr�ation de listes d�roulantes 
  dans les champs personnalis�s uniques (hors grille) 
- Ajout de la fonction cr�ation de messages personnalis�s 
  expliquant comment entrer des donn�es, et de la cr�ation 
  de commentaires/notes qui appara�tront dans tous les 
  communiqu�s (p.ex. aide �cran)
- Pour les comptes Si�ge, ajout de la fonction exportation 
  de mod�les personnalis�s � transmettre aux utilisateurs de 
  terrain, ou utilisateurs puissent exporter leurs propres mod�les. 
  Les utilisateurs pourront alors importer les param�tres de   
  personnalisation � partir d'un choix de mod�les.
- Chaque si�ge configur� peut dor�navant �tre activ� ou 
  d�sactiv� pour chaque communiqu� cr�� ou modifi�. L'Utilisateur 
  peut �galement d�signer certains comptes Si�ge � attribuer 
  par d�faut � tous les communiqu�s nouvellement cr��s.
- Les recherches peuvent d�sormais scanner � outre la derni�re 
  version - les versions pr�c�dentes de chaque communiqu�. 
- Am�liorations suppl�mentaires dans l'affichage des langues se 
  lisant de droite � gauche
- Inclut la traduction en perse de l'interface utilisateur
- L'exportation vers XML inclut d�sormais le type champ personnalis� 


J. Modifications de la version 2.5 � la version 2.6

- L'utilisateur peut dor�navant rechercher et afficher 
  l'int�gralit� du contenu de toutes les versions des 
  communiqu�s verrouill�s pr�sents dans son ordinateur, en 
  cliquant sur le bouton D�tails de Communiqu�. 
- Possibilit� am�lior�e de choisir sur le serveur soit la 
  r�cup�ration de toutes les versions d'un communiqu� verrouill�, 
  soit simplement la version la plus r�cente. L'utilisateur ne 
  poss�dant qu'une petite unit� de disque ou ayant une connexion 
  Internet lente peut choisir de ne r�cup�rer que la version la 
  plus r�cente des communiqu�s volumineux.
- Le nom de fichier des pi�ces jointes est dor�navant inclus 
  dans les recherches.
- Modification de la fonctionnalit� d'exportation XML afin de 
  mieux s'adapter aux champs personnalis�s et aux diff�rentes 
  versions des communiqu�s verrouill�s.
- Am�lioration de l'affichage des langues lues de droite � 
  gauche (comme l'Arabe)
- Inclusion du Guide de l'Utilisateur et du Guide de D�marrage 
  Rapide en Arabe.
- Plusieurs r�parations mineures de bogues, clarifications et 
  nettoyages sur les �crans de l'utilisateur.
- Lors de l'utilisation de Martus 2.6 en Arabe sous Mac OS, 
  certains probl�mes d'affichage d'�crans peuvent survenir en 
  cours d'ex�cution.



K. Modifications de la version 2.0.1 � la version 2.5.

- Possibilit� suppl�mentaire de cr�er de nouvelles versions 
  de communiqu�s verrouill�s de mani�re � ce que les 
  modifications ou ajouts puissent �tre effectu�s sur les 
  communiqu�s pr�c�demment verrouill�s. Dans cette mise � 
  jour de Martus, vous ne pourrez rechercher et afficher 
  que le contenu entier de la version la plus actualis�e 
  (vous pourrez afficher le titre/l'id du communiqu�/la date 
  de sauvegarde de toute version ant�rieure stock�e dans votre 
  ordinateur, en cliquant sur le bouton D�tails de Communiqu�).  
- Fonctionnalit� suppl�mentaire pour installer de nouvelles 
  traductions mises � jour � tout moment, suite � une �dition 
  compl�te de Martus. Un "Pack Langues" pour chaque langue (y 
  compris l'anglais) peut contenir la traduction de l'Interface 
  Utilisateur du Client Martus, le Guide de l'Utilisateur, le 
  Guide de D�marrage Rapide, le fichier Lisez-moi et l'aide en 
  ligne. Les packs de langues pourront �tre t�l�charg�s sur le 
  site web de Martus.
- Plusieurs modifications ont �t� effectu�es pour acc�l�rer le 
  traitement des communiqu�s et des dossiers (par exemple tri ou 
  d�placement des communiqu�s) 
- Am�lioration des fonctionnalit�s des champs personnalis�s 
  (par ex. taille des colonnes de la grille)
- Nouvelle traduction en Tha�  
- Nouvelle traduction en Arabe 
- Modifications effectu�es pour afficher correctement les 
  langues se lisant de Droite � Gauche (comme l'Arabe)
- Plusieurs am�liorations pour afficher l'Interface 
  Utilisateur du Client Martus sous Linux.
- Le probl�me suivant a �t� r�solu : la r�cup�ration ou 
  l'importation de communiqu�s comprenant des pi�ces jointes 
  tr�s volumineuses risquait de provoquer la fermeture de 
  Martus pour cause de "m�moire insuffisante". Les pi�ces 
  jointes inf�rieures � 20 m�gaoctets ne sont pas susceptibles 
  de provoquer cette erreur.
- Plusieurs r�parations mineures de bogues, clarifications et 
  nettoyages sur les �crans de l'utilisateur.
- Certains probl�mes d'affichage d'�cran peuvent appara�tre 
  dans le programme d'installation Martus 2.5 en N�palais et 
  en Tha�. Du fait de la difficult� de tester toutes les 
  versions de Windows dans ces langues, nous vous serions 
  reconnaissants de nous signaler toute erreur d'affichage � 
  l'installation de Martus.


L. Modifications de la version 2.0. � la version 2.0.1

- Ajout d'une barre de d�filement horizontale pour les 
  grilles de champs personnalis�s sup�rieurs � la largeur 
  d'�cran.
- Nouvelle traduction en Fran�ais
- Mise � jour de la documentation Utilisateur en Russe et en 
  Espagnol avec la fonctionnalit� 2.0
- Plusieurs clarifications et nettoyages apport�s � la 
  documentation de l'utilisateur en Anglais
- Modifications de l'utilitaire d'installation pour r�gler 
  les probl�mes de mise � niveau sous Windows 98 et ME en 
  langues autres que l'Anglais
- Ajout de l'option multi dossier (disquette) sur le site 
  de t�l�chargement


M. Modifications de la version 1.5. � la version 2.0.

- Vous pouvez d�sormais poss�der plusieurs comptes de si�ge, 
  et les param�trer � l'aide d'une interface am�lior�e. Cela 
  s'av�rera tr�s utile si vous souhaitez que plusieurs personnes 
  au sein de votre organisme r�visent vos communiqu�s. 
- Les comptes de Si�ge peuvent d�sormais envoyer les communiqu�s 
  � un serveur en lieu et place d'un bureau de terrain d�pourvu 
  d'acc�s Internet.
- Les fonctionnalit�s Champs Personnalis�s ont �t� �tendues de 
  mani�re � ce que vous puissiez d�sormais cr�er des champs 
  personnalis�s de types diff�rents (comme date, grille, Oui/Non).  
- Nous utilisons un nouvel utilitaire d'installation Windows en 
  code source libre (NSIS) qui peut �tre ex�cut� dans les 
  alphabets non latins.  
- Un nouveau dossier "Communiqu�s Enregistr�s" remplace les 
  fichiers Bo�te d'Envoi, Communiqu�s Envoy�s et Brouillons 
  de Communiqu�s
- D�sormais, chaque liste de Communiqu�s affiche une colonne 
  montrant si oui ou non le communiqu� a bien �t� envoy� au serveur
- La date du dernier enregistrement d'un communiqu� s'affiche 
  d�sormais dans les listes de pr�visualisation des communiqu�s 
  ainsi que dans l'en-t�te des communiqu�s
- Lors de la cr�ation/modification d'un communiqu�, le bouton 
  "Envoyer" a �t� remplac� par "Enregistrer Verrouill�" 
- La fonctionnalit� d'effacement rapide de Martus 1.5 a �t� 
  remplac�e par deux menus :  "Supprimer Mes Donn�es", qui 
  supprime les communiqu�s et la cl� de ce compte ; et 
  "Supprimer Toutes Donn�es et D�sinstaller Martus" qui 
  d�sinstalle Martus et supprime l'int�gralit� du r�pertoire 
  Martus, y compris les donn�es des autres comptes � � 
  n'utiliser qu'en cas d'urgence
- Am�lioration des fonctionnalit�s de sauvegarde de cl� � 
  aucun besoin d'effectuer une sauvegarde avant la cr�ation 
  des communiqu�s, mais il vous est rappel� de sauvegarder 
  votre cl� plus tard si vous ne l'avez encore fait.
- Pour effectuer une recherch� de communiqu�, l'on peut 
  utiliser soit l'anglais "and" et "or", soit la traduction 
  de leurs �quivalents. Cela permet aux utilisateurs n'ayant 
  pas acc�s au clavier autochtone de pouvoir effectuer les 
  recherches "et" et "ou".
- Un nouveau bouton "D�tails de Communiqu�" affiche une 
  identification unique de communiqu� ainsi que les comptes 
  de Si�ge pouvant afficher les donn�es priv�es de ce communiqu�
- A l'impression, vous avez d�sormais l'option de masquer ou 
  d'inclure les donn�es priv�es 
- Vous pouvez d�sormais acc�der � l'aide en ligne 
- Vous pouvez ins�rer un drapeau interdisant les communiqu�s 
  publics pour raisons de s�curit�
- Am�lioration de la fonctionnalit� Renommer les Dossiers
- Plusieurs r�parations mineures de bogues, clarifications 
  et nettoyages sur l'interface utilisateur.

Nous vous recommandons d'ex�cuter le Logiciel Client Martus 2.0 
avec le Logiciel Serveur Martus version 2.1 ou plus r�cent. A 
compter de la date de Martus Client version 2.0, tous les 
serveurs de production Martus ex�cutent la version 2.1, mais 
si vous ex�cutez votre propre serveur de sauvegarde Martus, 
vous devez proc�der � une mise � jour (le logiciel Serveur 
Martus est t�l�chargeable sur le site www.martus.org).  


N. Modifications de la version 1.2.1 � la version 1.5.

- Les comptes multiples peuvent d�sormais �tre param�tr�s 
  sur ordinateur. Cela modifie l'�cran d'ouverture de session
  de plusieurs mani�res : vous avez le choix entre ouvrir une 
  session sur un compte d�j� param�tr�, cr�er un nouveau compte, 
  ou restaurer � partir d'une sauvegarde de cl�. Un 
  sous-r�pertoire pour chaque compte appara�tra dans le 
  r�pertoire Martus.
- L'option de s�lection de la langue dans laquelle vous 
  souhaitez ex�cuter Martus est d�sormais disponible sur 
  l'�cran d'ouverture de session.
- Am�lioration des fonctionnalit�s de sauvegarde de cl�, 
  et option de sauvegarder dans plusieurs dossiers de "secret 
  partag�" � distribuer aux amis.  
- Fonctionnalit�s �tendues d'Effacement Rapide pour permettre 
  la suppression de la cl�, le nettoyage des communiqu�s et des 
  donn�es de la cl� avant suppression, ainsi que l'option 
  d'effectuer l'Effacement Rapide et de quitter Martus sans 
  invite ni interaction utilisateur.
- Un nouveau Menu Outils (Effacement Rapide, cl� et 
  actions Si�ge)
- Am�lioration de la communication avec les serveurs de 
  sauvegarde Martus, et simplification des messages transmis � 
  l'utilisateur sur le statut des serveurs
- Options linguistiques suppl�mentaires ajout�es � la liste de 
  s�lection de communiqu�
- R�paration d'un bogue qui emp�chait les versions pr�c�dentes 
  du programme Martus de r�cup�rer les communiqu�s � partir 
  d'un serveur de sauvegarde, si ces derniers contenaient des 
  informations publiques avec des lettres non anglaises. Ce 
  bogue n'a pas affect� la s�curit� de sauvegarde des 
  communiqu�s sur un serveur Martus. Le probl�me 
  n'apparaissait que si les communiqu�s �taient absents de 
  votre ordinateur, et que vous tentiez de r�cup�rer vos 
  propres communiqu�s verrouill�s, ou ceux de votre bureau 
  de terrain, contenant des donn�es publiques non anglaises 
  (le message re�u dans ce cas �tait "Erreur pendant la 
  r�cup�ration des r�sum�s de communiqu�s. Certains communiqu�s 
  du serveur ne s'afficheront pas.") Le bogue n'a affect� 
  la r�cup�ration ni  des communiqu�s enti�rement priv�s, ni 
  des brouillons. Si vous souhaitez r�cup�rer des communiqu�s 
  contenant des donn�es publiques en langue autre que l'anglais, 
  il vous faut acc�der � un serveur de  sauvegarde Martus de 
  production, ou mettre � jour votre propre serveur pour 
  ex�cuter le Logiciel Serveur Martus version 2.0 ou plus r�cente. 
- Mise � jour de la version Java vers 1.4.2_03
- Plusieurs r�parations mineures de bogues, clarifications 
  et nettoyages sur l'interface utilisateur.


O. Modifications de la version 1.2. � la version 1.2.1

- Introduction de la version Russe, comprenant une version 
  sp�ciale du programme d'installation en Russe.
- Mise � jour des Guides Utilisateur en Anglais et en 
  Espagnol avec la fonctionnalit� 1.2
- Cr�ation du script LinuxJavaInstall.txt pour simplifier 
  l'installation Java sur les ordinateurs GNU/Linux.


P. Modifications de la version 1.0.3 � la version 1.2.

- Vous pouvez d�sormais personnaliser les champs de tous les 
  communiqu�s cr��s ult�rieurement. Nous conseillons aux seuls 
  utilisateurs "avanc�s" de personnaliser les champs. Lorsque 
  vous choisissez cette option de menu, vous �tes avertis que 
  si vous n'�tes pas chevronn�, vous devez revenir en arri�re 
  ou risquer de d�r�gler votre syst�me. Si vous continuez, 
  vous recevez une liste de balises de champs standards existants, 
  s�par�es par des points-virgules. Vous pouvez supprimer ceux 
  que vous souhaitez, sauf quatre champs "obligatoires" : 
  entrydate (dateentr�e), language (langue), author (auteur), 
  et title (titre). Vous pouvez modifier la s�quence. Vous 
  pouvez �galement ins�rer vos propres champs personnalis�s. 
  Chaque champ personnalis� doit comporter une "balise", 
  compos�e d'une cha�ne de lettres minuscules ASCII suivies 
  d'une virgule, suivies de l'invite �cran pouvant contenir 
  un m�lange de caract�res et d'espaces. Si vous tentez 
  d'enregistrer une cha�ne de d�finition de champ personnalis� 
  qui viole une quelconque r�gle, vous serez inform� qu'elle 
  est invalide, sans pour autant savoir quelle r�gle vous 
  aurez viol�e.
- Vous avez d�sormais l'option de taper une fourchette de 
  dates (p�riode entre deux dates) pour la date d'�v�nement 
  du communiqu�.
- Ajout de l'Interface Utilisateur en Russe, avec traduction 
  de plus de 90% des messages.
- Vous pouvez d�sormais sp�cifier un dossier entier � 
  l'exportation.
- Si les ports normaux de Martus sont dans l'incapacit� de 
  communiquer avec un serveur de sauvegarde � cause de probl�mes 
  tels qu'une configuration de pare-feu, le programme Martus 
  r�utilise les ports 80/443.
- La convivialit� du processus de sauvegarde de cl� a �t� 
  am�lior�e.
- La nouvelle version de Java, v 1.4.1_03, est pr�sente 
  pour les t�l�chargements et les images CD. 
- Plusieurs r�parations mineures de bogues, clarifications et 
  nettoyages sur l'interface utilisateur, notamment une aide 
  accrue pour une utilisation sans souris.


Q. Modifications de la version 1.0.2 � la version 1.0.3

- Versions Martus compatibles Linux et Mac.
- Si vous avez install� une ancienne version du logiciel Martus, 
  vous pouvez en t�l�charger une version r�duite sans avoir 
  besoin de t�l�charger le fichier int�gral contenant tout Java.
- Les communiqu�s peuvent �tre export�s du programme en format 
  texte XML.
- V�rification de la version compatible Java pendant 
  l'installation et le d�marrage.
- Manipulation plus rapide des communiqu�s avec pi�ces jointes.
- Passage au curseur d'attente pendant les op�rations de 
  longue dur�e.
- Sous Windows, vous pouvez d�sormais afficher les pi�ces 
  jointes.
- Modifications du contrat de licence compatible GNU GPL, 
  simplifiant l'avertissement � l'utilisateur lors de l'emprunt 
  d'un code pour une utilisation ext�rieure au logiciel Martus, 
  et �tendant la couverture aux applications du serveur Martus.
- Vous pouvez d�sormais recevoir des messages du serveur Martus, 
  y compris une d�claration de conformit� au serveur.
- Une nouvelle bo�te de dialogue de Bienvenue s'affiche au 
  d�marrage du programme, indiquant s'il s'agit d'une version 
  officielle de ce programme.
- Simplification de l'installation du logiciel Martus pendant le 
  t�l�chargement du programme d'installation. Le programme de 
  v�rification Jar est d�sormais disponible en t�l�chargement s�par�.
- Une nouvelle fonctionnalit� d'Effacement Rapide supprimera toutes 
  les copies locales des communiqu�s de votre disque dur.
- R�paration d'un bogue susceptible de corrompre un communiqu� en 
  cas de r�cup�ration depuis un serveur, suivie de la modification 
  d'une copie par ajout d'une nouvelle pi�ce jointe.
- Mise � jour de quelques cha�nes de traduction en Espagnol.
- R�solution d'un probl�me de fen�tre n�cessitant la zone de 
  saisie courante en cas de d�passement de d�lai.
- Placement de la fen�tre de dialogue de d�but de session 
  au-dessus des autres, et ajout dans la barre des t�ches.
- Masquage de la fen�tre principale pendant l'�dition.
- Ajout d'une biblioth�que crypto � jour et d'une nouvelle version 
  de Java, v 1.4.1_02, supportant la frappe de caract�res �trangers 
  � l'aide du clavier num�rique et r�parant une perte de m�moire 
  qui vide lentement la m�moire disponible.
- Ajout des touches rapides Windows standard aux endroits cruciaux 
  comme Suppr et Control C, X, V et A.


R. Modifications de la version 1.0.1 � la version 1.0.2

- En choisissant de modifier un communiqu� verrouill� une copie 
  est effectu�e, mais si l'original comportait des pi�ces jointes, 
  le syst�me pouvait devenir instable pour finir par endommager 
  la copie et/ou l'original. Ce probl�me a �t� corrig�.
- Permet la r�partition CD du contenu par d�faut pouvant pr�charger 
  le champ D�tails de chaque communiqu�. Si DefaultDetails.txt est 
  sur le CD, il sera copi� dans le r�pertoire Martus � l'installation. 
  Puis, si DefaultDetails.txt est trouv� dans le r�pertoire Martus 
  � la cr�ation d'un nouveau compte, il donnera � l'utilisateur 
  la possibilit� de l'utiliser comme contenu de D�tails par d�faut 
  pour tout communiqu� cr��.
- R�organisation du code et de la structure du programme interne 
  pour en faciliter l'entretien.


S. Modifications de la version 1.0. � la version 1.0.1

- Demande en cours de l'installation du programme si 
  l'utilisateur souhaite que les fichiers du programme Martus, 
  de d�sinstallation Martus et de documentation soient ajout�s 
  au menu d�marrer de Windows.
- Mise � jour de l'Environnement Runtime de Java version 1.4.1 
  � la version 1.1.1_01, am�liorant ainsi quelques �l�ments de 
  s�curit� de Java.
- V�rification de la copie sur disque dur pendant l'installation, 
  de l'ensemble des sources tierces indispensables, licences Runtime, 
  fichiers de documentation et programmes Winsock pour Windows 95.
- Correction d'erreurs dans la Case Au Sujet De� et dans l'avis 
  de copyright de la documentation.


T. Instructions particuli�res de mise � jour de Winsock en cas 
   de probl�me d'ex�cution du logiciel Martus sous Windows 95 :

Java n�cessite la biblioth�que Microsoft Winsock 2.0. Votre 
syst�me est probablement d�j� �quip� de la version Winsock 2.0 
ou plus r�cente. Il reste cependant possible que certains syst�mes 
Microsoft Windows 95 aient une version ant�rieure de Winsock.

Pour v�rifier votre version de Winsock, faites une recherche 
"winsock.dll". Puis choisissez "Propri�t�s" dans le menu Fichier 
et cliquez sur l'onglet Version.

Si votre ordinateur sous Microsoft Windows 95 n'a pas la version 
Winsock 2.0 ou plus r�cente, vous pouvez ex�cuter le programme 
d'installation Winsock 2.0 situ� dans le r�pertoire Martus\Win95. 

Si vous �tes curieux, l'URL suivante contient les renseignements 
contribuant � d�terminer si les composants de Winsock 2.0 sont 
install�s sur une plateforme Microsoft Windows 95 :

http://support.microsoft.com/support/kb/articles/Q177/7/19.asp


