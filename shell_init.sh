#!/bin/bash

export CLASSPATH=$CLASSPATH:mysql-connector-java-8.0.16.jar:.
export APP_JDBC_URL=jdbc:mysql://db.labthreesixfive.com/egarc113?autoReconnect=true
export APP_JDBC_USER=USERNAME
export APP_JDBC_PW=WinterTwenty20_365_EMPLID

javac InnReservations.java
