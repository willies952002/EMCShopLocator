package de.guntram.mcmod.emcshoplocator;

import de.guntram.mcmod.emcshoplocator.config.ConfigurationHandler;
import de.guntram.mcmod.emcshoplocator.events.ChooseChestEventHandler;
import de.guntram.mcmod.emcshoplocator.gui.ShopSearchGuiHandler;
import de.guntram.mcmod.emcshoplocator.gui.ShopSearchKeyHandler;
import de.guntram.mcmod.emcshoplocator.gui.ShopSearchKeyRegistration;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.codec.binary.Hex;

@Mod(modid = EMCShopLocator.MODID, 
        version = EMCShopLocator.VERSION,
        clientSideOnly = true, 
        acceptedMinecraftVersions = "[1.12]", 
        guiFactory = "de.guntram.mcmod.emcshoplocator.config.SLGuiFactory"
)

public class EMCShopLocator
{
    @Instance
    public static EMCShopLocator instance;
    
    public static final String MODID = "emcshoplocator";
    public static final String VERSION = "1.5.0";
    private Pattern serverNameInfoPattern;
    private long lastSignUploadTime;
    private long lastSignSaveTime;
    public String serverName;
    public File configFile;
    
    ArrayList<Chunk> delayedChunks;
    HashMap<String, ShopSign> signs;
    HashMap<String, ShopSign> downloadedSigns;
    private boolean connectedToEMC;
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        instance=this;

        delayedChunks=new ArrayList<Chunk>();
        if (signs==null) { // which means preInit didn't find any to load
            signs=new HashMap<String, ShopSign>();
        }
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ShopSearchGuiHandler());
        ShopSearchKeyRegistration.init();
        MinecraftForge.EVENT_BUS.register(new ShopSearchKeyHandler());
        MinecraftForge.EVENT_BUS.register(new ChooseChestEventHandler(this));
        
        // init the xaero waypoint maker; we don't need to do this with JourneyMap due to how the JM API works
        try {
            new de.guntram.mcmod.emcshoplocator.XaeroMiniMap.ShopWaypointMaker().initialize();
        } catch (NoClassDefFoundError error) {
            System.err.println("Xaero Minimap not found, won't try to use it");
        }
        // same with voxelmap
        try {
            new de.guntram.mcmod.emcshoplocator.VoxelMap.ShopWaypointMaker().initialize();
        } catch (NoClassDefFoundError error) {
            System.err.println("Voxelmap not found, won't try to use it");
        }
        
        serverNameInfoPattern=Pattern.compile("Empire Minecraft - ([^,]+),");
        lastSignUploadTime=lastSignSaveTime=System.currentTimeMillis();
        if (ConfigurationHandler.isDownloadAllowed())
            new SignDownloaderThread(this).start();
        serverName="unknown";
    }

    @EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        ConfigurationHandler confHandler = ConfigurationHandler.getInstance();
        confHandler.load(event.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(confHandler);
        
        SignFile.setConfigFile(configFile=event.getSuggestedConfigurationFile());
        signs=SignFile.load();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onConnectedToServerEvent(ClientConnectedToServerEvent event) {
        if (event.isLocal())
            return;
        String ServerIcon=Minecraft.getMinecraft().getCurrentServerData().getBase64EncodedIconData();
        MessageDigest md;
        String digest="";
        try {
            md = MessageDigest.getInstance("SHA1");
            digest=Hex.encodeHexString(md.digest(ServerIcon.getBytes()));
            connectedToEMC=digest.equals("56b89ba7fb0a24e889e0e0af30041d7f94c7a0e7");
        } catch (NoSuchAlgorithmException ex) {
            connectedToEMC=false;
        }
        
        if (connectedToEMC)
            System.out.println("Connected to EMC");
        else
            System.out.println("Connected, this is not EMC though (server id is "+digest+")");
        
        lastSignUploadTime=System.currentTimeMillis();
        lastSignSaveTime=System.currentTimeMillis();
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onDisconnectFromServerEvent(ClientDisconnectionFromServerEvent event) {
        if (connectedToEMC) {
            if (ConfigurationHandler.isUploadAllowed()) {
                new SignUploaderThread(signs, this).start();
            }
            new SignSaverThread(signs, this).start();
        }
        connectedToEMC=false;
        serverName="unknown";
        delayedChunks.clear();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=false)
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!connectedToEMC)
            return;
        Chunk chunk=event.getChunk();
        ExtendedBlockStorage[] store=chunk.getBlockStorageArray();
	// When playing locally, store is filled right here, but
	// on a server, store seems to get filled later, so we need to do
	// this ugly hack that checks chunks every tick.
        if (store.length==0 || store[0]==null) {
            delayedChunks.add(chunk);
            return;
        }
        findShopSigns(chunk, store);
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!connectedToEMC || !ResPosition.isKnownEMCServer(serverName))
            return;

        // Don't count ticks, use real time. in case of client lag, or if we
        // don't get some ticks when server/world porting.
        // But we don't want the client to delay while waiting for the disk so
        // do this in another thread.

        long now=System.currentTimeMillis();
        if (ConfigurationHandler.isUploadAllowed() 
        && lastSignUploadTime + ConfigurationHandler.getUploadInterval()*60*1000 < now) {
            // Set next time to a long time in the future; when the thread
            // finishes it should reset that time. But if the thread
            // crashes (shouldn't happen, finally {} ...) at least we will
            // recover after a while.
            lastSignUploadTime = now+60*60*1000;
            // System.out.println("uploading sign data");
            new SignUploaderThread(signs, this).start();
        }

        if (lastSignSaveTime + ConfigurationHandler.getSaveInterval()*60*1000 < now) {
            lastSignSaveTime = now+60*60*1000;
            System.out.println("saving sign data");
            new SignSaverThread(signs, this).start();
        }
        
        // second part of ugly hack. At least we remove chunks from the list
        // once we've processed them so this shouldn't lag the client too much.
        if (delayedChunks==null)
            return;
        ArrayList<Chunk> processedChunks=new ArrayList<Chunk>();
        for (Chunk chunk:delayedChunks) {
            ExtendedBlockStorage[] store=chunk.getBlockStorageArray();
            if (store.length!=0 || store[0]!=null) {
                try {
                    findShopSigns(chunk, store);
                    processedChunks.add(chunk);
                } catch (ConcurrentModificationException ex) {
                    // Sometimes MC is still loading entity data in the
                    // while the chunk store is alreads filled. In this
                    // case, iterating over entities might result in this
                    // exception. Just don't remove the chunk and try
                    // again next tick. Bah!
                }
            }
        }
        for (Chunk chunk:processedChunks) {
            delayedChunks.remove(chunk);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=false)
    public void onClientChatEvent(ClientChatReceivedEvent event) {
        if (!connectedToEMC)
            return;
        ITextComponent message = event.getMessage();
        Matcher matcher=serverNameInfoPattern.matcher(message.getUnformattedText());
        // System.out.println("try to find server name in "+message.getUnformattedText());
        if (matcher.find()) {
            serverName=matcher.group(1);
            // System.out.println("setting server name to "+serverName);
        }
    }
   
    private void findShopSigns(Chunk chunk, ExtendedBlockStorage[] store) {

        // Some ugliness to filter out non-town worlds. We won't get the world
        // name from the server, so assume that there are at most 10 (typically 0)
        // bedrock blocks between levels 1 and 5 in town, and more elsewhere.

        for (int i=0; i<store.length; i++) {
            if (store[i]==null
            ||  store[i].getYLocation()>0) {
                continue;
            }
            int bedrock=0;
            for (int x=0; x<16; x++) {
                for (int y=1; y<5; y++) {
                    for (int z=0; z<16; z++) {
                        IBlockState state=store[i].get(x, y, z);
                        if (state.getBlock()==Blocks.BEDROCK) {
                            bedrock++;
                            if (bedrock>10)
                                return;
                        }
                    }
                }
            }
        }

        long now=System.currentTimeMillis();
        for (ShopSign shopsign:signs.values()) {
            if (shopsign.pos.getX() >= (chunk.x<<4)
            &&  shopsign.pos.getX() <= (chunk.x<<4)+15
            &&  shopsign.pos.getZ() >= (chunk.z<<4)
            &&  shopsign.pos.getZ() <= (chunk.z<<4)+15
            &&  shopsign.server.equals(serverName)) {
                if (shopsign.choosePosition==-1)
                    shopsign.markForDeletion(); // it will be replaced anyway
                else {
                    TileEntity entity = chunk.getTileEntity(shopsign.pos, Chunk.EnumCreateEntityType.IMMEDIATE);
                    if (entity==null
                    ||  !(entity instanceof TileEntitySign)
                    ||  !((TileEntitySign) entity).signText[3].getUnformattedText().startsWith("[CHOOSE")) {
                        shopsign.markForDeletion();
                    }
                }
            }
        }
        // System.out.println("marking for deletion took "+(System.currentTimeMillis()-now)+ "ms");
        
        Map<BlockPos, TileEntity> tiles=chunk.getTileEntityMap();
        for (BlockPos pos: tiles.keySet()) {
            TileEntity entity = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
            if (entity==null || !(entity instanceof TileEntitySign))
                continue;
            TileEntitySign sign=(TileEntitySign) entity;
            // System.out.println("found sign at "+pos+" second row is '"+sign.signText[1].getUnformattedText()+"' and third is '"+sign.signText[2].getUnformattedText()+"'");
            try {
                ShopSign shopsign = new ShopSign(sign, serverName);
                addSign(shopsign);
            } catch (NotAShopSignException ex) {
                // System.out.println("Not a shop sign "+ex.getMessage());
            }
        }
    }
    
    public void addSign(ShopSign shopsign) {
        if (shopsign!=null)
            signs.put(shopsign.getUniqueString(), shopsign);
    }
    
    public Collection<ShopSign> getSigns() {
        return signs.values();
    }
    
    public int getSignCount() {
        return signs.size();
    }
    
    void setSignSaveDone() {
        lastSignSaveTime = System.currentTimeMillis();
    }

    void setSignUploadDone() {
        lastSignUploadTime = System.currentTimeMillis();
    }

    void downloadFinished(HashMap<String, ShopSign> result) {
        for (ShopSign sign:result.values()) {
            String ustr=sign.getUniqueString();
            ShopSign savedSign = signs.get(ustr);
            if (savedSign==null || savedSign.lastSeenTime <= sign.lastSeenTime) {
                if (sign.markedForDeletion())
                    signs.remove(ustr);
                else
                    addSign(sign);
            } else if (!savedSign.markedForDeletion()) {
                savedSign.markNeedsUpload();
            }
        }
    }
    
    public void uploadAll() {
        for (ShopSign sign:signs.values()) {
            sign.markNeedsUpload();
        }
        new SignUploaderThread(signs, this).start();
    }
    
    public static boolean isDeveloperDebugVersion() {
        return Minecraft.getMinecraft().getSession().getUsername().equals("Giselbaer");
    }
}
