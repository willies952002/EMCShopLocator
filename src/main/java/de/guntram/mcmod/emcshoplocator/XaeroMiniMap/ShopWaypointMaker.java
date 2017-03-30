package de.guntram.mcmod.emcshoplocator.XaeroMiniMap;

import de.guntram.mcmod.emcshoplocator.EMCShopLocator;
import de.guntram.mcmod.emcshoplocator.gui.ShopSearchGui;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xaero.common.IXaeroMinimap;
import xaero.common.minimap.Minimap;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.minimap.XaeroMinimap;

public class ShopWaypointMaker {
    
    private boolean minimapAvailable;

    public void initialize() {
        Class c;
        
        try {
            c=Class.forName("xaero.minimap.XaeroMinimap");
            minimapAvailable=true;
            MinecraftForge.EVENT_BUS.register(this);
            System.out.println("xaero minimap found and registered");
        } catch (ClassNotFoundException ex) {
            minimapAvailable=false;
            System.out.println("xaero minimap not found");
        }
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        BlockPos pos;
        if ((pos=ShopSearchGui.getJourneyMapNewWaypointPos())!=null) {
            IXaeroMinimap xaero;
            xaero=XaeroMinimap.instance;
            // System.out.println("trying to add WP, instance="+xaero);
            Minimap minimap = xaero.getInterfaces().getMinimap();
            final Waypoint created = new Waypoint(pos.getX(), pos.getY(), pos.getZ(),
                    ShopSearchGui.getJourneyMapShopName(), "Shop", 0);
            for (int i=0; i<minimap.waypoints.list.size(); i++) {
                Waypoint existing=minimap.waypoints.list.get(i);
                if (existing.symbol.equals("Shop")) {
                    minimap.waypoints.list.remove(i);
                    i--;
                }
            }
            minimap.waypoints.list.add(created);
            try {
                xaero.getSettings().saveWaypoints(minimap.getCurrentWorld());
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
            ShopSearchGui.journeyMapNewWaypointPosHandled();
        }
    }
}
