# 🚀 NetWerkers - Gestion Intelligente de Projets & Événements

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**NetWerkers** est une application desktop moderne développée en JavaFX, conçue pour simplifier la gestion des projets, des missions et des événements. Grâce à l'intégration de l'intelligence artificielle (Gemini) et de services tiers (Google Calendar, DocuSign), elle offre une expérience utilisateur fluide et productive.

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

NetWerkers répond au besoin croissant d'outils collaboratifs centralisés. Que ce soit pour organiser un événement d'entreprise, assigner des missions à des collaborateurs ou gérer des documents légaux via signature électronique, NetWerkers combine puissance et simplicité.

**Le problème qu'il résout :** La dispersion des outils de gestion. NetWerkers regroupe la planification, la communication et l'IA en une seule interface élégante.

---

## ✨ Fonctionnalités Clés

- **🤖 Assistant IA (Gemini) :** Génération automatique de descriptions d'événements, aide à la rédaction de missions et chat intelligent.
- **📅 Gestion d'Événements :** Création, modification et suivi d'événements avec intégration de calendriers.
- **🛠️ Gestion de Missions (TSK) :** Système complet de tickets pour assigner et suivre l'avancement des tâches.
- **🔐 Authentification Sécurisée :** Login via Google OAuth2 et protection par ReCaptcha.
- **📄 Signature Électronique :** Intégration avec DocuSign pour la validation officielle des documents.
- **📊 Dashboard Dynamique :** Visualisation en temps réel des statistiques et de l'état des projets.

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
   git clone https://github.com/votre-utilisateur/netwerkers.git
   cd netwerkers
   ```

2. **Configurer la base de données :**
   - Créez une base de données MySQL nommée `netwerkers_db`.
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
- **Événements :** Accédez au module "Events" pour planifier vos activités.
- **Missions :** Utilisez le module "Missions" pour gérer le flux de travail de votre équipe.

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

Développé avec ❤️ par l'équipe NetWerkersq
