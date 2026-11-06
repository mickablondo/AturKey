# Exemple Spring Boot

Cet exemple montre le rôle du starter dans une application :

1. La dépendance `aturkey-spring-boot-starter` inclut `aturkey-core` et
   enregistre l'auto-configuration AturKey de Spring Boot.
2. Au démarrage, si Spring Boot a configuré JPA et qu'un
   `EntityManagerFactory` est disponible, l'auto-configuration fournit un bean
   `AturKeyUpdater` injectable, utilisant l'`EntityManager` transactionnel de
   Spring.
3. `CustomerService` injecte ce bean et appelle explicitement `update` depuis
   une méthode annotée `@Transactional`. Le starter n'intercepte pas les
   appels et ne met pas à jour les entités de lui-même.

L'entité `Customer` marque son champ `email` unique avec `@AturKey`. L'updater
utilise ce champ pour retrouver la ligne existante et y copie les valeurs
reçues, sans remplacer l'identifiant ni la version. Le profil par défaut
utilise une base H2 en mémoire et ne nécessite pas Docker.

## Construire et lancer l'application

```shell
mvn -pl examples/example-spring-boot -am package
java -jar examples/example-spring-boot/target/example-spring-boot-1.0.0.jar
```

## Lancer les tests

Avec Java 21 :

```shell
mvn -pl examples/example-spring-boot -am verify
```

`CustomerServiceTest` vérifie que Spring a créé le bean updater et qu'un client
peut être mis à jour à partir de son adresse e-mail. Cet exemple ne fournit
pas de point d'accès HTTP ni d'interface utilisateur : le test est la
démonstration de bout en bout.
