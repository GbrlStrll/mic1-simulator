# Docker

```bash
docker-compose up -d

# Conecte com cliente VNC em localhost:5900
# SENHA: mic1sim
# Exemplos de clientes VNC:
# - macOS: Screen Sharing (cmd+K, vnc://localhost:5900)
# - Linux: remmina vnc://localhost:5900
# - Windows: TightVNC em localhost:5900

# Ver logs
docker-compose logs -f

# Parar
docker-compose down
```

# Compilar (alternativa)

```bash
mvn clean package
java -jar target/mic1-simulator-1.0-SNAPSHOT.jar
```