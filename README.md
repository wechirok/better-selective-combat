# Better Selective Combat

Better Selective Combat lets server owners and singleplayer users exclude individual weapons from Better Combat by exact item ID.

The mod supports Fabric and NeoForge on every Minecraft release officially supported by Better Combat from 1.21.1 through 26.2. It can be installed on a dedicated server without requiring connecting clients to install it. Installing it in a client instance enables it for singleplayer through Minecraft's integrated server.

Disabled weapons can be managed with `/bsc` commands or by editing `config/better-selective-combat/disabled-weapons.json`. Changes made while players are connected require those players to reconnect before their Better Combat registry reflects the new selection.

Run `./gradlew buildAll` to build every supported artifact. Individual targets can be built with commands such as `./gradlew :mc1211-neoforge:build` or `./gradlew :mc262-fabric:build`.
