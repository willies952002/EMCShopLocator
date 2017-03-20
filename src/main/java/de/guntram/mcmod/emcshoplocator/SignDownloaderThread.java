/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.emcshoplocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

/**
 *
 * @author gbl
 */
public class SignDownloaderThread extends Thread {
    private final EMCShopLocator mod;
    public SignDownloaderThread(EMCShopLocator mod) {
        this.mod=mod;
    }
    @Override
    public void run() {
        HashMap<String, ShopSign> result=new HashMap<String, ShopSign>();
        BufferedReader reader;
        try {
            String s;
            URL download=new URL("http://minecraft.guntram.de/emcmarket/download.pl?"+
                    "client="+EMCShopLocator.MODID+"&clientversion="+EMCShopLocator.VERSION);
            reader=new BufferedReader(new InputStreamReader(download.openStream()));
            while ((s=reader.readLine())!=null) {
                try {
                    ShopSign newSign=ShopSign.fromString(s);
                    // do not check for toDelete here; this needs to be done in mod.downloadFinished()
                    result.put(newSign.getUniqueString(), newSign);
                } catch (NotAShopSignStringException ex) {
                    System.err.println(ex.getMessage());
                }
            }
            reader.close();
            System.out.println(Integer.toString(result.size())+" signs downloaded");
            mod.downloadFinished(result);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
