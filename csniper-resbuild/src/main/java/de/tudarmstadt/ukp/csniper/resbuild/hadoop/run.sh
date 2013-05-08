#!/bin/sh
JAR=target/de.tudarmstadt.ukp.csniper.resbuild-0.6.0-SNAPSHOT.jar
export HADOOP_CLASSPATH=
export CP=
for I in target/dependency/*;do HADOOP_CLASSPATH=$HADOOP_CLASSPATH:$I;done
for I in target/dependency/*;do CP=$CP,$I;done
HADOOP_CLASSPATH=$HADOOP_CLASSPATH:$JAR
export HADOOP_CLASSPATH
export HADOOP_HEAPSIZE=4096
hadoop jar $JAR $1 -libjars $CP,$JAR  $2 $3 $4 $5 $6 $7 $8 $9
