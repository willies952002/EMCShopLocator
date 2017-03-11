package de.guntram.mcmod.emcshoplocator.JourneyMap;

import de.guntram.mcmod.emcshoplocator.EMCShopLocator;
import de.guntram.mcmod.emcshoplocator.gui.ShopSearchGui;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.ModWaypoint;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.model.MapImage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@journeymap.client.api.ClientPlugin
public class ShopWaypointMaker implements IClientPlugin {
    
    private IClientAPI jmapi;
    private MapImage goldIcon;

    @Override
    public void initialize(IClientAPI icapi) {
        jmapi=icapi;
        goldIcon=new MapImage(new ResourceLocation("minecraft:textures/items/gold_ingot.png"), 32, 32);
        // System.out.println("initializing bridge, goldIcon is "+(goldIcon == null ? "null" : goldIcon));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getModId() {
        return EMCShopLocator.MODID;
    }

    @Override
    public void onEvent(ClientEvent ce) {

    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        BlockPos pos;
        if ((pos=ShopSearchGui.getJourneyMapNewWaypointPos())!=null) {
            // System.out.println("catching wp");
            ModWaypoint waypoint=new ModWaypoint(EMCShopLocator.MODID, "shop", "Shop Locations",
                    ShopSearchGui.getJourneyMapShopName(), 
                    pos, goldIcon, 0xffffff, false, DimensionType.OVERWORLD.getId());
            try {
                jmapi.show(waypoint);
            } catch (Exception ex) {

            }
            ShopSearchGui.journeyMapNewWaypointPosHandled();
        }
    }
}
