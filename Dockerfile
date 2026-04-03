# Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

# Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /build/target/*.jar app.jar
USER spring:spring
# Render (and similar) set PORT; must listen on 0.0.0.0:$PORT or health checks fail
EXPOSE 8080
# Render: prefer IPv4 (Supabase direct host can be IPv6-only → "Network unreachable" without this or pooler URL)
# MaxRAMPercentage: small instances
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Djava.net.preferIPv4Stack=true"
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.address=0.0.0.0 -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
