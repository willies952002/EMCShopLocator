/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.emcshoplocator;

import net.minecraft.tileentity.TileEntitySign;

/**
 *
 * @author gbl
 */
public class NotAShopSignException extends Exception {
    NotAShopSignException(String s) {
        super(s);
    }
    NotAShopSignException(TileEntitySign sign, Exception ex) {
        super(sign.signText[0].getFormattedText()+"\n"+
                sign.signText[1].getFormattedText()+"\n"+
                sign.signText[2].getFormattedText()+"\n", ex);
    }
    NotAShopSignException(TileEntitySign sign) {
        super(sign.signText[0].getFormattedText()+"\n"+
                sign.signText[1].getFormattedText()+"\n"+
                sign.signText[2].getFormattedText()+"\n");
    }
}
