# ✏️ CAMBIOS A HACER EN WORKFLOWS

**Objetivo:** Cambiar todas las referencias a `master` → `main`

---

## 📄 Archivo: `.github/workflows/backend-ci.yml`

**Línea ~9, busca esto:**

```yaml
on:
  push:
    branches:
      - master         ❌ ELIMINAR ESTA LÍNEA
      - develop
      - 'feature/**'
```

**Cambia a:**

```yaml
on:
  push:
    branches:
      - main           ✅ CAMBIAR (antes era master)
      - develop
      - 'feature/**'
```

---

## 📄 Archivo: `.github/workflows/frontend-ci.yml`

**Línea ~9, busca esto:**

```yaml
on:
  push:
    branches:
      - master         ❌ ELIMINAR ESTA LÍNEA
      - develop
      - 'feature/**'
```

**Cambia a:**

```yaml
on:
  push:
    branches:
      - main           ✅ CAMBIAR
      - develop
      - 'feature/**'
```

---

## 📄 Archivo: `.github/workflows/code-quality.yml`

**Busca la sección `on:`**

```yaml
on:
  push:
    branches:
      - master         ❌ CAMBIAR A main
      - develop
```

**Cambia a:**

```yaml
on:
  push:
    branches:
      - main           ✅
      - develop
```

---

## 📄 Archivo: `.github/workflows/secret-scan.yml`

**Busca la sección `on:`**

```yaml
on:
  push:
    branches:
      - master         ❌ CAMBIAR A main
```

**Cambia a:**

```yaml
on:
  push:
    branches:
      - main           ✅
```

---

## 📄 Archivo: `.github/workflows/codeql.yml`

**Busca la sección `on:`**

```yaml
on:
  push:
    branches: [ "main" ]   ✅ ESTE ESTÁ OK
  pull_request:
    branches: [ "main" ]   ✅ ESTE ESTÁ OK
```

**No necesita cambios.**

---

## 🔍 CÓMO HACER LOS CAMBIOS

### Opción A: En GitHub (web)

1. Ve a tu repositorio
2. Click en `.github/workflows/backend-ci.yml`
3. Click en el lápiz (✏️ Edit)
4. Cambia `master` → `main`
5. Scroll al fondo y haz commit

### Opción B: En tu máquina (recomendado)

```bash
cd tu-repo/.github/workflows

# Edita cada archivo con tu editor favorito
code backend-ci.yml    # o vim, nano, etc.

# Busca "master" y reemplaza por "main"

# Luego commit
git add .
git commit -m "chore: fix workflow triggers (master → main)"
git push origin main
```

---

## ✅ VERIFICACIÓN

Después de hacer los cambios, verifica:

```bash
# Busca si quedan referencias a "master"
grep -r "master" .github/workflows/

# Si no imprime nada, está todo bien ✅
```

---

## 🎯 RESUMEN DE CAMBIOS

| Archivo | Cambio | Línea aprox |
|---------|--------|-------------|
| backend-ci.yml | master → main | ~9 |
| frontend-ci.yml | master → main | ~9 |
| code-quality.yml | master → main | ~9 |
| secret-scan.yml | master → main | ~9 |
| codeql.yml | (Sin cambios) | OK |

---

**Después de esto, tu CI/CD estará completamente funcional.** ✅

