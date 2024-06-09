# Build stage
FROM maven:3.8.7-openjdk-18 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM amazoncorretto:17
ARG APP_VERSION=0.0.1-SNAPSHOT

WORKDIR /app
COPY --from=build /build/target/facturation-*.jar /app/

EXPOSE 8088

ENV DB_URL=jdbc:mysql://mysqlDb:3306/facturation

ENV JAR_VERSION=${APP_VERSION}

CMD java -jar -Dspring.datasource.url=${DB_URL}  facturation-${JAR_VERSION}.jar