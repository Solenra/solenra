# docker build --progress=plain --no-cache -t solenra:latest .
# docker-compose -f docker-compose.dev.yml up --build -d
# docker-compose -f docker-compose.dev.yml logs -f solenra
# docker-compose -f docker-compose.dev.yml down

############################
# 1) Build Client WAR
############################
FROM maven:3.9-eclipse-temurin-21 AS client-build

WORKDIR /client-war

# Copy client-war pom.xml first for dependency caching
COPY client-war/pom.xml .

# Go offline for Maven deps
RUN mvn -B dependency:go-offline

# Copy client WAR source
COPY client-war/ .

# Copy Angular source inside the image
COPY client/ ../client

# Build client WAR
RUN mvn -B clean package -DskipTests


############################
# 2) Build Server WAR
############################
FROM maven:3.9-eclipse-temurin-21 AS server-build

WORKDIR /server
COPY server/pom.xml .
RUN mvn -B dependency:go-offline

COPY server/ .
RUN mvn -B clean package -DskipTests


############################
# 3) Runtime: Tomcat
############################
FROM tomcat:11.0-jdk25

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# --- Client WAR → ROOT ---
COPY --from=client-build /client-war/target/client.war \
     /usr/local/tomcat/webapps/ROOT.war

# --- Spring Boot WAR → /server ---
COPY --from=server-build /server/target/server.war \
     /usr/local/tomcat/webapps/server.war

EXPOSE 8080

CMD ["catalina.sh", "run"]