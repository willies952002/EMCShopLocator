/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.emcshoplocator;

import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;

/**
 *
 * @author gbl
 */
public class ShopSign {
    String server;
    BlockPos pos;               // position
    int amount;
    int buyPrice, sellPrice;    // set to -1 to indicate buying/selling not available
    String shopOwner;           // line 1
    String item;                // line 4
    
    // this code assumes the sign has already been checked for shop-sign-ness.
    // Throws IllegalArgumentException if obviously not a shop sign.

    ShopSign(TileEntitySign sign, String servername, int buy, int sell) {
        server=servername;
        pos=sign.getPos();
        shopOwner=sign.signText[0].getUnformattedText();
        item=sign.signText[3].getUnformattedText();
        try {
            buyPrice=sellPrice=-1;
            amount=Integer.parseInt(sign.signText[1].getUnformattedText());
            buyPrice=buy;
            sellPrice=sell;
        } catch (Exception e) {
            throw new IllegalArgumentException("Not a shop sign", e);
        }
    }
    
    @Override
    public String toString() {
        return server + ":" + 
               pos.getX()+":"+pos.getY()+":"+pos.getZ()+":"+
               amount + ":"+
               buyPrice + ":" +
               sellPrice + ":" +
               shopOwner + ":" +
               item;
    }
    
    public String getUniqueString() {
        return server+":"+
                pos.getX()+":"+
                pos.getY()+":"+
                pos.getZ();
    }
}
