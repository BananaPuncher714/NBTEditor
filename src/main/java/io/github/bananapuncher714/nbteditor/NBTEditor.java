package io.github.bananapuncher714.nbteditor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * Sets/Gets NBT tags from ItemStacks 
 * Supports 1.8-1.21
 * 
 * Github: https://github.com/BananaPuncher714/NBTEditor
 * Spigot: https://www.spigotmc.org/threads/269621/
 * 
 * @version 7.19.10
 * @author BananaPuncher714
 */
public final class NBTEditor {
    private static final Set< ReflectionTarget > reflectionTargets;
    
    private static final Map< ClassId, Class< ? > > classCache;
    private static final Map< MethodId, Method > methodCache;
    private static final Map< ClassId, Constructor< ? > > constructorCache;
    private static final Map< Class< ? >, Constructor< ? > > NBTConstructors;
    private static final Map< Class< ? >, Class< ? > > NBTClasses;
    private static final Map< Class< ? >, Field > NBTTagFieldCache;
    private static Field NBTListData;
    private static Field NBTCompoundMap;
    private static Field skullProfile;
    private static final String VERSION;
    private static final MinecraftVersion LOCAL_VERSION;
    private static final String BUKKIT_VERSION;
    
    public static final Type COMPOUND = Type.COMPOUND;
    public static final Type LIST = Type.LIST;
    public static final Type NEW_ELEMENT = Type.NEW_ELEMENT;
    public static final Type DELETE = Type.DELETE;
    public static final Type CUSTOM_DATA = Type.CUSTOM_DATA;
    public static final Type ITEMSTACK_COMPONENTS = Type.ITEMSTACK_COMPONENTS;
    
    static {
        String cbPackage = Bukkit.getServer().getClass().getPackage().getName();
        String detectedVersion = cbPackage.substring( cbPackage.lastIndexOf( '.' ) + 1 );
        String bukkitVersion = Bukkit.getServer().getBukkitVersion();
        if ( !detectedVersion.startsWith( "v" ) ) {
            // Paper or something...
            detectedVersion = bukkitVersion;
        }
        
        VERSION = detectedVersion;
        LOCAL_VERSION = MinecraftVersion.get( VERSION );
        BUKKIT_VERSION = bukkitVersion;

        classCache = new HashMap< ClassId, Class< ? > >();
        methodCache = new HashMap< MethodId, Method >();
        constructorCache = new HashMap< ClassId, Constructor< ? > >();
        
        reflectionTargets = new TreeSet< ReflectionTarget >();
        reflectionTargets.addAll( Arrays.asList(
                new ReflectionTarget.v1_8().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_9().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_11().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_12().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_13().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_15().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_16().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_17().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_18_R1().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_18_R2().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_19_R1().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_19_R2().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_20_R1().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_20_R2().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_20_R3().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_20_R4().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_21_R1().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_21_R4().setClassFetcher( NBTEditor::getNMSClass ),
                new ReflectionTarget.v1_21_R5().setClassFetcher( NBTEditor::getNMSClass )
        ) );
        
        NBTClasses = new HashMap< Class< ? >, Class< ? > >();
        try {
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_17 ) ) {
                NBTClasses.put( Byte.class, Class.forName( "net.minecraft.nbt.NBTTagByte" ) );
                NBTClasses.put( Boolean.class, Class.forName( "net.minecraft.nbt.NBTTagByte" ) );
                NBTClasses.put( String.class, Class.forName( "net.minecraft.nbt.NBTTagString" ) );
                NBTClasses.put( Double.class, Class.forName( "net.minecraft.nbt.NBTTagDouble" ) );
                NBTClasses.put( Integer.class, Class.forName( "net.minecraft.nbt.NBTTagInt" ) );
                NBTClasses.put( Long.class, Class.forName( "net.minecraft.nbt.NBTTagLong" ) );
                NBTClasses.put( Short.class, Class.forName( "net.minecraft.nbt.NBTTagShort" ) );
                NBTClasses.put( Float.class, Class.forName( "net.minecraft.nbt.NBTTagFloat" ) );
                NBTClasses.put( Class.forName( "[B" ), Class.forName( "net.minecraft.nbt.NBTTagByteArray" ) );
                NBTClasses.put( Class.forName( "[I" ), Class.forName( "net.minecraft.nbt.NBTTagIntArray" ) );
            } else {
                NBTClasses.put( Byte.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagByte" ) );
                NBTClasses.put( Boolean.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagByte" ) );
                NBTClasses.put( String.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagString" ) );
                NBTClasses.put( Double.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagDouble" ) );
                NBTClasses.put( Integer.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagInt" ) );
                NBTClasses.put( Long.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagLong" ) );
                NBTClasses.put( Short.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagShort" ) );
                NBTClasses.put( Float.class, Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagFloat" ) );
                NBTClasses.put( Class.forName( "[B" ), Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagByteArray" ) );
                NBTClasses.put( Class.forName( "[I" ), Class.forName( "net.minecraft.server." + VERSION + "." + "NBTTagIntArray" ) );
            }
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        }

        NBTConstructors = new HashMap< Class< ? >, Constructor< ? > >();
        try {
            NBTConstructors.put( getNBTTag( Byte.class ), getNBTTag( Byte.class ).getDeclaredConstructor( byte.class ) );
            NBTConstructors.put( getNBTTag( Boolean.class ), getNBTTag( Boolean.class ).getDeclaredConstructor( byte.class ) );
            NBTConstructors.put( getNBTTag( String.class ), getNBTTag( String.class ).getDeclaredConstructor( String.class ) );
            NBTConstructors.put( getNBTTag( Double.class ), getNBTTag( Double.class ).getDeclaredConstructor( double.class ) );
            NBTConstructors.put( getNBTTag( Integer.class ), getNBTTag( Integer.class ).getDeclaredConstructor( int.class ) );
            NBTConstructors.put( getNBTTag( Long.class ), getNBTTag( Long.class ).getDeclaredConstructor( long.class ) );
            NBTConstructors.put( getNBTTag( Float.class ), getNBTTag( Float.class ).getDeclaredConstructor( float.class ) );
            NBTConstructors.put( getNBTTag( Short.class ), getNBTTag( Short.class ).getDeclaredConstructor( short.class ) );
            NBTConstructors.put( getNBTTag( Class.forName( "[B" ) ), getNBTTag( Class.forName( "[B" ) ).getDeclaredConstructor( Class.forName( "[B" ) ) );
            NBTConstructors.put( getNBTTag( Class.forName( "[I" ) ), getNBTTag( Class.forName( "[I" ) ).getDeclaredConstructor( Class.forName( "[I" ) ) );

            // This is for 1.15 since Mojang decided to make the constructors private
            for ( Constructor< ? > cons : NBTConstructors.values() ) {
                cons.setAccessible( true );
            }
        } catch( ClassNotFoundException | NoSuchMethodException | SecurityException e ) {
            e.printStackTrace();
        }

        NBTTagFieldCache = new HashMap< Class< ? >, Field >();
        try {
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_21_R4 ) ) {
                NBTTagFieldCache.put( NBTClasses.get( Byte.class ), NBTClasses.get( Byte.class ).getDeclaredField( "v" ) );
                NBTTagFieldCache.put( NBTClasses.get( Boolean.class ), NBTClasses.get( Boolean.class ).getDeclaredField( "v" ) );
                NBTTagFieldCache.put( NBTClasses.get( String.class ), NBTClasses.get( String.class ).getDeclaredField( "b" ) );
                NBTTagFieldCache.put( NBTClasses.get( Double.class ), NBTClasses.get( Double.class ).getDeclaredField( "c" ) );
                NBTTagFieldCache.put( NBTClasses.get( Integer.class ), NBTClasses.get( Integer.class ).getDeclaredField( "b" ) );
                NBTTagFieldCache.put( NBTClasses.get( Long.class ), NBTClasses.get( Long.class ).getDeclaredField( "b" ) );
                NBTTagFieldCache.put( NBTClasses.get( Float.class ), NBTClasses.get( Float.class ).getDeclaredField( "c" ) );
                NBTTagFieldCache.put( NBTClasses.get( Short.class ), NBTClasses.get( Short.class ).getDeclaredField( "b" ) );
                NBTTagFieldCache.put( NBTClasses.get( Class.forName( "[B" ) ), NBTClasses.get( Class.forName( "[B" ) ).getDeclaredField( "c" ) );
                NBTTagFieldCache.put( NBTClasses.get( Class.forName( "[I" ) ), NBTClasses.get( Class.forName( "[I" ) ).getDeclaredField( "c" ) );
                
                for ( Field field : NBTTagFieldCache.values() ) {
                    field.setAccessible( true );
                }                
            } else if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_17 ) ) {
                NBTTagFieldCache.put( NBTClasses.get( Byte.class ), NBTClasses.get( Byte.class ).getDeclaredField( "x" ) );
                NBTTagFieldCache.put( NBTClasses.get( Boolean.class ), NBTClasses.get( Boolean.class ).getDeclaredField( "x" ) );
                NBTTagFieldCache.put( NBTClasses.get( String.class ), NBTClasses.get( String.class ).getDeclaredField( "A" ) );
                NBTTagFieldCache.put( NBTClasses.get( Double.class ), NBTClasses.get( Double.class ).getDeclaredField( "w" ) );
                NBTTagFieldCache.put( NBTClasses.get( Integer.class ), NBTClasses.get( Integer.class ).getDeclaredField( "c" ) );
                NBTTagFieldCache.put( NBTClasses.get( Long.class ), NBTClasses.get( Long.class ).getDeclaredField( "c" ) );
                NBTTagFieldCache.put( NBTClasses.get( Float.class ), NBTClasses.get( Float.class ).getDeclaredField( "w" ) );
                NBTTagFieldCache.put( NBTClasses.get( Short.class ), NBTClasses.get( Short.class ).getDeclaredField( "c" ) );
                NBTTagFieldCache.put( NBTClasses.get( Class.forName( "[B" ) ), NBTClasses.get( Class.forName( "[B" ) ).getDeclaredField( "c" ) );
                NBTTagFieldCache.put( NBTClasses.get( Class.forName( "[I" ) ), NBTClasses.get( Class.forName( "[I" ) ).getDeclaredField( "c" ) );
                
                for ( Field field : NBTTagFieldCache.values() ) {
                    field.setAccessible( true );
                }
            } else {
                for ( Class< ? > clazz : NBTClasses.values() ) {
                    Field data = clazz.getDeclaredField( "data" );
                    data.setAccessible( true );
                    NBTTagFieldCache.put( clazz, data );
                }
            }
        } catch( ClassNotFoundException | NoSuchFieldException | SecurityException e ) {
            e.printStackTrace();
        }

        try {
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_21_R4 ) ) {
                try {
                    NBTListData    = getNMSClass(ClassId.NBTTagList).getDeclaredField("list");
                    NBTCompoundMap = getNMSClass(ClassId.NBTTagCompound).getDeclaredField("tags");
                } catch (NoSuchFieldException ex) {
                    NBTListData    = getNMSClass(ClassId.NBTTagList).getDeclaredField("v");
                    NBTCompoundMap = getNMSClass(ClassId.NBTTagCompound).getDeclaredField("x");
                }
            } else if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_17 ) ) {
                NBTListData = getNMSClass( ClassId.NBTTagList ).getDeclaredField( "c" );
                NBTCompoundMap = getNMSClass( ClassId.NBTTagCompound ).getDeclaredField( "x" );
            } else {
                NBTListData = getNMSClass( ClassId.NBTTagList ).getDeclaredField( "list" );
                NBTCompoundMap = getNMSClass( ClassId.NBTTagCompound ).getDeclaredField( "map" );
            }
            NBTListData.setAccessible( true );
            NBTCompoundMap.setAccessible( true );
            
            skullProfile = getNMSClass( ClassId.CraftMetaSkull ).getDeclaredField( "profile" );
            skullProfile.setAccessible( true );
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    private static Constructor< ? > getNBTTagConstructor( Class< ? > primitiveType ) {
        return NBTConstructors.get( getNBTTag( primitiveType ) );
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
    
    private static Method getMethod( MethodId name ) {
        if ( methodCache.containsKey( name ) ) {
            return methodCache.get( name );
        }
        
        for ( ReflectionTarget target : reflectionTargets ) {
            // Only check targets that are of the correct version
            if ( target.getVersion().lessThanOrEqualTo( LOCAL_VERSION ) ) {
                try {
                    Method method = target.fetchMethod( name );
                    if ( method != null ) {
                        methodCache.put( name, method );
                        
                        return method;
                    }
                } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException e ) {
                    e.printStackTrace();
                }
            }
        }
        
        // Prevent any additional reflection lookups, since we know it doesn't exist
        methodCache.put( name, null );
        
        return null;
    }

    private static Constructor< ? > getConstructor( ClassId id ) {
        if ( constructorCache.containsKey( id ) ) {
            return constructorCache.get( id );
        }
        
        for ( ReflectionTarget target : reflectionTargets ) {
            // Only check targets that are of the correct version
            if ( target.getVersion().lessThanOrEqualTo( LOCAL_VERSION ) ) {
                try {
                    Constructor< ? > cons = target.fetchConstructor( id );
                    if ( cons != null ) {
                        constructorCache.put( id, cons );
                    
                        return cons;
                    }
                } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException e ) {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }

    private static Class< ? > getNMSClass( ClassId id ) {
        if ( classCache.containsKey( id ) ) {
            return classCache.get( id );
        }
        
        for ( ReflectionTarget target : reflectionTargets ) {
            // Only check targets that are of the correct version
            if ( target.getVersion().lessThanOrEqualTo( LOCAL_VERSION ) ) {
                try {
                    Class< ? > clazz = target.fetchClass( id );
                    if ( clazz != null ) {
                        classCache.put( id, clazz );
                        
                        return clazz;
                    }
                } catch ( ClassNotFoundException e ) {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }

    private static String getMatch( String string, String regex ) {
        Pattern pattern = Pattern.compile( regex );
        Matcher matcher = pattern.matcher( string );
        if ( matcher.find() ) {
            return matcher.group( 1 );
        } else {
            return null;
        }
    }

    // For some reason, 1.11 and 1.12 have a constructor for ItemStack that accepts an NBTTagCompound
    private static Object createItemStack( Object compound ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchFieldException, SecurityException {
        if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_21_R5 ) ) {
            return ReflectionTarget.v1_21_R5.getItemFrom( registryAccess(), compound );
        } else if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_21_4 ) ) {
            Optional< ? > optional = ( Optional< ? > ) getMethod( MethodId.createStackOptional ).invoke( null, registryAccess(), compound );
            return optional.get();
        } else if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
            Method createStack = getMethod( MethodId.createStack );
            if ( createStack.getParameterCount() == 2 ) {
                // RegistryAccess, NBTagCompound
                return getMethod( MethodId.createStack ).invoke( null, registryAccess(), compound );
            } else {
                // NBTTagCompound
                return getMethod( MethodId.createStack ).invoke( null, compound );
            }
        } else if ( LOCAL_VERSION == MinecraftVersion.v1_11 || LOCAL_VERSION == MinecraftVersion.v1_12 ) {
            return getConstructor( ClassId.ItemStack ).newInstance( compound );
        } else {
            return getMethod( MethodId.createStack ).invoke( null, compound );            
        }
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
    
    public static MinecraftVersion getMinecraftVersion() {
        return LOCAL_VERSION;
    }

    /**
     * Creates a skull with the given url as the skin
     * 
     * @param skinURL
     * The URL of the skin, must be from mojang
     * @return
     * An item stack with count of 1
     */
    public static ItemStack getHead( String skinURL ) {
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
        Object profile = null;
        try {
            // Use a non-random UUID so heads will stack; in this case, Notch.
            profile = getConstructor( ClassId.GameProfile ).newInstance( UUID.fromString( "069a79f4-44e9-4726-a5be-fca90e38aaf5" ), "Notch" );
            Object propertyMap = getMethod( MethodId.getProperties ).invoke( profile );
            Object textureProperty = getConstructor( ClassId.Property ).newInstance( "textures", new String( Base64.getEncoder().encode( String.format( "{textures:{SKIN:{\"url\":\"%s\"}}}", skinURL ).getBytes() ) ) );
            getMethod( MethodId.putProperty ).invoke( propertyMap, "textures", textureProperty );
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e1 ) {
            e1.printStackTrace();
        }

        final Method setSkullResolvableProfile = getMethod( MethodId.setCraftMetaSkullResolvableProfile );
        Method setSkullProfile = getMethod( MethodId.setCraftMetaSkullProfile );
        if ( setSkullResolvableProfile != null ) {
            try {
                setSkullResolvableProfile.invoke( headMeta, getConstructor( ClassId.ResolvableProfile ).newInstance( profile ) );
            } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e ) {
                e.printStackTrace();
            }
        } else if ( setSkullProfile != null ) {
            try {
                setSkullProfile.invoke( headMeta, profile );
            } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                e.printStackTrace();
            }
        } else {
            try {
                skullProfile.set( headMeta, profile );
            } catch ( IllegalArgumentException | IllegalAccessException e ) {
                e.printStackTrace();
            }
        }
        head.setItemMeta( headMeta );
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
    @SuppressWarnings( "unchecked" )
    public static String getTexture( ItemStack head ) {
        ItemMeta meta = head.getItemMeta();
        if ( !( meta instanceof SkullMeta ) ) {
            throw new IllegalArgumentException( "Item is not a player skull!" );
        }
        try {
            Object profile = skullProfile.get( meta );
            
            Class< ? > resolvableProfile = getNMSClass( ClassId.ResolvableProfile );
            if ( resolvableProfile != null && resolvableProfile.isInstance( profile ) ) {
                final Method getGameProfile = getMethod( MethodId.getResolvableProfileGameProfile );
                profile = getGameProfile.invoke( profile );
            }
            
            if ( profile == null ) {
                return null;
            }
            
            final Collection< Object > properties = ( Collection< Object > ) getMethod( MethodId.propertyValues ).invoke( getMethod( MethodId.getProperties ).invoke( profile ) );
            for ( Object prop : properties ) {
                if ( "textures".equals( getMethod( MethodId.getPropertyName ).invoke( prop ) ) ) {
                    String texture = new String( Base64.getDecoder().decode( ( String ) getMethod( MethodId.getPropertyValue ).invoke( prop ) ) );
                    return getMatch( texture, "\\{\"url\":\"(.*?)\"\\}" );
                }
            }
            return null;
        } catch ( IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets an NBT tag in a given item with the specified keys
     * 
     * @param item
     * The itemstack to get the keys from
     * @param keys
     * The keys to fetch; an integer after a key value indicates that it should get the nth place of
     * the previous compound because it is a list;
     * @return
     * The item represented by the keys, and an integer if it is showing how long a list is.
     */
    private static Object getItemTag( ItemStack item, Object... keys ) {
        try {
            Object compound = getCompound( item );
            
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) && !BUKKIT_VERSION.startsWith( "1.20.4" ) && !BUKKIT_VERSION.startsWith( "1.20.5" ) ) {
                // Apply the components tag since all items should contain this if not already present, since 1.20.6
                if ( keys.length > 0 && keys[ 0 ] != Type.ITEMSTACK_COMPONENTS ) {
                    compound = getMethod( MethodId.compoundGet ).invoke( compound, "components" );
                }
            }
            
            return getTag( compound, keys );
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
            e.printStackTrace();
            return null;
        }
    }

    // Gets the NBTTagCompound
    @SuppressWarnings( "deprecation" )
    private static Object getCompound( ItemStack item ) {
        if ( item == null ) {
            return null;
        }
        try {
            Object stack = getMethod( MethodId.asNMSCopy ).invoke( null, item );

            Object tag = null;

            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_21_R5 ) ) {
                tag = ReflectionTarget.v1_21_R5.save( stack, registryAccess() );
            } else {
                Method saveOptional = null;
                if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                    saveOptional = getMethod( MethodId.saveOptional );
                }
                
                if ( saveOptional != null ) {
                    tag = saveOptional.invoke( stack, registryAccess() );
                } else {
                    if ( getMethod( MethodId.itemHasTag ).invoke( stack ).equals( true ) ) {
                        tag = getMethod( MethodId.getItemTag ).invoke( stack );
                    } else {
                        tag = getNMSClass( ClassId.NBTTagCompound ).newInstance();
                    }
                }
            }

            return tag;
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | NoSuchFieldException | SecurityException exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Gets an NBTCompound from the item provided. Use {@link #getNBTCompound(Object, Object...)} instead.
     * 
     * @param item
     * Itemstack
     * @param keys
     * Keys in descending order
     * @return
     * An NBTCompound
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    @SuppressWarnings( "deprecation" )
    private static NBTCompound getItemNBTTag( ItemStack item, Object... keys ) {
        if ( item == null ) {
            return null;
        }
        try {
            Object stack = null;
            stack = getMethod( MethodId.asNMSCopy ).invoke( null, item );

            Object tag = getNMSClass( ClassId.NBTTagCompound ).newInstance();
            
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_21_R5 ) ) {
                tag = ReflectionTarget.v1_21_R5.save( stack, registryAccess() );
            } else {
                Method saveOptional = null;
                if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                    saveOptional = getMethod( MethodId.saveOptional );
                }
                
                if ( saveOptional != null ) {
                    tag = saveOptional.invoke( stack, registryAccess() );
                } else {
                    tag = getMethod( MethodId.itemSave ).invoke( stack, tag );                                
                }
            }

            return getNBTTag( tag, keys );
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | NoSuchFieldException | SecurityException exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Sets an NBT tag in an item with the provided keys and value
     * Should use the {@link #set(Object, Object, Object...)} method instead
     * 
     * @param item
     * The itemstack to set
     * @param value
     * The value to set
     * @param keys
     * The keys to set, String for NBTCompound, int or null for an NBTTagList
     * @return
     * A new ItemStack with the updated NBT tags
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    @SuppressWarnings( "deprecation" )
    private static ItemStack setItemTag( ItemStack item, Object value, Object... keys ) {
        if ( item == null ) {
            return null;
        }
        try {
            Object stack = getMethod( MethodId.asNMSCopy ).invoke( null, item );

            Object tag = null;

            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_21_R5 ) ) {
                tag = ReflectionTarget.v1_21_R5.save( stack, registryAccess() );
            } else {
                Method saveOptional = null;
                if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                    saveOptional = getMethod( MethodId.saveOptional );
                }
                
                if ( saveOptional != null ) {
                    tag = saveOptional.invoke( stack, registryAccess() );
                } else {
                    if ( getMethod( MethodId.itemHasTag ).invoke( stack ).equals( true ) ) {
                        tag = getMethod( MethodId.getItemTag ).invoke( stack );
                    } else {
                        tag = getNMSClass( ClassId.NBTTagCompound ).newInstance();
                    }
                }
            }

            if ( keys.length == 0 && value instanceof NBTCompound ) {
                tag = ( ( NBTCompound ) value ).tag;
            } else {
                if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) && !BUKKIT_VERSION.startsWith( "1.20.4" ) && !BUKKIT_VERSION.startsWith( "1.20.5" ) ) {
                    // Apply the components tag since all items should contain this if not already present, since 1.20.6
                    if ( keys.length > 0 && keys[ 0 ] != Type.ITEMSTACK_COMPONENTS ) {
                        List< Object > keyList = new ArrayList< Object >( Arrays.asList( keys ) );
                        keyList.add( 0, "components" );
                        keys = keyList.toArray();
                    }
                }
                
                setTag( tag, value, keys );
            }

            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) && !BUKKIT_VERSION.startsWith( "1.20.4" ) && !BUKKIT_VERSION.startsWith( "1.20.5" ) ) {
                return ( ItemStack ) getMethod( MethodId.asBukkitCopy ).invoke( null, createItemStack( tag ) );
            } else {
                getMethod( MethodId.setItemTag ).invoke( stack, tag );
                return ( ItemStack ) getMethod( MethodId.asBukkitCopy ).invoke( null, stack );
            }
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | NoSuchFieldException | SecurityException exception ) {
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
    public static ItemStack getItemFromTag( NBTCompound compound ) {
        if ( compound == null ) {
            return null;
        }
        try {
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) && !BUKKIT_VERSION.startsWith( "1.20.4" ) && !BUKKIT_VERSION.startsWith( "1.20.5" ) ) {
                return ( ItemStack ) getMethod( MethodId.asBukkitCopy ).invoke( null, createItemStack( compound.tag ) );
            } else {                
                Object tag = compound.tag;
                Object count = getTag( tag, "Count" );
                Object id = getTag( tag, "id" );
                if ( count == null || id == null ) {
                    return null;
                }
                if ( count instanceof Byte && id instanceof String ) {
                    return ( ItemStack ) getMethod( MethodId.asBukkitCopy ).invoke( null, createItemStack( tag ) );
                }
                return null;
            }
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | NoSuchFieldException | SecurityException exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Gets an NBT tag in a given entity with the specified keys
     * 
     * @param entity
     * The entity to get the keys from
     * @param keys
     * The keys to fetch; an integer after a key value indicates that it should get the nth place of
     * the previous compound because it is a list;
     * @return
     * The item represented by the keys, and an integer if it is showing how long a list is.
     */
    private static Object getEntityTag( Entity entity, Object... keys ) {
        try {
            return getTag( getCompound( entity ), keys );
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
            e.printStackTrace();
            return null;
        }
    }

    // Gets the NBTTagCompound
    @SuppressWarnings( "deprecation" )
    private static Object getCompound( Entity entity ) {
        if ( entity == null ) {
            return entity;
        }
        try {
            Object NMSEntity = getMethod( MethodId.getEntityHandle ).invoke( entity );
            
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_21_R5 ) ) {
                final Object tagValueOutput = ReflectionTarget.v1_21_R5.newTagValueOutput( registryAccess() );
                
                getMethod( MethodId.getEntityTag ).invoke( NMSEntity, tagValueOutput );
                
                return getMethod( MethodId.convertToNbtTagCompound ).invoke( tagValueOutput );
            } else {
                Object tag = getNMSClass( ClassId.NBTTagCompound ).newInstance();
            
                getMethod( MethodId.getEntityTag ).invoke( NMSEntity, tag );
                
                return tag;
            }
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | NoSuchFieldException | SecurityException exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Gets an NBTCompound from the entity provided. Use {@link #getNBTCompound(Object, Object...)} instead.
     * 
     * @param entity
     * The Bukkit entity provided
     * @param keys
     * Keys in descending order
     * @return
     * An NBTCompound
     */
    @SuppressWarnings( "deprecation" )
    private static NBTCompound getEntityNBTTag( Entity entity, Object...keys ) {
        if ( entity == null ) {
            return null;
        }
        try {
            Object NMSEntity = getMethod( MethodId.getEntityHandle ).invoke( entity );

            Object tag;
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_21_R5 ) ) {
                final Object tagValueOutput = ReflectionTarget.v1_21_R5.newTagValueOutput( registryAccess() );
                
                getMethod( MethodId.getEntityTag ).invoke( NMSEntity, tagValueOutput );
                
                tag = getMethod( MethodId.convertToNbtTagCompound ).invoke( tagValueOutput );
            } else {
                tag = getNMSClass( ClassId.NBTTagCompound ).newInstance();
         
                getMethod( MethodId.getEntityTag ).invoke( NMSEntity, tag );
            }

            return getNBTTag( tag, keys );
        } catch ( IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException | SecurityException exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Sets an NBT tag in an entity with the provided keys and value
     * Should use the {@link #set(Object, Object, Object...)} method instead
     * 
     * @param entity
     * The entity to set
     * @param value
     * The value to set
     * @param keys
     * The keys to set, String for NBTCompound, int or null for an NBTTagList
     */
    @SuppressWarnings( "deprecation" )
    private static void setEntityTag( Entity entity, Object value, Object... keys ) {
        if ( entity == null ) {
            return;
        }
        try {
            Object NMSEntity = getMethod( MethodId.getEntityHandle ).invoke( entity );

            Object tag;
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_21_R5 ) ) {
                final Object tagValueOutput = ReflectionTarget.v1_21_R5.newTagValueOutput( registryAccess() );
                
                getMethod( MethodId.getEntityTag ).invoke( NMSEntity, tagValueOutput );
                
                tag = getMethod( MethodId.convertToNbtTagCompound ).invoke( tagValueOutput );
            } else {
                tag = getNMSClass( ClassId.NBTTagCompound ).newInstance();
         
                getMethod( MethodId.getEntityTag ).invoke( NMSEntity, tag );
            }

            if ( keys.length == 0 && value instanceof NBTCompound ) {
                tag = ( ( NBTCompound ) value ).tag;
            } else {
                setTag( tag, value, keys );
            }

            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_21_R5 ) ) {
                final Object valueInput = ReflectionTarget.v1_21_R5.getValueInputFromNbtTagCompound( registryAccess(), tag );
                
                getMethod( MethodId.setEntityTag ).invoke( NMSEntity, valueInput );
            } else {
                getMethod( MethodId.setEntityTag ).invoke( NMSEntity, tag );
            }
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | NoSuchFieldException | SecurityException exception ) {
            exception.printStackTrace();
        }
    }

    /**
     * Gets an NBT tag in a given block with the specified keys. Use {@link #getNBTCompound(Object, Object...)} instead.
     * 
     * @param block
     * The block to get the keys from
     * @param keys
     * The keys to fetch; an integer after a key value indicates that it should get the nth place of
     * the previous compound because it is a list;
     * @return
     * The item represented by the keys, and an integer if it is showing how long a list is.
     */
    private static Object getBlockTag( Block block, Object... keys ) {
        try {
            return getTag( getCompound( block ), keys );
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
            e.printStackTrace();
            return null;
        }
    }

    // Gets the NBTTagCompound
    @SuppressWarnings( "deprecation" )
    private static Object getCompound( Block block ) {
        try {
            if ( block == null || !getNMSClass( ClassId.CraftBlockState ).isInstance( block.getState() ) ) {
                return null;
            }
            Location location = block.getLocation();

            Object blockPosition = getConstructor( ClassId.BlockPosition ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

            Object nmsWorld = getMethod( MethodId.getWorldHandle ).invoke( location.getWorld() );

            Object tileEntity = getMethod( MethodId.getTileEntity ).invoke( nmsWorld, blockPosition );

            if ( tileEntity == null ) {
                throw new IllegalArgumentException( block + " is not a tile entity!" );
            }

            Object tag;
            
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                Method getTileTag = getMethod( MethodId.getTileTag );
                if ( getTileTag.getParameterCount() == 1 ) {
                    // RegistryAccess
                    tag = getTileTag.invoke( tileEntity, registryAccess() );
                } else {
                    // No params
                    tag = getTileTag.invoke( tileEntity );
                }
            } else if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_18_R1 ) ) {
                tag = getMethod( MethodId.getTileTag ).invoke( tileEntity );
            } else {
                tag = getNMSClass( ClassId.NBTTagCompound ).newInstance();
                getMethod( MethodId.getTileTag ).invoke( tileEntity, tag );
            }

            return tag;
        } catch( IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException exception ) {
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
    @SuppressWarnings( "deprecation" )
    private static NBTCompound getBlockNBTTag( Block block, Object... keys ) {
        try {
            if ( block == null || !getNMSClass( ClassId.CraftBlockState ).isInstance( block.getState() ) ) {
                return null;
            }
            Location location = block.getLocation();

            Object blockPosition = getConstructor( ClassId.BlockPosition ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

            Object nmsWorld = getMethod( MethodId.getWorldHandle ).invoke( location.getWorld() );

            Object tileEntity = getMethod( MethodId.getTileEntity ).invoke( nmsWorld, blockPosition );

            if ( tileEntity == null ) {
                throw new IllegalArgumentException( block + " is not a tile entity!" );
            }
            
            Object tag;
            
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                Method getTileTag = getMethod( MethodId.getTileTag );
                if ( getTileTag.getParameterCount() == 1 ) {
                    // RegistryAccess
                    tag = getTileTag.invoke( tileEntity, registryAccess() );
                } else {
                    // No params
                    tag = getTileTag.invoke( tileEntity );
                }
            } else if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_18_R1 ) ) {
                tag = getMethod( MethodId.getTileTag ).invoke( tileEntity );
            } else {
                tag = getNMSClass( ClassId.NBTTagCompound ).newInstance();
                getMethod( MethodId.getTileTag ).invoke( tileEntity, tag );
            }

            return getNBTTag( tag, keys );
        } catch( IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException exception ) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Sets an NBT tag in an block with the provided keys and value
     * Should use the {@link #set(Object, Object, Object...)} method instead
     * 
     * @param block
     * The block to set
     * @param value
     * The value to set
     * @param keys
     * The keys to set, String for NBTCompound, int or null for an NBTTagList
     */
    @SuppressWarnings( "deprecation" )
    private static void setBlockTag( Block block, Object value, Object... keys ) {
        try {
            if ( block == null || !getNMSClass( ClassId.CraftBlockState ).isInstance( block.getState() ) ) {
                return;
            }
            Location location = block.getLocation();

            Object blockPosition = getConstructor( ClassId.BlockPosition ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

            Object nmsWorld = getMethod( MethodId.getWorldHandle ).invoke( location.getWorld() );

            Object tileEntity = getMethod( MethodId.getTileEntity ).invoke( nmsWorld, blockPosition );

            if ( tileEntity == null ) {
                throw new IllegalArgumentException( block + " is not a tile entity!" );
            }
            
            Object tag;
            
            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                Method getTileTag = getMethod( MethodId.getTileTag );
                if ( getTileTag.getParameterCount() == 1 ) {
                    // RegistryAccess
                    tag = getTileTag.invoke( tileEntity, registryAccess() );
                } else {
                    // No params
                    tag = getTileTag.invoke( tileEntity );
                }
            } else if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_18_R1 ) ) {
                tag = getMethod( MethodId.getTileTag ).invoke( tileEntity );
            } else {
                tag = getNMSClass( ClassId.NBTTagCompound ).newInstance();
                getMethod( MethodId.getTileTag ).invoke( tileEntity, tag );
            }

            if ( keys.length == 0 && value instanceof NBTCompound ) {
                tag = ( ( NBTCompound ) value ).tag;
            } else {
                setTag( tag, value, keys );
            }

            if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_21_R5 ) ) {
                Method setTileTag = getMethod( MethodId.setTileTag );
                
                setTileTag.invoke( tileEntity, ReflectionTarget.v1_21_R5.getValueInputFromNbtTagCompound( registryAccess(), tag ) );
            } else if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                Method setTileTag = getMethod( MethodId.setTileTag );
                if ( setTileTag.getParameterCount() == 2 ) {
                    // NBTTagCompound, RegistryAccess
                    setTileTag.invoke( tileEntity, tag, registryAccess() );
                } else {
                    // NBTTagCompound
                    setTileTag.invoke( tileEntity, tag );
                }
            } else if ( LOCAL_VERSION == MinecraftVersion.v1_16 ) {
                getMethod( MethodId.setTileTag ).invoke( tileEntity, getMethod( MethodId.getTileType ).invoke( nmsWorld, blockPosition ), tag );
            } else {
                getMethod( MethodId.setTileTag ).invoke( tileEntity, tag );
            }
        } catch( IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException | SecurityException exception ) {
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
    public static void setSkullTexture( Block block, String texture ) {
        try {
            Object profile = getConstructor( ClassId.GameProfile ).newInstance( UUID.randomUUID(), null );
            Object propertyMap = getMethod( MethodId.getProperties ).invoke( profile );
            Object textureProperty = getConstructor( ClassId.Property ).newInstance( "textures", new String( Base64.getEncoder().encode( String.format( "{textures:{SKIN:{\"url\":\"%s\"}}}", texture ).getBytes() ) ) );
            getMethod( MethodId.putProperty ).invoke( propertyMap, "textures", textureProperty );
            
            Location location = block.getLocation();

            Object blockPosition = getConstructor( ClassId.BlockPosition ).newInstance( location.getBlockX(), location.getBlockY(), location.getBlockZ() );

            Object nmsWorld = getMethod( MethodId.getWorldHandle ).invoke( location.getWorld() );

            Object tileEntity = getMethod( MethodId.getTileEntity ).invoke( nmsWorld, blockPosition );

            // TODO This may use the new ResolvableProfile in 1.21+
            if ( getNMSClass( ClassId.TileEntitySkull ).isInstance( tileEntity) ) {
                getMethod( MethodId.setGameProfile ).invoke( tileEntity, profile );
            } else {
                throw new IllegalArgumentException( block + " is not a skull!" );
            }
            
        } catch( IllegalAccessException | InvocationTargetException | InstantiationException exception ) {
            exception.printStackTrace();
        }
    }

    private static Object getValue( Object object, Object... keys ) {
        if ( object instanceof ItemStack ) {
            return getItemTag( ( ItemStack ) object, keys );
        } else if ( object instanceof Entity ) {
            return getEntityTag( ( Entity ) object, keys );
        } else if ( object instanceof Block ) {
            return getBlockTag( ( Block ) object, keys );
        } else if ( object instanceof NBTCompound ) {
            try {
                return getTag( ( ( NBTCompound ) object ).tag, keys );
            } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, Block, or NBTCompound!" );
        }
    }

    /**
     * Gets an NBTCompound from an object
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * An NBTCompound, or null if none is stored at the provided location
     */
    public static NBTCompound getNBTCompound( Object object, Object... keys ) {
        if ( object == null ) {
            throw new NullPointerException( "Provided object was null!" );
        } else if ( object instanceof ItemStack ) {
            return getItemNBTTag( ( ItemStack ) object, keys );
        } else if ( object instanceof Entity ) {
            return getEntityNBTTag( ( Entity ) object, keys );
        } else if ( object instanceof Block ) {
            return getBlockNBTTag( ( Block ) object, keys );
        } else if ( object instanceof NBTCompound ) {
            try {
                return getNBTTag( ( ( NBTCompound ) object ).tag, keys );
            } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                e.printStackTrace();
                return null;
            }
        } else if ( getNMSClass( ClassId.NBTTagCompound ).isInstance( object ) ) {
            try {
                return getNBTTag( object, keys );
            } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, Block, or NBTCompound!" );
        }
    }

    /**
     * Gets a string from an object
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * A string, or null if none is stored at the provided location
     */
    public static String getString( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof String ? ( String ) result : null;
    }

    /**
     * Gets an int from an object
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * An integer, or 0 if none is stored at the provided location
     */
    public static int getInt( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof Integer ? ( int ) result : 0;
    }

    /**
     * Gets a double from an object
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * A double, or 0 if none is stored at the provided location
     */
    public static double getDouble( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof Double ? ( double ) result : 0;
    }

    /**
     * Gets a long from an object
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * A long, or 0 if none is stored at the provided location
     */
    public static long getLong( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof Long ? ( long ) result : 0;
    }

    /**
     * Gets a float from an object
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * A float, or 0 if none is stored at the provided location
     */
    public static float getFloat( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof Float ? ( float ) result : 0;
    }

    /**
     * Gets a short from an object
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * A short, or 0 if none is stored at the provided location
     */
    public static short getShort( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof Short ? ( short ) result : 0;
    }

    /**
     * Gets a byte from an object
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * A byte, or 0 if none is stored at the provided location
     */
    public static byte getByte( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof Byte ? ( byte ) result : 0;
    }

    /**
     * Gets a boolean from an object
     *
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * A boolean or false if none is stored at the provided location
     */
    public static boolean getBoolean( Object object, Object... keys ) {
        return getByte( object, keys ) == 1;
    }

    /**
     * Gets a byte array from an object
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * A byte array, or null if none is stored at the provided location
     */
    public static byte[] getByteArray( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof byte[] ? ( byte[] ) result : null;
    }

    /**
     * Gets an int array from an object
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * An int array, or null if none is stored at the provided location
     */
    public static int[] getIntArray( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result instanceof int[] ? ( int[] ) result : null;
    }

    /**
     * Checks if the object contains the given key
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * Whether or not the particular tag exists, may not be a primitive
     */
    public static boolean contains( Object object, Object... keys ) {
        Object result = getValue( object, keys );
        return result != null;
    }

    /**
     * Get the keys at the specific location, if it is a compound.
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * A set of keys
     */
    @SuppressWarnings( "unchecked" )
    public static Collection< String > getKeys( Object object, Object... keys ) {
        Object compound;
        if ( object instanceof ItemStack ) {
            compound = getCompound( ( ItemStack ) object );
            List< Object > keyList = new ArrayList< Object >( Arrays.asList( keys ) );
            keyList.add( 0, NBTEditor.ITEMSTACK_COMPONENTS );
            keys = keyList.toArray();
        } else if ( object instanceof Entity ) {
            compound = getCompound( ( Entity ) object );
        } else if ( object instanceof Block ) {
            compound = getCompound( ( Block ) object );
        } else if ( object instanceof NBTCompound ) {
            compound = ( ( NBTCompound ) object ).tag;
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, Block, or NBTCompound!" );
        }

        try {
            NBTCompound nbtCompound = getNBTTag( compound, keys );

            if ( nbtCompound != null && nbtCompound.tag != null ) {
                Object tag = nbtCompound.tag;
                if ( getNMSClass( ClassId.NBTTagCompound ).isInstance( tag ) ) {
                    return ( Collection< String > ) getMethod( MethodId.compoundKeys ).invoke( tag );
                } else {
                    return Collections.EMPTY_LIST;
                }
            } else {
                return Collections.EMPTY_LIST;
            }

        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
            e.printStackTrace();
        }

        return Collections.EMPTY_LIST;
    }

    /**
     * Gets the size of the list or NBTCompound at the given location.
     * 
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param keys
     * Keys in descending order
     * @return
     * The size of the list or compound at the given location.
     */
    public static int getSize( Object object, Object... keys ) {
        Object compound;
        if ( object instanceof ItemStack ) {
            compound = getCompound( ( ItemStack ) object );
        } else if ( object instanceof Entity ) {
            compound = getCompound( ( Entity ) object );
        } else if ( object instanceof Block ) {
            compound = getCompound( ( Block ) object );
        } else if ( object instanceof NBTCompound ) {
            compound = ( ( NBTCompound ) object ).tag;
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, Block, or NBTCompound!" );
        }

        try {
            NBTCompound nbtCompound = getNBTTag( compound, keys );
            if ( getNMSClass( ClassId.NBTTagCompound ).isInstance( nbtCompound.tag ) ) {
                return getKeys( nbtCompound ).size();
            } else if ( getNMSClass( ClassId.NBTTagList ).isInstance( nbtCompound.tag ) ) {
                return ( int ) getMethod( MethodId.listSize ).invoke( nbtCompound.tag );
            }
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
            e.printStackTrace();
            return 0;
        }

        throw new IllegalArgumentException( "Value is not a compound or list!" );
    }

    /**
     * Sets the value in the object with the given keys
     * 
     * @param <T>
     * ItemStack, Entity, Block, or NBTCompound.
     * @param object
     * Must be an ItemStack, Entity, Block, or NBTCompound
     * @param value
     * The value to set, can be an NBTCompound
     * @param keys
     * The keys in descending order
     * @return
     * The new item stack if the object provided is an item, else original object
     */
    @SuppressWarnings( "unchecked" )
    public static < T > T set( T object, Object value, Object... keys ) {
        if ( object instanceof ItemStack ) {
            return ( T ) setItemTag( ( ItemStack ) object, value, keys );
        } else if ( object instanceof Entity ) {
            setEntityTag( ( Entity ) object, value, keys );
        } else if ( object instanceof Block ) {
            setBlockTag( ( Block ) object, value, keys );
        } else if ( object instanceof NBTCompound ) {
            try {
                setTag( ( ( NBTCompound ) object ).tag, value, keys );
            } catch ( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException( "Object provided must be of type ItemStack, Entity, Block, or NBTCompound!" );
        }
        return object;
    }

    /**
     * Load an NBTCompound from a String.
     * 
     * @param json
     * A String in json format.
     * @return
     * An NBTCompound from the String provided. May or may not be a valid ItemStack.
     */
    public static NBTCompound getNBTCompound( String json ) {
        return NBTCompound.fromJson( json );
    }

    /**
     * Get an empty NBTCompound.
     * 
     * @return
     * A new NBTCompound that contains a NBTTagCompound object.
     */
    @SuppressWarnings( "deprecation" )
    public static NBTCompound getEmptyNBTCompound() {
        try {
            return new NBTCompound( getNMSClass( ClassId.NBTTagCompound ).newInstance() );
        } catch ( InstantiationException | IllegalAccessException e ) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings( "deprecation" )
    private static void setTag( Object tag, Object value, Object... keys ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object wrappedValue;
        // Get the real value of what we want to set here
        if ( value != null && value != Type.DELETE ) {
            if ( value instanceof NBTCompound ) {
                wrappedValue = ( ( NBTCompound ) value ).tag;
            } else if ( getNMSClass( ClassId.NBTTagList ).isInstance( value ) || getNMSClass( ClassId.NBTTagCompound ).isInstance( value ) ) {
                wrappedValue = value;
            } else if ( value == Type.COMPOUND ) {
                wrappedValue = getNMSClass( ClassId.NBTTagCompound ).newInstance();
            } else if ( value == Type.LIST ) {
                wrappedValue = getNMSClass( ClassId.NBTTagList ).newInstance();
            } else {
                if ( value instanceof Boolean ) {
                    value = ( byte ) ( ( Boolean ) value == true ? 1 : 0 );
                }
                Constructor< ? > cons = getNBTTagConstructor( value.getClass() );
                if ( cons != null ) {                    
                    wrappedValue = cons.newInstance( value );
                } else {
                    throw new IllegalArgumentException( "Provided value type(" + value.getClass() + ") is not supported!" );
                }
            }
        } else {
            wrappedValue = Type.DELETE;
        }

        Object compound = tag;
        for ( int index = 0; index < keys.length - 1; index++ ) {
            Object key = keys[ index ];
            Object prevCompound = compound;
            if ( key == Type.CUSTOM_DATA ) {
                // Only use the custom data key if the version is 1.20.5 or greater
                if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                    key = "minecraft:custom_data";
                } else {
                    continue;
                }
            } else if ( key == Type.ITEMSTACK_COMPONENTS ) {
                if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                    key = "components";
                } else {
                    key = "tag";
                }
            }
            
            if ( key instanceof Integer ) {
                int keyIndex = ( int ) key;
                List< ? > tagList = ( List< ? > ) NBTListData.get( compound );
                if ( keyIndex >= 0 && keyIndex < tagList.size() ) {
                    compound = tagList.get( keyIndex );
                } else {
                    compound = null;
                }
            } else if ( key != null && key != Type.NEW_ELEMENT ) {
                compound = getMethod( MethodId.compoundGet ).invoke( compound, ( String ) key );
            }
            if ( compound == null || key == null || key == Type.NEW_ELEMENT ) {
                if ( keys[ index + 1 ] == null || keys[ index + 1 ] instanceof Integer || keys[ index + 1 ] == Type.NEW_ELEMENT ) {
                    compound = getNMSClass( ClassId.NBTTagList ).newInstance();
                } else {
                    compound = getNMSClass( ClassId.NBTTagCompound ).newInstance();
                }
                if ( getNMSClass( ClassId.NBTTagList ).isInstance( prevCompound ) ) {
                    if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_14 ) ) {
                        getMethod( MethodId.listAdd ).invoke( prevCompound, getMethod( MethodId.listSize ).invoke( prevCompound ), compound );
                    } else {
                        getMethod( MethodId.listAdd ).invoke( prevCompound, compound );
                    }
                } else {
                    getMethod( MethodId.compoundSet ).invoke( prevCompound, ( String ) key, compound );
                }
            }
        }
        if ( keys.length > 0 ) {
            Object lastKey = keys[ keys.length - 1 ];
            if ( lastKey == null || lastKey == Type.NEW_ELEMENT ) {
                if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_14 ) ) {
                    getMethod( MethodId.listAdd ).invoke( compound, getMethod( MethodId.listSize ).invoke( compound ), wrappedValue );
                } else {
                    getMethod( MethodId.listAdd ).invoke( compound, wrappedValue );
                }
            } else if ( lastKey instanceof Integer ) {
                if ( wrappedValue == Type.DELETE ) {
                    getMethod( MethodId.listRemove ).invoke( compound, ( int ) lastKey );
                } else {
                    getMethod( MethodId.listSet ).invoke( compound, ( int ) lastKey, wrappedValue );
                }
            } else {
                if ( wrappedValue == Type.DELETE ) {
                    getMethod( MethodId.compoundRemove ).invoke( compound, ( String ) lastKey );
                } else {
                    getMethod( MethodId.compoundSet ).invoke( compound, ( String ) lastKey, wrappedValue );
                }
            }
        } else {
            // Add and replace all tags
            if ( wrappedValue != null ) {
                // Only if they're both an NBTTagCompound
                // Can't do anything if its a list or something
                if ( getNMSClass( ClassId.NBTTagCompound ).isInstance( wrappedValue ) && getNMSClass( ClassId.NBTTagCompound ).isInstance( compound ) )
                    for ( String key : getKeys( wrappedValue ) ) {
                        getMethod( MethodId.compoundSet ).invoke( compound, key, getMethod( MethodId.compoundGet ).invoke( wrappedValue, key ) );
                    }
            } else {
                // Did someone make an error?
                // NBTEditor.set( something, null );
                // Not sure what to do here
            }
        }
    }

    private static NBTCompound getNBTTag( Object tag, Object...keys ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object compound = tag;

        for ( Object key : keys ) {
            if ( compound == null ) {
                return null;
            } else if ( getNMSClass( ClassId.NBTTagCompound ).isInstance( compound ) ) {
                if ( key == Type.CUSTOM_DATA ) {
                    // Only use the custom data key if the version is 1.20.5 or greater
                    if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                        key = "minecraft:custom_data";
                    } else {
                        continue;
                    }
                } else if ( key == Type.ITEMSTACK_COMPONENTS ) {
                    if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                        key = "components";
                    } else {
                        key = "tag";
                    }
                }
                
                compound = getMethod( MethodId.compoundGet ).invoke( compound, ( String ) key );
            } else if ( getNMSClass( ClassId.NBTTagList ).isInstance( compound ) ) {
                int keyIndex = ( int ) key;
                List< ? > tagList = ( List< ? > ) NBTListData.get( compound );
                if ( keyIndex >= 0 && keyIndex < tagList.size() ) {
                    compound = tagList.get( keyIndex );
                } else {
                    compound = null;
                }
            }
        }
        return new NBTCompound( compound );
    }

    private static Object getTag( Object tag, Object... keys ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if ( keys.length == 0 ) {
            return getTags( tag );
        }

        Object nbtObj = tag;

        for ( Object key : keys ) {
            if ( nbtObj == null ) {
                return null;
            } else if ( getNMSClass( ClassId.NBTTagCompound ).isInstance( nbtObj ) ) {
                if ( key == Type.CUSTOM_DATA ) {
                    // Only use the custom data key if the version is 1.20.5 or greater
                    if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                        key = "minecraft:custom_data";
                    } else {
                        continue;
                    }
                } else if ( key == Type.ITEMSTACK_COMPONENTS ) {
                    if ( LOCAL_VERSION.greaterThanOrEqualTo( MinecraftVersion.v1_20_R4 ) ) {
                        key = "components";
                    } else {
                        key = "tag";
                    }
                }
                
                if ( key instanceof String ) {
                    nbtObj = getMethod( MethodId.compoundGet ).invoke( nbtObj, ( String ) key );                    
                } else {
                    throw new IllegalArgumentException( "Key " + key + " is not a string! Must provide a valid key for an NBT Tag Compound" );
                }
            } else if ( getNMSClass( ClassId.NBTTagList ).isInstance( nbtObj ) ) {
                if ( !( key instanceof Integer ) ) {
                    throw new IllegalArgumentException( "Key " + key + " is not an integer! Must provide a valid index for an NBT Tag List" );
                }
                int keyIndex = ( int ) key;
                List< ? > tagList = ( List< ? > ) NBTListData.get( nbtObj );
                if ( keyIndex >= 0 && keyIndex < tagList.size() ) {
                    nbtObj = tagList.get( keyIndex );
                } else {
                    nbtObj = null;
                }
            } else {
                return getNBTVar( nbtObj );
            }
        }
        if ( nbtObj == null ) {
            return null;
        } else if ( getNMSClass( ClassId.NBTTagList ).isInstance( nbtObj ) ) {
            return getTags( nbtObj );
        } else if ( getNMSClass( ClassId.NBTTagCompound ).isInstance( nbtObj ) ) {
            return getTags( nbtObj );
        } else {
            return getNBTVar( nbtObj );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static Object getTags( Object tag ) {
        Map< Object, Object > tags = new HashMap< Object, Object >();
        try {
            if ( getNMSClass( ClassId.NBTTagCompound ).isInstance( tag ) ) {
                Map< String, Object > tagCompound = ( Map< String, Object > ) NBTCompoundMap.get( tag );
                for ( String key : tagCompound.keySet() ) {
                    Object value = tagCompound.get( key );
                    if ( getNMSClass( ClassId.NBTTagEnd ).isInstance( value ) ) {
                        continue;
                    }
                    tags.put( key, getTag( value ) );
                }
            } else if ( getNMSClass( ClassId.NBTTagList ).isInstance( tag ) ) {
                List< Object > tagList = ( List< Object > ) NBTListData.get( tag );
                for ( int index = 0; index < tagList.size(); index++ ) {
                    Object value = tagList.get( index );
                    if ( getNMSClass( ClassId.NBTTagEnd ).isInstance( value ) ) {
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
    
    private static Object registryAccess() {
        try {
            return getMethod( MethodId.registryAccess ).invoke( getMethod( MethodId.getServer ).invoke( Bukkit.getServer() ) );
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
            e.printStackTrace();
            return null;
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

        public void set( Object value, Object... keys ) {
            try {
                setTag( tag, value, keys );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        /**
         * The exact same as the toString method
         * 
         * @return
         * Convert the compound to a string.
         */
        public String toJson() {
            return tag.toString();
        }

        public static NBTCompound fromJson( String json ) {
            try {
                return new NBTCompound( getMethod( MethodId.loadNBTTagCompound ).invoke( null, json ) );
            } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return tag.toString();
        }

        @Override
        public int hashCode() {
            return tag.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NBTCompound other = (NBTCompound) obj;
            if (tag == null) {
                if (other.tag != null)
                    return false;
            } else if (!tag.equals(other.tag))
                return false;
            return true;
        }
    }

    /**
     * Minecraft versions as enums
     * 
     * @author BananaPuncher714
     */
    public enum MinecraftVersion {
        v1_8,
        v1_9,
        v1_10,
        v1_11,
        v1_12,
        v1_13,
        v1_14,
        v1_15,
        v1_16,
        v1_17,
        v1_18_R1,
        v1_18_R2,
        v1_19_R1,
        v1_19_R2,
        v1_19_R3,
        v1_20_R1,
        v1_20( false ),
        v1_20_1( false ),
        v1_20_R2,
        v1_20_2( false ),
        v1_20_R3,
        v1_20_3( false ),
        v1_20_R4,
        v1_20_4( false ),
        v1_20_5( false ),
        v1_20_6( false ),
        v1_21_R1,
        v1_21( false ),
        v1_21_R2,
        v1_21_3( false ),
        v1_21_R3,
        v1_21_4( false ),
        v1_21_R4,
        v1_21_5( false ),
        v1_21_R5,
        v1_21_6( false ),
        v1_21_7( false ),
        v1_21_8( false ),
        v1_21_9( false ),
        v1_21_10( false ),
        v1_22;

        private boolean implemented = true;
        
        private MinecraftVersion() {
            this( true );
        }
        
        private MinecraftVersion( boolean implemented ) {
            this.implemented = implemented;
        }
        
        // Would be really cool if we could overload operators here
        public boolean greaterThanOrEqualTo( MinecraftVersion other ) {
            return ordinal() >= other.ordinal();
        }

        public boolean lessThanOrEqualTo( MinecraftVersion other ) {
            return ordinal() <= other.ordinal();
        }

        public static MinecraftVersion get( String v ) {
            v = v.replace( '.', '_' );
            
            for ( int i = MinecraftVersion.values().length; i > 0; --i ) {
                MinecraftVersion k = MinecraftVersion.values()[ i - 1 ];
                
                if ( v.contains( k.name().substring( 1 ) ) ) {
                    return getLastImplemented( k );
                }
            }
            
            // Return the latest version if none was found
            return MinecraftVersion.values()[ MinecraftVersion.values().length - 1 ];
        }
        
        private static MinecraftVersion getLastImplemented( MinecraftVersion v ) {
            for ( int i = v.ordinal(); i >= 0; --i ) {
                MinecraftVersion version = MinecraftVersion.values()[ i ];
                
                if ( version.implemented ) {
                    return version;
                }
            }
            return null;
        }
    }
    
    private enum Type {
        COMPOUND, LIST, NEW_ELEMENT, DELETE, CUSTOM_DATA, ITEMSTACK_COMPONENTS;
    }
    
    private enum ClassId {
        NBTBase,
        NBTTagCompound,
        NBTTagList,
        NBTTagEnd,
        MojangsonParser,
        ItemStack,
        Entity,
        EntityLiving,
        BlockPosition,
        IBlockData,
        World,
        TileEntity,
        TileEntitySkull,
        CraftItemStack,
        CraftMetaSkull,
        CraftEntity,
        CraftWorld,
        CraftBlockState,
        GameProfile,
        Property,
        PropertyMap,
        CraftServer,
        MinecraftServer,
        RegistryAccess,
        ResolvableProfile,
        IRegistryCustomDimension,
        Codec,
        NbtOps,
        DataResult,
        DynamicOps,
        TagValueInput,
        TagValueOutput,
        ValueInput,
        ValueOutput,
        ProblemReporter,
        ValueInputContextHelper
    }
    
    private enum MethodId {
        compoundGet,
        compoundSet,
        compoundHasKey,
        
        listSet,
        listAdd,
        listSize,
        
        listRemove,
        compoundRemove,
        
        compoundKeys,
        
        // Removed in 1.20.5
        itemHasTag,
        // Removed in 1.20.5
        getItemTag,
        
        setItemTag,
        itemSave,
        
        asNMSCopy,
        asBukkitCopy,
        
        getEntityHandle,
        getWorldHandle,
        getTileEntity,
        getTileType,
        
        getEntityTag,
        setEntityTag,
        
        createStack,
        
        setTileTag,
        getTileTag,
        
        getProperties,
        setGameProfile,
        
        setCraftMetaSkullProfile,
        
        propertyValues,
        putProperty,
        
        getPropertyName,
        getPropertyValue,
        
        loadNBTTagCompound,
        
        getServer,
        registryAccess,
        
        // Introduced in 1.20.5
        saveOptional,
        
        // Added in 1.21, some paper build
        setCraftMetaSkullResolvableProfile,
        getResolvableProfileGameProfile,
        
        // Added in 1.21.5
        createStackOptional,
        
        // Used in 1.21.6
        decoderParse,
        createSerializationContext,
        encoderEncodeStart,
        getOrThrow,
        createTagValueOutput,
        convertToNbtTagCompound
    }
    
    private static abstract class ReflectionTarget implements Comparable< ReflectionTarget > {
        private final MinecraftVersion version;
        
        // Provide an external alternative to fetching classes, kind of like a classloader
        private Function< ClassId, Class< ? > > classFetcher;
        
        private final Map< ClassId, String > classTargets = new HashMap< ClassId, String >();
        private final Map< MethodId, ConstructorTarget > methodTargets = new HashMap< MethodId, ConstructorTarget >();
        private final Map< ClassId, ConstructorTarget > constructorTargets = new HashMap< ClassId, ConstructorTarget >();
        
        protected ReflectionTarget( MinecraftVersion version ) {
            this.version = version;
        }
        
        protected MinecraftVersion getVersion() {
            return version;
        }
        
        protected final ReflectionTarget setClassFetcher( Function< ClassId, Class< ? > > func ) {
            this.classFetcher = func;
            
            return this;
        }
        
        protected final void addClass( ClassId name, String path ) {
            classTargets.put( name, path );
        }
        
        protected final MethodTarget addMethod( MethodId name, ClassId clazz, String methodName, Object... params ) {
            MethodTarget newTarget = new MethodTarget( clazz, methodName, params );
            methodTargets.put( name, newTarget );
            return newTarget;
        }
        
        protected final ReturnMethodTarget addMethod( MethodId name, ClassId clazz, ClassId returnType, Object... params ) {
            ReturnMethodTarget newTarget = new ReturnMethodTarget( clazz, returnType, params );
            methodTargets.put( name, newTarget );
            return newTarget;
        }
        
        protected final void addConstructor( ClassId clazz, Object... params ) {
            constructorTargets.put( clazz, new ConstructorTarget( clazz, params ) );
        }
        
        protected final Class< ? > fetchClass( final ClassId name ) throws ClassNotFoundException {
            String className = classTargets.get( name );
            return className != null ? Class.forName( className ) : null;
        }
        
        protected final Method fetchMethod( MethodId name ) throws NoSuchMethodException, SecurityException, ClassNotFoundException {
            ConstructorTarget constructorTarget = methodTargets.get( name );
            if ( constructorTarget instanceof MethodTarget ) {
                MethodTarget target = ( MethodTarget ) constructorTarget;
                Class< ? > clazz = findClass( target.clazz );
                Class< ? >[] params = convert( target.params );
                try {
                    return clazz.getMethod( target.name, params );
                } catch ( NoSuchMethodException e ) {
                    try {
                        Method method = clazz.getDeclaredMethod( target.name, params );
                        method.setAccessible( true );
                        return method;
                    } catch ( NoSuchMethodException e2 ) {
                        if ( target.silent ) {
                            return null;
                        } else {
                            throw e;
                        }
                    }
                }
            } else if ( constructorTarget instanceof ReturnMethodTarget ) {
                ReturnMethodTarget target = ( ReturnMethodTarget ) constructorTarget;
                Class< ? > clazz = findClass( target.clazz );
                Class< ? > returnClazz = findClass( target.returnType );
                Class< ? >[] params = convert( target.params );
                
                for ( Method method : clazz.getDeclaredMethods() ) {
                    if ( method.getReturnType().equals( returnClazz ) && matches( method.getParameterTypes(), params ) ) {
                        method.setAccessible( true );
                        return method;
                    }
                }
                return null;
            } else {
                return null;
            }
        }
        
        protected final Constructor< ? > fetchConstructor( ClassId name ) throws NoSuchMethodException, SecurityException, ClassNotFoundException {
            final ConstructorTarget target = constructorTargets.get( name );
            final Constructor< ? > constructor = target != null ? findClass( target.clazz ).getDeclaredConstructor( convert( target.params ) ) : null;
            if ( constructor != null ) {
                constructor.setAccessible( true );
            }
            return constructor;
        }
        
        private final boolean matches( Class< ? >[] params, Class< ? >[] find ) {
            if ( params.length != find.length ) {
                return false;
            } else {
                for ( int i = 0; i < params.length; ++i ) {
                    if ( !find[ i ].isAssignableFrom( params[ i ] ) ) {
                        return false;
                    }
                }
                return true;
            }
        }
        
        private final Class< ? >[] convert( Object[] objects ) throws ClassNotFoundException {
            Class< ? >[] params = new Class< ? >[ objects.length ];
            for ( int i = 0; i < objects.length; i++ ) {
                Object obj = objects[ i ];
                if ( obj instanceof Class ) {
                    params[ i ] = ( Class < ? > ) obj;
                } else if ( obj instanceof ClassId ) {
                    params[ i ] = findClass( ( ClassId ) obj );
                } else {
                    throw new IllegalArgumentException( "Invalid parameter type: " + obj );
                }
            }
            return params;
        }

        private final Class< ? > findClass( ClassId name ) throws ClassNotFoundException {
            return classFetcher != null ? classFetcher.apply( name ) : fetchClass( name );
        }
        
        @Override
        public int compareTo( ReflectionTarget o ) {
            // Reverse sort
            return o.version.compareTo( version );
        }
        
        private static class ConstructorTarget {
            final ClassId clazz;
            final Object[] params;
            
            public ConstructorTarget( ClassId clazz, Object... params ) {
                this.clazz = clazz;
                this.params = params;
            }
        }
        
        private static class MethodTarget extends ConstructorTarget {
            final String name;
            boolean silent = false;
            
            public MethodTarget( ClassId clazz, String name, Object... params ) {
                super( clazz, params );
                this.name = name;
            }
            
            public MethodTarget failSilently( boolean silent ) {
                this.silent = silent;
                return this;
            }
        }
        
        private static class ReturnMethodTarget extends ConstructorTarget {
            final ClassId returnType;
            
            public ReturnMethodTarget( ClassId clazz, ClassId returnType, Object... params ) {
                super( clazz, params );
                this.returnType = returnType;
            }
        }
        
        private static class v1_8 extends ReflectionTarget {
            protected v1_8() {
                super( MinecraftVersion.v1_8 );
                
                String craftbukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
                
                addClass( ClassId.NBTBase,             "net.minecraft.server." + VERSION + "." + "NBTBase" );
                addClass( ClassId.NBTTagCompound,      "net.minecraft.server." + VERSION + "." + "NBTTagCompound" );
                addClass( ClassId.NBTTagList,          "net.minecraft.server." + VERSION + "." + "NBTTagList" );
                addClass( ClassId.NBTTagEnd,           "net.minecraft.server." + VERSION + "." + "NBTTagEnd" );
                addClass( ClassId.MojangsonParser,     "net.minecraft.server." + VERSION + "." + "MojangsonParser" );
                addClass( ClassId.ItemStack,           "net.minecraft.server." + VERSION + "." + "ItemStack" );
                addClass( ClassId.Entity,              "net.minecraft.server." + VERSION + "." + "Entity" );
                addClass( ClassId.EntityLiving,        "net.minecraft.server." + VERSION + "." + "EntityLiving" );
                addClass( ClassId.BlockPosition,       "net.minecraft.server." + VERSION + "." + "BlockPosition" );
                addClass( ClassId.IBlockData,          "net.minecraft.server." + VERSION + "." + "IBlockData" );
                addClass( ClassId.World,               "net.minecraft.server." + VERSION + "." + "World" );
                addClass( ClassId.TileEntity,          "net.minecraft.server." + VERSION + "." + "TileEntity" );
                addClass( ClassId.TileEntitySkull,     "net.minecraft.server." + VERSION + "." + "TileEntitySkull" );
                addClass( ClassId.CraftServer,         craftbukkitPackage + "." + "CraftServer" );
                addClass( ClassId.CraftItemStack,      craftbukkitPackage + ".inventory." + "CraftItemStack" );
                addClass( ClassId.CraftMetaSkull,      craftbukkitPackage + ".inventory." + "CraftMetaSkull" );
                addClass( ClassId.CraftEntity,         craftbukkitPackage + ".entity." + "CraftEntity" );
                addClass( ClassId.CraftWorld,          craftbukkitPackage + "." + "CraftWorld" );
                addClass( ClassId.CraftBlockState,     craftbukkitPackage + ".block." + "CraftBlockState" );
                addClass( ClassId.GameProfile,         "com.mojang.authlib.GameProfile" );
                addClass( ClassId.Property,            "com.mojang.authlib.properties.Property" );
                addClass( ClassId.PropertyMap,         "com.mojang.authlib.properties.PropertyMap" );
                
                addMethod( MethodId.compoundGet, ClassId.NBTTagCompound, "get", String.class );
                addMethod( MethodId.compoundSet, ClassId.NBTTagCompound, "set", String.class, ClassId.NBTBase );
                addMethod( MethodId.compoundHasKey, ClassId.NBTTagCompound, "hasKey", String.class );
                
                addMethod( MethodId.listSet, ClassId.NBTTagList, "a", int.class, ClassId.NBTBase );
                
                addMethod( MethodId.listAdd, ClassId.NBTTagList, "add", ClassId.NBTBase );
                addMethod( MethodId.listSize, ClassId.NBTTagList, "size" );
                
                addMethod( MethodId.listRemove, ClassId.NBTTagList, "a", int.class );
                addMethod( MethodId.compoundRemove, ClassId.NBTTagCompound, "remove", String.class );
                
                addMethod( MethodId.compoundKeys, ClassId.NBTTagCompound, "c" );
                
                addMethod( MethodId.itemHasTag, ClassId.ItemStack, "hasTag" );
                addMethod( MethodId.getItemTag, ClassId.ItemStack, "getTag" );
                addMethod( MethodId.setItemTag, ClassId.ItemStack, "setTag", ClassId.NBTTagCompound );
                addMethod( MethodId.itemSave, ClassId.ItemStack, "save", ClassId.NBTTagCompound );
                
                addMethod( MethodId.asNMSCopy, ClassId.CraftItemStack, "asNMSCopy", ItemStack.class );
                addMethod( MethodId.asBukkitCopy, ClassId.CraftItemStack, "asBukkitCopy", ClassId.ItemStack );
                
                addMethod( MethodId.getEntityHandle, ClassId.CraftEntity, "getHandle" );
                
                addMethod( MethodId.getEntityTag, ClassId.Entity, "c", ClassId.NBTTagCompound );
                addMethod( MethodId.setEntityTag, ClassId.Entity, "f", ClassId.NBTTagCompound );
                
                addMethod( MethodId.createStack, ClassId.ItemStack, "createStack", ClassId.NBTTagCompound );
                
                addMethod( MethodId.setTileTag, ClassId.TileEntity, "a", ClassId.NBTTagCompound );
                addMethod( MethodId.getTileTag, ClassId.TileEntity, "b", ClassId.NBTTagCompound );
                
                addMethod( MethodId.getWorldHandle, ClassId.CraftWorld, "getHandle" );
                addMethod( MethodId.getTileEntity, ClassId.World, "getTileEntity", ClassId.BlockPosition );
                
                addMethod( MethodId.getProperties, ClassId.GameProfile, "getProperties" );
                addMethod( MethodId.setGameProfile, ClassId.TileEntitySkull, "setGameProfile", ClassId.GameProfile );
                
                addMethod( MethodId.propertyValues, ClassId.PropertyMap, "values" );
                addMethod( MethodId.putProperty, ClassId.PropertyMap, "put", Object.class, Object.class );
                
                addMethod( MethodId.getPropertyName, ClassId.Property, "getName" );
                addMethod( MethodId.getPropertyValue, ClassId.Property, "getValue" );
                
                addMethod( MethodId.loadNBTTagCompound, ClassId.MojangsonParser, "parse", String.class );
                
                addConstructor( ClassId.BlockPosition, int.class, int.class, int.class );
                addConstructor( ClassId.GameProfile, UUID.class, String.class );
                addConstructor( ClassId.Property, String.class, String.class );
            }
        }
        
        private static class v1_9 extends ReflectionTarget {
            protected v1_9() {
                super( MinecraftVersion.v1_9 );
                
                addMethod( MethodId.listRemove, ClassId.NBTTagList, "remove", int.class );
                
                addMethod( MethodId.compoundKeys, ClassId.NBTTagCompound, "c" );
                addMethod( MethodId.getTileTag, ClassId.TileEntity, "save", ClassId.NBTTagCompound );
            }
        }
        
        private static class v1_11 extends ReflectionTarget {
            protected v1_11() {
                super( MinecraftVersion.v1_11 );
                
                addConstructor( ClassId.ItemStack, ClassId.NBTTagCompound );
            }
        }
        
        private static class v1_12 extends ReflectionTarget {
            protected v1_12() {
                super( MinecraftVersion.v1_12 );
                
                addMethod( MethodId.setTileTag, ClassId.TileEntity, "load", ClassId.NBTTagCompound );
            }
        }
        
        private static class v1_13 extends ReflectionTarget {
            protected v1_13() {
                super( MinecraftVersion.v1_13 );
                
                addMethod( MethodId.compoundKeys, ClassId.NBTTagCompound, "getKeys" );
                
                addMethod( MethodId.createStack, ClassId.ItemStack, "a", ClassId.NBTTagCompound );
            }
        }
        
        private static class v1_15 extends ReflectionTarget {
            protected v1_15() {
                super( MinecraftVersion.v1_15 );
                
                // Fail silently if this method does not exist in later versions
                addMethod( MethodId.setCraftMetaSkullProfile, ClassId.CraftMetaSkull, "setProfile", ClassId.GameProfile ).failSilently( MinecraftVersion.v1_21_R1.lessThanOrEqualTo( LOCAL_VERSION ) );
            }
        }
        
        private static class v1_16 extends ReflectionTarget {
            protected v1_16() {
                super( MinecraftVersion.v1_16 );
                
                addMethod( MethodId.getEntityTag, ClassId.Entity, "save", ClassId.NBTTagCompound );
                addMethod( MethodId.setEntityTag, ClassId.Entity, "load", ClassId.NBTTagCompound );
                
                addMethod( MethodId.getTileType, ClassId.World, "getType", ClassId.BlockPosition );
                addMethod( MethodId.setTileTag, ClassId.TileEntity, "load", ClassId.IBlockData, ClassId.NBTTagCompound );
            }
        }
        
        private static class v1_17 extends ReflectionTarget {
            protected v1_17() {
                super( MinecraftVersion.v1_17 );
                
                addClass( ClassId.NBTBase,             "net.minecraft.nbt.NBTBase" );
                addClass( ClassId.NBTTagCompound,      "net.minecraft.nbt.NBTTagCompound" );
                addClass( ClassId.NBTTagList,          "net.minecraft.nbt.NBTTagList" );
                addClass( ClassId.NBTTagEnd,           "net.minecraft.nbt.NBTTagEnd" );
                addClass( ClassId.MojangsonParser,     "net.minecraft.nbt.MojangsonParser" );
                addClass( ClassId.ItemStack,           "net.minecraft.world.item.ItemStack" );
                addClass( ClassId.Entity,              "net.minecraft.world.entity.Entity" );
                addClass( ClassId.EntityLiving,        "net.minecraft.world.entity.EntityLiving" );
                addClass( ClassId.BlockPosition,       "net.minecraft.core.BlockPosition" );
                addClass( ClassId.IBlockData,          "net.minecraft.world.level.block.state.IBlockData" );
                addClass( ClassId.World,               "net.minecraft.world.level.World" );
                addClass( ClassId.TileEntity,          "net.minecraft.world.level.block.entity.TileEntity" );
                addClass( ClassId.TileEntitySkull,     "net.minecraft.world.level.block.entity.TileEntitySkull" );
                
                addMethod( MethodId.listSet, ClassId.NBTTagList, "set", int.class, ClassId.NBTBase );
                addMethod( MethodId.setTileTag, ClassId.TileEntity, "load", ClassId.NBTTagCompound );
            }
        }
        
        private static class v1_18_R1 extends ReflectionTarget {
            protected v1_18_R1() {
                super( MinecraftVersion.v1_18_R1 );
                
                addMethod( MethodId.compoundGet, ClassId.NBTTagCompound, "c", String.class );
                addMethod( MethodId.compoundSet, ClassId.NBTTagCompound, "a", String.class, ClassId.NBTBase );
                addMethod( MethodId.compoundHasKey, ClassId.NBTTagCompound, "e", String.class );
                
                addMethod( MethodId.listSet, ClassId.NBTTagList, "d", int.class, ClassId.NBTBase );
                addMethod( MethodId.listAdd, ClassId.NBTTagList, "c", int.class, ClassId.NBTBase );
                
                addMethod( MethodId.listRemove, ClassId.NBTTagList, "c", int.class );
                addMethod( MethodId.compoundRemove, ClassId.NBTTagCompound, "r", String.class );
                
                addMethod( MethodId.compoundKeys, ClassId.NBTTagCompound, "d" );
                
                addMethod( MethodId.itemHasTag, ClassId.ItemStack, "r" );
                addMethod( MethodId.getItemTag, ClassId.ItemStack, "s" );
                addMethod( MethodId.setItemTag, ClassId.ItemStack, "c", ClassId.NBTTagCompound );
                addMethod( MethodId.itemSave, ClassId.ItemStack, "b", ClassId.NBTTagCompound );
                
                addMethod( MethodId.getEntityTag, ClassId.Entity, "f", ClassId.NBTTagCompound );
                addMethod( MethodId.setEntityTag, ClassId.Entity, "g", ClassId.NBTTagCompound );
                
                addMethod( MethodId.setTileTag, ClassId.TileEntity, "a", ClassId.NBTTagCompound );
                addMethod( MethodId.getTileTag, ClassId.TileEntity, "m" );
                addMethod( MethodId.getTileEntity, ClassId.World, "c_", ClassId.BlockPosition );
                
                addMethod( MethodId.setGameProfile, ClassId.TileEntitySkull, "a", ClassId.GameProfile );
                
                addMethod( MethodId.loadNBTTagCompound, ClassId.MojangsonParser, "a", String.class );
            }
        }
        
        private static class v1_18_R2 extends ReflectionTarget {
            protected v1_18_R2() {
                super( MinecraftVersion.v1_18_R2 );
                
                addMethod( MethodId.itemHasTag, ClassId.ItemStack, "s" );
                addMethod( MethodId.getItemTag, ClassId.ItemStack, "t" );
            }
        }
        
        private static class v1_19_R1 extends ReflectionTarget {
            protected v1_19_R1() {
                super( MinecraftVersion.v1_19_R1 );
                
                addMethod( MethodId.itemHasTag, ClassId.ItemStack, "t" );
                addMethod( MethodId.getItemTag, ClassId.ItemStack, "u" );
            }
        }
        
        private static class v1_19_R2 extends ReflectionTarget {
            protected v1_19_R2() {
                super( MinecraftVersion.v1_19_R2 );
                
                addMethod( MethodId.compoundKeys, ClassId.NBTTagCompound, "e" );
            }
        }
        
        private static class v1_20_R1 extends ReflectionTarget {
            protected v1_20_R1() {
                super( MinecraftVersion.v1_20_R1 );
                
                addMethod( MethodId.itemHasTag, ClassId.ItemStack, "u" );
                addMethod( MethodId.getItemTag, ClassId.ItemStack, "v" );
            }
        }
        
        private static class v1_20_R2 extends ReflectionTarget {
            protected v1_20_R2() {
                super( MinecraftVersion.v1_20_R2 );
                
                addMethod( MethodId.getPropertyName, ClassId.Property, "name" );
                addMethod( MethodId.getPropertyValue, ClassId.Property, "value" );
            }
        }

        private static class v1_20_R3 extends ReflectionTarget {
            protected v1_20_R3() {
                super( MinecraftVersion.v1_20_R3 );

                addMethod( MethodId.getTileTag, ClassId.TileEntity, "o" );
            }
        }
        
        private static class v1_20_R4 extends ReflectionTarget {
            protected v1_20_R4() {
                super( MinecraftVersion.v1_20_R4 );
                
                // 1.20.4 and 1.20.5 are also 1_20_R4 but do not have these methods
                if ( !BUKKIT_VERSION.startsWith( "1.20.4" ) && !BUKKIT_VERSION.startsWith( "1.20.5" ) ) {
                    addClass( ClassId.MinecraftServer, "net.minecraft.server.MinecraftServer" );
                    addClass( ClassId.RegistryAccess, "net.minecraft.core.HolderLookup$a" );
                    addClass( ClassId.ResolvableProfile, "net.minecraft.world.item.component.ResolvableProfile" );
                    
                    addMethod( MethodId.getServer, ClassId.CraftServer, "getServer" );
                    addMethod( MethodId.registryAccess, ClassId.MinecraftServer, "bc" );
                    
                    addMethod( MethodId.saveOptional, ClassId.ItemStack, "a", ClassId.RegistryAccess );
                    addMethod( MethodId.createStack, ClassId.ItemStack, "a", ClassId.RegistryAccess, ClassId.NBTTagCompound );
                
                    addMethod( MethodId.getTileTag, ClassId.TileEntity, "b", ClassId.RegistryAccess );
                    addMethod( MethodId.setTileTag, ClassId.TileEntity, "a", ClassId.NBTTagCompound, ClassId.RegistryAccess );
                }
            }
        }
        
        private static class v1_21_R1 extends ReflectionTarget {
            protected v1_21_R1() {
                super( MinecraftVersion.v1_21_R1 );
                
                addClass( ClassId.ResolvableProfile, "net.minecraft.world.item.component.ResolvableProfile" );
                addClass( ClassId.IRegistryCustomDimension, "net.minecraft.core.IRegistryCustom$Dimension" );
                
                // The old setProfile with GameProfile may be used, so fail silently if that's the case
                addMethod( MethodId.setCraftMetaSkullResolvableProfile, ClassId.CraftMetaSkull, "setProfile", ClassId.ResolvableProfile ).failSilently( true );
                addMethod( MethodId.getResolvableProfileGameProfile, ClassId.ResolvableProfile, "f" );
                
                // v1_21_R1 includes 1.21, 1.21.1 and 1.21.2
                // but the IRegistryCustom.Dimensions method is different
                // so, find it dynamically
                addMethod( MethodId.registryAccess, ClassId.MinecraftServer, ClassId.IRegistryCustomDimension );
                
                addConstructor( ClassId.ResolvableProfile, ClassId.GameProfile );
            }
        }
        
        private static class v1_21_R4 extends ReflectionTarget {
            protected v1_21_R4() {
                super( MinecraftVersion.v1_21_R4 );
                
                addMethod( MethodId.compoundGet, ClassId.NBTTagCompound, "a", String.class );
                
                addMethod( MethodId.createStackOptional, ClassId.ItemStack, "a", ClassId.RegistryAccess, ClassId.NBTBase );
                
                addMethod( MethodId.listSet, ClassId.NBTTagList, "c", int.class, ClassId.NBTBase );
                addMethod( MethodId.listAdd, ClassId.NBTTagList, "d", int.class, ClassId.NBTBase );
                
                addMethod( MethodId.setEntityTag, ClassId.Entity, "i", ClassId.NBTTagCompound );
            }
        }
        
        private static class v1_21_R5 extends ReflectionTarget {
            private static Object ITEMSTACK_CODEC;
            private static Object NBT_OPS;
            private static Object PROBLEM_REPORTER;
            
            protected v1_21_R5() {
                super( MinecraftVersion.v1_21_R5 );
                
                addClass( ClassId.DataResult, "com.mojang.serialization.DataResult" );
                addClass( ClassId.Codec, "com.mojang.serialization.Codec" );
                addClass( ClassId.DynamicOps, "com.mojang.serialization.DynamicOps" );
                addClass( ClassId.NbtOps, "net.minecraft.nbt.DynamicOpsNBT" );
                addClass( ClassId.ValueInput, "net.minecraft.world.level.storage.ValueInput" );
                addClass( ClassId.TagValueInput, "net.minecraft.world.level.storage.TagValueInput" );
                addClass( ClassId.ValueOutput, "net.minecraft.world.level.storage.ValueOutput" );
                addClass( ClassId.TagValueOutput, "net.minecraft.world.level.storage.TagValueOutput" );
                addClass( ClassId.ValueInputContextHelper, "net.minecraft.world.level.storage.ValueInputContextHelper" );
                addClass( ClassId.ProblemReporter, "net.minecraft.util.ProblemReporter" );
                
                addMethod( MethodId.decoderParse, ClassId.Codec, "parse", ClassId.DynamicOps, Object.class );
                addMethod( MethodId.encoderEncodeStart, ClassId.Codec, "encodeStart", ClassId.DynamicOps, Object.class );
                addMethod( MethodId.createSerializationContext, ClassId.RegistryAccess, "a", ClassId.DynamicOps );
                addMethod( MethodId.getOrThrow, ClassId.DataResult, "getOrThrow" );
                addMethod( MethodId.createTagValueOutput, ClassId.TagValueOutput, "a", ClassId.ProblemReporter, ClassId.RegistryAccess );
                addMethod( MethodId.convertToNbtTagCompound, ClassId.TagValueOutput, "b" );
                
                addMethod( MethodId.getResolvableProfileGameProfile, ClassId.ResolvableProfile, "g" );
                addMethod( MethodId.setTileTag, ClassId.TileEntity, "b", ClassId.ValueInput );
                addMethod( MethodId.getEntityTag, ClassId.Entity, "b", ClassId.ValueOutput );
                addMethod( MethodId.setEntityTag, ClassId.Entity, "e", ClassId.ValueInput );
                
                addConstructor( ClassId.ValueInputContextHelper, ClassId.RegistryAccess, ClassId.DynamicOps );
                addConstructor( ClassId.TagValueInput, ClassId.ProblemReporter, ClassId.ValueInputContextHelper, ClassId.NBTTagCompound );
            }
            
            private static Object getItemStackCodec() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
                if ( ITEMSTACK_CODEC == null ) {
                    ITEMSTACK_CODEC = getNMSClass( ClassId.ItemStack ).getField( "b" ).get( null );
                }
                return ITEMSTACK_CODEC;
            }
            
            private static Object getNbtOps() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
                if ( NBT_OPS == null ) {
                    NBT_OPS = getNMSClass( ClassId.NbtOps ).getField( "a" ).get( null );
                }
                return NBT_OPS;
            }
            
            private static Object getProblemReporter() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
                if ( PROBLEM_REPORTER == null ) {
                    PROBLEM_REPORTER = getNMSClass( ClassId.ProblemReporter ).getField( "a" ).get( null );
                }
                return PROBLEM_REPORTER;
            }
            
            protected static Object newTagValueOutput( final Object registryAccess ) throws IllegalAccessException, InvocationTargetException, IllegalArgumentException, NoSuchFieldException, SecurityException {
                return getMethod( MethodId.createTagValueOutput ).invoke( null, getProblemReporter(), registryAccess );
            }
            
            protected static Object getValueInputFromNbtTagCompound( final Object registryAccess, final Object compound ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, SecurityException {
                final Object contextHelper = getConstructor( ClassId.ValueInputContextHelper ).newInstance( registryAccess, getNbtOps() );
                return getConstructor( ClassId.TagValueInput ).newInstance( getProblemReporter(), contextHelper, compound );
            }
            
            protected static Object getItemFrom( final Object registryAccess, final Object compound ) throws IllegalAccessException, InvocationTargetException, IllegalArgumentException, NoSuchFieldException, SecurityException {
                return getMethod( MethodId.getOrThrow ).invoke( getMethod( MethodId.decoderParse ).invoke( getItemStackCodec(), getMethod( MethodId.createSerializationContext ).invoke( registryAccess, getNbtOps() ), compound ) );
            }
            
            protected static Object save( final Object item, final Object registryAccess ) throws IllegalAccessException, InvocationTargetException, IllegalArgumentException, NoSuchFieldException, SecurityException {
                return getMethod( MethodId.getOrThrow ).invoke( getMethod( MethodId.encoderEncodeStart ).invoke( getItemStackCodec(), getMethod( MethodId.createSerializationContext ).invoke( registryAccess, getNbtOps() ), item ) );
            }
        }
    }
}
