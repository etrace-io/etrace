# For Maintainers of this project

## Set up your local development environment

1. install [Docker](https://www.docker.com/products/docker-desktop)
2. run docker compose file: `docker-compose up`. This will start up following images:

- kafka
- zookeeper
- hbase
- hdfs

3. to remove all local images, run `docker-compose down`.

## Deploy to OSSRH (Open Source Software Repository Host)

Following contents will lead you to install and configure your settings to deploy this project to OSSRH,
who will automatically synchronize your repositories to Central Maven Repository.

#### Step one: How to set up `PGP` signature to deploy to Central Maven Repository

Take this article for [reference](https://central.sonatype.org/pages/working-with-pgp-signatures.html#distributing
-your-public-key):

1. install [gnupg](https://formulae.brew.sh/formula/gnupg): `brew install gpg` .
2. generate a key pair: `gpg --gen-key`. In the interactive, it require you a passphrase. Write it down, you'll sign
 your files by inputting this passphrase.(also can be set in maven setting.xml to pass the input)
3. distribute your key to public. some available key servers are 'keys.gnupg.net', 'pool.sks-keyservers.net'.

```
gpg --keyserver hkp://keys.gnupg.net --send-keys YOUR_KEY_ID
gpg --keyserver hkp://pool.sks-keyservers.net --send-keys YOUR_KEY_ID
```

#### Step two: register an OSSRH account and configure to maven setting

The first, register a [JIRA account](https://issues.sonatype.org/secure/Signup!default.jspa). 
Then inform the maintainer of this project with your username. Then a new issue about allowing you to 
push the repository will be initiated and wait for official approval.

Then that issue is resolved. add following to your maven 'setting.xml':

```
<server>
    <id>ossrh</id>
    <username>YOUR_USERNAME</username>
    <password>YOUR_PASSWORD</password>
</server>
```

#### Step three: run and deploy

Simply run `mvn clean deploy -Possrh`. it should work!


## Run the 'Release' Progress

This project have include the [maven-release-plugin](https://maven.apache.org/maven-release/maven-release-plugin/index.html) to reduce the repetitive and manual work。

1. run `mvn release:prepare -DautoVersionSubmodules=true`
2. run `mvn release:perform -Possrh`

run `mvn release:rollback` if something goes wrong in the progress. And `mvn release:clean` to remove all generated files by `release:prepare`.

By the way, this plugin provide an easy way to update all POM version in a recursive way: 
```
mvn --batch-mode release:update-versions -DdevelopmentVersion=0.0.1-SNAPSHOT -DautoVersionSubmodules=true
```

## How to update licence header to file

run `mvn license:update-file-header` command.

## Integration test

Current to run integration test: `mvn verify -P integration-test`.

Another way to run integration test: [Maven Failsafe Plugin](http://maven.apache.org/surefire/maven-failsafe-plugin/usage.html) (not applied right now)


## Spring security

1. [spring security manual](https://docs.spring.io/spring-security/site/docs/5.1.10.RELEASE/reference/htmlsingle)
2. [Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture)
3. [Spring Security Authentication Provider](https://www.baeldung.com/spring-security-authentication-provider)

## Spring Data - ignore the parameter if it has a null value

Based on the answer on [StackOverflow](https://stackoverflow.com/questions/43780226/spring-data-ignore-parameter-if
-it-has-a-null-value/43781418), this project adopt **[Example](https://docs.spring.io/spring-data/jpa/docs/current
/reference/html/#query-by-example.introduction)** to handle nullable parameter in db query.

Go to io/etrace/api/service/DashboardService.java:52 for reference.

Also, keep watching Jira issue [Improve handling of null query method parameter values](https://jira.spring.io/browse/DATAJPA-209) and hope official team
 could support this via annotation. 

 

