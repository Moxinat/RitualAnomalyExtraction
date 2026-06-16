# Sound Assets Reference

All sounds must be exported as `.ogg` (Vorbis) mono at 44.1 kHz.
Stereo is only appropriate for music — all in-world sounds must be mono so
Hytale can apply 3D positional audio correctly.

Place finished files in `art/sounds/` matching the filename exactly.

---

## Required Files

### Ambient / Atmosphere

| Filename              | Type    | Length     | Notes                                                                 |
|-----------------------|---------|-----------|-----------------------------------------------------------------------|
| `ambient_wind_loop.ogg` | Loop  | 8–12 s    | Low, breathy wind. Seamless loop. No sharp transients at loop point.  Subtle creak or distant branch snap acceptable. Volume: quiet (played at 0.35 in-engine). |
| `heartbeat_loop.ogg`  | Loop    | 2–3 s     | Single dull heartbeat, minimal reverb. The engine scales its volume with proximity — record dry and unprocessed. |

### Player Interaction

| Filename         | Type    | Length   | Notes                                                               |
|------------------|---------|---------|---------------------------------------------------------------------|
| `item_pickup.ogg`| One-shot| 0.4–0.6 s| Cloth/leather rustle + soft thud. Should feel like grabbing something heavy. |
| `item_deposit.ogg`| One-shot| 0.8–1.2 s| Stone scrape + resonant low tone. Played at three different pitches (1.0×, 1.15×, 1.3×) by the engine — record at neutral pitch. |

### Ritual

| Filename             | Type    | Length   | Notes                                                                    |
|----------------------|---------|---------|--------------------------------------------------------------------------|
| `ritual_complete.ogg`| One-shot| 2.0–3.0 s| Dramatic low-to-high swell — the moment of completion. Think deep organ chord dissolving into a high shimmer. Stereo acceptable (it plays globally, not positionally). |

### The Monster

| Filename            | Type    | Length   | Notes                                                                     |
|---------------------|---------|---------|---------------------------------------------------------------------------|
| `monster_growl.ogg` | One-shot| 0.8–1.5 s| Guttural, non-human. Played 3D from Monster position — must be mono. Avoid jump-scare volume; it should be unsettling rather than startling. Tip: pitch-shift a deep animal growl down 30–40%. |
| `player_caught.ogg` | One-shot| 0.3–0.5 s| Short sharp sting — conveys impact/shock without being a long scream. Played at player position. |

---

### Decision Feedback

| Filename                 | Type    | Length   | Notes                                                              |
|--------------------------|---------|---------|---------------------------------------------------------------------|
| `decision_correct.ogg`   | One-shot| 0.6–1.0 s| Warm low chime — used for OBJECT_EXTRACTED and ANOMALY_CORRECT. Should feel satisfying, not celebratory. |
| `decision_wrong.ogg`     | One-shot| 0.4–0.7 s| Dissonant sting — used for WRONG_EXTRACT_FAKE and ANOMALY_WRONG. Not harsh; more like a wrong note on piano. |

---

## Nice-to-Have (expansion, cut if time is short)

| Filename               | Notes                                                              |
|------------------------|--------------------------------------------------------------------|
| `monster_growl_2.ogg`  | Alternate growl variation — prevents audio fatigue on repeat plays |
| `monster_growl_3.ogg`  | Third variant                                                      |
| `round_start.ogg`      | Subtle whoosh on farm teleport to cue the player that the round has begun |

---

## Blockbench Model Checklist

All models are `.bbmodel` format. Export each to `art/models/`.

| Model file              | Poly budget | Key detail                                                   |
|-------------------------|-------------|--------------------------------------------------------------|
| `the_monster.bbmodel`   | ~300 tris   | Tall, humanoid silhouette. No face — featureless or masked. Needs idle, walk, chase, and lunge animation clips. |
| `ritual_skull.bbmodel`  | ~80 tris    | Simple skull shape. Roots/vines wrapped around base. Sits flat for world placement and in-hand. |
| `ritual_lantern.bbmodel`| ~120 tris   | Iron cage frame around a dark flame. Chain loop at top for hand attachment point. |
| `ritual_effigy.bbmodel` | ~100 tris   | Crude stick-figure bound in twine. Asymmetric for visual interest. |

### Texture sheets

One texture PNG per model, `art/textures/<model_name>.png`.
Recommended size: 32×32 or 64×64. Hytale's style is low-res pixel art — avoid high-detail
photorealistic textures.

### Particle textures

| Texture file           | Size    | Notes                                       |
|------------------------|---------|---------------------------------------------|
| `particle_smoke.png`   | 16×16   | Soft circular puff, white centre, transparent edge |
| `particle_shimmer.png` | 8×8     | Small star/sparkle shape, additive blend    |

---

## Placeholder Strategy

If art assets are not ready and you need to test the plugin:

1. **Models** — use Hytale's built-in debug cube model for all items and the Watcher.
   Set each item's `model` field to `"builtin:debug_cube"` in its JSON.
2. **Textures** — single-colour 16×16 PNGs (red skull, orange lantern, brown effigy,
   grey altar, white Watcher).
3. **Sounds** — silence placeholder: 0.5s of silence as a valid `.ogg` file prevents
   the engine from logging missing-asset errors while you test game logic.
