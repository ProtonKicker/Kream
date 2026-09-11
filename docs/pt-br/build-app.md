# Compilando o APK

**Idiomas: [English](../build-app.md) · [Português (BR)](build-app.md) · [简体中文](../zh-Hans/build-app.md)**

O build precisa do Android SDK/NDK, de um JDK e de um interpretador **Python
3.10** (o Chaquopy compila o Python embutido contra 3.10). Nada que exponha
caminhos locais é commitado — `local.properties`, `.gradle/`, `build/`,
`firmware/toolchain/`, `firmware/.klipper-build/` e os artefatos baixados de
`fluidd`/`mainsail` são todos gitignored e regerados.

## Setup em um comando

| SO | Comando |
|---|---|
| Linux / macOS | `./scripts/setup.sh` |
| Windows (PowerShell) | `.\scripts\setup.ps1` |

O script:

1. verifica se há um JDK (17+, 21 recomendado);
2. baixa as command-line tools do Android se nenhum SDK estiver presente;
3. instala os pacotes fixados do SDK — `platform-tools`, `platforms;android-35`,
   `build-tools;35.0.0`, `ndk;23.2.8568313`, `cmake;3.22.1`;
4. garante que exista um interpretador Python 3.10 (via `pyenv` / `brew` /
   `winget`, ou mostra como instalar);
5. grava o `local.properties` (`sdk.dir` + `chaquopy.python`).

Adicione `--build` (bash) / `-Build` (PowerShell) para já compilar o APK arm64 debug.

## Compilar

```bash
./gradlew :app:assembleArm64Debug     # ou assembleArmv7Debug / assembleAmd64Debug
./gradlew :app:assembleRelease         # todos os ABIs, minificado (sem assinatura sem keystore)
```

Ou abra o projeto no Android Studio (Giraffe+) e clique em Run. Saída:
`app/build/outputs/apk/<abi>/<tipo>/KocoaBeam_<commit>_<abi>.apk`.

O `preBuild` baixa os bundles do Fluidd/Mainsail do GitHub (precisa de rede no
primeiro build) e regera `app/src/main/assets/` a partir das fontes vendoradas em
`app/src/main/{klipper,kalico,moonraker,happyhare}`.

## Pré-requisitos manuais (se não usar o script)

- **JDK 21** (Temurin). AGP 8.10 / Gradle 8.12 precisam de 17+.
- **Android SDK** com: `platforms;android-35`, `build-tools;35.0.0`,
  `ndk;23.2.8568313`, `cmake;3.22.1`.
- **Python 3.10** — qualquer CPython 3.10.x; `pyenv install 3.10.14` funciona bem.
- `local.properties` na raiz do repositório:

  ```properties
  sdk.dir=/caminho/absoluto/para/Android/Sdk
  chaquopy.python=/caminho/absoluto/para/python3.10
  ```

## Assinatura

Builds de debug usam a debug keystore. Para um release assinado, adicione ao
`local.properties` (já gitignored):

```properties
key.store=/caminho/absoluto/para/release.keystore
key.store.password=…
key.alias=…
key.key.password=…
```

Sem elas o release ainda é gerado, apenas sem assinatura.
