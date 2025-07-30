# LEVEL Plugin

[![Version](https://img.shields.io/badge/version-v1.13-blue.svg)](https://github.com/your-repo/LEVEL)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21+-green.svg)](https://www.spigotmc.org/)
[![Java](https://img.shields.io/badge/java-17+-orange.svg)](https://adoptium.net/)

## 📋 Description

**LEVEL** est un plugin Minecraft avancé axé sur la progression des joueurs, offrant un système de niveaux complet avec des fonctionnalités étendues pour enrichir l'expérience de jeu sur votre serveur.

## ✨ Fonctionnalités Principales

### 🎯 Système de Progression Avancé
- **Système de niveaux dynamique** avec XP configurable et niveaux maximum personnalisables
- **Sources d'expérience multiples** :
  - 🪓 Minage (minerais, bois) avec XP basée sur la rareté
  - 🌾 Agriculture (cultures matures, élevage)
  - 🎣 Pêche et activités diverses
  - ⚔️ Combat contre les mobs
- **Multiplicateurs d'expérience** :
  - Bonus weekend (x1.5)
  - Bonus nocturne (x1.2)
  - Bonus de groupe (x1.1 quand plusieurs joueurs sont proches)

### 🎮 Interface Utilisateur
- **BossBar personnalisable** avec affichage du territoire et de l'heure
- **TabList améliorée** avec niveaux des joueurs
- **NameTags dynamiques** affichant les niveaux
- **ActionBar** pour les notifications d'XP en temps réel
- **Menu de statistiques** interactif via `/stats`

### 🌍 Fonctionnalités Serveur
- **Accès aux dimensions basé sur le niveau** (Nether niveau 20+, End niveau 40+)
- **Intégration PlaceholderAPI** pour l'affichage dans d'autres plugins
- **Support Discord** avec notifications de connexion
- **Commandes automatiques** lors des montées de niveau (permissions LuckPerms)

### 🔧 Configuration Avancée
- **Système de debug complet** avec logging détaillé
- **Messages personnalisables** pour tous les événements
- **Sons configurables** pour les montées de niveau
- **Territoires et régions** personnalisables (WorldGuard)

## 📦 Installation

### Prérequis
- **Java 17+** (obligatoire)
- **Spigot/Paper 1.21+**
- **Maven** (pour la compilation)

### Dépendances
- **PlaceholderAPI** (optionnel, recommandé)
- **DatabaseAPI** (optionnel)
- **WorldGuard** (optionnel, pour les régions)
- **LuckPerms** (optionnel, pour les permissions automatiques)

### Installation Rapide
1. Téléchargez le fichier `LEVEL-1.13-shaded.jar` depuis les releases
2. Placez-le dans le dossier `plugins/` de votre serveur
3. Redémarrez le serveur
4. Configurez le plugin via `plugins/LEVEL/config.yml`

## 🔨 Compilation

```bash
# Cloner le repository
git clone https://github.com/your-repo/LEVEL.git
cd LEVEL

# Compiler avec Maven
mvn clean package

# Le JAR sera généré dans target/LEVEL-1.13-shaded.jar
```

## 🎮 Commandes

| Commande | Description | Permission |
|----------|-------------|------------|
| `/level` | Affiche votre niveau et expérience | `level.use` |
| `/stats [reset] [joueur]` | Ouvre le menu de statistiques | `level.stats` |

## ⚙️ Configuration

### Configuration Principale (`config.yml`)

```yaml
# Système de niveaux
levels:
  initialExp: 100              # XP pour passer niveau 1→2
  expIncrementPercent: 30      # Augmentation par niveau (%)
  maxLevel: 100                # Niveau maximum
  
# Multiplicateurs d'expérience
expMultipliers:
  weekend: 1.5                 # Bonus weekend
  nightTime: 1.2               # Bonus nocturne
  groupBonus: 1.1              # Bonus de groupe

# Accès aux dimensions
dimensionRequirements:
  nether: 20                   # Niveau requis pour le Nether
  end: 40                      # Niveau requis pour l'End
```

### Sources d'Expérience Configurables

Le plugin permet de configurer l'XP pour :
- **Minerais** : Coal (3 XP), Iron (5 XP), Diamond (20 XP), Ancient Debris (50 XP)
- **Agriculture** : Wheat, Carrots, Potatoes (3 XP), Pumpkin (5 XP)
- **Combat** : Zombie (20 XP), Enderman (50 XP), Wither Skeleton (75 XP)

## 🐛 Debug et Diagnostic

Le plugin inclut un système de diagnostic avancé :

```bash
# Exécuter le diagnostic automatique
./diagnostic_plugin.bat
```

### Options de Debug
```yaml
debug:
  enabled: false               # Mode debug général
  startup_diagnostics: true    # Diagnostics au démarrage
  player_actions: false        # Log des actions joueurs
  database_queries: false      # Log des requêtes DB
```

## 📊 Intégrations

### PlaceholderAPI
- `%level_level_{player}%` - Niveau du joueur
- `%level_exp_{player}%` - Expérience actuelle
- `%level_progress_{player}%` - Progression vers le niveau suivant

### LuckPerms
- Attribution automatique de permissions `lvl.{niveau}` lors des montées de niveau

## 🔄 Changelog

### v1.13 (2025-07-30)
- ✅ **Correction critique** : Résolution de l'erreur "invalid target release: 21"
- 🔧 **Compatibilité** : Migration vers Java 17 pour une meilleure compatibilité
- 🚀 **Performance** : Optimisation du système de compilation Maven
- 📝 **Documentation** : Mise à jour complète du README et guides d'installation

### Corrections Techniques
- Changement de la version Java de 21 vers 17 dans `pom.xml`
- Résolution des problèmes de compilation Maven
- Amélioration du système de diagnostic automatique

## 👥 Auteurs

- **Rudy** - Développeur principal
- **Shirito** - Développeur et maintenance

## 📄 Licence

Ce projet est sous licence privée. Tous droits réservés.

## 🆘 Support

Pour obtenir de l'aide ou signaler des bugs :
1. Vérifiez les logs du serveur pour les erreurs
2. Utilisez le script de diagnostic : `./diagnostic_plugin.bat`
3. Consultez les guides de dépannage dans le repository

---

*Plugin LEVEL - Système de progression avancé pour Minecraft 1.21+*