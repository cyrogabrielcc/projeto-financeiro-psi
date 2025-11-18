# =========================
# 1ª etapa: build da aplicação
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copia pom e baixa dependências em cache
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copia o código-fonte e empacota
COPY src src
RUN mvn -B -DskipTests package


FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia o fast-jar do Quarkus (target/quarkus-app)
COPY --from=build /build/target/quarkus-app /app

# Porta HTTP padrão do Quarkus
EXPOSE 8080

# Garante que o Quarkus escute em todas as interfaces no container
ENV QUARKUS_HTTP_HOST=0.0.0.0

# Comando de inicialização (fast-jar)
CMD ["java", "-jar", "/app/quarkus-run.jar"]
