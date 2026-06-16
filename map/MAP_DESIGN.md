# Ritual Anomaly Extraction — Map Design Spec

## Concept

Four separate areas: one Control Room hub plus three Farm variants (A, B, C). The player
teleports between them; they are never physically adjacent on the map. This means each
farm can be a completely isolated build — real and fake variants can coexist in the same
map file at different coordinates, separated by void or barrier blocks.

```
MAP OVERVIEW (not to scale)

  [CONTROL ROOM]          [FARM A]           [FARM B]           [FARM C]
   hub, portal             field              forest             ruins
   (0,0,0 area)         (200,0,0 area)     (400,0,0 area)     (600,0,0 area)
        |
   Teleport on
   portal enter
        |
   → farm_a, farm_b, or farm_c depending on round
```

---

## Area 1: Control Room

**Coordinates:** origin region, roughly (−10, 0, −10) to (10, 6, 10)

A dim stone chamber. Single exit portal — a shimmering vertical ring 2 blocks wide, 3
tall. Scoreboard on the north wall (placeholder sign for now). Mood: liminal, industrial,
quiet dread.

### Required Tags (place in world editor before scripting)

| Tag                    | Type     | Position (relative) | Notes                             |
|------------------------|----------|---------------------|-----------------------------------|
| `control_room_spawn`   | Entity   | (0, 1, 0)           | Player teleport destination       |
| `control_room_portal`  | Entity   | (0, 1, 4)           | Centre of trigger volume sphere   |

### Layout Notes
- Walls: cracked stone brick, 5 blocks tall
- Floor: dark polished stone
- Portal: the visual is just a 3-tall frame of glow-stone with custom portal particle in
  the centre. The `portal_shimmer.json` particle anchors to `control_room_portal`.
- Scoreboard wall: 3 signs arranged vertically north wall. Plugin writes to them via chat
  messages until a real scoreboard API is confirmed.
- No torches in the main chamber — the portal provides dim glow only.

---

## Area 2: Farm A — Open Field

**Coordinates:** (190, 0, −10) to (230, 12, 30)

Flat farmland. Wheat rows, hay bales, a small shed. Anomaly version: crops are wrong
season (snow patches, or wheat that's dead/black). Object: `ritual_skull`.

### Required Tags

| Tag                      | Position       | Notes                                        |
|--------------------------|----------------|----------------------------------------------|
| `farm_a_player_spawn`    | (195, 1, 0)    | Drop-in point at field edge                  |
| `farm_a_monster_spawn`   | (220, 1, 15)   | Monster starts near far waypoint             |
| `farm_a_extract_panel`   | (193, 1, −2)   | Panel next to player spawn, faces player     |
| `farm_a_anomaly_panel`   | (193, 1, 2)    | Panel next to player spawn, other side       |
| `ritual_skull` (real)    | (215, 1, 10)   | Centre of field — visible, risky grab        |
| `ritual_skull` (fake)    | —              | Absent. Object entity does not exist.        |

### Patrol Path (Farm A — 3 waypoints)
Matches `MonsterSystem.java` `PATROL_PATHS.get("farm_a")`:

| Index | Coords        | Area            |
|-------|---------------|-----------------|
| 0     | (200, 1, 5)   | Field entry     |
| 1     | (220, 1, 20)  | Far field edge  |
| 2     | (210, 1, −5)  | Shed side       |

### Anomaly Design (Farm A fake)
- Crops are out of season: replace some wheat rows with bare dirt or dead crop
- One patch of snow where there should be none
- The shed door is sealed with stone (was open in real version)
- Visual feel: something is subtly wrong but you need to look for it

---

## Area 3: Farm B — Forest Clearing

**Coordinates:** (390, 0, −10) to (440, 15, 30)

Dense dark tree ring surrounding a central clearing. Cabin with a lantern inside.
Anomaly version: cabin door is wrong (wrong wood type, iron instead of wood), or the
clearing has an extra path that doesn't belong. Object: `ritual_lantern`.

### Required Tags

| Tag                      | Position       | Notes                                        |
|--------------------------|----------------|----------------------------------------------|
| `farm_b_player_spawn`    | (395, 1, 0)    | Treeline entry gap                           |
| `farm_b_monster_spawn`   | (425, 1, 20)   | Far side of clearing                         |
| `farm_b_extract_panel`   | (393, 1, −2)   | Panel at treeline entry, left side           |
| `farm_b_anomaly_panel`   | (393, 1, 2)    | Panel at treeline entry, right side          |
| `ritual_lantern` (real)  | (415, 2, 10)   | Cabin interior, on a table                   |
| `ritual_lantern` (fake)  | —              | Absent.                                      |

### Patrol Path (Farm B — 3 waypoints)
Matches `MonsterSystem.java` `PATROL_PATHS.get("farm_b")`:

| Index | Coords        | Area                |
|-------|---------------|---------------------|
| 0     | (400, 1, 5)   | Treeline inner ring |
| 1     | (420, 1, 15)  | Cabin approach      |
| 2     | (410, 1, −3)  | East tree gap       |

### Anomaly Design (Farm B fake)
- Cabin door material is iron instead of dark wood
- Extra torches on the south wall (real version has none there)
- The table the lantern sits on is moved or missing — or the lantern hook is empty
- Clearing has a gravel path that leads nowhere (doesn't exist in real version)

---

## Area 4: Farm C — Ruins

**Coordinates:** (590, 0, −10) to (650, 18, 40)

Crumbling stone hall, arched entrance, rubble piles. The object is on a plinth.
Anomaly version: the plinth is wrong material, or the arch has extra engravings.
Object: `ritual_effigy`.

### Required Tags

| Tag                      | Position       | Notes                                        |
|--------------------------|----------------|----------------------------------------------|
| `farm_c_player_spawn`    | (595, 1, 0)    | Ruin entrance                                |
| `farm_c_monster_spawn`   | (630, 1, 25)   | Inner hall far end                           |
| `farm_c_extract_panel`   | (593, 1, −2)   | Panel at entrance, left pillar               |
| `farm_c_anomaly_panel`   | (593, 1, 2)    | Panel at entrance, right pillar              |
| `ritual_effigy` (real)   | (620, 2, 15)   | On stone plinth, inner hall                  |
| `ritual_effigy` (fake)   | —              | Absent.                                      |

### Patrol Path (Farm C — 4 waypoints)
Matches `MonsterSystem.java` `PATROL_PATHS.get("farm_c")`:

| Index | Coords        | Area                   |
|-------|---------------|------------------------|
| 0     | (600, 1, 5)   | Entrance hall          |
| 1     | (620, 1, 20)  | Plinth room            |
| 2     | (640, 1, 30)  | Back chamber           |
| 3     | (610, 1, −5)  | Outer ruin courtyard   |

### Anomaly Design (Farm C fake)
- Plinth is wood planks instead of stone (wrong material)
- Arch engravings added that aren't in the real version
- A door is sealed that should be open — player can't reach where the effigy would be
- One wall has a torch bracket but no torch (real version has torch there)

---

## Decision Panels (all farms)

Each farm has two panels flanking the player spawn. These are physical blocks the player
walks up to and enters the trigger volume:

- **[EXTRACT]** panel — left side. Green-tinted block or sign reading "EXTRACT".
  Walking into it fires `onExtractPressed()`.
- **[ANOMALY]** panel — right side. Red-tinted block or sign reading "ANOMALY".
  Walking into it fires `onAnomalyPressed()`.

The panels are always visible immediately at spawn so the player can call anomaly without
venturing deeper. This is intentional — the tension is whether to trust your read of the
farm from spawn, or explore to confirm and risk the monster.

---

## Build Checklist

- [ ] Control Room area built and tagged
- [ ] Farm A (real) built — object at correct position, patrol path matches Java
- [ ] Farm A (fake) built — anomaly differences in place, no object entity
- [ ] Farm B (real) built — object at correct position, patrol path matches Java
- [ ] Farm B (fake) built — anomaly differences in place, no object entity
- [ ] Farm C (real) built — object at correct position, patrol path matches Java
- [ ] Farm C (fake) built — anomaly differences in place, no object entity
- [ ] Decision panels (EXTRACT/ANOMALY) placed and tagged at all 3 farms
- [ ] Trigger volumes added (see `data/triggers/trigger_volumes.json`)
- [ ] Monster spawn points tagged in all 3 farms and control room
- [ ] Control room portal visual + particle attached to `control_room_portal` tag

---

## After Building: Verify Java Coordinates

Once block coordinates are confirmed, cross-check these files:

- `MonsterSystem.java` — `PATROL_PATHS` array — Y values may differ if terrain height varies
- `trigger_volumes.json` — all `anchorTag` values must match tags placed in world editor
- `ControlRoomSystem.java` — teleport destination tags must exist in world
