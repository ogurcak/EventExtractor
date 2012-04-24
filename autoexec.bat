MD bin

"C:\Program Files\Java\jdk1.7.0_03\bin\javac" -d bin -encoding UTF-8 -cp lib\*;extraction_methods\* src\Extractor\* src\Server\*

"C:\Program Files\Java\jdk1.7.0_03\bin\java" -cp bin\;lib\*;extraction_methods\*; Server.Main 5000 -Dfile.encoding=UTF-8 -Djava.security.manager -Djava.security.policy=distr/java.policy -Djava.util.logging.config.file=distr/logging.properties

