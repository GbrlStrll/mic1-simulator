FROM --platform=linux/amd64 maven:3.9-eclipse-temurin-21

RUN apt-get update && apt-get install -y \
    libx11-dev \
    libxext-dev \
    libxrender-dev \
    libxtst-dev \
    libxi-dev \
    libgtk-3-dev \
    libgdk-pixbuf2.0-dev \
    libfontconfig1 \
    libxrandr2 \
    libasound2t64 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn dependency:go-offline

EXPOSE 5900

CMD ["mvn", "javafx:run"]
