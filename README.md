# Fepbox-Utility

Lightweight utility plugin for modern Paper/Spigot servers (API 1.21.4). Provides quality-of-life commands (homes, warps, tpa, invsee, enderchest, aliases, etc.) with simple YAML/SQL storage and configurable modules.

## Features
- Homes & GUI: `/home` opens a 3-row bed GUI with per-slot permissions (`home.1`, `home.2` ...), left-click teleport, right-click set. Warmup title countdown, safe-location precheck.
- Warps: create, delete, rename, GUI listing with permission-aware icons.
- TPA suite: requests, accept/deny/toggle/ignore.
- Inventory tools: `/invsee` (with armor/offhand, live updates, readonly toggle), `/ec` (readonly + admin logging).
- Moderation & utility: `/freeze`, `/kickall`, `/gm`, `/fly`, `/heal`, `/feed`, `/repair`, `/hat`, `/msg`/`/r`.
- Aliases: `/createalias`, `/deletealias` to map custom shortcuts to commands.
- Block teleport snap: `/tpb` teleports you to the center of your current block (X/Z snapped to `.5`).
- Configurable modules: enable/disable each feature in `config.yml`; YAML, SQLite, or MySQL storage.

## Commands (selected)
| Command | Description | Permission |
| --- | --- | --- |
| `/home [name]` | Teleport to home or open GUI if no args | `fepboxutility.home`* (slot perms `home.N`) |
| `/sethome [name]` | Set a home | `fepboxutility.sethome` |
| `/warps`, `/setwarp <name>`, `/warp <name>` | Warp GUI / manage warps | `fepboxutility.warp.*` |
| `/tpa <player>`, `/tpaccept`, `/tpdeny`, `/tpatoggle` | TPA suite | `fepboxutility.tpa` |
| `/invsee <player>` | View inventory incl. armor/offhand | `fepboxutility.invsee` |
| `/ec [player]` | Ender chest view (readonly toggle) | `fepboxutility.ec` |
| `/createalias <alias> <cmd...>` | Create command alias | `fepboxutility.alias` |
| `/tpb` | Teleport yourself to block center | `fepboxutility.tpb` |

*Homes per-slot permissions: `home.1`, `home.2`, ... (or `fepboxutility.home.N`). Slots without permission show bedrock.

## Configuration
- `config.yml` toggles modules under `modules.*`, sets storage type (`YAML`, `SQLITE`, `MYSQL`), GUI sizes, cooldowns, warmups, read-only flags for `enderchest`/`invsee`, and `homes.gui-slots` (default 5).
- `messages.yml` contains MiniMessage-formatted messages with a configurable `prefix`.
- `aliases` section persists custom aliases created at runtime.

## Building
Requires Java 21+ and Maven:
```sh
mvn clean package
```
The shaded plugin jar will be in `target/`.

## Notes
- Safe teleport is enforced by default; if no safe location is found, the teleport is cancelled immediately.
- Inventory interactions respect `invsee.readonly` and `enderchest.readonly`.
- Warmup titles show a live countdown; teleport fires after the final second and plays an enderman teleport sound.
