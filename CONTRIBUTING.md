# For Contributor/Maintainers 

Following contents help you to build your local development environment.

## 1. Download and install requirements.

1. install [Docker](https://www.docker.com/products/docker-desktop).
2. set up your Java development environment.
3. set up your Maven environment.

## 2. Set up your local development environment

1. set your git config. (Setting your email as [github private email address](https://github.com/settings/emails) is one good practise.):

```
git config user.name $YOUR_GITHUB_NAME
git config user.email $YOUR_GITHUB_EMAIL
# to double check config
git config --local -l

# sign your code with GPG. GPG sign is optional but strongly recommended. 
git config commit.gpgsign true
```

2. config your hosts in `/etc/hosts`。

```
127.0.0.1 kafka hbase proemtheus mysql zookeeper
```

That's all. Then follow the guidelines in README to run _ETrace_ project.

Feel free to fork the project and push your request!

## 3. Set up `GPG`
GPG sign validate you codes and contribute. It's not mandatory but strongly recommended.

[Signing commits](https://docs.github.com/en/free-pro-team@latest/github/authenticating-to-github/signing-commits) doc on github explained how to sign the commits.

[GPG Suite](https://gpgtools.org/) is a useful to maintain GPG key on MacOS.

Take this article for [reference](https://central.sonatype.org/pages/working-with-pgp-signatures.html#distributing-your-public-key):

1. install [gnupg](https://formulae.brew.sh/formula/gnupg): `brew install gpg` .
2. generate a key pair: `gpg --gen-key`. In the interactive, it require you a passphrase. Write it down, you'll sign
 your files by inputting this passphrase.(also can be set in maven setting.xml to pass the input)
3. distribute your key to public. some available key servers are 'keys.gnupg.net', 'pool.sks-keyservers.net'.

```
gpg --keyserver hkp://keys.gnupg.net --send-keys YOUR_KEY_ID
gpg --keyserver hkp://pool.sks-keyservers.net --send-keys YOUR_KEY_ID
```

---

# Continues Integration

This part explain how _ETrace_ project run [Continues Integration].
Default, test is ignored to simplify coding progress, 
but this [github workflow](https://github.com/etrace-io/etrace/blob/master/.github/workflows/maven-test.yml) will test every commit.

## Unit Test
Run integration test: `mvn verify -P unit-test`.

## Integration test

Current to run integration test: `mvn verify -P integration-test`.

Another way to run integration test: [Maven Failsafe Plugin](http://maven.apache.org/surefire/maven-failsafe-plugin/usage.html) (not involved yet)

---
# Deploy component to Central Maven Repository

## automatically upload to Central Maven Repository

1. Upgrade Release Version

```
mvn --batch-mode release:update-versions -DdevelopmentVersion=0.0.1-SNAPSHOT -DautoVersionSubmodules=true
```

2. Then commit code and pull the request.

3. On _ETrace_ github page, [create one release](https://github.com/etrace-io/etrace/releases/new). 
Then corresponding [github workflow](https://github.com/etrace-io/etrace/blob/master/.github/workflows/maven-deploy.yml)
will do the rest -- upload to Central Maven Repository automatically.

## manual upload to Central Maven Repository
Sometime, you may want to deploy from your local machine (if you're the maintainer of this project).

1. Prepare your environment: register an OSSRH account and configure to maven setting

The first, register a [JIRA account](https://issues.sonatype.org/secure/Signup!default.jspa). 
Then inform the maintainer of this project with your username. Then a new issue about allowing you to 
push the repository will be initiated and wait for the official approval.

When that issue resolved, add following settings to your maven 'setting.xml':

```
<server>
    <id>ossrh</id>
    <username>YOUR_JIRA_USERNAME</username>
    <password>YOUR_JIRA_PASSWORD</password>
</server>
```
2. Upgrade Release Version
```
mvn --batch-mode release:update-versions -DdevelopmentVersion=0.0.1-SNAPSHOT -DautoVersionSubmodules=true
```
3. Then commit code and pull the request. 

4. This project have included the [maven-release-plugin](https://maven.apache.org/maven-release/maven-release-plugin/index.html) to reduce the repetitive and manual work。
   
   1. run `mvn release:prepare -DautoVersionSubmodules=true`
   2. run `mvn release:perform -Possrh`
   
run `mvn release:rollback` if something goes wrong in the progress. And `mvn release:clean` to remove all generated files by `release:prepare`.

5. Simply run `mvn clean deploy -Possrh`. it should work!

---
# Related background information about Dev

## How to update licence header to file
run `mvn license:update-file-header` command.

## Spring security

1. [spring security manual](https://docs.spring.io/spring-security/site/docs/5.1.10.RELEASE/reference/htmlsingle)
2. [Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture)
3. [Spring Security Authentication Provider](https://www.baeldung.com/spring-security-authentication-provider)

## Spring Data - ignore the parameter if it has a null value

Based on the answer on [StackOverflow](https://stackoverflow.com/questions/43780226/spring-data-ignore-parameter-if-it-has-a-null-value/43781418), 
this project adopt **[Example](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#query-by-example.introduction)** to handle nullable parameter in db query.

Go to io/etrace/api/service/DashboardService.java:52 for reference.

Also, keep watching Jira issue [Improve handling of null query method parameter values](https://jira.spring.io/browse/DATAJPA-209) and hope official team could support this via annotation. 
