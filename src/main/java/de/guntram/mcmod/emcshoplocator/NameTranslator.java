/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.emcshoplocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author gbl
 */
class NameTranslator {
    
    static Map<Pattern, String> translations;
    
    public static void setConfigFile(File configFile) {
        translations=new HashMap<>();
        File translationFile= new File(configFile.getAbsolutePath()+".translation.txt");
        if (!translationFile.exists()) {
            createExampleTranslationFile(translationFile);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(translationFile))) {
            String line;
            int lineno=0;
            while ((line=reader.readLine())!=null) {
                lineno++;
                int pos;
                String key, value;
                if (line.startsWith("#"))
                    continue;
                if ((pos=line.indexOf('='))>0) {
                    key=line.substring(0, pos);
                    value=line.substring(pos+1);
                    try {
                        translations.put(Pattern.compile(key), value);
                    } catch (PatternSyntaxException ex) {
                        System.err.println("Line "+lineno+": pattern syntax exception "+ex.getDescription()+" compiling "+key);
                    }
                } else {
                    System.err.println("Line "+lineno+": no = found, ignoring");
                }
            }
        } catch (IOException ex) {
            System.err.println("translation file "+translationFile.getAbsolutePath()+" error "+ex.getMessage()+", not translating item names");
        }
    }
    
    static void createExampleTranslationFile(File translationFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(translationFile))) {
            writer.println("# This is an example item name translation file.");
            writer.println("# Add replacements line per line, separating original and replacement with a =");
            writer.println("# For example, the following replaces Pine log with Spruce log :");
            writer.println("Pine Log=Spruce Log");
            writer.println("# meaning - when the sign says Pine Log, you can search for Spruce Log to find it");
            writer.println("# Lines that start with a # are considered comments and ignored");
            writer.println("# You can use java regular expressions, as in ");
            writer.println("#(.*) horse armor=\\1 barding");
            writer.println("# to replace horse armor with barding and keep the gold, iron, or diamond in front of it.");
            writer.println("# or replace abbreviations of shulker with shulker, ignoring case:");
            writer.println("#(?i)sh[a-z]*r=shulker");
            writer.println("# (You must remove the comments for those to work)");
        } catch (IOException ex) {
            System.err.println("Error "+ex.getMessage()+" when trying to write example translation file "+translationFile.getAbsolutePath());
        }
    }
    
    static Set messaged;
    
    static String applyTranslations(String itemName) {
        if (messaged==null)
            messaged=new HashSet();
        String replace=itemName;
        for (Entry<Pattern, String> e:translations.entrySet()) {
            replace=e.getKey().matcher(itemName).replaceAll(e.getValue());
        }
        if (!(itemName.equals(replace))) {
            if (messaged.contains(itemName)) {
                messaged.add(itemName);
                System.out.println("replacing "+itemName+" with "+replace);
            }
        }
        return replace;
    }
}
