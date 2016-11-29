# Spark Examples

Experiments with [Apache Spark](http://spark.apache.org).

## Prerequisites

### Install Spark

    brew install apache-spark
    /usr/local/Cellar/apache-spark/2.0.2/libexec/sbin/start-all.sh

### Download StackOverflow's users file

- Download `stackoverflow.com-Users.7z` from [Stack Exchange Data Dump](https://archive.org/details/stackexchange);
- Download `stackoverflow.com-Badges.7z` from [Stack Exchange Data Dump](https://archive.org/details/stackexchange);
- run `./prepare-input.sh`

### Build

    sbt assembly

### Run

    spark-submit --master spark://<master>:<port> --class sparking.GetUsers target/scala-2.10/spark-examples-assembly-1.0.0.jar
