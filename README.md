# NBTEditor
A utility class for editing the NBT tags of items, skulls, mobs, and tile entities

View the original spigot resource page [here](https://www.spigotmc.org/threads/269621/).

## Maven
Include this in your dependencies:
```xml
<dependency>
    <groupId>io.github.bananapuncher714</groupId>
    <artifactId>nbteditor</artifactId>
    <version>7.20.3</version>
</dependency>
```

You'll need to include the [CodeMC](https://ci.codemc.io/) Maven repository too:
```xml
<repository>
    <id>CodeMC</id>
    <url>https://repo.codemc.org/repository/maven-public/</url>
</repository>
```

### Github Packages
Alternatively, if you choose, you can use Github Packages instead. Make sure to have an authentication token in your .m2/settings.xml for the id `github`. To use this project with maven, add this to your `pom.xml`:
```xml
<repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/BananaPuncher714/NBTEditor</url>
</repository>
```

## Building
If you would like to install and build locally, you can clone and run the `install.sh` script. You will need to do several things beforehand:
- Have docker permissions
- Export the environment variable M2_DIRECTORY to point at your maven m2 location(such as `~/.m2`)