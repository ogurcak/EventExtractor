MD bin
"C:\Program Files\Java\jdk1.7.0_01\bin\javac" -d bin -encoding UTF-8 -cp src\Server\Main.java src\Server\HTTPServer.java src\Extractor\Event.java 
"C:\Program Files\Java\jdk1.7.0_01\bin\java" -cp bin\; Server.Main -Dfile.encoding=UTF-8 -Djava.security.manager -Djava.security.policy=distr/java.policy -Djava.util.logging.config.file=distr/logging.properties

