# 🔴 DIAGNÓSTICO - Problemas en CI/CD y Branches

**Análisis:** 26-05-2026  
**Repositorio:** github.com/412010-PIOTTI/MyCar  

---

## 🚨 PROBLEMAS IDENTIFICADOS

### 1. **RAMAS DESORGANIZADAS** ❌

```
Tienes AHORA:
├── main                 ← rama principal (OK)
├── develop             ← rama de integración
├── master              ← REDUNDANTE ❌ (eliminar)
├── feature/entitiesCreation       ← vieja
├── feature/github-actions-ci      ← vieja
├── feature/repo-setup             ← vieja
├── copilot/setup-github-actions-best-practices ← experimental
└── origin/HEAD → main
```

**Problema:** No hay flujo gitflow correcto. Features se mezclan con main.

---

### 2. **WORKFLOWS INCORRECTOS** ❌

```
Archivos ACTUALES:
├── ci.yml          ❌ INCORRECTO
│   • Corre en: feature/*, bugfix/*, hotfix/*
│   • Hace PR a: main (INCORRECTO - debería ser develop)
│   • Busca pom.xml en raíz (INCORRECTO - está en backend/)
│
├── maven.yml        ❌ REDUNDANTE
│   • Busca pom.xml en raíz (no existe)
│   • No soporta monorepo (backend/ + frontend/)
│
├── codeql.yml       ✅ OK (pero podría optimizarse)
│
└── FALTA: frontend workflow ❌
```

**Problema:** Los workflows apuntan a rutas incorrectas.

---

### 3. **PROTECCIÓN DE RAMAS** ❌

```
Estado ACTUAL:
├── main           → SIN protección ❌
├── develop        → SIN protección ❌
├── feature/*      → SIN protección ❌
```

**Problema:** Cualquiera puede pushear directo a main (peligroso).

---

## 📋 DETALLE DE CADA PROBLEMA

### Problema #1: ci.yml apunta mal

**Archivo actual:**
```yaml
name: CI - Backend

on:
  push:
    branches:
      - 'feature/**'
      - 'bugfix/**'
      - 'hotfix/**'
  pull_request:
    branches:
      - main   # ❌ AQUÍ ESTÁ EL ERROR
```

**Por qué es un problema:**
- PRs se hacen a `main` pero deberían ser a `develop`
- El workflow espera main pero el flujo correcto es:
  ```
  feature/x → develop (no main)
  develop → main solo en releases
  ```

---

### Problema #2: maven.yml busca pom.xml en raíz

**Archivo actual:**
```yaml
- name: Build with Maven
  run: mvn -B package --file pom.xml   # ❌ busca en raíz
```

**Por qué falla:**
```
Tu estructura:
MyCar/
├── backend/
│   └── pom.xml          ← aquí está
├── frontend/
│   └── package.json
└── pom.xml              ← NO EXISTE

El workflow busca: ./pom.xml ❌
Debería buscar: ./backend/pom.xml ✅
```

---

### Problema #3: No hay workflow para frontend

**Estado:**
- Backend: tiene workflows (aunque mal)
- Frontend: ❌ NO TIENE VALIDACIÓN AUTOMÁTICA

**Necesitas:**
```yaml
# ci-frontend.yml
- npm ci
- npm run build
- npm run test
```

---

### Problema #4: Sin protección de ramas

**Cómo está GitHub ahora:**
```
Settings → Branches → Branch protection rules:
(vacío - sin restricciones)
```

**Resultado:**
- ❌ Alguien puede hacer: git push origin main (peligroso)
- ❌ Sin revisión de código
- ❌ Sin validación de tests

---

## ✅ PLAN DE ACCIÓN

### Fase 1: Arreglar Workflows (LOCAL)
```
1. Actualizar ci.yml (PRs a develop, no main)
2. Actualizar maven.yml (buscar en backend/)
3. Crear ci-frontend.yml (frontend validation)
4. Crear ci-develop.yml (integración)
5. Commitear y pushear a main
```

### Fase 2: Limpiar Ramas (GITHUB)
```
1. Eliminar rama master (Settings → Branches)
2. Eliminar feature/github-actions-ci
3. Eliminar feature/repo-setup
4. Deletar copilot/* branches
5. Mantener: main, develop, feature/entitiesCreation
```

### Fase 3: Proteger Ramas (GITHUB)
```
1. main → requiere 1 review + tests verdes
2. develop → requiere 1 review (más flexible)
3. feature/* → nadie hace push directo (no existe)
```

---

## 🎯 QUÉ DEBE PASAR DESPUÉS

### Flujo CORRECTO:

```
1. Creas: feature/nueva-funcion (desde develop)
2. Trabaja + commits
3. Push a: origin/feature/nueva-funcion
4. GitHub Actions ejecuta: ci.yml
   ✅ Backend compila
   ✅ Frontend compila
5. Abres PR a: develop (no main)
6. GitHub Actions ejecuta: ci-develop.yml
   ✅ Tests completos
   ✅ Cobertura
   ✅ CodeQL
7. Se aprueba + mergea a develop
8. Cuando liberas: develop → PR a main
   ✅ Últimos checks
   ✅ Se mergea a main
```

---

## 🔍 VERIFICACIÓN RÁPIDA

### En GitHub, ve a:
1. **Actions** → ¿Cuántos workflows hay?
   - Deberías ver: ci, ci-frontend, codeql, ci-develop
   
2. **Branches** → ¿Cuántas hay?
   - main, develop, feature/entitiesCreation
   
3. **Settings → Branches** → Branch protection rules
   - main (activada), develop (recomendada)

---

## 📊 ANTES vs DESPUÉS

| Item | Antes | Después |
|------|-------|---------|
| Ramas | main + develop + master | ✅ main + develop |
| CI en features | Sí pero apunta a main ❌ | ✅ Apunta a develop |
| CI en develop | No ❌ | ✅ Sí |
| CI en frontend | No ❌ | ✅ Sí |
| maven.yml | Busca pom.xml en raíz ❌ | ✅ En backend/ |
| Protección | Ninguna ❌ | ✅ main + develop |

---

## 🚀 PRÓXIMO PASO

Espera a que te dé los **archivos correctos** y los pasos exactos para implementar.

Voy a preparar:
1. Nuevos workflows corregidos
2. Pasos de cómo aplicarlos
3. Cómo proteger las ramas en GitHub
