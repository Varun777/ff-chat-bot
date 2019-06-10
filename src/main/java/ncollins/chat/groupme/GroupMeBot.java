package ncollins.chat.groupme;

import ncollins.chat.ChatBot;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GroupMeBot implements ChatBot {
    private HttpClient client;
    private final String botId;
    private final String botName;
    private final String botKeyword;
    private static final String GROUP_ME_URL = "https://api.groupme.com/v3/bots/post";

    public GroupMeBot(String botId, String botName){
        this.botId = botId;
        this.botName = botName;
        this.botKeyword = "@" + this.botName;
        this.client = HttpClient.newHttpClient();
    }

    public String getBotKeyword() {
        return botKeyword;
    }

    @Override
    public void processResponse(String fromUser, String text, String[] imageUrls) {
        if(text.matches("^$")) sendMessage(fromUser, buildHelpMessage());
        else if(text.matches("^help$")) sendMessage(fromUser, buildShowCommandsMessage());
    }

    private void sendMessage(String fromUser, String text){
        sendMessage(fromUser, text, ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    public void sendMessage(String fromUser, String text, String[] imageUrls) {
        String attachments = buildAttachmentsPayload(imageUrls);
        String payload = "{" +
                "\"bot_id\": \"" + botId + "\"," +
                "\"text\": \"" + text + "\"," +
                "\"attachments\": " + attachments + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROUP_ME_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String buildAttachmentsPayload(String[] imageUrls){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(String imageUrl : imageUrls){
            sb.append("{\"type\": \"image\",\"url\": \"" + imageUrl + "\"}");
        }
        sb.append("]");

        return sb.toString();
    }

    private String buildHelpMessage(){
        return "you rang? type '" + getBotKeyword() + " help' to see what i can do.";
    }

    private String buildShowCommandsMessage(){
        return "commands:\\n" +
                    getBotKeyword() + " help -- show bot commands";
    }
}
