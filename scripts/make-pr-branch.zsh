#!/usr/bin/env zsh
#
# Create feature/klipper-0.13 and commit the working tree in semantic,
# feature-grouped commits.
#
#   ./scripts/make-pr-branch.zsh            # do it
#   ./scripts/make-pr-branch.zsh --dry-run  # print the plan only
#
# Notes:
#  - Run from the repo root, on a clean index (nothing already staged).
#  - Files are assigned to exactly one commit. Files touched by several
#    features go with their dominant change; a final commit sweeps anything
#    not matched so the tree is never left dirty.
# ---------------------------------------------------------------------------
set -euo pipefail

DRY=0
[[ "${1:-}" == "--dry-run" ]] && DRY=1

BRANCH="feature/klipper-0.13"
ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

[[ -n "$(git status --porcelain)" ]] || { print -u2 "ERROR: nothing to commit."; exit 1; }
git rev-parse --verify -q main >/dev/null || { print -u2 "ERROR: no 'main' branch."; exit 1; }

run() { if (( DRY )); then print -r -- "  + $*"; else "$@"; fi }

# stage_commit <subject> [-- body] :: <pathspec> ...
stage_commit() {
  local subject="$1"; shift
  local body=""
  if [[ "${1:-}" == "--" ]]; then shift; body="$1"; shift; fi
  local specs=("$@")
  print "\n=== $subject"
  # stage each pathspec; tolerate ones that match nothing (already gone / never tracked)
  local s
  for s in "${specs[@]}"; do
    if (( DRY )); then print -r -- "  + git add -A -- $s"
    else git add -A -- "$s" 2>/dev/null || true
    fi
  done
  if (( DRY )); then return; fi
  if git diff --cached --quiet; then
    print "  (nothing matched — skipped)"
    return
  fi
  if [[ -n "$body" ]]; then
    git commit -q -m "$subject" -m "$body"
  else
    git commit -q -m "$subject"
  fi
  git --no-pager log -1 --format="  committed %h %s"
}

unstage() { run git restore --staged -- "$@"; }

# --- branch -------------------------------------------------------------
if (( DRY )); then
  print "=== would run: git switch -c $BRANCH ; git reset -q main"
else
  git switch -c "$BRANCH" 2>/dev/null || git switch "$BRANCH"
  # branch tip -> main, all changes unstaged in the working tree (idempotent re-run)
  git reset -q main
fi

# --- 1. housekeeping ---------------------------------------------------
stage_commit "chore: tighten .gitignore and drop tracked clutter" \
  -- ".gitignore" "K icon.psd" "K icon.svg" "dummy.keystore" \
     "EventBus/README.md" "scripts/ensure_submodule_excludes.sh"

# --- 2. vendored tree slimming --------------------------------------
stage_commit "chore(vendor): strip firmware-only and dev files from vendored engine trees" \
  -- "app/src/main/klipper/lib" "app/src/main/klipper/src" "app/src/main/klipper/scripts" \
     "app/src/main/klipper/bin-templates" "app/src/main/klipper/docs" "app/src/main/klipper/test" \
     "app/src/main/klipper/Makefile" "app/src/main/klipper/Kconfig" "app/src/main/klipper/.github" \
     "app/src/main/klipper/.gitignore" "app/src/main/klipper/README.md" \
     "app/src/main/kalico/lib" "app/src/main/kalico/src" "app/src/main/kalico/scripts" \
     "app/src/main/kalico/board_configs" "app/src/main/kalico/Makefile" "app/src/main/kalico/Kconfig" \
     "app/src/main/kalico/pyproject.toml" "app/src/main/kalico/uv.lock" "app/src/main/kalico/.github" \
     "app/src/main/kalico/.gitignore" "app/src/main/kalico/README.md" \
     "app/src/main/moonraker_timelapse/docs" "app/src/main/moonraker_timelapse/scripts" \
     "app/src/main/moonraker_timelapse/Makefile" "app/src/main/moonraker_timelapse/.editorconfig" \
     "app/src/main/moonraker_timelapse/.github" "app/src/main/moonraker_timelapse/README.md" \
     "app/src/main/klipper_tmc_autotune/.github" "app/src/main/klipper_tmc_autotune/install.sh" \
     "app/src/main/klipper_tmc_autotune/.gitignore" "app/src/main/klipper_tmc_autotune/README.md" \
     "app/src/main/klipper_tmc_autotune/docs"

# --- 3. Klipper / Kalico 0.13 -------------------------------------
stage_commit "feat(klipper): update vendored Klipper and Kalico to 0.13" \
  -- "app/src/main/klipper" "app/src/main/kalico" "app/CMakeLists.txt" \
     "app/src/main/assets/klipper/klippy" "app/src/main/assets/kalico/klippy"
# move the mcu.py beam patch and the jinja2 stub removal to their own commits
unstage app/src/main/klipper/klippy/mcu.py app/src/main/kalico/klippy/mcu.py \
        app/src/main/assets/klipper/klippy/mcu.py app/src/main/assets/kalico/klippy/mcu.py \
        app/src/main/assets/klipper/beam_ext/jinja2.py app/src/main/assets/kalico/beam_ext/jinja2.py \
        app/src/main/assets/klipper/beam_ext/ext_version 2>/dev/null || true
(( DRY )) || git commit -q --amend --no-edit

# --- 4. [mcu] option fix -----------------------------------------
stage_commit "fix(klipper): consume stock [mcu] options on the virtual-serial path" \
  -- "app/src/main/klipper/klippy/mcu.py" "app/src/main/kalico/klippy/mcu.py" \
     "app/src/main/assets/klipper/klippy/mcu.py" "app/src/main/assets/kalico/klippy/mcu.py"

# --- 5. gcode macro fix (jinja2 stub) --------------------------
stage_commit "fix(klipper): remove stub jinja2 so gcode macros use the real interpreter" \
  -- "app/src/main/assets/klipper/beam_ext/jinja2.py" \
     "app/src/main/assets/kalico/beam_ext/jinja2.py" \
     "app/src/main/assets/klipper/beam_ext/ext_version"

# --- 6. Moonraker / Fluidd / Mainsail --------------------------
stage_commit "feat: update Moonraker 0.11.0, Fluidd 1.37.5, Mainsail 2.19.0" \
  -- "app/src/main/moonraker" "gradle.properties" \
     "app/src/main/assets/moonraker"
unstage app/src/main/moonraker/moonraker/components/machine.py \
        app/src/main/moonraker/moonraker/components/proc_stats.py \
        app/src/main/moonraker/moonraker/components/file_manager/file_manager.py \
        app/src/main/moonraker/moonraker/components/file_manager/metadata.py 2>/dev/null || true
(( DRY )) || git commit -q --amend --no-edit

# --- 7. Moonraker Android startup fixes -----------------------
stage_commit "fix(moonraker): survive Android startup (proc_stats hwmon, network-poll spam)" \
  -- "app/src/main/moonraker/moonraker/components/machine.py" \
     "app/src/main/moonraker/moonraker/components/proc_stats.py"

# --- 8. in-process metadata / thumbnails --------------------
stage_commit "fix(moonraker): extract gcode metadata and thumbnails in-process" \
  -- "app/src/main/moonraker/moonraker/components/file_manager/file_manager.py" \
     "app/src/main/moonraker/moonraker/components/file_manager/metadata.py" \
     "app/python_wheels"

# --- 9. web frontend serving --------------------------------
stage_commit "fix(web): per-frontend ports, correct MIME types and request proxy" \
  -- "app/src/main/java/ru/ytkab0bp/beamklipper/service/WebService.kt"

# --- 10. in-app log viewer ---------------------------------
stage_commit "feat(app): in-app log viewer (Klipper / Moonraker / app logs)" \
  -- "app/src/main/java/ru/ytkab0bp/beamklipper/ui/screens/LogsScreen.kt" \
     "app/src/main/java/ru/ytkab0bp/beamklipper/utils/BeamLogs.kt" \
     "app/src/main/java/ru/ytkab0bp/beamklipper/ui/NavHost.kt" \
     "app/src/main/java/ru/ytkab0bp/beamklipper/ui/screens/HomeScreen.kt" \
     "app/src/main/res"

# --- 11. Happy Hare v4 -------------------------------------
stage_commit "feat(happy-hare): update to v4.0.0" \
  -- "app/src/main/happyhare" \
     "app/src/main/assets/klipper/klippy/extras/mmu" \
     "app/src/main/assets/kalico/klippy/extras/mmu"

# --- 12. bundled add-ons + build deps --------------------
stage_commit "feat: bundle opt-in Klipper add-ons and firmware/pillow build deps" \
  -- "app/build.gradle" \
     "app/src/main/klipper_led_effect" "app/src/main/klipper_z_calibration" \
     "app/src/main/klipper_auto_speed" "app/src/main/klipper_tmc_autotune" \
     "app/src/main/assets/klipper/kamp" "app/src/main/assets/klipper/addons_version" \
     "app/src/main/java/ru/ytkab0bp/beamklipper/service/BaseKlippyService.kt" \
     "app/src/main/java/ru/ytkab0bp/beamklipper/BundleInstaller.kt" \
     "app/src/main/java/ru/ytkab0bp/beamklipper/KlipperApp.kt" \
     "app/src/main/assets/kalico/klippy/extras/autotune_tmc.py" \
     "app/src/main/assets/kalico/klippy/extras/motor_constants.py" \
     "app/src/main/assets/kalico/klippy/extras/motor_database.cfg" \
     "app/src/main/assets/kalico/klippy/extras/z_calibration.py"

# --- 13. firmware build tooling -------------------------
stage_commit "feat(firmware): reproducible MCU firmware build tooling (script + Docker)" \
  -- "firmware" "scripts/build_firmware.sh" "scripts/build_firmware.ps1" \
     "scripts/build_neptune3pro_firmware.sh"

# --- 14. dev setup scripts -----------------------------
stage_commit "chore(dev): one-shot setup scripts (Linux / macOS / Windows)" \
  -- "scripts/setup.sh" "scripts/setup.ps1"

# --- 15. docs ----------------------------------------
stage_commit "docs: rewrite documentation and add pt-BR / zh-Hans translations" \
  -- "docs" "README.md" "README.pt-br.md" "README.zh-Hans.md" "README.zh-Hant.md"

# --- 16. sweep anything not matched above -----------
if (( DRY )); then
  print "\n=== final commit: any files not matched by the groups above ==="
  print "  (run without --dry-run to see and commit them)"
else
  git add -A
  if ! git diff --cached --quiet; then
    git commit -q -m "chore: sweep remaining regenerated bundle assets"
    git --no-pager log -1 --format="  committed %h %s"
  fi
fi

print "\nDone. Branch: $BRANCH"
(( DRY )) || { print "\nCommits on the branch:"; git --no-pager log --oneline main..HEAD; }
print "\nReview, then:  git push -u origin $BRANCH"