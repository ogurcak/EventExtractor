MD bin

javac -d bin -encoding UTF-8 -cp lib\*;extraction_methods\* src\Extractor\* src\Server\*

java -cp bin\;lib\*;extraction_methods\*; Server.Main -Dfile.encoding=UTF-8 -Djava.security.manager -Djava.security.policy=distr/java.policy -Djava.util.logging.config.file=distr/logging.properties

