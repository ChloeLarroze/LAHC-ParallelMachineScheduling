# LAHC-ParallelMachineScheduling
Implementation of the Late Acceptance Hill Climbing metaheuristic for unrelated parallel machine scheduling problems with release dates and sequence-dependent setup times (R|rⱼ, sᵢⱼₖ|Cₘₐₓ).

## Prérequis
- **Java 11 ou supérieur** avec JavaFX (le projet utilise Zulu JDK 25 qui inclut JavaFX)
- Terminal/ligne de commande

## Lancement de l'Application

### Interface Graphique (recommandé)

```bash
cd LAHC-ParallelMachineScheduling
./build.sh
```

L'interface graphique s'ouvre automatiquement.

### Ligne de Commande (pour main)

```bash
./main.sh
```

Compile et exécute le programme en mode console avec une instance aléatoire.

### Ligne de Commande (pour tests)

```bash
./tests.sh
```

Donne la possibilité d'exécuter les tests unitaires.


## Utilisation de l'Interface
### 1. Choisir le Type d'Instance

Trois options disponibles dans le menu déroulant :
- **Instance aléatoire** : Génère automatiquement une instance avec 5-10 jobs et 2-4 machines
- **Depuis un fichier** : Charge l'instance depuis `resources/Instance.txt` (format spécifique)
- **Entrée manuelle** : Non implémenté actuellement

### 2. Lancer la Résolution
1. Cliquez sur **"Lancer la résolution"**
2. L'algorithme s'exécute (généralement < 1 seconde pour petites instances)
3. Les résultats apparaissent dans la zone de texte :
   - Nombre de jobs et machines
   - Makespan final (temps total de production)
   - Ordonnancement par machine

### 3. Visualiser le Diagramme de Gantt
1. Après résolution, le bouton **"Afficher le Gantt"** devient actif
2. Cliquez dessus pour ouvrir une nouvelle fenêtre
3. Le diagramme montre :
   - **Blocs gris hachurés** : temps de setup (préparation entre jobs)
   - **Blocs colorés** : temps de traitement des jobs
   - **Blocs blancs pointillés** : temps d'inactivité (attente de release date)
   - **Flèches colorées** : dates de release (rⱼ) des jobs
   - **Légende** : correspondance couleur-job


## Modifier les Paramètres
### Instance Aléatoire

Dans `MainFX.java`, lignes 73-74 :
```java
int randomJobNumber = 5 + (int)(Math.random() * 6); // 5-10 jobs
int randomMachineNumber = 2 + (int)(Math.random() * 3); // 2-4 machines
```

Ajustez les valeurs pour tester différentes tailles d'instances.

### Algorithme LAHC

Dans `algo/metaheuristic/LAHCMetaheuristic.java` :
```java
private int historyLength; // Taille de la liste L (défaut: jobs × machines)
private int maxIterations; // Nombre max d'itérations sans amélioration
```

Augmenter `historyLength` → plus d'exploration mais plus lent
Augmenter `maxIterations` → potentiellement meilleures solutions mais plus long

### Format de Fichier d'Instance

Le fichier `resources/Instance.txt` suit ce format :
```
<nombre_jobs> <nombre_machines>
<p_00> <p_01> ... <p_0m>  # Temps de traitement job 0
<p_10> <p_11> ... <p_1m>  # Temps de traitement job 1
...
<r_0> <r_1> ... <r_n>     # Release dates
<s_000> <s_001> ...       # Setup times (matrice 3D aplatie)
```

Voir `utils/InstanceReader.java` pour le parsing détaillé.

## Problèmes Courants

### L'interface ne s'ouvre pas
- Vérifiez que JavaFX est installé : `java --list-modules | grep javafx`
- Utilisez un JDK qui inclut JavaFX (Zulu, Liberica, Bellsoft)

### Erreur "cannot find symbol"
- Recompilez : `rm -rf bin && ./gui.sh`
- Vérifiez que tous les fichiers .java sont présents

### Le Gantt semble incorrect
- Vérifiez que `Schedule.calculateSchedule()` utilise bien la version corrigée (setup ne peut pas commencer avant release date)

## Pour Aller Plus Loin

**Tester sur des instances spécifiques :**
1. Créez un fichier dans `resources/`
2. Modifiez `MainFX.java` ligne 68 pour pointer vers votre fichier
3. Sélectionnez "Depuis un fichier" dans l'interface


> Pour toute question sur l'implémentation ou l'utilisation du code, référez-vous aux commentaires dans le code source ou consultez le rapport complet du projet.

---

**Version :** 1.0  
**Dernière mise à jour :** Octobre 2025  
**Testé sur :** macOS (Intel), Java 25 + JavaFX
