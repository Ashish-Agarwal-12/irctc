# GitHub push checklist

Use this before the first push (or any push that might include local junk).

---

## What should be in GitHub

- Source code under `app/src/main/java` and `app/src/test`
- JSON seed data under `app/src/main/resources/localDb/`
- Gradle files: `settings.gradle`, `app/build.gradle`, `gradle/`, `gradlew`, `gradlew.bat`, `gradle.properties`
- Docs: `README.md`, `docs/`
- `.gitignore`, `.gitattributes`

---

## What must NOT be pushed

| Path | Why |
|------|-----|
| `.gradle/` | Local Gradle cache |
| `build/` | Root build output |
| `app/build/` | Module build output (classes, jars, reports) |
| `.idea/` | IntelliJ / Android Studio project files (machine-specific) |
| `.vscode/` | Editor settings (optional personal config) |
| `*.class`, `*.jar` (outside wrapper) | Compiled artifacts |
| `app/src/main/java/.../localDb/` | **Obsolete** duplicate DB; real data is in `resources/localDb/` |
| OS junk (`Thumbs.db`, `.DS_Store`) | Not project source |

---

## `.gitignore` (project root)

The root `.gitignore` is configured to ignore the items above. After updating it, remove already-tracked ignored files from the index (keeps them on disk):

```powershell
git rm -r --cached .idea
git rm -r --cached .vscode
git rm -r --cached app/build
git rm -r --cached build
git rm -r --cached .gradle
git rm -r --cached app/src/main/java/ticket/booking/localDb
```

Then stage the cleaned tree:

```powershell
git add .gitignore README.md docs app/src gradle settings.gradle app/build.gradle gradlew gradlew.bat gradle.properties .gitattributes
git status
```

Review `git status` and confirm no `build/`, `.idea/`, or `.vscode/` files are staged.

---

## Security reminder

Sample `users.json` contains demo credentials and password hashes for learning. Do not commit real secrets, API keys, or personal passwords.
