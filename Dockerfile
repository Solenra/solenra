# docker build --progress=plain --no-cache -t solenra:latest .
# docker-compose -f docker-compose.dev.yml up --build -d
# docker-compose -f docker-compose.dev.yml logs -f solenra
# docker-compose -f docker-compose.dev.yml down

############################
# 1) Build Angular frontend
############################
FROM node:20-alpine AS angular-build

WORKDIR /client
COPY client/package*.json ./
RUN npm ci

COPY client/ .
RUN npm run build -- --configuration production


############################
# 2) Build Spring Boot WAR
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

# --- Angular → ROOT ---
COPY --from=angular-build /client/dist/client/browser/ \
     /usr/local/tomcat/webapps/ROOT/

# --- Spring Boot WAR → /server ---
COPY --from=server-build /server/target/server.war \
     /usr/local/tomcat/webapps/server.war

EXPOSE 8080

CMD ["catalina.sh", "run"]