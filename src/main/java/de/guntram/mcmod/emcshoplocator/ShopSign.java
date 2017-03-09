/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.emcshoplocator;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    String itemName;            // line 4
    int choosePosition;         // only on CHOOSE signs. -1 otherwise as 0 is a perfectly acceptable position.
    long lastSeenTime;
    private boolean uploaded;
    
    private static Pattern line2Pattern, line3Patternb, line3Patterns, line3Patternbs;
    static {
        line2Pattern=Pattern.compile("^\\d+$");
        line3Patternb=Pattern.compile("^B (\\d+K?)$");
        line3Patterns=Pattern.compile("^(\\d+K?) S$");
        // The spaces may be omitted. For example: "B14800:14200S" with M4sterMiners beacons.
        line3Patternbs=Pattern.compile("^B ?(\\d+K?) ?: ?(\\d+K?) ?S$");
    };

    public ShopSign(TileEntitySign sign, String serverName, int choosePosition, String itemName) {
        this(sign, serverName);
        this.choosePosition=choosePosition;
        this.itemName=itemName;
    }

    // this code assumes the sign has already been checked for shop-sign-ness.
    // Throws IllegalArgumentException if obviously not a shop sign.

    public ShopSign(TileEntitySign sign, String serverName) {
        try {
            if (line2Pattern.matcher(sign.signText[1].getUnformattedText()).matches()) {
                Matcher m;
                String buySell=sign.signText[2].getUnformattedText();
                m=line3Patternb.matcher(buySell);
                if (m.matches())
                    init(sign, serverName, signval(m.group(1)), -1);
                else {
                    m=line3Patterns.matcher(buySell);
                    if (m.matches())
                        init(sign, serverName, -1, signval(m.group(1)));
                    else {
                    m=line3Patternbs.matcher(buySell);
                    if (m.matches())
                        init(sign, serverName, signval(m.group(1)), signval(m.group(2)));
                    }
                }
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private void init(TileEntitySign sign, String servername, int buy, int sell) {
        server=servername;
        pos=sign.getPos();
        shopOwner=sign.signText[0].getUnformattedText();
        itemName=sign.signText[3].getUnformattedText();
        choosePosition=-1;       // will may reset in choose-constructor
        try {
            buyPrice=sellPrice=-1;
            amount=Integer.parseInt(sign.signText[1].getUnformattedText());
            buyPrice=buy;
            sellPrice=sell;
        } catch (Exception e) {
            throw new IllegalArgumentException("Not a shop sign", e);
        }
        uploaded=false;
        lastSeenTime=System.currentTimeMillis();
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
    
    // this should correspond to toString see below
    static ShopSign fromString(String s) throws NotAShopSignStringException {
        String[] parts = s.split(":");
        if (parts.length<10)
            throw new NotAShopSignStringException("invalid number of columns: "+s);
        try {
            int x=Integer.parseInt(parts[1]);
            int y=Integer.parseInt(parts[2]);
            int z=Integer.parseInt(parts[3]);
            int amount=Integer.parseInt(parts[4]);
            int buy=Integer.parseInt(parts[5]);
            int sell=Integer.parseInt(parts[6]);
            ShopSign result=new ShopSign(parts[0], x, y, z, amount, buy, sell, parts[7], parts[8]);
            // parts[10] is the res number and ignored here
            if (parts.length>=11)
                result.choosePosition=Integer.parseInt(parts[10]);
            else
                result.choosePosition=-1;           // old file format without choose positions
            if (parts.length>=12)       result.lastSeenTime=Long.parseLong(parts[11]);
                
            result.uploaded=true;
            return result;
        } catch (NumberFormatException ex) {
            throw new NotAShopSignStringException(ex);
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
               itemName + ":" +
               ResPosition.getResAt(server, pos.getX(), pos.getZ()).resNumber + ":"+
               choosePosition + ":" +
               lastSeenTime;
    }
    
    public String getUniqueString() {
        String result= server+":"+
                pos.getX()+":"+
                pos.getY()+":"+
                pos.getZ();
        if (choosePosition!=0)
            result+=":"+choosePosition;
        return result;
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

    private int signval(String s) {
        if (s.endsWith("K")) {
            return Integer.parseInt(s.substring(0, s.length()-1))*1000;
        } else {
            return Integer.parseInt(s);
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
    public int getChoosePosition() { return choosePosition; }
}
