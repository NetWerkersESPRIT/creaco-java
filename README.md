# 🚀 Creaco - Content Management System

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9-red.svg)](https://maven.apache.org/)

**Creaco** est une application desktop moderne développée en JavaFX, conçue pour accompagner les créateurs de contenu dans leur processus créatif et optimiser la collaboration au sein de leurs équipes. Grâce à l'intégration de l'intelligence artificielle (Gemini) et de services de productivité (Google Calendar, DocuSign), Creaco rend la production de contenu plus fluide et professionnelle.

---

## 📋 Table des Matières

- [À propos du Projet](#-à-propos-du-projet)
- [Fonctionnalités Clés](#-fonctionnalités-clés)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Utilisation](#-utilisation)
- [Technologies Utilisées](#-technologies-utilisées)
- [Contribution](#-contribution)
- [Licence](#-licence)

---

## 🌟 À propos du Projet

Creaco répond au besoin croissant d'outils collaboratifs dédiés à la création. Qu'il s'agisse de planifier des campagnes de contenu, d'assigner des tâches de production à des collaborateurs ou de gérer des contrats via signature électronique, Creaco centralise tout votre workflow créatif.

**Le problème qu'il résout :** La fragmentation des outils de création. Creaco regroupe la planification stratégique, la gestion opérationnelle et l'assistance par IA dans une interface unique et inspirante.

---

## ✨ Fonctionnalités Clés

- **🤖 Assistant Créatif IA (Gemini) :** Génération de scripts, aide à la rédaction de posts et brainstorming intelligent.
- **📅 Calendrier Éditorial :** Planification des publications et suivi des événements de contenu avec intégration de calendriers.
- **🛠️ Workflow de Production (TSK) :** Système d'assignation des tâches de création (montage, rédaction, design) pour une équipe coordonnée.
- **🔐 Authentification Sécurisée :** Accès sécurisé via Google OAuth2 pour protéger vos contenus.
- **📄 Signature Électronique :** Validation des contrats de partenariat et droits d'auteur via DocuSign.
- **📊 Analytics & Dashboard :** Suivi des performances et de l'avancement des projets créatifs.

---

## ⚙️ Installation

### Prérequis
- **Java JDK 21** ou supérieur.
- **Maven 3.8+**.
- **MySQL 8.0+**.
- Un IDE (IntelliJ IDEA recommandé, ou VS Code).

### Étapes d'installation

1. **Cloner le repository :**
   ```bash
   git clone https://github.com/votre-utilisateur/creaco-java.git
   cd creaco-java
   ```

2. **Configurer la base de données :**
   - Créez une base de données MySQL nommée `creaco_db`.
   - Importez le fichier `.sql` (si disponible) ou laissez Hibernate générer les tables.

3. **Installer les dépendances :**
   ```bash
   mvn clean install
   ```

---

## 🛠️ Configuration

Pour faire fonctionner les services externes (IA, Mail, OAuth), vous devez configurer vos clés API.

1. Copiez le fichier d'exemple :
   ```bash
   cp config.properties.example config.properties
   ```
2. Éditez `config.properties` avec vos informations :
   ```properties
   GEMINI_API_KEY=votre_cle_ici
   RECAPTCHA_SITE_KEY=votre_cle_ici
   # ... autres clés
   ```

---

## 🚀 Utilisation

Pour lancer l'application, exécutez la commande suivante :

```bash
mvn javafx:run
```

### Navigation
- **Connexion :** Utilisez vos identifiants ou le bouton Google Login.
- **Contenus :** Accédez au module "Events" pour gérer votre calendrier éditorial.
- **Workflow :** Utilisez le module "Missions" pour la coordination d'équipe.

---

## 🛠 Technologies Utilisées

- **Frontend :** JavaFX (FXML, CSS) pour une interface riche et responsive.
- **Backend :** Java 21, Maven pour la gestion de projet.
- **Base de données :** MySQL avec JDBC.
- **IA :** Google Gemini API.
- **Services Cloud :** Google OAuth2, DocuSign API, APIVerve.

---

## 🤝 Contribution

Les contributions sont les bienvenues ! Pour contribuer :

1. Forkez le projet.
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/AmazingFeature`).
3. Committez vos modifications (`git commit -m 'Add some AmazingFeature'`).
4. Pushez vers la branche (`git push origin feature/AmazingFeature`).
5. Ouvrez une **Pull Request**.

---

## 📄 Licence

Distribué sous la licence **MIT**. Voir le fichier `LICENSE` pour plus d'informations.

---

Développé avec ❤️ par l'équipe NetWerkers
