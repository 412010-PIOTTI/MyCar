# MyCar 🚗

**AutoGest** es una aplicación web de gestión integral del vehículo. Permite a los propietarios registrar y centralizar toda la información de su auto identificado por patente: gastos, mantenimiento, documentos y alertas. La plataforma genera un historial digital completo y transferible.

---

## Stack tecnológico

| Capa       | Tecnología              |
|------------|-------------------------|
| Backend    | Java 17 + Spring Boot 3 |
| Frontend   | Angular 19              |
| Base de datos | SQL Server           |
| Build      | Maven                   |

---

## Requisitos previos

Antes de levantar el proyecto, asegurate de tener instalado:

- **Java 17+** → `java -version`
- **Maven 3.8+** → `mvn -version` (o usar el wrapper incluido `./mvnw`)
- **Node.js 20+** → `node -version`
- **Angular CLI 19** → `npm install -g @angular/cli`
- **SQL Server** (local o Docker) con una instancia disponible
- **Git** → `git -version`

---

## Setup del Backend (Spring Boot)

### 1. Clonar el repositorio

```bash
git clone https://github.com/412010-PIOTTI/MyCar.git
cd MyCar
```

### 2. Configurar la base de datos

Crear la base de datos en SQL Server:

```sql
CREATE DATABASE mycar_db;
```

Crear el archivo de configuración local (no se versiona):

```bash
# En backend/src/main/resources/
cp backend/src/main/resources/application.properties backend/src/main/resources/application-local.properties
```

Editar `application-local.properties` con tus credenciales:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=mycar_db;encrypt=false
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

### 3. Compilar y ejecutar

```bash
cd backend

# Con Maven wrapper (recomendado)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# O con Maven instalado
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

El backend quedará disponible en: `http://localhost:8080`

---

## Setup del Frontend (Angular)

### 1. Ir al directorio del frontend

```bash
cd frontend
```

### 2. Instalar dependencias

```bash
npm install
```

### 3. Ejecutar en modo desarrollo

```bash
ng serve
```

La app quedará disponible en: `http://localhost:4200`

> El frontend está preconfigurado para apuntar al backend en `http://localhost:8080` (`src/environments/environment.ts`).

---

## Flujo de trabajo con Git

### Convención de ramas

| Prefijo     | Uso                                      | Ejemplo                        |
|-------------|------------------------------------------|--------------------------------|
| `feature/`  | Nueva funcionalidad                      | `feature/registro-vehiculo`    |
| `bugfix/`   | Corrección de bug no crítico             | `bugfix/validacion-patente`    |
| `hotfix/`   | Corrección urgente sobre producción      | `hotfix/login-error`           |

### Cómo crear una rama y hacer PR

```bash
# Siempre partir desde main actualizado
git checkout main
git pull origin main

# Crear rama nueva
git checkout -b feature/nombre-de-la-funcionalidad

# Trabajar, commitear
git add .
git commit -m "feat: descripción clara del cambio"

# Subir rama
git push origin feature/nombre-de-la-funcionalidad
```

Luego abrir un **Pull Request** en GitHub hacia `main`. La rama `main` está protegida: no se permiten pushes directos.

### Convención de commits (recomendada)

```
feat:     nueva funcionalidad
fix:      corrección de bug
docs:     cambios en documentación
style:    formato, sin cambios de lógica
refactor: refactorización sin cambio de comportamiento
test:     agregar o modificar tests
chore:    tareas de build, configuración
```

---

## Estructura del proyecto

```
MyCar/
├── backend/                        # Spring Boot (Maven)
│   ├── src/
│   │   ├── main/java/              # Código fuente Java
│   │   └── main/resources/        # application.properties
│   └── pom.xml
├── frontend/                       # Angular 19
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/
│   │   │   │   ├── guards/         # authGuard
│   │   │   │   ├── interceptors/   # authInterceptor (JWT)
│   │   │   │   └── services/       # AuthService
│   │   │   ├── features/
│   │   │   │   ├── auth/           # login, register (lazy-loaded)
│   │   │   │   ├── dashboard/      # dashboard (lazy-loaded)
│   │   │   │   ├── vehicles/       # lista y detalle (lazy-loaded)
│   │   │   │   └── expenses/       # lista de gastos (lazy-loaded)
│   │   │   ├── app.config.ts       # HttpClient + interceptor + router
│   │   │   └── app.routes.ts       # Rutas principales con lazy loading
│   │   ├── environments/
│   │   │   └── environment.ts      # apiUrl → localhost:8080
│   │   └── styles.css              # Tailwind CSS
│   ├── tailwind.config.js
│   └── package.json
├── .gitignore
└── README.md
```

---

## Equipo

Proyecto de Tesis — Tecnicatura en Programación  
Repositorio: [github.com/412010-PIOTTI/MyCar](https://github.com/412010-PIOTTI/MyCar)
