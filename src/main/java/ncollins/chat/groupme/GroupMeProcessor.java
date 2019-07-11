package ncollins.chat.groupme;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import ncollins.chat.ChatBotProcessor;
import ncollins.model.Order;
import ncollins.model.chat.ImagePayload;
import ncollins.model.chat.MentionPayload;
import ncollins.model.chat.Pin;
import ncollins.model.espn.Outcome;
import ncollins.espn.EspnMessageBuilder;
import ncollins.gif.GifGenerator;
import ncollins.magiceightball.MagicAnswerGenerator;
import ncollins.salt.SaltGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GroupMeProcessor implements ChatBotProcessor {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private GroupMeBot mainBot;
    private GroupMeBot espnBot;
    private GifGenerator gifGenerator = new GifGenerator();
    private SaltGenerator saltGenerator = new SaltGenerator();
    private MagicAnswerGenerator answerGenerator = new MagicAnswerGenerator();
    private EspnMessageBuilder espnMessageBuilder = new EspnMessageBuilder();
    private String accessToken;
    private HttpClient client;

    public GroupMeProcessor(GroupMeBot mainBot, GroupMeBot espnBot, String accessToken){
        this.mainBot = mainBot;
        this.espnBot = espnBot;
        this.accessToken = accessToken;
        this.client = HttpClient.newHttpClient();
    }

    public GroupMeBot getMainBot(){
        return mainBot;
    }

    private GroupMeBot getEspnBot(){
        return espnBot;
    }

    @Override
    public void processResponse(String fromUser, String text, String[] imageUrls, long currentTime) {
        logger.info("Incoming message: " + text);

        text = text.toLowerCase();

        if(text.contains("@here"))
            getMainBot().sendMessage("@here \uD83D\uDC40\uD83D\uDC46", buildMentionAllPayload(new int[]{0,5}));
        if(text.contains("#pin"))
            getMainBot().addPin(new Pin(text.replaceAll("\\p{C}", " / "), fromUser, currentTime));

        if(text.startsWith(getMainBot().getBotKeyword()))
            processBotResponse(text.replace(getMainBot().getBotKeyword(), "").trim());
        else processEasterEggResponse(text);
    }

    private void processBotResponse(String text){
        if(text.matches("^$"))
            getMainBot().sendMessage(buildHelpMessage());
        else if(text.matches("^help$"))
            getMainBot().sendMessage(buildShowCommandsMessage());
        else if(text.startsWith("gif "))
            getMainBot().sendMessage(buildGifMessage(text.replace("gif","").trim()));
        else if(text.startsWith("salt "))
            getMainBot().sendMessage(buildSaltMessage(text.replace("salt","").trim()));
        else if(text.equals("show pins"))
            getMainBot().sendMessage(buildPinsMessage());
        else if(text.matches("^delete pin \\d*$")){
            int index =   Integer.parseInt(text.replaceAll("\\D+",""));
            if(0 > index || index >= getMainBot().getPins().size())
                getMainBot().sendMessage("pick a valid number jagoff");
            else getMainBot().deletePin(index);
        } else if(text.startsWith("show "))
            processEspnResponse(text.replace("show","").trim());
        else if(text.endsWith("?"))
            getMainBot().sendMessage(buildMagicAnswerMessage());
    }

    private void processEspnResponse(String text){
        // {top|bottom} [TOTAL] scores
        if(text.matches("(top|bottom) \\d* ?scores$")){
            Order order = text.startsWith("top") ? Order.DESC : Order.ASC;
            String totalStr = text.replaceAll("\\D+","");
            int total = totalStr.isEmpty() ? 10 : Integer.parseInt(totalStr);
            getEspnBot().sendMessage(espnMessageBuilder.buildScoresMessage(order,total));
        // {top|bottom} [TOTAL] [POSITION|players]
        } else if(text.matches("(top|bottom) \\d* ?players$")) {
            Order order = text.startsWith("top") ? Order.DESC : Order.ASC;
            String totalStr = text.replaceAll("\\D+","");
            int total = totalStr.isEmpty() ? 10 : Integer.parseInt(totalStr);
            getEspnBot().sendMessage(espnMessageBuilder.buildPlayersMessage(order, total, null));
        // [TOTAL] {win|loss} streaks
        } else if(text.matches("\\d* ?(win|loss) streaks$")) {
            Outcome outcome = text.contains(" win ") ? Outcome.WIN : Outcome.TIE;
            String totalStr = text.replaceAll("\\D+", "");
            int total = totalStr.isEmpty() ? 10 : Integer.parseInt(totalStr);
            getEspnBot().sendMessage(espnMessageBuilder.buildOutcomeStreakMessage(outcome, total));
        // [TOTAL] blowouts
        } else if(text.matches("(^|\\s\\d+)blowouts$")){
            String totalStr = text.replaceAll("\\D+","");
            int total = totalStr.isEmpty() ? 10 : Integer.parseInt(totalStr);
            getEspnBot().sendMessage(espnMessageBuilder.buildBlowoutsMessage(total));
        // [TOTAL] heartbreaks
        } else if(text.matches("(^|\\s\\d+)heartbreaks$")){
            String totalStr = text.replaceAll("\\D+","");
            int total = totalStr.isEmpty() ? 10 : Integer.parseInt(totalStr);
            getEspnBot().sendMessage(espnMessageBuilder.buildHeartbreaksMessage(total));
        // matchups
        } else if(text.equals("matchups"))
            getEspnBot().sendMessage(espnMessageBuilder.buildMatchupsMessage());
        // standings
        else if(text.equals("standings"))
            getEspnBot().sendMessage(espnMessageBuilder.buildStandingsMessage());
        // jujus
        else if(text.equals("jujus"))
            getEspnBot().sendMessage(espnMessageBuilder.buildJujusMessage());
        // salties
        else if(text.equals("salties"))
            getEspnBot().sendMessage(espnMessageBuilder.buildSaltiesMessage());
    }

    private void processEasterEggResponse(String text){
        if(text.contains("wonder"))
            getMainBot().sendMessage("https://houseofgeekery.files.wordpress.com/2013/05/tony-wonder-arrested-development-large-msg-132259950538.jpg");
        else if(text.contains("same"))
            getMainBot().sendMessage("https://media1.tenor.com/images/7c981c036a7ac041e66b0c87b42542f2/tenor.gif");
        else if(text.contains("gattaca"))
            getMainBot().sendMessage(gifGenerator.translateGif("rafi gattaca"));
        else if(text.matches(".+ de[a]?d$")){
            getMainBot().sendMessage("", new ImagePayload("https://i.groupme.com/498x278.gif.f652fb0c235746b3984a5a4a1a7fbedb.preview"));
        }
    }

    private String buildHelpMessage(){
        return "you rang? type '" + getMainBot().getBotKeyword() + " help' to see what i can do.";
    }

    private String buildShowCommandsMessage(){
        return "commands:\\n" +
                "@here -- sends a mention notification to group\\n" +
                "#pin -- pin a message to view later\\n" +
                getMainBot().getBotKeyword() + " help -- show bot commands\\n" +
                getMainBot().getBotKeyword() + " gif [SOMETHING] -- post a random gif of something\\n" +
                getMainBot().getBotKeyword() + " salt [SOMEONE] -- throw salt at someone\\n" +
                getMainBot().getBotKeyword() + " [QUESTION]? -- ask a yes/no question\\n" +
                getMainBot().getBotKeyword() + " show pins -- show all pinned messages\\n" +
                getMainBot().getBotKeyword() + " delete pin [INDEX] -- delete a pinned message\\n" +
                getMainBot().getBotKeyword() + " show {top|bottom} [TOTAL] scores -- top/bottom scores this year\\n" +
                getMainBot().getBotKeyword() + " show matchups -- matchups for the current week\\n" +
                getMainBot().getBotKeyword() + " show standings -- standings this year\\n" +
                getMainBot().getBotKeyword() + " show {top|bottom} [TOTAL] [POSITION|players] -- best/worst players this year\\n" +
                getMainBot().getBotKeyword() + " show [TOTAL] {win|loss} streaks -- longest win/loss streaks this year\\n" +
                getMainBot().getBotKeyword() + " show jujus -- this years jujus\\n" +
                getMainBot().getBotKeyword() + " show salties -- this years salties\\n" +
                getMainBot().getBotKeyword() + " show [TOTAL] blowouts -- biggest blowouts this year\\n" +
                getMainBot().getBotKeyword() + " show [TOTAL] heartbreaks -- closest games this year";
    }

    private String buildGifMessage(String query){
        return gifGenerator.translateGif(query);
    }

    private String buildSaltMessage(String recipient) {
        return saltGenerator.throwSalt(recipient);
    }

    private String buildMagicAnswerMessage(){
        return answerGenerator.getRandom();
    }

    private String buildPinsMessage(){
        List<Pin> pins = getMainBot().getPins();

        StringBuilder sb = new StringBuilder();
        for(int i=0; i < pins.size(); i++){
            sb.append(pins.get(i).toString() + " (id:" + i + ")\\n\\n");
        }

        return sb.toString();
    }

    private MentionPayload buildMentionAllPayload(int[] loci){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groupme.com/v3/groups/" + mainBot.getBotGroupId() + "?token=" + accessToken))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            JsonArray jsonArray = new JsonParser().parse(response).getAsJsonObject().getAsJsonObject("response").getAsJsonArray("members");

            int[] userIds = new int[jsonArray.size()];
            for(int i=0; i < jsonArray.size(); i++){
                userIds[i] = jsonArray.get(i).getAsJsonObject().get("user_id").getAsInt();
            }

            int[][] loci2D = new int[userIds.length][2];
            for(int i=0; i < userIds.length; i++){
                loci2D[i] = loci;
            }

            return new MentionPayload(userIds, loci2D);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}