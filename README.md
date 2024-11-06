
<p align="center">
  <img src="img/Logo.png" width="250" height="250" />
</p>

# CourierPrime


## About

CourierPrime is a physical mail system for Spigot/Paper Minecraft servers that allows users to send letters in the form of books
and receive them through couriers. Admins can use this to send letters to all players at once as a way of sending a
message that people will be sure to see.

## Purpose

CourierPrime is maintaining fork of a Spigot plugin
called [CourierNew](https://github.com/jeremynoesen/CourierNew). Many projects nowadays rely on this plugin,
and recend Minecraft updates, broke some mechanics of the plugin. This fork is made with this in mind, with the intention
of keeping the plugin accessible for free, forever.

## Usage

- `/letter <message>` - Compose a new letter with the specified message. Minecraft color codes and \n linebreaks are
  allowed in the message. If holding a not-sent letter written by you, this command will append the specified message to
  the letter.
- `/post <player>` - Send a letter to a specified player. You can list multiple players by separating their usernames
  with a comma. Use the command with only `*` to send to all online players, or `**` to send to all players who have
  ever joined the server.
- `/unread` - Retrieve unread mail, if any.
- `/shred` - Delete the letter in your hand.
- `/shredall` - Delete all letters in your inventory.
- `/couriernew help` - Show the help message.
- `/couriernew reload` - Reload all configuration files.

## Requirements

- Spigot or Paper 1.21
- Java 21 or higher

## Installation

1. Download the latest release.
2. Put the jar in your plugins folder.
3. Start or restart your server.

## Configuration

After running for the first time, the default configs will be generated. The main configuration will look like this:

```yaml
receive-delay: 100
resend-delay: 2400
remove-delay: 200
spawn-distance: 5
courier-entity-type: VILLAGER
blocked-gamemodes: []
blocked-worlds: []
```

- `receive-delay` - This is the delay, in ticks, for when a letter should be received. This is used on join, after
  sending, and after leaving a blocked world or gamemode.
- `resend-delay` - How long to wait, in ticks, before trying to resend a letter when the mail was not taken
- `remove-delay` - How long to wait, in ticks, after the courier spawns before removing it
- `spawn-distance` - How far away to spawn the courier from the player in blocks
- `blocked-gamemodes` - Gamemodes that receiving mail isn't allowed in. (SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR)
- `blocked-worlds` - Names of worlds that receiving is blocked in

For the message configuration, you can use color codes. You can also use the placeholder `$PLAYER$` in messages that
have it by default to replace it with player name(s). The messages and their names should explain what they are used
for.

The third configuration file is actually used to store outgoing mail. Don't modify this file unless you know exactly
what you are doing!

### Permissions

- `couriernew.letter` - Allows players to write/edit letters
- `couriernew.post.one` - Allows players to send letters to one player at a time
- `couriernew.post.multiple` - Allows players to send letters to multiple players at a time
- `couriernew.post.allonline` - Allows players to send letters to all online players
- `couriernew.post.all` - Allows players to send letters to all players who ever joined the server
- `couriernew.unread` - Allows players to retrieve unread mail
- `couriernew.shred` - Allows players to shred a letter
- `couriernew.shredall` - Allows players to shred all in their inventory
- `couriernew.help` - Allows players to use the help command
- `couriernew.reload` - Allows for reloading of configs

## Demonstration

<div align="center" ><img src="img/demo.gif" alt="Demonstration" title="Demonstration" /></div>

## Building



## Troubleshooting

Courier not spawning?

- In the WorldGuard config, set `block-plugin-spawning` to false.
- Set `allow-npcs` to true in `server.properties`.
- If you are using EssentialsProtect, make sure to not block villager spawning.
- Also check to make sure that no other plugins block mob spawning.
