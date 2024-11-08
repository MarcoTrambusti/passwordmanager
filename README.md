![img.png](img.png)

# Password Manager

[![Java CI with Maven in Linux](https://github.com/MarcoTrambusti/passwordmanager/actions/workflows/maven.yml/badge.svg)](https://github.com/MarcoTrambusti/passwordmanager/actions/workflows/maven.yml)
[![Coverage Status](https://coveralls.io/repos/github/MarcoTrambusti/passwordmanager/badge.svg?branch=main)](https://coveralls.io/github/MarcoTrambusti/passwordmanager?branch=main)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=MarcoTrambusti_passwordmanager&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=MarcoTrambusti_passwordmanager)

### Get Started
Clone the repository on your machine and locate inside the folder:

```
git clone https://github.com/MarcoTrambusti/passwordmanager.git 
cd passwordmanager
```
Build docker mariadb image through docker compose

```
docker compose --project-name=passwordmanager up -d
```

Run the app using maven (omit -DskipTests to run th test)

```
mvn clean package -DskipTests exec:java
```