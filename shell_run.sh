#!/bin/bash

export CLASSPATH=$CLASSPATH:mysql-connector-java-8.0.16.jar:.
export APP_JDBC_URL=jdbc:mysql://db.labthreesixfive.com/egarc113?autoReconnect=true

read -p 'Username: ' uservar
read -p 'EMPLID: ' passvar
export APP_JDBC_USER=$uservar
export APP_JDBC_PW=WinterTwenty20_365_$passvar

javac jdbc/src/InnReservations.java
java -classpath jdbc/src/ InnReservations
