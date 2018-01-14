# MwoScoreboardHelper
Keep track of your stats and the stats of players you encounter to get a feel for your teammates and enemies before the match starts (and to go nuts analyzing all the data if you are so inclined)  
**this is still _very_ experimental and is only in use by me (happynev) and a friend**
### What does it do?
Basically it analyzes screenshots from your matches to build a database of players and their stats  
this allows it to provide the following infos:
* At the beginning of a match: 
  * show what you know about your teammates and enemies, their favourite mechs, K/D ratio and a lot more.
  * quickly show the total tonnage of your team and the distribution of weight classes
* At the end of a match: 
  * show how players performed relative to other matches
  * show statistics per team like total damage dealt, median match score etc
  * session stats like total cbills earned, your K/D ratio for today, etc
* Outside of matches:
  * View list of players you met
  * View usage statistics per mech  
 like "what is the most used clan mech?" -->duh, KDK-3 with 4.8% of all encountered clan mechs. btw: it deals 409 damage on average)
### What system do i need?
* Windows only (sorry, no other native OCR libs included)
* CPU: Faster=better, duh. More CPU Cores will directly multiply the speed of the trace
* RAM: around 1GB (on top of MWO) should be enough
* Java: you need to have a somewhat up-to-date jave8 JRE installed. get it at https://java.com/download/ 
### How do i install and run it?
1. download the ZIP from this repository
1. extract wherever you want (usually the same PC you play MWO on, but can be a different machine if you can access your screenshot forlder from there)
1. doubleclick the MwoScoreboardHelper.jar file (associate with java if the java installer didn't do that for you)
   * you can also place a shortcut on your desktop
1. on the "settings" tab enter your exact playername and configure the screenshot directory
1. on the "Mech stats" tab, click the "import mech data from smurfy" button. this is also necessary after each MWO patch (that brings new mechs)
1. on the "Scoreboard Watcher" main tab, enable looking for new screenshots by clicking the button in the lower left corner
1. you're set!
### How do i use it?
* Usually you'll want to have it running alongside MWO on a second screen (as mentioned above you can also run it on another computer that has direct access to your screenshot directory)
* Before the match starts, on the match preparation screen, take a screenshot (F12 in steam, or whatever you prefer)
 * it will show you the stats of players you've encountered before
* At the end of the match, take a screenshot of the Rewards screen.
  * it will record your earnings, Solo Kills, KMDDs, Component Destructions (for future analysis...)
  * customize the fields you want to see on the "settings" tab
*  At the end of the match, take a screenshot of the Summary screen.
  * **This step is important**, because this is where all the data is collected
  * it will show you lots of stats how players performed
  * shows some summarized stats for both teams
  * shows stats across the current session like cbills/xp earned, matches played, etc.
  * customize the fields you want to see on the "settings" tab
### Which gamemodes are supported?
* Currently only QP is supported (Solo and Group)
* Faction Play looks like it works, but it will seriously mess up your stats. don't try it.
### Which Resolutions are supported?
In theory, any 16:9 or 16:10 resolution should work. However on lower resolutions the accuracy of the OCR tracing suffers a lot.  
Examples:
* 1280x700: utterly unusable
* 1920x1080: not tested... hopefully usable, although i'm not convinced
* 1920x1200: not tested... certainly better than 1920x1080
* 2560x1600: works fine (haven't had any complaints)
* 3860x2160: works great (from own experience)
### I already have an extensive screenshot collection, can i import it?
yes you can! just make sure the images are located in the configured screenshot directory. it may take a while to trace&import them all, but when it's done it's done.

## Feature list current and planned
TODO...
## Technical stuff

## Thanks
* Smurfy for providing an API for useful 'Mech data. [(Smurfy's Mechlab)](http://mwo.smurfy-net.de/)
* Scurro, Tarogato and Chimmy for providing leaderboard data [(The Jarl's List)](https://leaderboard.isengrim.org/)

### What Libraries are used?
* GUI: JavaFX2 (included in Java8)
* OCR Tracing: Tesseract [(github)](https://github.com/tesseract-ocr/tesseract) and Tess4J[(sourceforge)](http://tess4j.sourceforge.net/)
* Database: H2DB [(website)](http://www.h2database.com/html/main.html)
* Json: Google GSON [(github)](https://github.com/google/gson)
* Some Apache Commons dependencies
