# MyCar

Aplicación web de gestión integral del vehículo. Permite registrar y centralizar toda la información de un auto identificado por patente: gastos, mantenimiento, documentos y alertas. Genera un historial digital completo y transferible.

---

## Stack tecnológico

| Capa          | Tecnología                        |
|---------------|-----------------------------------|
| Backend       | Java 17 + Spring Boot 4 + Maven   |
| Frontend      | Angular 19 + Tailwind CSS         |
| Base de datos | SQL Server 2022                   |
| Auth          | Spring Security + JWT (JJWT 0.12) |
| Contenedores  | Docker + Docker Compose           |

---

## Requisitos previos

- **Java 17** — `java -version`
- **Docker Desktop** — para levantar SQL Server sin instalarlo localmente
- **Node.js 20+** — `node --version`
- **Git**

> Maven no necesita instalación separada: el proyecto incluye `./mvnw`.

---

## Inicio rápido (recomendado)

### 1. Clonar el repositorio

```bash
git clone https://github.com/412010-PIOTTI/MyCar.git
cd MyCar
```

### 2. Crear el archivo de variables de entorno

```bash
# Windows (PowerShell)
copy .env.example .env

# Mac / Linux
cp .env.example .env
```

El archivo `.env` ya viene con una contraseña válida para desarrollo local. No lo commitees (está en `.gitignore`).

### 3. Levantar la base de datos con Docker

```bash
docker-compose up sqlserver -d
```

Esto levanta únicamente SQL Server en el puerto `1433` y crea automáticamente la base de datos `mycar_db`. El primer arranque tarda ~30 segundos.

Para verificar que está listo:

```bash
docker logs mycar-sqlserver
# Debe terminar con: "SQL Server listo en localhost:1433"
```

### 4. Configurar el backend para desarrollo local

Crear el archivo (no se versiona):

```
backend/src/main/resources/application-local.properties
```

Con este contenido:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=mycar_db;encrypt=false
spring.datasource.username=sa
spring.datasource.password=MyCar_Dev_2024!
spring.jpa.hibernate.ddl-auto=update
```

> Usá la misma contraseña que definiste en `.env`.

### 5. Levantar el backend

**Opción A — Desde IntelliJ IDEA (recomendado):**

1. `File → Open` → seleccioná `backend/pom.xml` → **Open as Project**
2. Esperá que Maven descargue dependencias
3. Abrí `MycarApplication.java` → click derecho → **Run**
4. En la run configuration: **Edit Configurations** → **Active profiles**: `local` → Apply
5. Run

**Opción B — Desde terminal:**

```bash
# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"

cd backend
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

El backend queda disponible en `http://localhost:8080`.

### 6. Verificar que funciona

```bash
curl http://localhost:8080/ping
# → {"status":"ok","app":"MyCar"}
```

### 7. Levantar el frontend

```bash
cd frontend
npm ci
npm start
# → http://localhost:4200
```

---

## API de autenticación

| Método | Endpoint              | Body                              | Respuesta       |
|--------|-----------------------|-----------------------------------|-----------------|
| POST   | `/api/auth/register`  | `{name, email, password}`         | `201` + JWT     |
| POST   | `/api/auth/login`     | `{email, password}`               | `200` + JWT     |

**Register — 201 Created:**
```json
{
  "token": "<JWT>",
  "id": 1,
  "name": "Ana Pérez",
  "email": "ana@example.com",
  "role": "USER"
}
```

**Errores:**
- `400` — campo inválido: `{"errors": {"campo": "mensaje"}}`
- `401` — credenciales incorrectas: `{"detail": "Invalid email or password"}`
- `403` — cuenta desactivada: `{"detail": "Account is disabled"}`
- `409` — email ya registrado: `{"detail": "Email already registered: ..."}`

El JWT debe enviarse en todos los requests protegidos:
```
Authorization: Bearer <token>
```

---

## Stack completo con Docker (opcional)

Para levantar backend + base de datos + frontend en contenedores:

```bash
docker-compose up --build
```

| Servicio  | URL                      |
|-----------|--------------------------|
| Frontend  | http://localhost:4200     |
| Backend   | http://localhost:8080     |
| SQL Server| localhost:1433            |

---

## Tests

```bash
cd backend
./mvnw test --no-transfer-progress
```

Los tests usan H2 en memoria — no requieren Docker ni SQL Server.

---

## Estructura del proyecto

```
MyCar/
├── backend/                          # Spring Boot 4
│   ├── src/main/java/.../
│   │   ├── domain/
│   │   │   ├── entity/               # Entidades JPA
│   │   │   └── repository/           # Spring Data JPA
│   │   ├── application/service/      # Lógica de negocio (@Transactional)
│   │   ├── infrastructure/security/  # JWT, Spring Security
│   │   └── web/
│   │       ├── controller/           # REST controllers
│   │       ├── dto/                  # Request / Response DTOs
│   │       └── exception/            # @ControllerAdvice global
│   └── pom.xml
├── frontend/                         # Angular 19
│   └── src/app/
│       ├── core/                     # guards, interceptors, services
│       └── features/                 # módulos lazy-loaded
├── docker/
│   └── init-db.sh                    # Crea mycar_db al primer arranque
├── docker-compose.yml
├── .env.example                      # Plantilla de variables de entorno
└── README.md
```

---

## Flujo de Git

```
feature/** → develop → main
```

| Rama         | Uso                                         |
|--------------|---------------------------------------------|
| `main`       | Producción — solo via PR desde `develop`    |
| `develop`    | Integración — base para nuevas features     |
| `feature/**` | Nueva funcionalidad (desde `develop`)       |
| `bugfix/**`  | Corrección de bug (desde `develop`)         |
| `hotfix/**`  | Corrección urgente (desde `main`)           |

```bash
# Crear una feature
git checkout develop
git pull origin develop
git checkout -b feature/nombre-funcionalidad

# Commitear
git add <archivos>
git commit -m "feat: descripción del cambio"

# Abrir PR hacia develop en GitHub
git push origin feature/nombre-funcionalidad
```

### Convención de commits

| Prefijo    | Uso                              |
|------------|----------------------------------|
| `feat:`    | Nueva funcionalidad              |
| `fix:`     | Corrección de bug                |
| `chore:`   | Build, dependencias, config      |
| `refactor:`| Refactorización sin nuevo feat   |
| `test:`    | Tests                            |
| `docs:`    | Documentación                    |

---

## Equipo

Proyecto de Tesis — Tecnicatura en Programación  
Repositorio: [github.com/412010-PIOTTI/MyCar](https://github.com/412010-PIOTTI/MyCar)
