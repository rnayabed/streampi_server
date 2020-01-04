package updaterPKG;

import java.io.IOException;
import java.util.Arrays;

import StreamPiServer.Main;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import twitter4j.JSONArray;
import twitter4j.JSONObject;

public class gitRepo {
    private String repoURL;
    private String repoVer;
    private String downloadLink;

    String changelogRaw;

    public gitRepo(String urlIn) throws Exception{
        this.repoURL = urlIn;

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(repoURL);
        request.addHeader("content-type", "application/json");
        HttpResponse result = httpClient.execute(request);
        String json = EntityUtils.toString(result.getEntity(), "UTF-8");

        //System.out.println(json);
        JsonElement jelement = new JsonParser().parse(json);
        JsonArray jarr = jelement.getAsJsonArray();
        JsonObject jo = (JsonObject) jarr.get(0);


        repoVer = jo.get("tag_name").getAsString();

        changelogRaw = jo.get("body").getAsString();
        changelogRaw = changelogRaw.substring(changelogRaw.indexOf("*"),changelogRaw.indexOf("## Installation Instructions"));

        JsonArray assetsArray = jo.get("assets").getAsJsonArray();
        downloadLink = "unavailable";
        for(int i = 0;i<assetsArray.size();i++)
        {
            String assetLink = assetsArray.get(i).getAsJsonObject().get("browser_download_url").getAsString();
            if(assetLink.contains(Main.config.get("system_os")))
            {
                downloadLink = assetLink;
                break;
            }
        }
    }

    public String getRepoVer() { return repoVer; }
    public String getChangelog() { return changelogRaw; }
    public String getDownloadLink() { return downloadLink; }
}
