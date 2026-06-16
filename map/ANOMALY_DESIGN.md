# Anomaly Design Guide

## The Core Rule

Every fake farm must have exactly one deliberate difference from the real version.
Players need to notice it. The difference should be:
- Visible without moving far from spawn
- Subtle enough that a player in a hurry might miss it
- Consistent — the same category of anomaly every time that farm is fake

Do not make anomalies random per-round. The farms are always either real or fake
as a whole — the randomness is which farm the player visits, not what the anomaly is.

---

## Farm A — Open Field

**Real version:** Healthy wheat crops in regular rows, wooden shed door open, no snow.

**Fake anomaly:** One crop row is visibly wrong — out-of-season dead wheat (brown,
withered) where the others are green. A small snow patch sits in the NW corner of the
field where there should be none.

**Why it works:** Crop state and weather are environmental details players notice subliminally.
The dead row is visible from the spawn decision panels if the player looks left. The snow
patch is only visible if they step further in — rewarding players who scout before deciding.

**Builder note:** The real and fake farm_a builds sit at different X coordinates in the
same map file. Swap teleport destination coordinates in `ControlRoomSystem.java` TODO block
(and update `farm_a_player_spawn` tag) once both builds are in place.

---

## Farm B — Forest Clearing

**Real version:** Wooden cabin door, no torches on south wall, lantern on the table inside.

**Fake anomaly:** The cabin door material is iron door instead of dark wood. Additionally,
two torches appear on the south cabin wall that do not exist in the real version.

**Why it works:** The door is the first thing the player sees walking toward the cabin.
Iron-vs-wood is high contrast. The torches are a backup tell for players who miss the door —
one anomaly with two visual markers ensures readability without being obvious from spawn.

**Builder note:** In the fake version, do not place the `ritual_lantern` entity. The table
should still be present (empty). This way a player who walks in expecting to grab the object
finds nothing — a strong secondary confirmation that something is wrong.

---

## Farm C — Ruins

**Real version:** Stone plinth for the effigy, open archway between the entrance hall and
inner chamber, torch bracket on the east inner wall with a torch lit.

**Fake anomaly:** The plinth is built of wood planks instead of stone. The east wall torch
bracket is present but the torch is absent (bracket without flame).

**Why it works:** Plinth material is the primary tell — noticeably wrong texture. The empty
torch bracket is visible once inside the inner chamber. Together they give the player two
confirmation signals without making it trivially obvious from the entrance.

**Builder note:** In the fake version, the open archway is sealed with a stone wall. The
`ritual_effigy` entity is absent. Players who don't spot the anomaly and try to push through
find a wall where a door should be — a strong "something's wrong" moment.

---

## Decision Timing

Players make their call at the decision panels flanking the farm entrance — before or after
exploring. The monster is active from round start. The intended experience:

1. **Rush call** (from spawn, without exploring): risky, fast. Correct if lucky/skilled.
2. **Scout then call**: safer read of anomalies, but the monster has more time to find you.
3. **Explore to grab then extract**: only correct on real farms, high reward, highest risk.

The anomaly design supports all three — each fake farm has something visible from or near
spawn (door material, dead crops) for rush-callers, and deeper confirmation for scouts.

---

## Playtesting Checklist

Before finalising each farm pair (real + fake):

- [ ] Stand at the decision panels in the fake version — is the anomaly visible from here?
- [ ] Walk to midfield/midforest/mid-hall — is the anomaly still readable?
- [ ] Time how long it takes to confirm the anomaly safely — should be 10–20 seconds
- [ ] Confirm the real version has zero anomalous details (test with fresh eyes)
- [ ] Confirm the object entity is absent in the fake version
- [ ] Confirm the monster patrol path is identical in both real and fake versions
