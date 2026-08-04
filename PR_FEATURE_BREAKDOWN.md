# CourierPrime WIP Feature Breakdown

This document separates the current `feature/custom-entity-couriers` work into features that can be implemented, reviewed, tested, and merged independently.

## Current status

- `origin/main`: `4a21d09`
- `feature/custom-entity-couriers` and its remote tracking branch: `65f7c6c` (`test: testing mm integration`)
- The pushed branch contains a compile-blocking call in `CustomEntityManager`:

  ```java
  return activeProvider.spawnEntity(, location);
  ```

- The working directory contains additional uncommitted changes beyond that pushed commit.
- `CourierType.java` is currently untracked and must be included in any commit that uses the new courier-type design.
- The current working directory builds successfully with the configured Paper 26.2 / Java 25 toolchain, but Gradle reports `test NO-SOURCE`; there are no automated tests.

The pushed branch should therefore be treated as the original WIP baseline. The local worktree is a later, broader implementation pass and should not be merged as one undifferentiated change.

## Recommended merge strategy

Use the following order. Each item should be its own branch or clearly separated commit series.

1. Decide and document the supported Paper/Java baseline.
2. Implement the courier-type abstraction and vanilla selection without external plugins.
3. Add MythicMobs integration.
4. Add ModelEngine integration.
5. Harden courier lifecycle and retry behavior.
6. Merge the mail delivery and persistence fixes separately.
7. Merge letter metadata and forwarding fixes separately.
8. Merge reload/configuration robustness and documentation.

The platform upgrade should be a separate PR unless the project explicitly wants the custom-courier PR to raise the minimum supported Paper and Java versions.

## Feature F0 — Define the custom courier contract

### Purpose

Replace the incomplete global custom-entity design from the pushed branch with one clear contract for selecting and spawning couriers.

### Current problem

The pushed implementation introduces `custom-entities.enabled`, `entity-id`, and `preferred-plugin`, but the spawn call does not pass an entity ID and cannot compile. The working tree instead introduces per-player values such as `mm:CourierOwl` and `meg:delivery_dragon`.

### Recommended contract

- Vanilla type: `VILLAGER`, `COW`, `PARROT`, etc.
- MythicMobs type: `mm:<mob-id>`
- ModelEngine type: `meg:<model-id>`
- `enabled-courier-types` controls what players may select.
- An administrator may assign a valid type directly with `/courier set`.
- A failed custom spawn falls back to the configured vanilla default.

### Work

- Remove the unused global `custom-entities.entity-id` and `preferred-plugin` behavior, or provide an explicit migration from it.
- Make every spawn request carry the selected courier type or provider plus identifier.
- Decide whether `/courier set` should support offline players.
- Decide whether a saved custom selection remains valid when the dependency is later disabled. The current implementation falls back to vanilla.

### Acceptance criteria

- The plugin compiles from a clean clone.
- The configuration and command documentation describe the same model.
- A server without MythicMobs or ModelEngine still starts and supports vanilla couriers.
- Invalid custom types never reach the spawn provider.

## Feature F1 — Courier type parsing, selection, and persistence

### Purpose

Give players and administrators a safe way to choose a courier type, independent of the provider implementation.

### Files

- `src/main/java/com/joaorihan/courierprime/courier/CourierType.java`
- `src/main/java/com/joaorihan/courierprime/courier/CourierSelectManager.java`
- `src/main/java/com/joaorihan/courierprime/command/CourierSelectCommand.java`
- `src/main/java/com/joaorihan/courierprime/config/MainConfig.java`
- `src/main/resources/config.yml`
- `src/main/resources/lang/en-us.yml`
- `src/main/resources/lang/pt-br.yml`
- The selection-related portion of `Courier.java`

### Behavior

- Parse spawnable vanilla `EntityType` values.
- Parse and normalize `mm:` and `meg:` values.
- Persist selections in `couriers.yml`.
- Preserve existing vanilla selections.
- Reject malformed UUIDs and malformed saved values without preventing startup.
- Support `/courier select <type>`.
- Support administrator-only `/courier set <player> <type>`.

### Acceptance criteria

- `/courier select cow` succeeds when `COW` is enabled.
- An unenabled vanilla type is rejected by `/courier select`.
- `/courier set <player> sheep` works for an administrator even when `SHEEP` is not enabled for normal selection.
- Invalid types do not overwrite an existing valid selection.
- A restart preserves the selected type.
- Tab completion and the documented command syntax agree.

### Follow-up issue

The current administrator tab completion only lists enabled types, even though administrator assignment intentionally bypasses the enabled list. Either list all valid built-in types for administrators or document that disabled types must be entered manually.

## Feature F2 — MythicMobs courier provider

### Purpose

Spawn a selected MythicMobs mob as the courier without making MythicMobs mandatory.

### Files

- `src/main/java/com/joaorihan/courierprime/integration/CustomEntityProvider.java`
- `src/main/java/com/joaorihan/courierprime/integration/CustomEntityManager.java`
- `src/main/java/com/joaorihan/courierprime/integration/MythicMobsProvider.java`
- `build.gradle.kts`
- `src/main/resources/plugin.yml`
- The custom-spawn portion of `Courier.java`

### Behavior

- Detect MythicMobs only when it is installed and enabled.
- Validate the requested mob ID before saving a selection.
- Spawn the requested mob through the MythicMobs API.
- Return the Bukkit entity to the normal courier lifecycle.
- Fall back to a vanilla courier if the provider is unavailable or spawning fails.

### Acceptance criteria

- The plugin starts with no MythicMobs installation.
- `/courier select mm:<valid-id>` succeeds with MythicMobs installed.
- A valid MythicMobs courier can be clicked and delivers mail.
- `/courier select mm:<invalid-id>` is rejected and does not change the saved selection.
- Disabling MythicMobs does not crash the server or prevent vanilla delivery.
- The actual MythicMobs version used on the server matches the compile-only API version closely enough for runtime operation.

## Feature F3 — ModelEngine courier provider

### Purpose

Spawn a vanilla host entity with a ModelEngine model attached.

### Files

- `src/main/java/com/joaorihan/courierprime/integration/ModelEngineProvider.java`
- `src/main/java/com/joaorihan/courierprime/integration/CustomEntityManager.java`
- `build.gradle.kts`
- `src/main/resources/config.yml`
- `src/main/resources/plugin.yml`
- The custom-spawn portion of `Courier.java`

### Behavior

- Detect ModelEngine only when it is installed and enabled.
- Validate the model/blueprint ID before saving a selection.
- Spawn the configured base entity.
- Attach the requested model.
- Remove the base entity if model creation or attachment fails.
- Fall back to a vanilla courier when the provider cannot spawn the model.

### Acceptance criteria

- The plugin starts without ModelEngine installed.
- A valid `meg:<model-id>` selection spawns a visible, clickable courier.
- The configured `modelengine-base-entity` is honored.
- Invalid model IDs are rejected.
- Failed model creation does not leave an invisible or orphaned host entity.
- Disabling ModelEngine does not crash the server or prevent vanilla delivery.

## Feature F4 — Courier lifecycle and retry hardening

### Purpose

Ensure all courier types behave consistently after death, timeout, reload, movement, or duplicate spawn attempts.

### Files

- `src/main/java/com/joaorihan/courierprime/courier/Courier.java`
- `src/main/java/com/joaorihan/courierprime/courier/CourierManager.java`
- The active-courier cleanup portion of `ConfigManager` and `CourierPrime`

### Behavior

- Do not cast every courier to `LivingEntity`.
- Remove dead entities from the active map.
- Avoid two active couriers for one recipient.
- Retry undelivered mail after timeout or external entity removal.
- Preserve click delivery for vanilla, MythicMobs, and ModelEngine host entities.

### Acceptance criteria

- A courier is removed after the configured timeout.
- An undelivered letter eventually receives a replacement courier.
- Killing a courier does not leave a stale map entry.
- Repeated join/reload events do not create duplicate couriers.
- Non-living but valid configured entities do not produce a `ClassCastException`.
- A delivered courier does not send an “ignored” message.

## Feature F5 — Mail recipient resolution and persistence

### Purpose

Make sending reliable for online players, known offline players, multiple recipients, and server restarts.

### Files

- `src/main/java/com/joaorihan/courierprime/letter/LetterSender.java`
- `src/main/java/com/joaorihan/courierprime/letter/OutgoingManager.java`
- `src/main/java/com/joaorihan/courierprime/command/PostCommand.java`

### Behavior

- Reject unknown recipients.
- Do not cast offline recipients to `Player`.
- Support comma-separated and whitespace-separated multiple recipients.
- Save outgoing mail after send and receive operations.
- Preserve undelivered mail when the inventory is full.
- Load malformed outgoing data without crashing the plugin.

### Acceptance criteria

- Online delivery works.
- Known offline delivery waits until the player joins.
- Unknown players are rejected and the sender keeps the letter.
- Multiple recipients each receive a copy.
- A restart preserves pending mail.
- A full inventory does not delete pending letters.
- Console logs contain no cast, serialization, or null-pointer errors.

### Remaining review point

Forwarding currently changes the book generation to `COPY_OF_ORIGINAL` before recipient validation. A failed forward should leave the sender’s book unchanged; fix this before treating the forwarding/mail feature as complete.

## Feature F6 — Letter metadata, ownership, forwarding, and page safety

### Purpose

Protect letter ownership and make inspection, anonymous letters, forwarding, and long messages reliable.

### Files

- `src/main/java/com/joaorihan/courierprime/letter/LetterManager.java`
- `src/main/java/com/joaorihan/courierprime/letter/LetterUtil.java`
- `src/main/java/com/joaorihan/courierprime/command/ForwardCommand.java`
- `src/main/java/com/joaorihan/courierprime/command/InspectCommand.java`

### Behavior

- Store UUID ownership metadata for new letters.
- Retain a legacy name-based fallback for old letters.
- Keep anonymous letters anonymous in `/inspect`.
- Mark successful forwards as copies.
- Prevent resending or repeatedly forwarding the same letter.
- Split long content into valid book pages.
- Handle ordinary or malformed written books without exceptions.

### Acceptance criteria

- A player cannot send another player’s letter.
- A player-name change does not break ownership of new letters.
- `/inspect` reports the visible author, not hidden ownership metadata.
- Anonymous letters display as anonymous.
- A forwarded letter is marked as a copy and cannot be forwarded repeatedly.
- Messages longer than 256 characters are delivered without book API errors.

## Feature F7 — Reload, configuration validation, and message lifecycle

### Purpose

Make `/courieradmin reload` safe and ensure invalid configuration does not crash the server.

### Files

- `src/main/java/com/joaorihan/courierprime/CourierPrime.java`
- `src/main/java/com/joaorihan/courierprime/config/ConfigManager.java`
- `src/main/java/com/joaorihan/courierprime/config/MainConfig.java`
- `src/main/java/com/joaorihan/courierprime/config/MessageManager.java`
- `src/main/java/com/joaorihan/courierprime/command/AbstractCommand.java`
- `src/main/java/com/joaorihan/courierprime/updates/UpdateChecker.java`
- Language resources

### Behavior

- Cancel plugin tasks before rebuilding state.
- Remove active couriers before reload.
- Avoid registering duplicate listeners and commands.
- Reschedule pending online mail after reload.
- Fall back safely for invalid entity, gamemode, world, language, and ModelEngine host settings.
- Make commands use the current message manager after reload.

### Acceptance criteria

- Reload does not duplicate event messages or commands.
- Active couriers are removed exactly once.
- Pending online mail is delivered after reload.
- Invalid values produce warnings and safe defaults rather than startup failure.
- Switching language and reloading changes command messages without restarting.

## Feature F8 — Platform, build, packaging, and documentation

### Purpose

Upgrade and document the build/runtime baseline independently from gameplay behavior.

### Files

- `build.gradle.kts`
- `gradle/wrapper/gradle-wrapper.properties`
- `src/main/resources/plugin.yml`
- `README.md`
- `.gitignore`

### Current changes

- Gradle 9.6.1.
- Shadow plugin 9.6.1.
- Paper API 26.2.
- Java 25 toolchain.
- MythicMobs and ModelEngine compile-only dependencies.
- Updated build and configuration documentation.
- Ignored generated `bin/` output.

### Acceptance criteria

- `./gradlew clean build` succeeds from a clean checkout.
- The shaded JAR is produced in `build/libs`.
- The JAR starts on the documented Paper/Java versions.
- The plugin does not crash when optional dependencies are absent.
- The project’s supported-version policy is explicit. If Paper 1.21 / Java 21 support is still required, this upgrade must be revised or made a separate compatibility branch.

## Recommended PR boundaries

### PR 1 — Platform baseline

Only include F8 if the minimum supported Paper and Java versions are intentionally changing. Do not mix mail behavior changes into this PR.

### PR 2 — Courier type core

Implement F0 and F1 with vanilla couriers only. This gives the project a complete, testable selection model before optional integrations are introduced.

### PR 3 — MythicMobs integration

Implement F2 and test with and without MythicMobs installed.

### PR 4 — ModelEngine integration

Implement F3 and test model attachment and cleanup independently.

### PR 5 — Courier lifecycle

Implement F4 and test timeout, death, retry, reload, and duplicate prevention across all courier types.

### PR 6 — Mail delivery persistence

Implement F5. This should be reviewable without requiring either optional entity plugin.

### PR 7 — Letter integrity

Implement F6, including the failed-forward mutation fix.

### PR 8 — Reload and configuration robustness

Implement F7, then update documentation and examples.

## Final integration checklist

Before merging the feature set, verify all of the following on a real Paper server using the shaded JAR:

- Vanilla courier selection and persistence.
- Startup with neither optional plugin installed.
- Valid and invalid MythicMobs selections.
- Valid and invalid ModelEngine selections.
- Fallback after disabling an optional plugin.
- Courier click delivery for each provider.
- Courier death, timeout, retry, and duplicate prevention.
- Online, offline, unknown, and multiple-recipient mail.
- Inventory-full delivery and restart persistence.
- Anonymous letters, forwarding, inspection, and long pages.
- Reload with active couriers and pending mail.
- Invalid configuration values and missing language keys.
- Command registration and permissions on the target Paper version.
- No `ClassCastException`, `NoClassDefFoundError`, `NullPointerException`, or `Could not pass event` messages in the console.
