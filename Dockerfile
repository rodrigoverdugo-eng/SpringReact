# ── Stage 1: Build frontend ───────────────────────────────────────────────────
FROM node:20-alpine AS frontend-build
WORKDIR /frontend

ARG VITE_APP_TITLE
ENV VITE_APP_TITLE=$VITE_APP_TITLE

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
# VITE_OUTDIR=dist para que el output quede en /frontend/dist
RUN VITE_OUTDIR=dist npm run build

# ── Stage 2: Build backend ────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /backend

# Cachear dependencias antes de copiar el código fuente
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -q

COPY backend/src ./src

# Copiar el build del frontend en los recursos estáticos del backend
COPY --from=frontend-build /frontend/dist ./src/main/resources/static

RUN mvn clean package -DskipTests -Dskip.frontend=true -q

# ── Stage 3: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=backend-build /backend/target/springreact-backend-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
