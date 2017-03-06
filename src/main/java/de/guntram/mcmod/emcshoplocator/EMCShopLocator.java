package de.guntram.mcmod.emcshoplocator;

import de.guntram.mcmod.emcshoplocator.gui.ShopSearchGuiHandler;
import de.guntram.mcmod.emcshoplocator.gui.ShopSearchKeyEvent;
import de.guntram.mcmod.emcshoplocator.gui.ShopSearchKeyHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = EMCShopLocator.MODID, version = EMCShopLocator.VERSION)
public class EMCShopLocator
{
    @Instance
    public static EMCShopLocator instance;
    
    public static final String MODID = "emcshoplocator";
    public static final String VERSION = "0.1";
    private Pattern line2Pattern, line3Patternb, line3Patterns, line3Patternbs;
    private Pattern serverNameInfoPattern;
    private long lastSignUploadTime;
    private long lastSignSaveTime;
    private String serverName;
    public File configFile;
    
    ArrayList<Chunk> delayedChunks;
    HashMap<String, ShopSign> signs;
    
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
        ShopSearchKeyHandler.init();
        MinecraftForge.EVENT_BUS.register(new ShopSearchKeyEvent());
        
        line2Pattern=Pattern.compile("^\\d+$");
        line3Patternb=Pattern.compile("^B (\\d+K?)$");
        line3Patterns=Pattern.compile("^(\\d+K?) S$");
        // The spaces may be omitted. For example: "B14800:14200S" with M4sterMiners beacons.
        line3Patternbs=Pattern.compile("^B ?(\\d+K?) ?: ?(\\d+K?) ?S$");
        serverNameInfoPattern=Pattern.compile("Empire Minecraft - ([^,]+),");
        lastSignUploadTime=lastSignSaveTime=System.currentTimeMillis();
        serverName="unknown";
    }

    @EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        signs=SignFile.load(configFile=event.getSuggestedConfigurationFile());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onConnectedToServerEvent(ClientConnectedToServerEvent event) {
        if (event.isLocal())
            return;
        // Don't do this if we already got a chat event that indicates the server.
        if (serverName.equals("unknown")) {
            if ((serverName=Minecraft.getMinecraft().getCurrentServerData().serverName)==null)
                serverName="unknown";
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=false)
    public void onChunkLoad(ChunkEvent.Load event) {
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

        // Don't count ticks, use real time. in case of client lag, or we don't
        // get some ticks when server/world porting.
        // But we don't want the client to delay while waiting for the disk so
        // do this in another thread.

        long now=System.currentTimeMillis();
        if (lastSignUploadTime + 5*60*1000 < now) {
            // Set next time to a long time in the future; when the thread
            // finishes it should reset that time. But if the thread
            // crashes (shouldn't happen, finally {} ...) at least we will
            // recover after a while.
            lastSignUploadTime = now+60*60*1000;
            System.out.println("uploading sign data");
            new SignUploaderThread(signs, this).start();
        }

        if (lastSignSaveTime + 5*60*1000 < now) {
            lastSignSaveTime = now+60*60*1000;
            System.out.println("saving sign data");
            new SignSaverThread(signs, this).start();
        }
        
        // second part of ugly hack. At least we remove chunks from the list
        // once we've processed them so this shouldn't lag the client too much.
        if (delayedChunks==null)
            return;
        ArrayList<Chunk> toRemove=new ArrayList<Chunk>();
        for (Chunk chunk:delayedChunks) {
            ExtendedBlockStorage[] store=chunk.getBlockStorageArray();
            if (store.length!=0 || store[0]!=null) {
                try {
                    findShopSigns(chunk, store);
                    toRemove.add(chunk);
                } catch (ConcurrentModificationException ex) {
                    // Sometimes MC is still loading entity data in the
                    // while the chunk store is alreads filled. In this
                    // case, iterating over entities might result in this
                    // exception. Just don't remove the chunk and try
                    // again next tick. Bah!
                }
            }
        }
        for (Chunk chunk:toRemove) {
            delayedChunks.remove(chunk);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=false)
    public void onClientChatEvent(ClientChatReceivedEvent event) {
        ITextComponent message = event.getMessage();
        Matcher matcher=serverNameInfoPattern.matcher(message.getUnformattedText());
        // System.out.println("try to find server name in "+message.getUnformattedText());
        if (matcher.find()) {
            serverName=matcher.group(1);
            System.out.println("setting server name to "+serverName);
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
        
        Map<BlockPos, TileEntity> tiles=chunk.getTileEntityMap();
        for (BlockPos pos: tiles.keySet()) {
            TileEntity entity = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
            if (entity==null || !(entity instanceof TileEntitySign))
                continue;
            TileEntitySign sign=(TileEntitySign) entity;
            // System.out.println("found sign at "+pos+" second row is '"+sign.signText[1].getUnformattedText()+"' and third is '"+sign.signText[2].getUnformattedText()+"'");
            try {
                ShopSign shopsign=null;
                if (line2Pattern.matcher(sign.signText[1].getUnformattedText()).matches()) {
                    Matcher m;
                    String buySell=sign.signText[2].getUnformattedText();
                    m=line3Patternb.matcher(buySell);
                    if (m.matches())
                        shopsign=new ShopSign(sign, serverName, signval(m.group(1)), -1);
                    else {
                        m=line3Patterns.matcher(buySell);
                        if (m.matches())
                            shopsign=new ShopSign(sign, serverName, -1, signval(m.group(1)));
                        else {
                        m=line3Patternbs.matcher(buySell);
                        if (m.matches())
                            shopsign=new ShopSign(sign, serverName, signval(m.group(1)), signval(m.group(2)));
                        }
                    }
                }
                if (shopsign!=null && !shopsign.equals(signs.get(shopsign.getUniqueString()))) {
                    signs.put(shopsign.getUniqueString(), shopsign);
                }
            } catch (NumberFormatException ex) {
                    System.out.println(Arrays.toString(ex.getStackTrace()));
            } catch (IllegalArgumentException ex) {
                    System.out.println(Arrays.toString(ex.getStackTrace()));
            }
        }
        
        // @TODO: remove signs we have in our database that aren't in the chunk anymore
    }
    
    public Collection<ShopSign> getSigns() {
        return signs.values();
    }
    
    private int signval(String s) {
        if (s.endsWith("K")) {
            return Integer.parseInt(s.substring(0, s.length()-1))*1000;
        } else {
            return Integer.parseInt(s);
        }
    }

    void setSignSaveDone() {
        lastSignSaveTime = System.currentTimeMillis();
    }

    void setSignUploadDone() {
        lastSignUploadTime = System.currentTimeMillis();
    }
}
