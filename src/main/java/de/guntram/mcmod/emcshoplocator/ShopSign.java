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
    String itemName;                // line 4
    private boolean uploaded;

    // this code assumes the sign has already been checked for shop-sign-ness.
    // Throws IllegalArgumentException if obviously not a shop sign.

    ShopSign(TileEntitySign sign, String servername, int buy, int sell) {
        server=servername;
        pos=sign.getPos();
        shopOwner=sign.signText[0].getUnformattedText();
        itemName=sign.signText[3].getUnformattedText();
        try {
            buyPrice=sellPrice=-1;
            amount=Integer.parseInt(sign.signText[1].getUnformattedText());
            buyPrice=buy;
            sellPrice=sell;
        } catch (Exception e) {
            throw new IllegalArgumentException("Not a shop sign", e);
        }
        uploaded=false;
    }
    
    private ShopSign(String s, int x, int y, int z, int a, int bP, int sP, String sO, String it) {
        this.server=s;
        this.pos=new BlockPos(x, y, z);
        this.amount=a;
        this.buyPrice=bP;
        this.sellPrice=sP;
        this.shopOwner=sO;
        this.itemName=it;
        // This is called when loading from a file, so don't reupload.
        this.uploaded=true;
    }
    
    @Override
    public String toString() {
        return server + ":" + 
               pos.getX()+":"+pos.getY()+":"+pos.getZ()+":"+
               amount + ":"+
               buyPrice + ":" +
               sellPrice + ":" +
               shopOwner + ":" +
               itemName + ":" + ResPosition.getResAt(server, pos.getX(), pos.getZ()).resNumber;
    }
    
    public String getUniqueString() {
        return server+":"+
                pos.getX()+":"+
                pos.getY()+":"+
                pos.getZ();
    }
    
    public boolean isUploaded() { return uploaded; }
    public void markUploaded() { uploaded=true; }
    
    public boolean equals(ShopSign other) {
        if (other==null)
            return false;
        return this.pos.getX()==other.pos.getX()
            && this.pos.getY()==other.pos.getY()
            && this.pos.getZ()==other.pos.getZ()
            // not comparing pos.world here as we should only have town world entries
            && this.amount == other.amount
            && this.buyPrice == other.buyPrice
            && this.sellPrice == other.sellPrice
            && this.server.equals(other.server)
            && this.shopOwner.equals(other.shopOwner)
            && this.itemName.equals(other.itemName);
    }

    static ShopSign fromString(String s) throws NotAShopSignStringException {
        String[] parts = s.split(":");
        // As the res number is saved to the file, but not used when reading, we have 10 columns but only use 0..8
        if (parts.length!=10)
            throw new NotAShopSignStringException("invalid number of columns: "+s);
        try {
            int x=Integer.parseInt(parts[1]);
            int y=Integer.parseInt(parts[2]);
            int z=Integer.parseInt(parts[3]);
            int amount=Integer.parseInt(parts[4]);
            int buy=Integer.parseInt(parts[5]);
            int sell=Integer.parseInt(parts[6]);
            ShopSign result=new ShopSign(parts[0], x, y, z, amount, buy, sell, parts[7], parts[8]);
            result.uploaded=true;
            return result;
        } catch (NumberFormatException ex) {
            throw new NotAShopSignStringException(ex);
        }
    }
    
    public String getItemName() { return itemName; }
    public String getServer() { return server; }
    public int getRes() { return ResPosition.getResAt(server, pos.getX(), pos.getZ()).resNumber; }
    public String getShopOwner() { return shopOwner; }
    public double getBuyPerItem() { return 1.0*buyPrice/amount; }
    public double getSellPerItem() { return 1.0*sellPrice/amount; }
    public int getAmount() { return amount; }
    public int getBuyPrice() { return buyPrice; }
    public int getSellPrice() { return sellPrice; }
    public BlockPos getPos() { return pos; }
}
