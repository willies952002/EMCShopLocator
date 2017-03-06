/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.emcshoplocator;

/**
 *
 * @author gbl
 */
public class NotAShopSignStringException extends Exception {
    NotAShopSignStringException(NumberFormatException ex) {
        super(ex);
    }
    NotAShopSignStringException(String s) {
        super(s);
    }
}
