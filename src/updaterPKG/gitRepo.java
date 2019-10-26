package updaterPKG;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class gitRepo {
    private String repoURL;
    private String repoVer;

    public gitRepo(String urlIn){
        this.repoURL = urlIn;
    }

    public void repoRequest(){
        try{
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(repoURL);
            request.addHeader("content-type", "application/json");
            HttpResponse result = httpClient.execute(request);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");

            //System.out.println(json);
            JsonElement jelement = new JsonParser().parse(json);
            JsonArray jarr = jelement.getAsJsonArray();
            JsonObject jo = (JsonObject) jarr.get(0);
            String tagName = jo.get("tag_name").toString();

            //sets the version number of the repo it pulls and fixes the problem of the JSON returning quotes
            repoVer = tagName.substring(1, tagName.length() - 1);;
        } catch (IOException ex){
            System.out.println(Arrays.toString(ex.getStackTrace()));
        }
    }

    public String getRepoVer() {
        return repoVer;
    }
}
