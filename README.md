![Better Selective Combat](./logo.png)

<div align="center">

  Lets you choose which weapons should bypass Better Combat and use their original combat behavior instead.

</div>

## Commands

| Command                    | Description                         |
| -------------------------- | ----------------------------------- |
| `/bsc`                     | Show the mod version                |
| `/bsc help`                | Show available commands             |
| `/bsc disable <weapon_id>` | Disable a weapon                    |
| `/bsc enable <weapon_id>`  | Enable a previously disabled weapon |
| `/bsc status <weapon_id>`  | Show whether a weapon is disabled   |
| `/bsc list [page]`         | List disabled weapons               |
| `/bsc reload`              | Reload the config file              |

`disable`, `enable`, and `reload` require `better_selective_combat.manage` through [LuckPerms](https://github.com/LuckPerms/LuckPerms) or op permission.

## Config

Weapon IDs use the standard `namespace:path` format, for example:

```text
minecraft:diamond_sword
```

The server config file is stored at:

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

After editing it manually, use `/bsc reload`

Client preferences are stored separately at:

```text
config/betterselectivecombat/client.json
```

Players who are already connected must reconnect for changes to take effect on their client.

## License

[GNU General Public License v3.0](LICENSE)
