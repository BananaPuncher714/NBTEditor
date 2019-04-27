package io.github.bananapuncher714.nbteditor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

/**
 * Sets/Gets NBT tags from ItemStacks
 * Supports 1.8-1.14
 *
 * Github: https://github.com/BananaPuncher714/NBTEditor
 * Spigot: https://www.spigotmc.org/threads/single-class-nbt-editor-for-items-skulls-mobs-and-tile-entities-1-8-1-13.269621/
 *
 * @version 7.5
 * @author BananaPuncher714
 */
public final class NBTEditor {
    private static final Map< String, Class<?> > classCache;
    private static final Map< String, Method > methodCache;
    private static final Map< Class< ? >, Constructor< ? > > constructorCache;
    private static final Map< Class< ? >, Class< ? > > NBTClasses;
    private static final Map< Class< ? >, Field > NBTTagFieldCache;
    private static  Field NBTListData;
    private static Field NBTCompoundMap;
    private static final String VERSION;

    static {
        VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        classCache = new HashMap<>();
        try {
            classCache.put( "NBTBase", Class.forName( "net.minecraft.server." + VERSION + "." + "NBTBase" ) );
            classCache.put( "NBTTagCompound", Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagCompound" ) );
            classCache.put( "NBTTagList", Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagList" ) );

            classCache.put( "ItemStack", Class.forName( "net.minecraft.server." + VERSION + "." + "ItemStack" ) );
            classCache.put( "CraftItemStack", Class.forName( "org.bukkit.craftbukkit." + VERSION + ".inventory." + "CraftItemStack" ) );

            classCache.put( "Entity", Class.forName( "net.minecraft.server." + VERSION + "." + "Entity" ) );
            classCache.put( "CraftEntity", Class.forName( "org.bukkit.craftbukkit." + VERSION + ".entity." + "CraftEntity" ) );
            classCache.put( "EntityLiving", Class.forName( "net.minecraft.server." + VERSION + "." + "EntityLiving" ) );

            classCache.put( "CraftWorld", Class.forName( "org.bukkit.craftbukkit." + VERSION + "." + "CraftWorld" ) );
            classCache.put( "CraftBlockState", Class.forName( "org.bukkit.craftbukkit." + VERSION + ".block." + "CraftBlockState" ) );
            classCache.put( "BlockPosition", Class.forName( "net.minecraft.server." + VERSION + "." + "BlockPosition" ) );
            classCache.put( "TileEntity", Class.forName( "net.minecraft.server." + VERSION + "." + "TileEntity" ) );
            classCache.put( "World", Class.forName( "net.minecraft.server." + VERSION + "." + "World" ) );

            classCache.put( "TileEntitySkull", Class.forName( "net.minecraft.server." + VERSION + "." + "TileEntitySkull" ) );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        NBTClasses = new HashMap<>();
        try {
            NBTClasses.put( Byte.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagByte" ) );
            NBTClasses.put( String.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagString" ) );
            NBTClasses.put( Double.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagDouble" ) );
            NBTClasses.put( Integer.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagInt" ) );
            NBTClasses.put( Long.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagLong" ) );
            NBTClasses.put( Short.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagShort" ) );
            NBTClasses.put( Float.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagFloat" ) );
            NBTClasses.put( Class.forName( "[B" ), Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagByteArray" ) );
            NBTClasses.put( Class.forName( "[I" ), Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagIntArray" ) );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        methodCache = new HashMap<>();
        try {
            methodCache.put( "get", getNMSClass( "NBTTagCompound" ).getMethod( "get", String.class ) );
            methodCache.put( "set", getNMSClass( "NBTTagCompound" ).getMethod( "set", String.class, getNMSClass( "NBTBase" ) ) );
            methodCache.put( "hasKey", getNMSClass( "NBTTagCompound" ).getMethod( "hasKey", String.class ) );
            methodCache.put( "setIndex", getNMSClass( "NBTTagList" ).getMethod( "a", int.class, getNMSClass( "NBTBase" ) ) );
            if ( VERSION.contains( "1_14" ) ) {
                methodCache.put( "getTypeId", getNMSClass( "NBTBase" ).getMethod( "getTypeId" ) );
                methodCache.put( "add", getNMSClass( "NBTTagList" ).getMethod( "add", int.class, getNMSClass( "NBTBase" ) ) );
            } else {
                methodCache.put( "add", getNMSClass( "NBTTagList" ).getMethod( "add", getNMSClass( "NBTBase" ) ) );
            }

            if ( VERSION.contains( "1_8" ) ) {
                methodCache.put( "listRemove", getNMSClass( "NBTTagList" ).getMethod( "a", int.class )  );
            } else {
                methodCache.put( "listRemove", getNMSClass( "NBTTagList" ).getMethod( "remove", int.class )  );
            }
            methodCache.put( "remove", getNMSClass( "NBTTagCompound" ).getMethod( "remove", String.class ) );

            methodCache.put( "hasTag", getNMSClass( "ItemStack" ).getMethod( "hasTag" ) );
            methodCache.put( "getTag", getNMSClass( "ItemStack" ).getMethod( "getTag" ) );
            methodCache.put( "setTag", getNMSClass( "ItemStack" ).getMethod( "setTag", getNMSClass( "NBTTagCompound" ) ) );
            methodCache.put( "asNMSCopy", getNMSClass( "CraftItemStack" ).getMethod( "asNMSCopy", ItemStack.class ) );
            methodCache.put( "asBukkitCopy", getNMSClass( "CraftItemStack" ).getMethod( "asBukkitCopy", getNMSClass( "ItemStack" ) ) );

            methodCache.put( "getEntityHandle", getNMSClass( "CraftEntity" ).getMethod( "getHandle" ) );
            methodCache.put( "getEntityTag", getNMSClass( "Entity" ).getMethod( "c", getNMSClass( "NBTTagCompound" ) ) );
            methodCache.put( "setEntityTag", getNMSClass( "Entity" ).getMethod( "f", getNMSClass( "NBTTagCompound" ) ) );

            if ( VERSION.contains( "1_12" ) || VERSION.contains( "1_13" ) || VERSION.contains( "1_14" ) ) {
                methodCache.put( "setTileTag", getNMSClass( "TileEntity" ).getMethod( "load", getNMSClass( "NBTTagCompound" ) ) );
            } else {
                methodCache.put( "setTileTag", getNMSClass( "TileEntity" ).getMethod( "a", getNMSClass( "NBTTagCompound" ) ) );
            }
            methodCache.put( "getTileEntity", getNMSClass( "World" ).getMethod( "getTileEntity", getNMSClass( "BlockPosition" ) ) );
            methodCache.put( "getWorldHandle", getNMSClass( "CraftWorld" ).getMethod( "getHandle" ) );

            methodCache.put( "setGameProfile", getNMSClass( "TileEntitySkull" ).getMethod( "setGameProfile", GameProfile.class ) );
        } catch( Exception e ) {
            e.printStackTrace();
        }

        try {
            methodCache.put( "getTileTag", getNMSClass( "TileEntity" ).getMethod( "save", getNMSClass( "NBTTagCompound" ) ) );
        } catch( NoSuchMethodException exception ) {
            try {
                methodCache.put( "getTileTag", getNMSClass( "TileEntity" ).getMethod( "b", getNMSClass( "NBTTagCompound" ) ) );
            } catch ( Exception exception2 ) {
                exception2.printStackTrace();
            }
        } catch( Exception exception ) {
            exception.printStackTrace();
        }

        constructorCache = new HashMap<>();
        try {
            constructorCache.put( getNBTTag( Byte.class ), getNBTTag( Byte.class ).getConstructor( byte.class ) );
            constructorCache.put( getNBTTag( String.class ), getNBTTag( String.class ).getConstructor( String.class ) );
            constructorCache.put( getNBTTag( Double.class ), getNBTTag( Double.class ).getConstructor( double.class ) );
            constructorCache.put( getNBTTag( Integer.class ), getNBTTag( Integer.class ).getConstructor( int.class ) );
            constructorCache.put( getNBTTag( Long.class ), getNBTTag( Long.class ).getConstructor( long.class ) );
            constructorCache.put( getNBTTag( Float.class ), getNBTTag( Float.class ).getConstructor( float.class ) );
            constructorCache.put( getNBTTag( Short.class ), getNBTTag( Short.class ).getConstructor( short.class ) );
            constructorCache.put( getNBTTag( Class.forName( "[B" ) ), getNBTTag( Class.forName( "[B" ) ).getConstructor( Class.forName( "[B" ) ) );
            constructorCache.put( getNBTTag( Class.forName( "[I" ) ), getNBTTag( Class.forName( "[I" ) ).getConstructor( Class.forName( "[I" ) ) );

            constructorCache.put( getNMSClass( "BlockPosition" ), getNMSClass( "BlockPosition" ).getConstructor( int.class, int.class, int.class ) );
        } catch( Exception e ) {
            e.printStackTrace();
        }

        NBTTagFieldCache = new HashMap<>();
        try {
            for ( Class< ? > clazz : NBTClasses.values() ) {
                Field data = clazz.getDeclaredField( "data" );
                data.setAccessible( true );
                NBTTagFieldCache.put( clazz, data );
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }

        try {
            NBTListData = getNMSClass( "NBTTagList" ).getDeclaredField( "list" );
            NBTListData.setAccessible( true );
            NBTCompoundMap = getNMSClass( "NBTTagCompound" ).getDeclaredField( "map" );
            NBTCompoundMap.setAccessible( true );
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

    private static Class< ? > getNBTTag( Class< ? > primitiveType ) {
        if ( NBTClasses.containsKey( primitiveType ) )
            return NBTClasses.get( primitiveType );
        return primitiveType;
    }

    private static Object getNBTVar( Object object ) {
        if ( object == null ) {
            return null;
        }
        Class< ? > clazz = object.getClass();
        try {
            if ( NBTTagFieldCache.containsKey( clazz ) ) {
                return NBTTagFieldCache.get( clazz ).get( object );
            }
        } catch ( Exception exception ) {
            exception.printStackTrace();
        }
        return null;
    }

    private static Method getMethod( String name ) {
        return methodCache.getOrDefault(name, null);
    }

    private static Constructor< ? > getConstructor( Class< ? > clazz ) {
        return constructorCache.getOrDefault(clazz, null);
    }

    private static Class<?> getNMSClass(String name) {
        Class<?> clazz = classCache.get( name );

        if (clazz == null) {
            try {
                clazz = Class.forName("net.minecraft.server." + VERSION + "." + name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return clazz;
    }

    private static String getMatch( String string, String regex ) {
        Pattern pattern = Pattern.compile( regex );
        Matcher matcher = pattern.matcher( string );

        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Gets the Bukkit version
     *
     * @return
     * The Bukkit version in standard package format
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Creates a skull with the given url as the skin
     *
     * @param skinURL
     * The URL of the skin, must be from mojang
     * @return
     * An item stack with count of 1
     */
    public static ItemStack getHead(String skinURL ) {
        Material material = Material.getMaterial( "SKULL_ITEM" );
        if ( material == null ) {
            // Most likely 1.13 materials
            material = Material.getMaterial( "PLAYER_HEAD" );
        }
        ItemStack head = new ItemStack( material, 1, ( short ) 3 );
        if ( skinURL == null || skinURL.isEmpty() ) {
            return head;
        }
        ItemMeta headMeta = head.getItemMeta();
        GameProfile profile = new GameProfile( UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64( String.format( "{textures:{SKIN:{\"url\":\"%s\"}}}", skinURL ).getBytes() );
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
        } catch ( NoSuchFieldException | SecurityException e ) {
            e.printStackTrace();
        }
        profileField.setAccessible(true);
        try {
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }

    /**
     * Fetches the texture of a skull
     *
     * @param head
     * The item stack itself
     * @return
     * The URL of the texture
     */
    public static String getTexture(ItemStack head ) {
        ItemMeta meta = head.getItemMeta();
        Field profileField;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
        } catch ( NoSuchFieldException | SecurityException e ) {
            e.printStackTrace();
            throw new IllegalArgumentException( "Item is not a player skull!" );
        }
        profileField.setAccessible(true);
        try {
            GameProfile profile = ( GameProfile ) profileField.get( meta );
            if ( profile == null ) {
                return null;
            }

            for ( Property prop : profile.getProperties().values() ) {
                if ( prop.getName().equals( "textures" ) ) {
                    String texture = new String( Base64.decodeBase64( prop.getValue() ) );
                    return getMatch( texture, "\\{\"url\":\"(.*?)\"\\}" );
                }
            }
            return null;
        } catch ( IllegalArgumentException | IllegalAccessException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @deprecated
     *
     * Gets an NBT tag in a given item with the specified keys
     *
     * @param item
     * The itemstack to get the keys from
     * @param key
     * The keys to fetch; an integer after a key value indicates that it should get the nth place of
     * the previous compound because it is a list;
     * @return
     * The item represented by the keys, and an integer if it is showing how long a list is.
     */
    public static Object getItemTag(ItemStack item, Object... keys ) {
        if ( item == null ) {
            return null;
        }
        try {
            Object stack;
            stack = getMethod( "asNMSCopy" ).invoke( null, item );

            Object tag;

            if ( getMethod( "hasTag" ).invoke( stack ).equals( true ) ) {
                tag = getMethod( "getTag" ).invoke( stack );
            } else {
                tag = getNMSClass( "NBTTagCompound" ).newInstance();
            }

            return getTag( tag, keys );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Gets an NBTCompound from the item provided
     *
     * @param item
     * Itemstack
     * @param keys
     * Keys in descending order
     * @return
     * An NBTCompound
     */
    public static NBTCompound getItemNBTTag(ItemStack item, Object... keys ) {
        if ( item == null ) {
            return null;
        }
        try {
            Object stack;
            stack = getMethod( "asNMSCopy" ).invoke( null, item );

            Object tag;

            if ( getMethod( "hasTag" ).invoke( stack ).equals( true ) ) {
                tag = getMethod( "getTag" ).invoke( stack );
            } else {
                tag = getNMSClass( "NBTTagCompound" ).newInstance();
                Object count = getConstructor( getNBTTag( Integer.class ) ).newInstance( item.getAmount() );
                getMethod( "set" ).invoke( tag, "Count", count );
                Object id = getConstructor( getNBTTag( String.class ) ).newInstance( item.getType().name().toLowerCase() );
                getMethod( "set" ).invoke( tag, "id", id );
            }

            return getNBTTag( tag, keys );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * @deprecated
     *
     * Sets an NBT tag in an item with the provided keys and value
     *
     * @param item
     * The itemstack to set
     * @param key
     * The keys to set, String for NBTCompound, int or null for an NBTTagList
     * @param value
     * The value to set
     * @return
     * A new ItemStack with the updated NBT tags
     */
    public static ItemStack setItemTag(ItemStack item, Object value, Object... keys ) {
        if ( item == null ) {
            return null;
        }
        try {
            Object stack = getMethod( "asNMSCopy" ).invoke( null, item );

            Object tag = getMethod( "hasTag" ).invoke( stack ).equals( true ) ? getMethod( "getTag" ).invoke( stack ) getNMSClass( "NBTTagCompound" ).newInstance():

            setTag( tag, value, keys );
            getMethod( "setTag" ).invoke( stack, tag );
            return ( ItemStack ) getMethod( "asBukkitCopy" ).invoke( null, stack );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Constructs an ItemStack from a given NBTCompound
     *
     * @param compound
     * An NBTCompound following an ItemStack structure
     * @return
     * A new ItemStack
     */
    public static ItemStack getItemFromTag(NBTCompound compound ) {
        if ( compound == null ) {
            return null;
        }
        try {
            Object tag = compound.tag;
            Object count = getTag( tag, "Count" );
            Object id = getTag( tag, "id" );
            if ( count == null || id == null ) {
                return null;
            }
            if ( count instanceof Byte && id instanceof String ) {
                int amount = ( byte ) count;
                String material = ( String ) id;
                Material type = Material.valueOf( material.substring( material.indexOf( ":" ) + 1 ).toUpperCase() );
                return NBTEditor.setItemTag( new ItemStack( type, amount ), tag );
            }
            return null;
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * @deprecated
     *
     * Gets an NBT tag in a given entity with the specified keys
     *
     * @param block
     * The entity to get the keys from
     * @param key
     * The keys to fetch; an integer after a key value indicates that it should get the nth place of
     * the previous compound because it is a list;
     * @return
     * The item represented by the keys, and an integer if it is showing how long a list is.
     */
    public static Object getEntityTag(Entity entity, Object... keys ) {
        if ( entity == null ) {
            return entity;
        }
        try {
            Object NMSEntity = getMethod( "getEntityHandle" ).invoke( entity );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

            getMethod( "getEntityTag" ).invoke( NMSEntity, tag );

            return getTag( tag, keys );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Gets an NBTCompound from the entity provided
     *
     * @param entity
     * The Bukkit entity provided
     * @param keys
     * Keys in descending order
     * @return
     * An NBTCompound
     */
    public static NBTCompound getEntityNBTTag(Entity entity, Object...keys ) {
        if ( entity == null ) {
            return null;
        }
        try {
            Object NMSEntity = getMethod( "getEntityHandle" ).invoke( entity );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

            getMethod( "getEntityTag" ).invoke( NMSEntity, tag );

            return getNBTTag( tag, keys );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * @deprecated
     *
     * Sets an NBT tag in an entity with the provided keys and value
     *
     * @param item
     * The entity to set
     * @param key
     * The keys to set, String for NBTCompound, int or null for an NBTTagList
     * @param value
     * The value to set
     * @return
     * A new ItemStack with the updated NBT tags
     */
    public static void setEntityTag(Entity entity, Object value, Object... keys ) {
        if ( entity == null ) {
            return;
        }
        try {
            Object NMSEntity = getMethod( "getEntityHandle" ).invoke( entity );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance() ;

            getMethod( "getEntityTag" ).invoke( NMSEntity, tag );

            setTag( tag, value, keys );

            getMethod( "setEntityTag" ).invoke( NMSEntity, tag );
        } catch ( Exception exception ) {
            exception.printStackTrace();
        }
    }

    /**
     * @deprecated
     *
     * Gets an NBT tag in a given block with the specified keys
     *
     * @param block
     * The block to get the keys from
     * @param key
     * The keys to fetch; an integer after a key value indicates that it should get the nth place of
     * the previous compound because it is a list;
     * @return
     * The item represented by the keys, and an integer if it is showing how long a list is.
     */
    public static Object getBlockTag(Block block, Object... keys ) {
        try {
            if ( block == null || !getNMSClass( "CraftBlockState" ).isInstance( block.getState() ) ) {
                return null;
            }
            Location location = block.getLocation();

            Object blockPosition = getConstructor( getNMSClass( "BlockPosition" ) ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

            Object nmsWorld = getMethod( "getWorldHandle" ).invoke( location.getWorld() );

            Object tileEntity = getMethod( "getTileEntity" ).invoke( nmsWorld, blockPosition );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

            getMethod( "getTileTag" ).invoke( tileEntity, tag );

            return getTag( tag, keys );
        } catch( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Gets an NBTCompound from the block provided
     *
     * @param block
     * The block provided
     * @param keys
     * Keys in descending order
     * @return
     * An NBTCompound
     */
    public static Object getBlockNBTTag(Block block, Object... keys ) {
        try {
            if ( block == null || !getNMSClass( "CraftBlockState" ).isInstance( block.getState() ) ) {
                return null;
            }
            Location location = block.getLocation();

            Object blockPosition = getConstructor( getNMSClass( "BlockPosition" ) ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

            Object nmsWorld = getMethod( "getWorldHandle" ).invoke( location.getWorld() );

            Object tileEntity = getMethod( "getTileEntity" ).invoke( nmsWorld, blockPosition );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

            getMethod( "getTileTag" ).invoke( tileEntity, tag );

            return getNBTTag( tag, keys );
        } catch( Exception exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * @deprecated
     *
     * Sets an NBT tag in an block with the provided keys and value
     *
     * @param item
     * The block to set
     * @param key
     * The keys to set, String for NBTCompound, int or null for an NBTTagList
     * @param value
     * The value to set
     * @return
     * A new ItemStack with the updated NBT tags
     */
    public static void setBlockTag(Block block, Object value, Object... keys ) {
        try {
            if ( block == null || !getNMSClass( "CraftBlockState" ).isInstance( block.getState() ) ) {
                return;
            }
            Location location = block.getLocation();

            Object blockPosition = getConstructor( getNMSClass( "BlockPosition" ) ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

            Object nmsWorld = getMethod( "getWorldHandle" ).invoke( location.getWorld() );

            Object tileEntity = getMethod( "getTileEntity" ).invoke( nmsWorld, blockPosition );

            Object tag = getNMSClass( "NBTTagCompound" ).newInstance();

            getMethod( "getTileTag" ).invoke( tileEntity, tag );

            setTag( tag, value, keys );

            getMethod( "setTileTag" ).invoke( tileEntity, tag );
        } catch( Exception exception ) {
            exception.printStackTrace();
        }
    }

    /**
     * Sets the texture of a skull block
     *
     * @param block
     * The block, must be a skull
     * @param texture
     * The URL of the skin
     */
    public static void setSkullTexture(Block block, String texture ) {
        GameProfile profile = new GameProfile( UUID.randomUUID(), null );
        profile.getProperties().put( "textures", new com.mojang.authlib.properties.Property( "textures", new String( Base64.encodeBase64( String.format( "{textures:{SKIN:{\"url\":\"%s\"}}}", texture ).getBytes() ) ) ) );

        try {
            Location location = block.getLocation();

            Object blockPosition = getConstructor( getNMSClass( "BlockPosition" ) ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

            Object nmsWorld = getMethod( "getWorldHandle" ).invoke( location.getWorld() );

            Object tileEntity = getMethod( "getTileEntity" ).invoke( nmsWorld, blockPosition );

            getMethod( "setGameProfile" ).invoke( tileEntity, profile );
        } catch( Exception exception ) {
            exception.printStackTrace();
        }
    }

    /**
     * Gets a string from an object
     *
     * @param object
     * Must be an ItemStack, Entity, or Block
     * @param keys
     * Keys in descending order
     * @return
     * A string, or null if none is stored at the provided location
     */
    public static String getString(Object object, Object... keys ) {
        Object result;
        if ( object instanceof ItemStack ) {
            result = getItemTag( ( ItemStack ) object, keys );
        } else if ( object instanceof Entity ) {
            result = getEntityTag( ( Entity ) object, keys );
        } else if ( object instanceof Block ) {
            result = getBlockTag( ( Block ) object, keys );
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, or Block!" );
        }
        return result instanceof String ? ( String ) result : null;
    }

    /**
     * Gets an int from an object
     *
     * @param object
     * Must be an ItemStack, Entity, or Block
     * @param keys
     * Keys in descending order
     * @return
     * An integer, or 0 if none is stored at the provided location
     */
    public static int getInt(Object object, Object... keys ) {
        Object result;
        if ( object instanceof ItemStack ) {
            result = getItemTag( ( ItemStack ) object, keys );
        } else if ( object instanceof Entity ) {
            result = getEntityTag( ( Entity ) object, keys );
        } else if ( object instanceof Block ) {
            result = getBlockTag( ( Block ) object, keys );
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, or Block!" );
        }
        return result instanceof Integer ? (int) result : 0;
    }

    /**
     * Gets a long from an object
     *
     * @param object
     * Must be an ItemStack, Entity, or Block
     * @param keys
     * Keys in descending order
     * @return
     * A long, or 0 if none is stored at the provided location
     */
    public static long getLong(Object object, Object... keys ) {
        Object result;
        if ( object instanceof ItemStack ) {
            result = getItemTag( ( ItemStack ) object, keys );
        } else if ( object instanceof Entity ) {
            result = getEntityTag( ( Entity ) object, keys );
        } else if ( object instanceof Block ) {
            result = getBlockTag( ( Block ) object, keys );
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, or Block!" );
        }
        return result instanceof Long ? ( long ) result : 0;
    }

    /**
     * Gets a float from an object
     *
     * @param object
     * Must be an ItemStack, Entity, or Block
     * @param keys
     * Keys in descending order
     * @return
     * A float, or 0 if none is stored at the provided location
     */
    public static float getFloat(Object object, Object... keys ) {
        Object result;
        if ( object instanceof ItemStack ) {
            result = getItemTag( ( ItemStack ) object, keys );
        } else if ( object instanceof Entity ) {
            result = getEntityTag( ( Entity ) object, keys );
        } else if ( object instanceof Block ) {
            result = getBlockTag( ( Block ) object, keys );
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, or Block!" );
        }
        return result instanceof Float ? ( float ) result : 0;
    }

    /**
     * Gets a short from an object
     *
     * @param object
     * Must be an ItemStack, Entity, or Block
     * @param keys
     * Keys in descending order
     * @return
     * A short, or 0 if none is stored at the provided location
     */
    public static short getShort(Object object, Object... keys ) {
        Object result;
        if ( object instanceof ItemStack ) {
            result = getItemTag( ( ItemStack ) object, keys );
        } else if ( object instanceof Entity ) {
            result = getEntityTag( ( Entity ) object, keys );
        } else if ( object instanceof Block ) {
            result = getBlockTag( ( Block ) object, keys );
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, or Block!" );
        }
        return result instanceof Short ? ( short ) result : 0;
    }

    /**
     * Gets a byte from an object
     *
     * @param object
     * Must be an ItemStack, Entity, or Block
     * @param keys
     * Keys in descending order
     * @return
     * A byte, or 0 if none is stored at the provided location
     */
    public static byte getByte(Object object, Object... keys ) {
        Object result;
        if ( object instanceof ItemStack ) {
            result = getItemTag( ( ItemStack ) object, keys );
        } else if ( object instanceof Entity ) {
            result = getEntityTag( ( Entity ) object, keys );
        } else if ( object instanceof Block ) {
            result = getBlockTag( ( Block ) object, keys );
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, or Block!" );
        }
        return result instanceof Byte ? ( byte ) result : 0;
    }

    /**
     * Gets a byte array from an object
     *
     * @param object
     * Must be an ItemStack, Entity, or Block
     * @param keys
     * Keys in descending order
     * @return
     * A byte array, or null if none is stored at the provided location
     */
    public static byte[] getByteArray(Object object, Object... keys ) {
        Object result;
        if ( object instanceof ItemStack ) {
            result = getItemTag( ( ItemStack ) object, keys );
        } else if ( object instanceof Entity ) {
            result = getEntityTag( ( Entity ) object, keys );
        } else if ( object instanceof Block ) {
            result = getBlockTag( ( Block ) object, keys );
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, or Block!" );
        }
        return result instanceof byte[] ? ( byte[] ) result : null;
    }

    /**
     * Gets an int array from an object
     *
     * @param object
     * Must be an ItemStack, Entity, or Block
     * @param keys
     * Keys in descending order
     * @return
     * An int array, or null if none is stored at the provided location
     */
    public static int[] getIntArray(Object object, Object... keys ) {
        Object result;
        if ( object instanceof ItemStack ) {
            result = getItemTag( ( ItemStack ) object, keys );
        } else if ( object instanceof Entity ) {
            result = getEntityTag( ( Entity ) object, keys );
        } else if ( object instanceof Block ) {
            result = getBlockTag( ( Block ) object, keys );
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, or Block!" );
        }
        return result instanceof int[] ? ( int[] ) result : null;
    }

    /**
     * Checks if the object contains the given key
     *
     * @param object
     * Must be an ItemStack, Entity, or Block
     * @param keys
     * Keys in descending order
     * @return
     * Whether or not the particular tag exists, may not be a primitive
     */
    public static boolean contains(Object object, Object... keys ) {
        Object result;
        if ( object instanceof ItemStack ) {
            result = getItemTag( ( ItemStack ) object, keys );
        } else if ( object instanceof Entity ) {
            result = getEntityTag( ( Entity ) object, keys );
        } else if ( object instanceof Block ) {
            result = getBlockTag( ( Block ) object, keys );
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, or Block!" );
        }
        return result != null;
    }

    /**
     * Sets the value in the object with the given keys
     *
     * @param object
     * Must be an ItemStack, Entity, or Block
     * @param value
     * The value to set, can be an NBTCompound
     * @param keys
     * The keys in descending order
     * @return
     * The new item stack if the object provided is an item, else original object
     */
    public static < T > T set(T object, Object value, Object... keys ) {
        if ( object instanceof ItemStack ) {
            return ( T ) setItemTag( ( ItemStack ) object, value, keys );
        } else if ( object instanceof Entity ) {
            setEntityTag( ( Entity ) object, value, keys );
        } else if ( object instanceof Block ) {
            setBlockTag( ( Block ) object, value, keys );
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, or Block!" );
        }
        return object;
    }

    private static void setTag( Object tag, Object value, Object... keys ) throws Exception {
        Object notCompound = value != null ? (getNMSClass( "NBTTagList" ).isInstance( value ) || getNMSClass( "NBTTagCompound" ).isInstance( value )) ? value : getConstructor( getNBTTag( value.getClass() ) ).newInstance( value ) : null;

        Object compound = tag;
        for ( int index = 0; index < keys.length; index++ ) {
            Object key = keys[ index ];
            if ( index + 1 == keys.length ) {
                if ( key == null ) {
                    if ( VERSION.contains( "1_14" ) ) {
                        int type = ( int ) getMethod( "getTypeId" ).invoke( notCompound );
                        getMethod( "add" ).invoke( compound, type, notCompound );
                    } else {
                        getMethod( "add" ).invoke( compound, notCompound );
                    }
                } else if ( key instanceof Integer ) {
                    if ( notCompound == null ) {
                        getMethod( "listRemove" ).invoke( compound, ( int ) key );
                    } else {
                        getMethod( "setIndex" ).invoke( compound, key, notCompound );
                    }
                } else {
                    if ( notCompound == null ) {
                        getMethod( "remove" ).invoke( compound, ( String ) key );
                    } else {
                        getMethod( "set" ).invoke( compound, key, notCompound );
                    }
                }
                break;
            }
            Object oldCompound = compound;
            if ( key instanceof Integer ) {
                compound = ( ( List< ? > ) NBTListData.get( compound ) ).get( ( int ) key );
            } else if ( key != null ) {
                compound = getMethod( "get" ).invoke( compound, ( String ) key );
            }
            if ( compound == null || key == null ) {
                if ( keys[ index + 1 ] == null || keys[ index + 1 ] instanceof Integer ) {
                    compound = getNMSClass( "NBTTagList" ).newInstance();
                } else {
                    compound = getNMSClass( "NBTTagCompound" ).newInstance();
                }
                if ( oldCompound.getClass().getSimpleName().equals( "NBTTagList" ) ) {
                    getMethod( "add" ).invoke( oldCompound, compound );
                } else {
                    if ( notCompound == null ) {
                        getMethod( "remove" ).invoke( oldCompound, ( String ) key );
                    } else {
                        getMethod( "set" ).invoke( oldCompound, key, compound );
                    }
                }
            }
        }
    }

    private static NBTCompound getNBTTag( Object tag, Object...keys ) throws Exception {
        Object compound = tag;

        for ( Object key : keys ) {
            if ( compound == null ) {
                return null;
            } else if ( getNMSClass( "NBTTagCompound" ).isInstance( compound ) ) {
                compound = getMethod( "get" ).invoke( compound, ( String ) key );
            } else if ( getNMSClass( "NBTTagList" ).isInstance( compound ) ) {
                compound = ( ( List< ? > ) NBTListData.get( compound ) ).get( ( int ) key );
            }
        }
        return new NBTCompound( compound );
    }

    private static Object getTag( Object tag, Object... keys ) throws Exception {
        if ( keys.length == 0 ) {
            return getTags( tag );
        }

        Object notCompound = tag;

        for ( Object key : keys ) {
            if ( notCompound == null ) {
                return null;
            } else if ( getNMSClass( "NBTTagCompound" ).isInstance( notCompound ) ) {
                notCompound = getMethod( "get" ).invoke( notCompound, ( String ) key );
            } else if ( getNMSClass( "NBTTagList" ).isInstance( notCompound ) ) {
                notCompound = ( ( List< ? > ) NBTListData.get( notCompound ) ).get( ( int ) key );
            } else {
                return getNBTVar( notCompound );
            }
        }
        if ( notCompound == null ) {
            return null;
        } else if ( getNMSClass( "NBTTagList" ).isInstance( notCompound ) ) {
            return getTags( notCompound );
        } else if ( getNMSClass( "NBTTagCompound" ).isInstance( notCompound ) ) {
            return getTags( notCompound );
        } else {
            return getNBTVar( notCompound );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static Object getTags( Object tag ) {
        Map< Object, Object > tags = new HashMap<>();
        try {
            if ( getNMSClass( "NBTTagCompound" ).isInstance( tag ) ) {
                Map< String, Object > tagCompound = ( Map< String, Object > ) NBTCompoundMap.get( tag );
                for ( String key : tagCompound.keySet() ) {
                    Object value = tagCompound.get( key );
                    if ( getNMSClass( "NBTTagEnd" ).isInstance( value ) ) {
                        continue;
                    }
                    tags.put( key, getTag( value ) );
                }
            } else if ( getNMSClass( "NBTTagList" ).isInstance( tag ) ) {
                List< Object > tagList = ( List< Object > ) NBTListData.get( tag );
                for ( int index = 0; index < tagList.size(); index++ ) {
                    Object value = tagList.get( index );
                    if ( getNMSClass( "NBTTagEnd" ).isInstance( value ) ) {
                        continue;
                    }
                    tags.put( index, getTag( value ) );
                }
            } else {
                return getNBTVar( tag );
            }
            return tags;
        } catch ( Exception e ) {
            e.printStackTrace();
            return tags;
        }
    }

    /**
     * A class for holding NBTTagCompounds
     */
    public static final class NBTCompound {
        protected final Object tag;

        protected NBTCompound( Object tag ) {
            this.tag = tag;
        }
    }
}
