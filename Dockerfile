FROM eclipse-temurin:21-jdk-jammy AS compilacion
WORKDIR /app
COPY src/main/java src/main/java
RUN mkdir -p build/classes && find src/main/java -name '*.java' > /tmp/fuentes && javac --add-modules jdk.httpserver -encoding UTF-8 -d build/classes @/tmp/fuentes
ADD https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.7.8/postgresql-42.7.8.jar /app/build/lib/postgresql.jar

RUN chmod 644 /app/build/lib/postgresql.jar && echo "2a32a9dcbc42d67a50ad3a0de5efd102c8d2be46720045f2cbd6689f160ab7c7  /app/build/lib/postgresql.jar" | sha256sum -c -

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd --system --uid 10001 obraplan && mkdir -p output data && chown -R obraplan /app
COPY --from=compilacion --chown=obraplan /app/build build
COPY --chown=obraplan src/main/resources/static src/main/resources/static
COPY --chown=obraplan knowledge knowledge
COPY --chown=obraplan config/integrations.example.json config/integrations.json
USER obraplan
ENV OBRAPLAN_ENV=production OBRAPLAN_HOST=0.0.0.0 TZ=America/Bogota
EXPOSE 8080
CMD ["java", "--add-modules", "jdk.httpserver", "-cp", "build/classes:build/lib/*", "co.obraplan.Server"]
