
<p align="center">
  <img src="img/Logo.png" width="250" height="250" />
</p>

# CourierPrime

---

### About

CourierPrime is a physical mail system for Spigot/Paper Minecraft servers that allows users to send letters in the form of books
and receive them through couriers. Admins can use this to send letters to all players at once as a way of sending a
message that people will be sure to see.


### Purpose

CourierPrime is maintaining fork of a Spigot plugin
called [CourierNew](https://github.com/jeremynoesen/CourierNew). Many projects nowadays rely on this plugin,
and recent Minecraft updates, broke some mechanics of the plugin. This fork is made with this in mind, with the intention
of keeping the plugin accessible for free, forever.

---

## Usage

`< >` - Mandatory arguments
<br>
`( )` - Optional arguments

- `/letter <message>` - Compose a new letter with the specified message. Minecraft color codes and `\n` linebreaks are
  allowed in the message. If holding a not-sent letter written by you, this command will append the specified message to
  the letter.
- `/anonletter <message>` - Similar to `/letter`, but the book doesn't contain specifications about who wrote the letter
- `/post <player>` - Send a letter to a specified player. You can list multiple players by separating their usernames
  with a comma. Use the command with only `*` to send to all online players, or `**` to send to all players who have
  ever joined the server.
- `/inspect` - Shows a message containing information about a letter item
- `/unread` - Retrieve unread mail, if any.
- `/shred (all)` - Delete the letter in your hand, or all the letters in your inventory.
- `/courierprime help` - Show the help message.
- `/courieradmin <reload/block/unblock>` - Admin utility command
- `/courier <select/set> <EntityType/player> (EntityType)` - Change the EntityType used for a player's courier.

---

## Permissions

- `courierprime.letter` - Allows players to write/edit letters
- `courierprime.letter.anonymous` - Allows players to write anonymous letters
- `courierprime.post.one` - Allows players to send letters to one player at a time
- `courierprime.post.multiple` - Allows players to send letters to multiple players at a time
- `courierprime.post.allonline` - Allows players to send letters to all online players
- `courierprime.post.all` - Allows players to send letters to all players who ever joined the server
- `courierprime.inspect` - Allows the player to get information about a letter
- `courierprime.unread` - Allows players to retrieve unread mail
- `courierprime.shred` - Allows players to shred a letter
- `courierprime.help` - Allows players to use the help command
- `courierprime.courier.select` - Allows players to select their currier EntityType
- `courierprime.admin` - Allows for reloading of configs and other admin commands

---

### Requirements

- Spigot or Paper 1.21
- Java 21 or higher


### Installation

1. Download the latest release.
2. Put the jar in your plugins folder.
3. Start or restart your server.

---

## Configuration

After running for the first time, the default configs will be generated. The main configuration will look like this:

```yaml
lang: "en-us"

receive-delay: 100
resend-delay: 2400
remove-delay: 200
spawn-distance: 5
default-courier-entity-type: VILLAGER
blocked-gamemodes: []
blocked-worlds: []


letter:
  # if the creation of anonymous letters is enabled.
  anonymous-letters-enabled: true     # default: true

  # if the letter items will support custom model data
  use-custom-model-data: false        # default: false

  # custom model data of letter items.
  # only works if use-custom-model-data is set to true
  letter-custom-model-data: 0
  anon-letter-custom-model-data: 0

# Add any EntityType you'd like your players be able to use as Couriers
# List of EntityTypes https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html
enabled-courier-types:
  - COW
  - SHEEP
  - PIG
  ...
```
- `lang` - Name of the language file the plugin will load. Available languages can be foung in the `/lang` directory
- `receive-delay` - This is the delay, in ticks, for when a letter should be received. This is used on join, after
  sending, and after leaving a blocked world or gamemode.
- `resend-delay` - How long to wait, in ticks, before trying to resend a letter when the mail was not taken
- `remove-delay` - How long to wait, in ticks, after the courier spawns before removing it
- `spawn-distance` - How far away to spawn the courier from the player in blocks
- `default-courier-entity-type` - The entity type used to spawn a courier for a player that hasn't set their priority using `/courier select`
- `blocked-gamemodes` - Gamemodes that receiving mail isn't allowed in. (SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR)
- `blocked-worlds` - Names of worlds that receiving is blocked in
- `anonymous-letters-enabled` - Rather or not the `/anonletter` command will be registered by the plugin. Changes to this config option might require a restart to be applied correctly
- `use-custom-model-data` - Rather or not will the plugin apply CustomModelData to the created letters
- `letter-custom-model-data` - Regular letters' CustomModelData.
- `anon-letter-custom-model-data` - Anonymous letters' CustomModelData.
- `enabled-courier-types` - A list of EntityTypes accepted by the `/courier select` command.

`letter-custom-model-data` and `anon-letter-custom-model-data` will only work if `use-custom-model-data` is set to true

For the message configuration, you can use color codes. You can also use the placeholder `$PLAYER$` in messages that
have it by default, to replace it with player name(s). The messages and their names should explain what they are used
for.

You can create a new language yml file, and load it on the config, as long as it contains all messages.

The third configuration file is actually used to store outgoing mail. Don't modify this file unless you know exactly
what you are doing!


---

## Demonstration

<div align="center" ><img src="img/demo.gif" alt="Demonstration" title="Demonstration" /></div>

--- 

## Troubleshooting

Courier not spawning?

- In the WorldGuard config, set `block-plugin-spawning` to false.
- Set `allow-npcs` to true in `server.properties`.
- If you are using EssentialsProtect, make sure to not block villager spawning.
- Also check to make sure that no other plugins block mob spawning.


---

## Building

1. Clone the repository:
   ```bash
   git clone https://github.com/joaorihan/CourierPrime.git && cd CourierPrime
   ```

2. Build the plugin:
   ```bash
   mvn clean package
   ```

3. Find the `.jar` file in the `target` directory and place it in your server's `plugins` folder.


---

## Contributing

We welcome contributions to **CourierPrime**! To get started:

1. **Fork the Repository**  
   Click the **Fork** button at the top right of this page to create your own copy of the repository.

2. **Clone Your Fork**  
   Clone your forked repository to your local machine:
   ```bash
   git clone https://github.com/joaorihan/CourierPrime.git
   cd CourierPrime
   ```

3. **Create a New Branch**  
   Create a feature branch for your changes:
   ```bash
   git checkout -b feature/your-feature-name
   ```

4. **Make Your Changes**  
   Implement your feature or bug fix. Be sure to follow the project's coding conventions and test your changes.

5. **Commit Your Changes**  
   Commit your changes with a clear and descriptive message:
   ```bash
   git commit -m "feat: Add your feature or fix"
   ```

6. **Push to Your Fork**  
   Push your branch to your forked repository:
   ```bash
   git push origin feature/your-feature-name
   ```

7. **Open a Pull Request**  
   Go to the original repository and open a pull request from your branch. Be sure to describe your changes in detail.

### Guidelines

- Write clear and concise commit messages.
- Ensure your code adheres to the project's style and standards.
- Test your changes before submitting a pull request.

Thank you for contributing!

---
