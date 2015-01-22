#!/bin/bash

cd ~/Downloads
mkdir -p /tmp/spark

7z e stackoverflow.com-Badges.7z
mv Badges.xml /tmp/spark/

7z e stackoverflow.com-Users.7z
mv Users.xml /tmp/spark/
