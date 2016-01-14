Martus(tm) Version Logiciel 3.3 README_fr.txt
---------------------------------------------

Table des Matières :
A. Modifications de la version 3.2 à la version 3.3     (08-2008)
B. Modifications de la version 3.1 à la version 3.2     (09-2007)
C. Modifications de la version 3.0 à la version 3.1     (04-2007)
D. Modifications de la version 2.9 à la version 3.0     (09-2006)
E. Modifications de la version 2.8.1 à la version 2.9   (03-2006)
F. Modifications de la version 2.8 à la version 2.8.1   (11-2005)
G. Modifications de la version 2.7.2 à la version 2.8   (09-2005)
H. Modifications de la version 2.7 à la version 2.7.2   (08-2005)
I. Modifications de la version 2.6 à la version 2.7     (04-2005)
J. Modifications de la version 2.5 à la version 2.6     (02-2005)
K. Modifications de la version 2.0.1 à la version 2.5   (11-2004)
L. Modifications de la version 2.0. à la version 2.0.1  (08-2004)
M. Modifications de la version 1.5. à la version 2.0    (07-2004)
N. Modifications de la version 1.2.1 à la version 1.5   (02-2004)
O. Modifications de la version 1.2. à la version 1.2.1  (12-2003)
P. Modifications de la version 1.0.3 à la version 1.2   (09-2003)
Q. Modifications de la version 1.0.2 à la version 1.0.3 (05-2003)
R. Modifications de la version 1.0.1 à la version 1.0.2 (02-2003)
S. Modifications de la version 1.0. à la version 1.0.1  (01-2003)
T. Instructions particulières de mise à jour de Winsock 
   en cas de problème d'ouverture du logiciel Martus sous 
   Windows 95 :

Voir http://www.martus.org pour tous renseignements sur le 
logiciel Martus.

Voir le Guide de l'Utilisateur Martus pour les instructions 
d'installation du logiciel Martus. 

Pour toute traduction mise à jour de la documentation de 
l'utilisateur, ainsi que la documentation des versions 
précédentes en diverses langues, suivez le lien 
http://www.martus.org/downloads/.  

A. Modifications de la version 3.2 à la version 3.3     

Cette publication est disponible en anglais mais aussi en 
d'autres langues, comme indiqué à la page téléchargement du
logiciel sur le site http://www.martus.org .  
Les autres langues seront disponibles par la suite, comme 
packs de langues, à cette même page (http://www.martus.org/downloads).

- Ajout d’une fonction qui permet de verrouiller plusieurs 
  Brouillons simultanément. Pour cela, allez dans Editer > 
  Verrouiller le(s)communiqué(s).
- Nouvelle fonction qui permet d’ajouter un Siège à un groupe 
  De communiqués ou à un dossier de communiqués. Pour cela, allez 
  dans Editer > Mettre à jour l’accès du Siège. Cette option sera 
  grisée sauf si au moins un communiqué est sélectionné et un Siège 
  est configuré. Martus affichera une barre de progression pendant
  la mise à jour et vous permettra d’annuler l’opération si vous 
  le souhaitez. Les brouillons de communiqués seront mis à jour 
  pour correspondre aux nouvelles informations du Siège. Pour les 
  communiqués verrouillés, Martus génèrera automatiquement une 
  nouvelle version verrouillée de chaque communiqué.
- Ajout d’une fonction qui permet d’afficher plusieurs types
  d’images en pièce jointe dans Martus lors de la prévisualisation,
  la création et l’édition de communiqués, ainsi que dans l’aperçu 
  des détails de communiqué.
- Plusieurs réparations mineures de bogues, clarifications et 
  nettoyages Sur les écrans de l'utilisateur.

REMARQUE (option disponible à partir de Martus 3.1, mais pas encore incluse dans le Guide de l’utilisateur) :  
Il existe un mécanisme supplémentaire qui permet d’accélérer le chargement et la navigation dans Martus lorsqu'un compte possède un
grand nombre de communiqués. Vous pouvez ajouter " --folders-unsorted"
à la fin de la commande dans votre raccourci Martus se trouvant sur 
le bureau. Cette option permet de ne pas trier les dossiers lorsque 
vous les chargez au départ dans Martus (car le tri peut prendre du 
temps avec un grand nombre de communiqués). Vous pouvez toujours 
cliquer sur un en-tête de colonne dans la liste de prévisualisation 
des communiqués pour trier le dossier, si vous le souhaitez, mais 
l'ajout de cette option vous permettra de gagner du temps au 
démarrage et lors de la saisie de nouveaux dossiers dans Martus.


B. Modifications de la version 3.1 à la version 3.2     

- Nouvelle fonction d'affichage des communiqués qui vous permet de :
  1) masquer/afficher des champs dans les communiqués (longs champs
  qui prennent beaucoup de place), 2) créer des sections dans vos
  communiqués que vous pouvez également masquer/afficher, 3) afficher
  des grilles dans une vue "étendue" (et possibilité d'ajouter des
  rangées à partir de cette vue), 4) insérer des champs les uns
  à la suite des autres sur une rangée, 5) réduire la taille par 
  défaut des grilles pour gagner de la place.
- Ajout d'une fonction qui permet d'exiger que certains champs
  soient renseignés avant l'enregistrement d'un communiqué. 
- Ajout de la fonction de création de listes déroulantes guidées
  par les données, où les valeurs d'un champ déroulant sont basées
  sur des données qui ont été saisies ailleurs dans un champ de
  grille du communiqué.
- Ajout d'une boîte de dialogue indiquant l'état de progression de
  la recherche et la possibilité d'annuler des recherches.
- Amélioration des performances de recherche et des
  autres opérations relatives aux communiqués, en particulier
  pour les utilisateurs possédant un grand nombre de communiqués. 
- Correction de plusieurs problèmes : 1) les paramètres de
  configuration (p. ex des Sièges) n'étaient pas enregistrés pour
  les utilisateurs dont les communiqués avaient subi un grand
  nombre de personnalisations, 2) erreurs lors du glisser-déposer
  de communiqués pour leur envoi/extraction à/à partir d'un serveur.
- Amélioration des messages destinés aux utilisateurs (p. ex.
  des message d'erreur lors de la personnalisation).
- Plusieurs réparations mineures de bogues, clarifications 
  et nettoyages sur les écrans de l'utilisateur.


C. Modifications de la version 3.0 à la version 3.1

- Nouvelle fonction qui notifie les Sièges si des
  communiqués de terrain sont à extraire. Pour l'activer dans
  votre compte Martus, allez dans Options > Préférences, puis
  sélectionnez "Rechercher automatiquement les nouveaux communiqués
  de bureau de terrain". Environ une fois par heure, un message
  apparaît dans la barre d'état (coin inférieur gauche de l'écran)
  indiquant que Martus recherche les nouveaux communiqués de bureau
  de terrain. Un autre message s'affiche dans la barre d'état si des
  communiqués de bureau de terrain sont à extraire. A ce stade, vous
  pouvez aller dans le menu Serveur pour charger l'écran Extraire.  
- Ajout de la fonction de tri des communiqués dans l'écran Extraire
  en cliquant sur les en-têtes de colonnes. La fonction de tri
  est uniquement croissante.
- Amélioration des performances et correction des problèmes de mémoire
  possibles lors de la connexion et la déconnexion d'utilisateurs
  possédant un grand nombre de communiqués. 
- Résolution des problèmes introduits dans la version 3.0, à savoir : 
  1) Les champs des fourchettes de dates qui n'apparaissent pas sur
  les rapports sous forme de page
  2) La personnalisation de la partie privée qui est perdue lors de
  la fermeture de Martus
  3) Les problèmes de sélection de fichiers sur un Mac (p.ex. lors de
  la configuration de sièges, l'ajout de fichiers en pièce jointe
  à des communiqués, la restauration d'une clé et l'importation d'un
  modèle personnalisé)
  4) L'affichage incohérent de la date en Arabe entre les détails de
  communiqué et les décomptes sommaires 
- Plusieurs réparations mineures de bogues, clarifications 
  et nettoyages sur les écrans de l'utilisateur.


D. Modifications de la version 2.9 à la version 3.0     

- Ajout de la fonction de Rapport. Les rapports affichent les
  résultats des communiqués qui correspondent à un certain
  critère de recherche. Ils peuvent être imprimés ou enregistrés
  dans un fichier. Les rapports peuvent contenir des sous-ensembles
  de champs de communiqué et être formattés sous forme de tableau, 
  avec une rangée pour chaque communiqué.
  Les rapports peuvent être groupés et triés selon plusieurs champs,
  avec un décompte sommaire des communiqués pour chaque groupement.
- Ajout de la fonction de personnalisation du format de la partie 
  inférieure/privée des communiqués.
- Nouvelle fonction d'Importation qui permet aux utilisateurs
  d'importer des données électroniques dans le format de communiqué
  Martus, comprenant à la fois le texte et les fichiers joints.
  La fonction d'Exportation a été également mise à jour pour
  correspondre à la structure d'Importation et permet désormais
  d'exporter des pièces jointes.
- Amélioration de la fonction de Recherche, comprenant la possibilité
  de rechercher dans les colonnes particulières d'une grille
  personnalisée, la fusion de champs similaires dans la liste de
  sélection des champs, et des clarifications si plusieurs champs
  possèdent les mêmes étiquettes.
- Modification de l'affichage de l'année par défaut dans la date des
  communiqués en "Inconnue", au lieu de l'année en cours.
- Amélioration significative des performances pour les compteS
  possédant un grand nombre de communiqués et spécifiquement lors du 
  chargement de l'écran d'extraction.    
  Messages d'état supplémentaires aux utilisateurs lors d'opérations
  potentiellement longues.
- Amélioration de la personnalisation, comprenant les messages
  supplémentaires aux utilisateurs et l'affichage de longues
  étiquettes de champs personnalisés sur plusieurs lignes.


E. Modifications de la version 2.8.1 à la version 2.9

- A partir de la version 2.6 du Serveur (Mars 2006), accélération 
  de plusieurs opérations majeures client/serveur. De façon précise, 
  les opérations suivantes sont désormais plus rapides : 
  téléchargement/envoi de communiqués, extraction de vos propres
  communiqués ou communiqués de bureau, initialisation lors de la
  connection au serveur avec votre compte.
- Amélioration du fonctionnement des comptes comportant un grand
  nombre de communiqués. Accélération des actions suivantes :  
  affichage des dossiers/classement/déplacement des communiqués, etc.
- Modification des opérations d'extraction qui apparaîtront désormais
  en tâches de fond (de la même manière que l'envoi de communiqués
  à un serveur), afin que vous puissiez continuer de travailler 
  dans Martus sans attendre la fin de l'extraction.  Lorsqu'elle
  est terminée, les communiqués sont affichés dans le dossier
  approprié "Extraits". Pour annuler une extraction, revenez à la
  boîte de dialogue "Extraire".
- Amélioration des messages transmis à l'utilisateur sur le statut
  des serveurs.
- Ajout de la fonction de recherche dans les colonnes d'une grille
  (au lieu de la totalité du texte de la grille) lors de la
  spécification d'un champ dans une Recherche Avancée, et ajout
  de l'option de recherche uniquement dans la dernière version
  des communiqués.
- Résolution de problèmes introduits dans les versions 2.8 et 2.8.1.
  De façon précise, localisation des résultats de recherche
  incorrects dans les champs déroulants comportant des espaces
  dans les valeurs choisies, et incorporation de balises de  
  personnalisation dans la liste des champs de recherche où
  les étiquettes étaient laissées vides (p.ex. pour les en-têtes
  de section) afin qu'il n'y ait aucune valeur vide dans la liste
  de champs. 
- Ajout de la fonction d'insertion et de suppression de rangées
  dans les grilles personnalisées et l'écran de recherche.
- Utilisation de tout l'espace écran disponible lors de l'affichage
  de données de communiqué et de la boîte de dialogue Info Contact.
- Déplacement de l'option "Renvoyer Communiqués" sous le menu
  du serveur (pour les Sièges qui sauvegardent les communiqués sur
  les serveurs pour les bureaux qui n'ont pas accès à internet).
- Suppression des messages trompeurs "Tous les communiqués n'ont
  pas été extraits", qui apparaissaient lorsqu'un compte Siège
  n'avait pas la permission d'afficher les anciennes versions
  de certains communiqués.
- Plusieurs mises à jour concernant les préférences de date :  
  localisation des formats de date, choix de formats additionnels,
  modifications effectuées pour afficher correctement les dates
  thaï et perses (et convertir celles précédemment saisies).
  Les dates perses utilisent un algorithme arithmétique bien connu
  pour calculer les années bissextiles. Création également d'un
  outil d'aide au diagnostique des paramètres de date.  
- Modification pour aider les programmes de traitement de textes
  à afficher correctement les accents dans les fichiers rapport
  en html.
- Ajout du kurde dans la liste déroulante des langues disponibles.
  Si vous avez besoin d'aide pour l'affichage des caractères kurdes
  dans Martus, veuillez contacter help@martus.org .
- Implémentation initiale d'un outil d'importation de données
  pour permettre la conversion de fichiers électroniques
  (format .csv ou .xml) au format de communiqués Martus.
  Cette version initiale prend en charge l'importation de tous
  les types de champs, exceptées les grilles personnalisées
  et les pièces jointes. Su vous avez besoin d'aide/instructions
  sur l'exécution de cet utilitaire, veuillez contacter  
  help@martus.org .
- Plusieurs réparations mineures de bogues, clarifications 
  et nettoyages sur les écrans de l'utilisateur.


F. Modifications de la version 2.8 à la version 2.8.1

Nous avons noté l’apparition d’un problème dans la version 
2.8.1 lorsque vous effectuez une recherche dans les champs 
déroulants personnalisés qui contiennent des espaces dans 
leurs choix déroulants. Actuellement, Martus retrouvera 
correctement des communiqués si vous recherchez des valeurs 
dans le menu déroulant en utilisant « Tous les Champs » et 
« contient ». Le problème apparaîtra uniquement si vous 
effectuez une recherche dans un champs déroulant spécifique. 
Ce problème sera réglé dans la prochaine édition du logiciel, 
mais en attendant, si vous personnalisez vos communiqués nous 
vous conseillons de créer des choix déroulants qui ne 
contiennent pas d'espace entre les mots (par contre, si vous 
le désirez, vous pouvez utiliser des tirets entre les mots.) 

- Résolution d'un problème introduit dans la version 2.8 où
  les dates et les fourchettes de dates ayant des valeurs 
  antérieures au 1er janvier 1970 n'étaient pas correctement
  affichées ni mémorisées.
- Intégration des traductions en thaïlandais et en russe de 
  l'interface utilisateur de la version 2.8 du logiciel


G. Modifications de la version 2.7.2 à la version 2.8

Cette publication est disponible en anglais mais aussi en 
d'autres langues, comme indiqué à la page téléchargement du
logiciel sur le site http://www.martus.org .  
Les autres langues seront disponibles par la suite, comme 
packs de langues, à cette même page 
(http://www.martus.org/downloads).

- Ajout de la fonction création de colonnes de la grille de
  types différents (listes déroulantes, cases à cocher, 
  dates et fourchettes de dates)
- La fonction de Recherche Avancée permet désormais à 
  l'utilisateur de spécifier des champs particuliers pour 
  la recherche (incluant les champs personnalisés), en plus
  de la recherche dans la totalité des communiqués. La 
  recherche peut combiner l'utilisation de différents champs 
  et/ou options.
- Amélioration des fonctionnalités d'impression afin de 
  pouvoir imprimer à la fois plusieurs communiqués 
  sélectionnés.
- Ajout de la fonction sauvegarde du/des communiqué(s) dans 
  un fichier html, avec l'option d'inclure ou non les 
  données privées
- Création d'une nouvelle option "Organiser les dossiers" 
  dans le menu pour permettre à l'utilisateur de mettre les 
  dossiers dans n'importe quel ordre.
- Ajout de messages d'avertissement à l'encontre de 
  l'utilisateur si la version de la traduction n'est pas la 
  même que celle du logiciel, et affichage de la date de 
  tout pack de langue existant dans la boîte de dialogue 
  A propos de
- Affichage du logo Martus à l'exécution du programme afin 
  que l'utilisateur sache qu'il est en train de démarrer
- Mise à jour des bibliothèques de cryptage afin de pouvoir 
  utiliser Bouncy Castle Java Cryptography Extension
- Améliorations supplémentaires dans l'affichage et 
  l'impression des langues se lisant de droite à gauche
- Intégration des polices système afin d'afficher à partir 
  du menu certaines langues (comme le népalais)
- Modification du comportement des communiqués afin que les 
  modifications des brouillons comprennent les nouveaux 
  paramètres de personnalisation et de configuration Siège.
- A partir de la version 2.4 du Serveur Martus, ajout d'une 
  fonction qui permet à l'utilisateur du Client Martus de 
  recevoir des nouvelles du serveur de sauvegarde Martus 
  lorsqu'il se connecte (à savoir des messages à propos des 
  nouvelles versions disponibles en téléchargement, ou de la
  durée d'indisponibilité lors de la maintenance du serveur)
- Modification de la méthode de vérification des fichiers 
  téléchargés de MD5 à SHA1
- Mise à jour de la documentation utilisateur en anglais 
  (Guide de Démarrage Rapide et Guide de l'Utilisateur)
- Plusieurs réparations mineures de bogues, clarifications 
  et nettoyages sur les écrans de l'utilisateur.


H. Modifications de la version 2.7 à la version 2.7.2

- Remplacement de la traduction incomplète/inexacte en 
  népalais de la version 2.0.1 de l'interface utilisateur du 
  logiciel Martus par la mise à jour d'un nouveau pack de 
  langue népalaise à la page http://www.martus.org/downloads.   
  Ce pack de langue inclut les traductions en népalais de 
  l'interface utilisateur du logiciel (utilisable pour les 
  versions 2.5 et ultérieures, et comportant 90 % des chaînes 
  traduites en népalais), de l'aide intégrée (version 2.0.1), 
  du Guide de Démarrage Rapide (version 2.0.1), du Guide de 
  l'Utilisateur (version 2.0.1), et du fichier Lisez-moi
  (partiellement traduit jusqu'à la version 2.6).

Sous Windows, pour exécuter le logiciel Martus en népalais 
pour la version 2.7.2 et les versions précédentes, afin que 
les menus puissent s'afficher correctement, vous devez 
effectuer une petite modification dans la commande utilisée 
pour démarrer Martus (à partir de l'invite de commande et de 
n'importe quel raccourci ou alias du bureau/Menu Démarrer 
créé lors de l'installation).  

Pour l'exécuter à partir de la ligne de commande, allez dans 
le répertoire Martus et entrez : 
C:\Martus\bin\javaw.exe -Dswing.useSystemFontSettings=false -jar C:\Martus\Martus.jar 

Pour modifier vos raccourcis, faites un clic droit sur 
l'icône, choisissez Propriétés, et changez la Cible vers :
C:\Martus\bin\javaw.exe -Dswing.useSystemFontSettings=false -jar C:\Martus\Martus.jar 


I. Modifications de la version 2.6 à la version 2.7

Cette publication est disponible en anglais mais aussi en 
d'autres langues, comme indiqué à la page téléchargement du
logiciel sur le site http://www.martus.org .  
Les autres langues seront disponibles par la suite, comme 
packs de langues, à cette même page 
(http://www.martus.org/downloads).

- Ajout de la fonction création de listes déroulantes 
  dans les champs personnalisés uniques (hors grille) 
- Ajout de la fonction création de messages personnalisés 
  expliquant comment entrer des données, et de la création 
  de commentaires/notes qui apparaîtront dans tous les 
  communiqués (p.ex. aide écran)
- Pour les comptes Siège, ajout de la fonction exportation 
  de modèles personnalisés à transmettre aux utilisateurs de 
  terrain, ou utilisateurs puissent exporter leurs propres modèles. 
  Les utilisateurs pourront alors importer les paramètres de   
  personnalisation à partir d'un choix de modèles.
- Chaque siège configuré peut dorénavant être activé ou 
  désactivé pour chaque communiqué créé ou modifié. L'Utilisateur 
  peut également désigner certains comptes Siège à attribuer 
  par défaut à tous les communiqués nouvellement créés.
- Les recherches peuvent désormais scanner – outre la dernière 
  version - les versions précédentes de chaque communiqué. 
- Améliorations supplémentaires dans l'affichage des langues se 
  lisant de droite à gauche
- Inclut la traduction en perse de l'interface utilisateur
- L'exportation vers XML inclut désormais le type champ personnalisé 


J. Modifications de la version 2.5 à la version 2.6

- L'utilisateur peut dorénavant rechercher et afficher 
  l'intégralité du contenu de toutes les versions des 
  communiqués verrouillés présents dans son ordinateur, en 
  cliquant sur le bouton Détails de Communiqué. 
- Possibilité améliorée de choisir sur le serveur soit la 
  récupération de toutes les versions d'un communiqué verrouillé, 
  soit simplement la version la plus récente. L'utilisateur ne 
  possédant qu'une petite unité de disque ou ayant une connexion 
  Internet lente peut choisir de ne récupérer que la version la 
  plus récente des communiqués volumineux.
- Le nom de fichier des pièces jointes est dorénavant inclus 
  dans les recherches.
- Modification de la fonctionnalité d'exportation XML afin de 
  mieux s'adapter aux champs personnalisés et aux différentes 
  versions des communiqués verrouillés.
- Amélioration de l'affichage des langues lues de droite à 
  gauche (comme l'Arabe)
- Inclusion du Guide de l'Utilisateur et du Guide de Démarrage 
  Rapide en Arabe.
- Plusieurs réparations mineures de bogues, clarifications et 
  nettoyages sur les écrans de l'utilisateur.
- Lors de l'utilisation de Martus 2.6 en Arabe sous Mac OS, 
  certains problèmes d'affichage d'écrans peuvent survenir en 
  cours d'exécution.



K. Modifications de la version 2.0.1 à la version 2.5.

- Possibilité supplémentaire de créer de nouvelles versions 
  de communiqués verrouillés de manière à ce que les 
  modifications ou ajouts puissent être effectués sur les 
  communiqués précédemment verrouillés. Dans cette mise à 
  jour de Martus, vous ne pourrez rechercher et afficher 
  que le contenu entier de la version la plus actualisée 
  (vous pourrez afficher le titre/l'id du communiqué/la date 
  de sauvegarde de toute version antérieure stockée dans votre 
  ordinateur, en cliquant sur le bouton Détails de Communiqué).  
- Fonctionnalité supplémentaire pour installer de nouvelles 
  traductions mises à jour à tout moment, suite à une édition 
  complète de Martus. Un "Pack Langues" pour chaque langue (y 
  compris l'anglais) peut contenir la traduction de l'Interface 
  Utilisateur du Client Martus, le Guide de l'Utilisateur, le 
  Guide de Démarrage Rapide, le fichier Lisez-moi et l'aide en 
  ligne. Les packs de langues pourront être téléchargés sur le 
  site web de Martus.
- Plusieurs modifications ont été effectuées pour accélérer le 
  traitement des communiqués et des dossiers (par exemple tri ou 
  déplacement des communiqués) 
- Amélioration des fonctionnalités des champs personnalisés 
  (par ex. taille des colonnes de la grille)
- Nouvelle traduction en Thaï  
- Nouvelle traduction en Arabe 
- Modifications effectuées pour afficher correctement les 
  langues se lisant de Droite à Gauche (comme l'Arabe)
- Plusieurs améliorations pour afficher l'Interface 
  Utilisateur du Client Martus sous Linux.
- Le problème suivant a été résolu : la récupération ou 
  l'importation de communiqués comprenant des pièces jointes 
  très volumineuses risquait de provoquer la fermeture de 
  Martus pour cause de "mémoire insuffisante". Les pièces 
  jointes inférieures à 20 mégaoctets ne sont pas susceptibles 
  de provoquer cette erreur.
- Plusieurs réparations mineures de bogues, clarifications et 
  nettoyages sur les écrans de l'utilisateur.
- Certains problèmes d'affichage d'écran peuvent apparaître 
  dans le programme d'installation Martus 2.5 en Népalais et 
  en Thaï. Du fait de la difficulté de tester toutes les 
  versions de Windows dans ces langues, nous vous serions 
  reconnaissants de nous signaler toute erreur d'affichage à 
  l'installation de Martus.


L. Modifications de la version 2.0. à la version 2.0.1

- Ajout d'une barre de défilement horizontale pour les 
  grilles de champs personnalisés supérieurs à la largeur 
  d'écran.
- Nouvelle traduction en Français
- Mise à jour de la documentation Utilisateur en Russe et en 
  Espagnol avec la fonctionnalité 2.0
- Plusieurs clarifications et nettoyages apportés à la 
  documentation de l'utilisateur en Anglais
- Modifications de l'utilitaire d'installation pour régler 
  les problèmes de mise à niveau sous Windows 98 et ME en 
  langues autres que l'Anglais
- Ajout de l'option multi dossier (disquette) sur le site 
  de téléchargement


M. Modifications de la version 1.5. à la version 2.0.

- Vous pouvez désormais posséder plusieurs comptes de siège, 
  et les paramétrer à l'aide d'une interface améliorée. Cela 
  s'avèrera très utile si vous souhaitez que plusieurs personnes 
  au sein de votre organisme révisent vos communiqués. 
- Les comptes de Siège peuvent désormais envoyer les communiqués 
  à un serveur en lieu et place d'un bureau de terrain dépourvu 
  d'accès Internet.
- Les fonctionnalités Champs Personnalisés ont été étendues de 
  manière à ce que vous puissiez désormais créer des champs 
  personnalisés de types différents (comme date, grille, Oui/Non).  
- Nous utilisons un nouvel utilitaire d'installation Windows en 
  code source libre (NSIS) qui peut être exécuté dans les 
  alphabets non latins.  
- Un nouveau dossier "Communiqués Enregistrés" remplace les 
  fichiers Boîte d'Envoi, Communiqués Envoyés et Brouillons 
  de Communiqués
- Désormais, chaque liste de Communiqués affiche une colonne 
  montrant si oui ou non le communiqué a bien été envoyé au serveur
- La date du dernier enregistrement d'un communiqué s'affiche 
  désormais dans les listes de prévisualisation des communiqués 
  ainsi que dans l'en-tête des communiqués
- Lors de la création/modification d'un communiqué, le bouton 
  "Envoyer" a été remplacé par "Enregistrer Verrouillé" 
- La fonctionnalité d'effacement rapide de Martus 1.5 a été 
  remplacée par deux menus :  "Supprimer Mes Données", qui 
  supprime les communiqués et la clé de ce compte ; et 
  "Supprimer Toutes Données et Désinstaller Martus" qui 
  désinstalle Martus et supprime l'intégralité du répertoire 
  Martus, y compris les données des autres comptes – à 
  n'utiliser qu'en cas d'urgence
- Amélioration des fonctionnalités de sauvegarde de clé – 
  aucun besoin d'effectuer une sauvegarde avant la création 
  des communiqués, mais il vous est rappelé de sauvegarder 
  votre clé plus tard si vous ne l'avez encore fait.
- Pour effectuer une recherché de communiqué, l'on peut 
  utiliser soit l'anglais "and" et "or", soit la traduction 
  de leurs équivalents. Cela permet aux utilisateurs n'ayant 
  pas accès au clavier autochtone de pouvoir effectuer les 
  recherches "et" et "ou".
- Un nouveau bouton "Détails de Communiqué" affiche une 
  identification unique de communiqué ainsi que les comptes 
  de Siège pouvant afficher les données privées de ce communiqué
- A l'impression, vous avez désormais l'option de masquer ou 
  d'inclure les données privées 
- Vous pouvez désormais accéder à l'aide en ligne 
- Vous pouvez insérer un drapeau interdisant les communiqués 
  publics pour raisons de sécurité
- Amélioration de la fonctionnalité Renommer les Dossiers
- Plusieurs réparations mineures de bogues, clarifications 
  et nettoyages sur l'interface utilisateur.

Nous vous recommandons d'exécuter le Logiciel Client Martus 2.0 
avec le Logiciel Serveur Martus version 2.1 ou plus récent. A 
compter de la date de Martus Client version 2.0, tous les 
serveurs de production Martus exécutent la version 2.1, mais 
si vous exécutez votre propre serveur de sauvegarde Martus, 
vous devez procéder à une mise à jour (le logiciel Serveur 
Martus est téléchargeable sur le site www.martus.org).  


N. Modifications de la version 1.2.1 à la version 1.5.

- Les comptes multiples peuvent désormais être paramétrés 
  sur ordinateur. Cela modifie l'écran d'ouverture de session
  de plusieurs manières : vous avez le choix entre ouvrir une 
  session sur un compte déjà paramétré, créer un nouveau compte, 
  ou restaurer à partir d'une sauvegarde de clé. Un 
  sous-répertoire pour chaque compte apparaîtra dans le 
  répertoire Martus.
- L'option de sélection de la langue dans laquelle vous 
  souhaitez exécuter Martus est désormais disponible sur 
  l'écran d'ouverture de session.
- Amélioration des fonctionnalités de sauvegarde de clé, 
  et option de sauvegarder dans plusieurs dossiers de "secret 
  partagé" à distribuer aux amis.  
- Fonctionnalités étendues d'Effacement Rapide pour permettre 
  la suppression de la clé, le nettoyage des communiqués et des 
  données de la clé avant suppression, ainsi que l'option 
  d'effectuer l'Effacement Rapide et de quitter Martus sans 
  invite ni interaction utilisateur.
- Un nouveau Menu Outils (Effacement Rapide, clé et 
  actions Siège)
- Amélioration de la communication avec les serveurs de 
  sauvegarde Martus, et simplification des messages transmis à 
  l'utilisateur sur le statut des serveurs
- Options linguistiques supplémentaires ajoutées à la liste de 
  sélection de communiqué
- Réparation d'un bogue qui empêchait les versions précédentes 
  du programme Martus de récupérer les communiqués à partir 
  d'un serveur de sauvegarde, si ces derniers contenaient des 
  informations publiques avec des lettres non anglaises. Ce 
  bogue n'a pas affecté la sécurité de sauvegarde des 
  communiqués sur un serveur Martus. Le problème 
  n'apparaissait que si les communiqués étaient absents de 
  votre ordinateur, et que vous tentiez de récupérer vos 
  propres communiqués verrouillés, ou ceux de votre bureau 
  de terrain, contenant des données publiques non anglaises 
  (le message reçu dans ce cas était "Erreur pendant la 
  récupération des résumés de communiqués. Certains communiqués 
  du serveur ne s'afficheront pas.") Le bogue n'a affecté 
  la récupération ni  des communiqués entièrement privés, ni 
  des brouillons. Si vous souhaitez récupérer des communiqués 
  contenant des données publiques en langue autre que l'anglais, 
  il vous faut accéder à un serveur de  sauvegarde Martus de 
  production, ou mettre à jour votre propre serveur pour 
  exécuter le Logiciel Serveur Martus version 2.0 ou plus récente. 
- Mise à jour de la version Java vers 1.4.2_03
- Plusieurs réparations mineures de bogues, clarifications 
  et nettoyages sur l'interface utilisateur.


O. Modifications de la version 1.2. à la version 1.2.1

- Introduction de la version Russe, comprenant une version 
  spéciale du programme d'installation en Russe.
- Mise à jour des Guides Utilisateur en Anglais et en 
  Espagnol avec la fonctionnalité 1.2
- Création du script LinuxJavaInstall.txt pour simplifier 
  l'installation Java sur les ordinateurs GNU/Linux.


P. Modifications de la version 1.0.3 à la version 1.2.

- Vous pouvez désormais personnaliser les champs de tous les 
  communiqués créés ultérieurement. Nous conseillons aux seuls 
  utilisateurs "avancés" de personnaliser les champs. Lorsque 
  vous choisissez cette option de menu, vous êtes avertis que 
  si vous n'êtes pas chevronné, vous devez revenir en arrière 
  ou risquer de dérégler votre système. Si vous continuez, 
  vous recevez une liste de balises de champs standards existants, 
  séparées par des points-virgules. Vous pouvez supprimer ceux 
  que vous souhaitez, sauf quatre champs "obligatoires" : 
  entrydate (dateentrée), language (langue), author (auteur), 
  et title (titre). Vous pouvez modifier la séquence. Vous 
  pouvez également insérer vos propres champs personnalisés. 
  Chaque champ personnalisé doit comporter une "balise", 
  composée d'une chaîne de lettres minuscules ASCII suivies 
  d'une virgule, suivies de l'invite écran pouvant contenir 
  un mélange de caractères et d'espaces. Si vous tentez 
  d'enregistrer une chaîne de définition de champ personnalisé 
  qui viole une quelconque règle, vous serez informé qu'elle 
  est invalide, sans pour autant savoir quelle règle vous 
  aurez violée.
- Vous avez désormais l'option de taper une fourchette de 
  dates (période entre deux dates) pour la date d'évènement 
  du communiqué.
- Ajout de l'Interface Utilisateur en Russe, avec traduction 
  de plus de 90% des messages.
- Vous pouvez désormais spécifier un dossier entier à 
  l'exportation.
- Si les ports normaux de Martus sont dans l'incapacité de 
  communiquer avec un serveur de sauvegarde à cause de problèmes 
  tels qu'une configuration de pare-feu, le programme Martus 
  réutilise les ports 80/443.
- La convivialité du processus de sauvegarde de clé a été 
  améliorée.
- La nouvelle version de Java, v 1.4.1_03, est présente 
  pour les téléchargements et les images CD. 
- Plusieurs réparations mineures de bogues, clarifications et 
  nettoyages sur l'interface utilisateur, notamment une aide 
  accrue pour une utilisation sans souris.


Q. Modifications de la version 1.0.2 à la version 1.0.3

- Versions Martus compatibles Linux et Mac.
- Si vous avez installé une ancienne version du logiciel Martus, 
  vous pouvez en télécharger une version réduite sans avoir 
  besoin de télécharger le fichier intégral contenant tout Java.
- Les communiqués peuvent être exportés du programme en format 
  texte XML.
- Vérification de la version compatible Java pendant 
  l'installation et le démarrage.
- Manipulation plus rapide des communiqués avec pièces jointes.
- Passage au curseur d'attente pendant les opérations de 
  longue durée.
- Sous Windows, vous pouvez désormais afficher les pièces 
  jointes.
- Modifications du contrat de licence compatible GNU GPL, 
  simplifiant l'avertissement à l'utilisateur lors de l'emprunt 
  d'un code pour une utilisation extérieure au logiciel Martus, 
  et étendant la couverture aux applications du serveur Martus.
- Vous pouvez désormais recevoir des messages du serveur Martus, 
  y compris une déclaration de conformité au serveur.
- Une nouvelle boîte de dialogue de Bienvenue s'affiche au 
  démarrage du programme, indiquant s'il s'agit d'une version 
  officielle de ce programme.
- Simplification de l'installation du logiciel Martus pendant le 
  téléchargement du programme d'installation. Le programme de 
  vérification Jar est désormais disponible en téléchargement séparé.
- Une nouvelle fonctionnalité d'Effacement Rapide supprimera toutes 
  les copies locales des communiqués de votre disque dur.
- Réparation d'un bogue susceptible de corrompre un communiqué en 
  cas de récupération depuis un serveur, suivie de la modification 
  d'une copie par ajout d'une nouvelle pièce jointe.
- Mise à jour de quelques chaînes de traduction en Espagnol.
- Résolution d'un problème de fenêtre nécessitant la zone de 
  saisie courante en cas de dépassement de délai.
- Placement de la fenêtre de dialogue de début de session 
  au-dessus des autres, et ajout dans la barre des tâches.
- Masquage de la fenêtre principale pendant l'édition.
- Ajout d'une bibliothèque crypto à jour et d'une nouvelle version 
  de Java, v 1.4.1_02, supportant la frappe de caractères étrangers 
  à l'aide du clavier numérique et réparant une perte de mémoire 
  qui vide lentement la mémoire disponible.
- Ajout des touches rapides Windows standard aux endroits cruciaux 
  comme Suppr et Control C, X, V et A.


R. Modifications de la version 1.0.1 à la version 1.0.2

- En choisissant de modifier un communiqué verrouillé une copie 
  est effectuée, mais si l'original comportait des pièces jointes, 
  le système pouvait devenir instable pour finir par endommager 
  la copie et/ou l'original. Ce problème a été corrigé.
- Permet la répartition CD du contenu par défaut pouvant précharger 
  le champ Détails de chaque communiqué. Si DefaultDetails.txt est 
  sur le CD, il sera copié dans le répertoire Martus à l'installation. 
  Puis, si DefaultDetails.txt est trouvé dans le répertoire Martus 
  à la création d'un nouveau compte, il donnera à l'utilisateur 
  la possibilité de l'utiliser comme contenu de Détails par défaut 
  pour tout communiqué créé.
- Réorganisation du code et de la structure du programme interne 
  pour en faciliter l'entretien.


S. Modifications de la version 1.0. à la version 1.0.1

- Demande en cours de l'installation du programme si 
  l'utilisateur souhaite que les fichiers du programme Martus, 
  de désinstallation Martus et de documentation soient ajoutés 
  au menu démarrer de Windows.
- Mise à jour de l'Environnement Runtime de Java version 1.4.1 
  à la version 1.1.1_01, améliorant ainsi quelques éléments de 
  sécurité de Java.
- Vérification de la copie sur disque dur pendant l'installation, 
  de l'ensemble des sources tierces indispensables, licences Runtime, 
  fichiers de documentation et programmes Winsock pour Windows 95.
- Correction d'erreurs dans la Case Au Sujet De… et dans l'avis 
  de copyright de la documentation.


T. Instructions particulières de mise à jour de Winsock en cas 
   de problème d'exécution du logiciel Martus sous Windows 95 :

Java nécessite la bibliothèque Microsoft Winsock 2.0. Votre 
système est probablement déjà équipé de la version Winsock 2.0 
ou plus récente. Il reste cependant possible que certains systèmes 
Microsoft Windows 95 aient une version antérieure de Winsock.

Pour vérifier votre version de Winsock, faites une recherche 
"winsock.dll". Puis choisissez "Propriétés" dans le menu Fichier 
et cliquez sur l'onglet Version.

Si votre ordinateur sous Microsoft Windows 95 n'a pas la version 
Winsock 2.0 ou plus récente, vous pouvez exécuter le programme 
d'installation Winsock 2.0 situé dans le répertoire Martus\Win95. 

Si vous êtes curieux, l'URL suivante contient les renseignements 
contribuant à déterminer si les composants de Winsock 2.0 sont 
installés sur une plateforme Microsoft Windows 95 :

http://support.microsoft.com/support/kb/articles/Q177/7/19.asp


