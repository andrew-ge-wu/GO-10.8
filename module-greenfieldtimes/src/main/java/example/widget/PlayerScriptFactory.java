package example.widget;

public class PlayerScriptFactory
{

    String createPlayerScript(String link, String imageLink, String playerId)
    {
        if (link == null ||link.trim().length()<=0){
            return "";
        }
        String playerString = "<script type='text/javascript' src='js/jwplayer.js'></script>";

        playerString += "<div id=\"player_" + playerId   + "\"></div>";
        playerString += "<script type=\"text/javascript\">";
        playerString += "jwplayer(\"player_"+playerId+"\").setup({";
        playerString += "flashplayer: \"/swf/player.swf\",";
        playerString += "file: \""+link+"\",";
        playerString += "image: \""+imageLink+"\",";
        playerString += "height: " + (240 + 24) + ",";
        playerString += "width: 320";
        playerString += "});";
        playerString += "</script>";

        return playerString;
    }

}
