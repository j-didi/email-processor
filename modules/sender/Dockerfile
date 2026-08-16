FROM maven:3.9.7-eclipse-temurin-17 AS build
WORKDIR /app

# 1. Copia o POM pai da raiz
COPY pom.xml .

# 2. Instala apenas o POM pai no repositório local sem validar os submódulos ausentes (-N)
RUN mvn clean install -N -DskipTests

# 3. Copia e instala o módulo compartilhado (shared)
COPY modules/shared/ ./shared/
WORKDIR /app/shared
RUN mvn clean install -DskipTests

# 4. Volta para a raiz, copia e compila o sender
WORKDIR /app
COPY modules/sender/ ./sender/
WORKDIR /app/sender
RUN mvn clean package -DskipTests

# Estágio final - Mantido em Alpine para garantir uma imagem de produção leve e segura
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Busca o JAR gerado dentro da subpasta correspondente do estágio de build
COPY --from=build /app/sender/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
