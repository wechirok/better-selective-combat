![Better Selective Combat](./logo.png)

<div align="center">

Lets you choose which weapons should bypass Better Combat and use their original combat behavior instead.

### Use `.` to toggle Better Combat!

### Use `,` to toggle Better Combat for the held item!

You can rebind both keys in the Controls settings.

</div>

## Multiplayer commands

These commands manage which weapons bypass Better Combat for all players.

| Command                    | Description                                   |
| -------------------------- | --------------------------------------------- |
| `/bsc`                     | Show the mod version                          |
| `/bsc help`                | Show available commands                       |
| `/bsc disable <weapon_id>` | Disable Better Combat for a weapon            |
| `/bsc disable held`        | Disable Better Combat for the held item       |
| `/bsc enable <weapon_id>`  | Re-enable Better Combat for a disabled weapon |
| `/bsc enable held`         | Re-enable Better Combat for the held item     |
| `/bsc status <weapon_id>`  | Show whether a weapon is disabled             |
| `/bsc status held`         | Show whether the held item is disabled        |
| `/bsc list [page]`         | List disabled weapons                         |
| `/bsc reload`              | Reload the multiplayer config file            |

`disable`, `enable`, and `reload` require `better_selective_combat.manage` through [LuckPerms](https://github.com/LuckPerms/LuckPerms) or op permission.

> [!NOTE]
> ## Known issues
> - On servers without BSC, disabling Better Combat for a two-handed weapon on the client cannot restore offhand use. The server still applies Better Combat’s two-handed restriction, so BSC must be installed on both the client and server for the offhand fix to work.

## Config

The multiplayer config file is stored at:

```text
config/betterselectivecombat/server.json
```

Example:

```json
{
  "disabled_weapons": [
    "minecraft:diamond_sword",
    "examplemod:reallysuperdoopergreatsword"
  ]
}
```

The file is created automatically. You can edit it directly or use `/bsc disable` and `/bsc enable`.

After editing it manually, use `/bsc reload`.

Players who are already connected must reconnect for changes to take effect on their client.

Client preferences are stored separately at:

```text
config/betterselectivecombat/client.json
```

Example:

```json
{
  "enabled": true,
  "ignored_weapons": [
    "minecraft:diamond_sword"
  ]
}
```

Client preferences are updated automatically and reconnect is not required.

## License

[GNU General Public License v3.0](LICENSE)
