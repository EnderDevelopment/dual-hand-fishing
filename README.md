# Minecraft Fishing Plugin

Dual-Hand Fishing is a Minecraft plugin that allows players to fish with both hands at the same time. It adds off-hand fishing support alongside normal main-hand fishing by creating and managing a separate fishing hook for an off-hand fishing rod.

## Install

1. Build the plugin with Java 21 and Maven:
   ```bash
   mvn package
   ```
2. Copy the generated `target/plugin.jar` file to the server's `plugins` directory.
3. Restart the server.
4. Confirm that the server is running Paper 1.21 or a compatible version.

## Use

1. Place a fishing rod in the player's off hand.
2. Right-click with the off-hand fishing rod to cast the off-hand hook.
3. Right-click again with the off-hand fishing rod to reel it in.
4. Use a fishing rod in the main hand normally at the same time.
5. When the off-hand hook catches fish or loot, the plugin generates fishing loot, drops it in the world, and pulls the drops toward the player.
6. The off-hand rod loses durability when reeled in and is removed when its durability is exhausted.

The off-hand hook is removed when the player quits, changes worlds, or changes held items. Spectators cannot cast an off-hand hook.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/e28bfc23-2575-4e8e-b0f0-a42a94383d5b?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=e28bfc23-2575-4e8e-b0f0-a42a94383d5b
