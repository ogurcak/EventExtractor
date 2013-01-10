MD bin

"C:\Program Files\Java\jdk1.7.0_10\bin\javac" -d bin -encoding UTF-8 -cp lib\*;..\GATE\lib\*;..\GATE\bin\*;extraction_methods\* src\Extractor\* src\Server\* src\Server\Method\* src\Server\POSTActions\*



"C:\Program Files\Java\jdk1.7.0_10\bin\java" -Dlog4j.configuration=file:log4j/log4j.properties -cp bin\;lib\*;..\GATE\lib\*;..\GATE\bin\*;extraction_methods\*; Server.Main 5000 -Dfile.encoding=UTF-8 -Djava.security.manager -Djava.security.policy=distr/java.policy