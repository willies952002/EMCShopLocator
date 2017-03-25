package de.guntram.mcmod.emcshoplocator.VoxelMap;

import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.util.Waypoint;
import de.guntram.mcmod.emcshoplocator.gui.ShopSearchGui;
import java.util.ArrayList;
import java.util.TreeSet;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ShopWaypointMaker {
    
    private boolean minimapAvailable;

    public void initialize() {
        Class c;
        
        try {
            c=Class.forName("com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap");
            minimapAvailable=true;
            MinecraftForge.EVENT_BUS.register(this);
            System.out.println("Voxelmap found and registered");
        } catch (ClassNotFoundException ex) {
            minimapAvailable=false;
            System.out.println("Voxelmap not found");
        }
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        final float myRed=65535f/65536;
        final float myGreen=65534f/65536;
        final float myBlue=65533f/65536;
        BlockPos pos;
        if ((pos=ShopSearchGui.getJourneyMapNewWaypointPos())!=null) {
            AbstractVoxelMap voxelmap;
            voxelmap=AbstractVoxelMap.getInstance();
            // System.out.println("trying to add WP, instance="+xaero);
            TreeSet dimensions=new TreeSet();
            dimensions.add(0);
            IWaypointManager manager = voxelmap.getWaypointManager();
            final Waypoint created = new Waypoint(ShopSearchGui.getJourneyMapShopName(),
                    pos.getX(), pos.getZ(), pos.getY(),
                    true, myRed, myGreen, myBlue, "diamond",
                    "", // seems like empty string gets translated to current world
                    dimensions);
            ArrayList<Waypoint> mapWaypointList = voxelmap.getWaypointManager().getWaypoints();
            for (int i=0; i<mapWaypointList.size(); i++) {
                Waypoint existing=mapWaypointList.get(i);
                if (existing.red==myRed && existing.green==myGreen && existing.blue==myBlue) {
                    // mapWaypointList.remove(i);
                    manager.deleteWaypoint(existing);
                    i--;
                }
            }
            manager.addWaypoint(created);
            ShopSearchGui.journeyMapNewWaypointPosHandled();
        }
    }
}
