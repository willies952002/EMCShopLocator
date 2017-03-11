/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.emcshoplocator;

import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author gbl
 */
public class SignSaverThread extends Thread {

    HashMap<String, ShopSign> toSave;
    EMCShopLocator emcShopLocator;
    
    public SignSaverThread(HashMap<String, ShopSign> signs, EMCShopLocator mod) {
        toSave=signs;
        emcShopLocator=mod;
    }
    
    @Override
    public void run() {
        try {
            SignFile.save(toSave);
        } catch (IOException ex) {
            System.err.println("Can not save shop sign info: "+ex);
        } finally {
            emcShopLocator.setSignSaveDone();
        }
    }
}

