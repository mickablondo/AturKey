# AturKey

AturKey permet de mettre à jour une entité JPA à partir d'un champ métier
plutôt que de son identifiant. Cette bibliothèque Jakarta Persistence est
portable et ne dépend ni de Spring ni d'Hibernate à l'exécution.

## Prérequis

- Java 21 ou ultérieur
- Une implémentation de Jakarta Persistence fournie par l'application

## Installation

```xml
<dependency>
    <groupId>io.github.mickablondo</groupId>
    <artifactId>aturkey-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Utilisation

Annotez un champ de l'entité avec `@AturKey`, puis appelez le service de résolution dans
une transaction active :

```java
@Entity
public class Customer {
    @Id
    private Long id;

    @Version
    private long version;

    @AturKey
    @Column(unique = true, nullable = false)
    private String email;

    private String name;
}

Customer updated = new AturKeyUpdater(entityManager).update(customer);
```

AturKey recherche l'entité existante par `email`, copie les valeurs reçues sur
cette entité gérée et préserve son identifiant et sa version. Par défaut, les
valeurs nulles de l'objet reçu remplacent les valeurs existantes. Utilisez
`@AturKey(ignoreNullLikeAtur = true)` pour conserver les valeurs existantes
lorsque les champs correspondants de l'objet reçu sont nuls. Une clé nulle (comme Atur, hihi), une entité
introuvable, plusieurs résultats ou plusieurs champs `@AturKey` provoquent une
exception explicite.

La clé métier devrait être indexée et soumise à une contrainte d'unicité en
base. La détection applicative des doublons ne remplace pas cette contrainte
en cas d'écritures concurrentes.

## Compilation et tests

Lancez les tests avec Java 21 :

```shell
mvn verify
```

## Starter Spring Boot

L'exemple Spring Boot complet se trouve dans
[`examples/example-spring-boot`](examples/example-spring-boot/README.md).

Dans une application Spring Boot, ajoutez
`io.github.mickablondo:aturkey-spring-boot-starter` en plus de votre
implémentation JPA. Le starter inclut `aturkey-core` et enregistre son
auto-configuration auprès de Spring Boot. Au démarrage, si un
`EntityManagerFactory` JPA est disponible, Spring crée un bean
`AturKeyUpdater` qui utilise l'`EntityManager` transactionnel de l'application.
Si votre application déclare déjà son propre bean `AturKeyUpdater`, le starter
ne le remplace pas.

```xml
<dependency>
    <groupId>io.github.mickablondo</groupId>
    <artifactId>aturkey-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Injectez ensuite le bean dans un service et appelez-le dans une méthode
transactionnelle :

```java
@Service
class CustomerService {
    private final AturKeyUpdater updater;

    CustomerService(AturKeyUpdater updater) {
        this.updater = updater;
    }

    @Transactional
    Customer update(Customer incoming) {
        return updater.update(incoming);
    }
}
```

Le starter se contente de fournir le bean injectable : il n'intercepte pas les
appels aux dépôts et ne déclenche pas automatiquement les mises à jour. C'est
votre application qui décide quand appeler `update`. Consultez
[l'exemple Spring Boot](examples/example-spring-boot/README.md) pour voir la
configuration complète et son test.


## 🔑 Que la clé soit avec toi… Atur s’occupe du reste !