# 🚀 PLAN DE ARREGLO RÁPIDO - CI/CD y Branches

**Estado actual:** Workflows duplicados + Branches desorganizadas + Sin protección  
**Solución:** 3 cambios simples en GitHub  

---

## 📋 RESUMEN EJECUTIVO

Tienes **3 problemas críticos**:

| # | Problema | Solución |
|---|----------|----------|
| 1️⃣ | Rama `master` redundante | ❌ Eliminarla en GitHub |
| 2️⃣ | Workflows apuntan a `master` y `main` | ✏️ Actualizar triggers |
| 3️⃣ | Sin protección de ramas | 🔒 Activar rules en GitHub |

**Tiempo total:** ~15 minutos

---

## 🎯 LO QUE NECESITAS HACER

### PASO 1: En tu máquina local (2 minutos)

```bash
cd ~/Desktop/TESIS/mycar

# 1. Actualiza local
git fetch origin
git checkout main
git pull origin main

# 2. Limpia workflows viejos/duplicados (los actualizaremos en GitHub)
rm .github/workflows/maven.yml

# 3. Commit
git add .
git commit -m "chore: update CI workflows configuration"

# 4. Push
git push origin main
```

---

### PASO 2: En GitHub → Settings → Branches (5 minutos)

#### 2A: Eliminar rama `master` ❌

1. Ve a: **github.com/412010-PIOTTI/MyCar**
2. Click en: **Settings**
3. Click en: **Branches** (lado izquierdo)
4. Encuentra rama `master`
5. Click en 🗑️ **Delete branch**
6. Confirma

**Resultado:** Solo quedan `main` y `develop`

---

#### 2B: Proteger rama `main` 🔒

1. En **Settings → Branches**
2. Click en: **Add rule**
3. Branch name pattern: `main`
4. Checkea:
   - ✅ Require pull request reviews before merging
   - ✅ Require status checks to pass before merging
   - ✅ Require branches to be up to date before merging
   - ✅ Dismiss stale pull request approvals
5. Click: **Create**

**Resultado:** No se puede pushear directo a `main`. Requiere PR + tests verdes.

---

#### 2C: Proteger rama `develop` 🔒 (recomendado)

Repite 2B pero con `develop`:
- Branch name pattern: `develop`
- Solo checkea:
   - ✅ Require pull request reviews
   - ✅ Require status checks

(Más permisiva que `main`)

---

### PASO 3: Revisar que workflows están correctos (5 minutos)

En tu repo, los workflows IMPORTANTES son:

```
✅ backend-ci.yml      ← Compila + tests backend
✅ frontend-ci.yml     ← Compila + tests frontend
✅ code-quality.yml    ← Calidad de código
✅ secret-scan.yml     ← Detecta secrets en el código
✅ codeql.yml          ← Análisis de seguridad
```

**PROBLEMA:** Estos workflows todavía tienen triggers a `master`.

---

### PASO 4: Actualizar triggers de workflows (5 minutos)

En cada workflow, cambia:

**ANTES:**
```yaml
on:
  push:
    branches:
      - master      ❌ ELIMINAR
      - develop
      - 'feature/**'
```

**DESPUÉS:**
```yaml
on:
  push:
    branches:
      - main        ✅ CAMBIAR (antes era master)
      - develop
      - 'feature/**'
```

**Workflows a actualizar:**
- `backend-ci.yml` - línea ~9
- `frontend-ci.yml` - línea ~9
- `code-quality.yml` - línea ~9
- `secret-scan.yml` - línea ~9

---

## 📝 CHECKLIST DETALLADO

### LOCAL (máquina)
- [ ] `git fetch origin`
- [ ] `git checkout main`
- [ ] `git pull origin main`
- [ ] Eliminar/verificar workflows viejos
- [ ] `git add . && git commit && git push`

### GITHUB - Settings → Branches

**Eliminar:**
- [ ] Rama `master` (🗑️ Delete)

**Proteger `main`:**
- [ ] Add rule: Branch = `main`
- [ ] ✅ Require pull request reviews
- [ ] ✅ Require status checks
- [ ] ✅ Require up to date
- [ ] ✅ Dismiss stale approvals

**Proteger `develop` (opcional):**
- [ ] Add rule: Branch = `develop`
- [ ] ✅ Require pull request reviews
- [ ] ✅ Require status checks

### Workflows - Verificar triggers

En cada archivo `.github/workflows/*.yml`:
- [ ] Cambiar `master` → `main`
- [ ] Dejar `develop` como está
- [ ] Mantener `feature/**`

---

## 🔄 FLUJO CORRECTO DESPUÉS

```
1. Creas rama
   git checkout develop
   git checkout -b feature/mi-feature

2. Trabajas + commits
   git push origin feature/mi-feature

3. Abres PR
   → SIEMPRE a develop
   → GitHub Actions ejecuta automáticamente
   → backend-ci, frontend-ci, code-quality, secret-scan

4. Si todo ✅ verde
   → Se puede mergear a develop

5. Cuando liberas (release)
   develop → PR a main
   → Últimos checks
   → Se mergea a main
```

---

## ⚠️ COSAS CRÍTICAS

### ❌ NUNCA hagas esto después:

```bash
git push origin main              # GitHub lo bloqueará
git merge feature/x → main        # GitHub lo bloqueará
git commit -m "arreglé algo"      # Sin convención
```

### ✅ SIEMPRE haz esto:

```bash
feature/x → develop (PR)          # Correcto
develop → main (release)          # Correcto
feat: descripción                 # Convención
```

---

## 🆘 SI ALGO FALLA

### Si un workflow no corre:
1. Ve a: **Actions** en GitHub
2. Selecciona el workflow que falló
3. Clic en el último run
4. Lee los detalles del error
5. Arregla en tu rama + push

### Si no puedes pushear a main:
Correcto, eso es lo que queremos. Haz PR en su lugar.

### Si necesitas arreglar algo en main urgente:
1. Crea rama `hotfix/nombre`
2. Hazla desde `main`
3. Mergea a `main` + `develop`

---

## ✅ VERIFICACIÓN FINAL

Después de aplicar estos pasos, ve a GitHub y verifica:

1. **Actions** → ¿Los workflows se ejecutan en verde?
2. **Branches** → ¿Solo ves main, develop y features?
3. **Settings → Branches** → ¿main y develop protegidas?

Si todo está ✅, ¡LISTO!

---

## 📞 DUDAS FRECUENTES

**P: ¿Debo eliminar todas las feature branches viejas?**  
R: Sí, `feature/github-actions-ci`, `feature/repo-setup`, etc. Puedes borrarlas sin problemas.

**P: ¿Qué pasa con `copilot/setup-github-actions-best-practices`?**  
R: Bórrala también, es experimental.

**P: ¿Y si alguien intenta pushear a main?**  
R: GitHub lo rechaza automáticamente. Debe hacer PR.

**P: ¿Los workflows corren automáticamente?**  
R: Sí. Cada push/PR dispara los workflows.

---

## 🎓 Después de esto tu proyecto estará:

✅ Gitflow correcto (feature → develop → main)  
✅ Workflows validando cambios automáticamente  
✅ Ramas protegidas (no puedes romper nada)  
✅ Convención de commits clara  
✅ Listo para producción  

---

**¿Empezamos? Hazme saber cuando termines PASO 1 (el local).**

