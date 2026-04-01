@echo off
cd Java\FabCat
call mvn package -X
call mvn exec:java -X