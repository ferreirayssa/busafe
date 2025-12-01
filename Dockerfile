# ======================
# STAGE 1: build
# ======================
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copia apenas o pom.xml e baixa dependências (cache)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# --- CORREÇÃO AQUI ---
# Dá permissão de execução para o script wrapper (necessário se vindo do Windows)
RUN chmod +x mvnw
# ---------------------

RUN ./mvnw dependency:go-offline -B

# Copia o código-fonte e empacota
COPY src src
RUN ./mvnw clean package -DskipTests

# ======================
# STAGE 2: runtime
# ======================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copia apenas o JAR do stage de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
