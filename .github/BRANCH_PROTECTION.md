# Branch Protection Rules — MyCar (GitFlow)

> Configurar en: GitHub → Settings → Branches → Add branch ruleset

---

## Modelo de ramas

```
main          ← producción estable, solo recibe de release/* y hotfix/*
  └── develop   ← integración continua, base para features
        ├── feature/nombre     ← nueva funcionalidad
        ├── bugfix/nombre      ← fix de bug en desarrollo
        └── support/nombre     ← soporte de versiones

release/x.x.x  ← sale de develop, mergea a main Y a develop
hotfix/x.x.x   ← sale de main, mergea a main Y a develop
```

---

## Regla 1 — Protección de `main`

**GitHub → Settings → Branches → Add ruleset**

| Campo | Valor |
|-------|-------|
| Ruleset name | `protect-main` |
| Target branches | `main` |
| Enforcement status | Active |

**Require a pull request before merging**
- ✅ Required approvals: `1`
- ✅ Dismiss stale pull request approvals when new commits are pushed
- ✅ Require review from code owners (opcional, crear CODEOWNERS)

**Require status checks to pass**
- ✅ Require branches to be up to date before merging
- Status checks requeridos:
  - `Gitleaks — detectar secretos`
  - `Compilar, Testear y Analizar`
  - `Lint, Build y Audit`

**Restrict pushes that create matching branches**
- ✅ Block force pushes
- ✅ Restrict who can push: solo tú (owner del repo)

**Allowed merge strategies**
- ✅ Allow merge commits (para PR de release/* y hotfix/*)
- ❌ Desactivar squash y rebase (para mantener historial GitFlow limpio)

**Branch naming restriction (mediante ruleset avanzado)**
- Solo permiten PRs desde ramas que coinciden con:
  - `release/**`
  - `hotfix/**`

---

## Regla 2 — Protección de `develop`

| Campo | Valor |
|-------|-------|
| Ruleset name | `protect-develop` |
| Target branches | `develop` |
| Enforcement status | Active |

**Require a pull request before merging**
- ✅ Required approvals: `1`
- ✅ Dismiss stale pull request approvals when new commits are pushed

**Require status checks to pass**
- ✅ Require branches to be up to date before merging
- Status checks requeridos:
  - `Gitleaks — detectar secretos`
  - `Compilar, Testear y Analizar`
  - `Lint, Build y Audit`

**Block force pushes**
- ✅ Activado

**Branch naming restriction**
- Solo permiten PRs desde ramas que coinciden con:
  - `feature/**`
  - `bugfix/**`
  - `support/**`
  - `release/**` (para el merge de vuelta a develop al cerrar una release)

---

## Regla 3 — Naming convention en ramas auxiliares (opcional, recomendado)

**GitHub → Settings → Branches → Add ruleset**

| Campo | Valor |
|-------|-------|
| Ruleset name | `branch-naming` |
| Target branches | All branches |
| Enforcement status | Active |

**Require matching branches**
Patrón permitido (regex):
```
^(main|develop|feature/.+|release/\d+\.\d+\.\d+|hotfix/\d+\.\d+\.\d+|bugfix/.+|support/.+)$
```

Esto impide crear ramas con nombres como `arreglo`, `mi-rama`, `prueba`, etc.

---

## Comandos Git para crear ramas (flujo diario)

```bash
# Nueva feature (desde develop)
git checkout develop
git pull origin develop
git checkout -b feature/nombre-descriptivo

# Nueva release (desde develop)
git checkout develop
git pull origin develop
git checkout -b release/1.0.0

# Hotfix (desde main)
git checkout main
git pull origin main
git checkout -b hotfix/1.0.1

# Bugfix durante desarrollo (desde develop)
git checkout develop
git pull origin develop
git checkout -b bugfix/descripcion-del-bug
```

---

## GitHub Secrets requeridos

Configurar en: **GitHub → Settings → Secrets and variables → Actions → New repository secret**

| Secret | Descripción |
|--------|-------------|
| `DB_URL` | `jdbc:sqlserver://HOST:1433;databaseName=mycar` |
| `DB_USERNAME` | Usuario de SQL Server |
| `DB_PASSWORD` | Contraseña de SQL Server |
| `JWT_SECRET` | Clave secreta para firmar JWT (mínimo 256 bits) |
| `OPENAI_API_KEY` | API key de OpenAI para Spring AI / RAG |
| `SENDGRID_API_KEY` | API key de SendGrid para alertas por email |
| `SONAR_TOKEN` | Token de SonarCloud (obtenido en sonarcloud.io) |
| `NG_APP_API_URL` | URL base de la API para el build del frontend |

---

## Checklist de setup inicial

- [ ] Hacer el primer commit en `main`
- [ ] Crear rama `develop` desde `main`
- [ ] Subir ambas ramas a GitHub (`git push origin main develop`)
- [ ] Configurar ruleset `protect-main` en GitHub Settings
- [ ] Configurar ruleset `protect-develop` en GitHub Settings
- [ ] Agregar todos los GitHub Secrets listados arriba
- [ ] Instalar CodeRabbit GitHub App en el repo
- [ ] Crear cuenta en SonarCloud y obtener SONAR_TOKEN
- [ ] Crear `feature/github-action-config` desde `develop`
- [ ] Abrir PR de `feature/github-action-config` → `develop` para probar el pipeline
