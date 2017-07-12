# EMCShopLocator
Find shops on EMC that sell a specific item

** This mod works on Empire Minecraft - play.emc.gs - only. If you're playing somewhere else, you won't be interested in it. **
Description

 

EMC has a great shop system, but finding specific things can still be hard. You'll get that stack of oak planks for a decent price anywhere, but when you're out shopping for a rare item, finding a shop that has it can be hard. Finding the shop that has the best price can be even harder. And when your favourite shop is sold out, where do you find replacement? What if you need 2 DC of granite, your favourite shop sells that 4 blocks at a time, and you don't want to spend two hours clicking the same shop sign over and over?

 

This is where the shop locator comes in to help.

 

While you're walking through town, this mod will find shop signs around you, read items and prices from them, and save them into its own database. Later, you can open an ingame GUI, enter the name of an item to get a list of similar items, then click one to get a list of shops selling this item, sorted by price. Clicking one entry in the list gives you a glance at the buy/sell price and amount, residence, and coordinates on that residence so you can find the shop quickly.

 

 

Now, you just need to walk to that location and buy your stuff - or sell it if there is a sell price as well, and you can decide if you like the buy/sell amount even before you walk there. To speed up getting there, you can click the server or residence links to switch to that server and visit that residence. And if you have JourneyMap installed as well, clicking the XYZ coordinates will set a waypoint there to make finding your shop sign even easier.

 
##Usage

 

Install Forge and this mod. The EMC Website has a series of great tutorials about this.

 

After starting minecraft, walk around and visit your favourite shops to build your shop sign database. Visit some other shops as well (/v +shop and/or /v +mall) to have the mod learn about new ones. Wait a few seconds between visiting them to give the mod time to scan the area for shop signs.

 

When you're done, press the # key (keybinding can be changed in the controls options). The gui - see above - will pop up. Enter some text in the textbox left top, press return, and the item list to the left will be filled. Click on one of the items, and you'll see a list of shops to the right. Click one of those shops to see details.

 
##Enchanted Items and [CHOOSE] signs

 

The mod doesn't know anything the players can't figure out themselves, so there's no way to know what a chest contains unless you open that chest. And when a sign says Potion-9skdfh, the mod doesn't know any more about the potion than you do. So, that potion will shop up as Potion-9skdfh in your item list, and if the shop sells different kinds of potions in a [CHOOSE] chest, the mod doesn't know anything but, well, there's a [CHOOSE] chest.

 

However, if you open that [CHOOSE] chest, the server will send the contents to you, and the mod will remember those contents. So, if you see a shop that sells that "I don't need that right now, but it will be useful later" item in a [CHOOSE] chest, you should click that sign once to allow the mod to update its database.

 

When a [CHOOSE] chest contains enchanted items, or books, the mod will remember those enchantments as well, and display them in a shorter form. For example, a diamond shovel having Efficiency V, Unbreaking III, and Mending, will be displayed as "Diamond Shovel (E5,U3,M)" in the items list.

 
To make sure the mod doesn't slow down your minecraft, 
it has been optimized using
 [![JProfiler Logo](https://www.ej-technologies.com/images/product_banners/jprofiler_small.png "Logo")](https://www.ej-technologies.com/products/jprofiler/overview.html).
