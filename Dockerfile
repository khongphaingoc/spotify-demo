FROM node:22-bookworm-slim AS frontend
WORKDIR /build/frontend
RUN npm install --global pnpm@10.9.0
COPY frontend/package.json frontend/pnpm-lock.yaml frontend/pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile
COPY frontend/ ./
RUN pnpm build

FROM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /build/backend
COPY backend/pom.xml ./
RUN mvn -B dependency:go-offline
COPY backend/src/ src/
COPY --from=frontend /build/frontend/dist/ src/main/resources/static/
RUN mvn -B package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend /build/backend/target/spotify-int1313-1.0.0.jar /app/app.jar
COPY demo-audio/ /app/demo-audio/
ENV PORT=8080 AUDIO_DIR=/app/demo-audio
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar","--server.address=0.0.0.0"]
