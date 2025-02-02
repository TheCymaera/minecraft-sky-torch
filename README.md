# Sky Torch
## Introduction
This plugin was developed for a video:
[Orbital Laser in Minecraft (Sky Torch)](https://youtu.be/OKXTGbp6AMk)

## Installation
1. Download the JAR from the [releases page](https://github.com/TheCymaera/minecraft-sky-torch/releases/).
2. Set up a [Paper](https://papermc.io/downloads) or [Spigot](https://getbukkit.org/download/spigot) server. (Instructions below)
3. Add the JAR to the `plugins` folder.
4. Download the world folder from [Planet Minecraft](https://www.planetminecraft.com/project/ambertry-forest/).
	- This is optional. You can use any world you like.
5. Place the world folder in the server directory. Name it `world`.

## Running a Server
1. Download a server JAR from [Paper](https://papermc.io/downloads) or [Spigot](https://getbukkit.org/download/spigot).
2. Run the following command `java -Xmx1024M -Xms1024M -jar server.jar nogui`.
3. I typically use the Java runtime bundled with my Minecraft installation so as to avoid version conflicts.
   - In Modrinth, you can find the Java runtime location inside the profile options menu.
4. Accept the EULA by changing `eula=false` to `eula=true` in the `eula.txt` file.
5. Join the server with `localhost` as the IP address.


## Commands
Autocomplete will show available options.
```
# Get control items
/items

# Load an options preset
/preset <preset:enum>

# Reload options (after editing the config file manually)
/reload_config

# Change the laser's scale
/scale <scale:double>
```

## Development
1. Clone or download the repo.
2. Run Maven `package` to build the plugin. The resulting JAR will be in the `target` folder.
3. For convenience, set up a symlink and add the link to the server `plugins` folder.
   - Windows: `mklink /D newFile.jar originalFile.jar`
   - Mac/Linux: `ln -s originalFile.jar newFile.jar `

## License
You may use the plugin and source code for both commercial or non-commercial purposes.

Attribution is appreciated but not due.

Do not resell without making substantial changes.