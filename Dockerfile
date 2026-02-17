FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

# Give permission to mvnw
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/main-backend-0.0.1-SNAPSHOT.jar"]
