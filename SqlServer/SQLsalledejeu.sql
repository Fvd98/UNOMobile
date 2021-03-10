CREATE DATABASE SALLEDEJEU
GO

USE SALLEDEJEU
GO

CREATE TABLE JOUEURS(
	NomUtilisateur VARCHAR(50) PRIMARY KEY,
	Nom VARCHAR(50) NOT NULL,
	Prenom VARCHAR(50) NOT NULL,
	DateInscription DATE NOT NULL,
	MotDePasse VARCHAR(50) NOT NULL,
	NbPartieTotale INT DEFAULT 0,
	NbPartieGagnee INT DEFAULT 0,
	NbPartiePerdue INT DEFAULT 0
)
GO

CREATE TABLE COULEURS_CARTES(
	IDCouleur INT PRIMARY KEY,
	NomCouleur VARCHAR(10) NOT NULL
)
GO

CREATE TABLE TYPES_CARTES(
	IDType INT PRIMARY KEY,
	NomType VARCHAR(15) NOT NULL
)

CREATE TABLE PARTIES(
	IDPartie INT IDENTITY PRIMARY KEY,
	Joueur1 VARCHAR(50) FOREIGN KEY REFERENCES JOUEURS(NomUtilisateur),
	Joueur2 VARCHAR(50) FOREIGN KEY REFERENCES JOUEURS(NomUtilisateur),
	DatePartie DATE,
	Resultat VARCHAR(25) DEFAULT 'Aucun',
	JoueurTour VARCHAR(50) FOREIGN KEY REFERENCES JOUEURS(NomUtilisateur) NULL
)
GO

CREATE TABLE CARTES(
	IDCarte INT IDENTITY(0,1) PRIMARY KEY, 
	TypeCarte INT FOREIGN KEY REFERENCES TYPES_CARTES(IDType), 
	CouleurCarte INT FOREIGN KEY REFERENCES COULEURS_CARTES(IDCouleur)
)
GO

/* Sauvegarde l'emplacement de chaque carte de chaque partie sauvegardée */
CREATE TABLE SAUVEGARDES(
	IDSauvegarde INT IDENTITY PRIMARY KEY, 
	IDCarte INT FOREIGN KEY REFERENCES CARTES(IDCarte),
	IDPartie INT FOREIGN KEY REFERENCES PARTIES(IDPartie),
	NomUtilisateur Varchar(50) FOREIGN KEY REFERENCES JOUEURS(NomUtilisateur) NULL
)
GO

INSERT INTO TYPES_CARTES VALUES
	(0,'C0'),
	(1,'C1'),
	(2,'C2'),
	(3,'C3'),
	(4,'C4'),
	(5,'C5'),
	(6,'C6'),
	(7,'C7'),
	(8,'C8'),
	(9,'C9'),
	(10,'CPlus2'),
	(11,'CPlus4'),
	(12,'CSkip'),
	(13,'CReverse'),
	(14,'CCouleur')
GO

INSERT INTO COULEURS_CARTES VALUES
	(0,'rouge'),
	(1,'vert'),
	(2,'bleu'),
	(3,'jaune'),
	(4,'noir')
GO

INSERT INTO CARTES VALUES
	(0,0),
	(0,1),
	(0,2),
	(0,3),
	(1,0),(1,0),
	(1,1),(1,1),
	(1,2),(1,2),
	(1,3),(1,3),
	(2,0),(2,0),
	(2,1),(2,1),
	(2,2),(2,2),
	(2,3),(2,3),
	(3,0),(3,0),
	(3,1),(3,1),
	(3,2),(3,2),
	(3,3),(3,3),
	(4,0),(4,0),
	(4,1),(4,1),
	(4,2),(4,2),
	(4,3),(4,3),
	(5,0),(5,0),
	(5,1),(5,1),
	(5,2),(5,2),
	(5,3),(5,3),
	(6,0),(6,0),
	(6,1),(6,1),
	(6,2),(6,2),
	(6,3),(6,3),
	(7,0),(7,0),
	(7,1),(7,1),
	(7,2),(7,2),
	(7,3),(7,3),
	(8,0),(8,0),
	(8,1),(8,1),
	(8,2),(8,2),
	(8,3),(8,3),
	(9,0),(9,0),
	(9,1),(9,1),
	(9,2),(9,2),
	(9,3),(9,3),
	(10,0),(10,0),
	(10,1),(10,1),
	(10,2),(10,2),
	(10,3),(10,3),
	(11,0),(11,0),
	(11,1),(11,1),
	(11,2),(11,2),
	(11,3),(11,3),
	(12,0),(12,0),
	(12,1),(12,1),
	(12,2),(12,2),
	(12,3),(12,3),
	(13,4),
	(13,4),
	(13,4),
	(13,4),
	(14,4),
	(14,4),
	(14,4),
	(14,4)
GO

CREATE TRIGGER insert_sauvegarde ON SAUVEGARDES INSTEAD OF INSERT
AS BEGIN
	DECLARE @IdPartieActuel INT
	DECLARE @J1 VARCHAR(50)
	DECLARE @J2 VARCHAR(50)

	SET @IdPartieActuel = (SELECT TOP 1 IDPartie FROM INSERTED)
	SET @J1 = (SELECT Joueur1 FROM PARTIES WHERE IDPartie = @IdPartieActuel)
	SET @J2 = (SELECT Joueur2 FROM PARTIES WHERE IDPartie = @IdPartieActuel)

	IF EXISTS(SELECT IDPartie FROM SAUVEGARDES WHERE IDPartie IN(SELECT IDPartie FROM PARTIES WHERE (Joueur1 = @J1 AND Joueur2 = @J2) OR (Joueur1 = @J2 AND Joueur2 = @J1)))
	BEGIN
		DELETE FROM SAUVEGARDES WHERE IDPartie IN(SELECT IDPartie FROM PARTIES WHERE (Joueur1 = @J1 AND Joueur2 = @J2) OR (Joueur1 = @J2 AND Joueur2 = @J1))
	END
	INSERT INTO SAUVEGARDES SELECT IDCarte,IDPartie,NomUtilisateur FROM INSERTED
END
GO

INSERT INTO JOUEURS VALUES('FRANCIS', 'Vermette-David', 'Francis', GETDATE(), '123', 0, 0, 0)
INSERT INTO JOUEURS VALUES('MARIKA', 'Groulx', 'Marika', GETDATE(), '123', 0, 0, 0)
INSERT INTO JOUEURS VALUES('KATHASTIA', 'Cadieux', 'Chantal', GETDATE(), '123', 0, 0, 0)
GO

