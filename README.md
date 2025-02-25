# A-starter-java
Here are instructions to launch the java part of the starter, described in [this paper](https://dzone.com/articles/a-starter-for-a-distributed-multi-language-analyti).
## Intellij Idea
### Install dependencies
+ In the Gradle tab, click `Reload all Gradle projects`(wheel) to install dependencies.
+ For the composer to use its gRPC client, go to the Gradle tab, then `A-starter-java -> composer-core -> Tasks -> other -> generateProto`. gRPC files will appear in `composer-core/build/generated/source/proto/main`. 
### Run configurations
The app follows hexagonal architecture. Every inbound adapter has its own `public static void main` method and an Idea run configuration. These configurations are as follows.
#### Composer dev
+ Name: Composer_dev
+ JDK: java 18,
+ Module name: -cp `A-starter-java.composer-adapter.main`,
+ Main class name: `org.composer.adapter.ComposerAdapter`,
+ Working directory: `<absolute-path-to-A-starter-java>/A-starter-java`,
+ Environment variables: `SPRING_PROFILES_ACTIVE=dev`.
#### Composer GraphQL dev
+ Name: Composer_GraphQL_dev
+ java 18,
+ Module name: -cp `A-starter-java.composer-graphql-adapter.main`,
+ Main class name: `org.composer.adapter.GraphqlAdapter`,
+ Working directory: `<absolute-path-to-A-starter-java>/A-starter-java`,
+ Environment variables: `SPRING_PROFILES_ACTIVE=dev`.

## Docker
For every inbound adapter, 
+ Go to the Gradle tab, then `A-starter-java -> <inbound adp name> -> Tasks -> build -> bootJar`, 
+ From `A-starter-main` (NOT `A-starter-java`!) folder, run `docker compose up <service name>`.

For example, to build and run a container for the Composer GraphQL app, we first build the app's bootable jar. 
Then, go to `A-starter-main` (NOT `A-starter-java`!) folder and run `docker compose up composer-graphql`.