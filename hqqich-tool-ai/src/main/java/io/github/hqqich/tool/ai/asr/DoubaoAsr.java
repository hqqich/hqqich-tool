package io.github.hqqich.tool.ai.asr;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.hqqich.tool.base.exception.UtilException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by chenhao on 2026/4/28 is 15:31.<p/>
 *
 * @author chenhao
 */
public class DoubaoAsr {

    private static final String BASE_URL = "https://openspeech-direct.zijieapi.com";

    private static final String SUBMIT_URL = BASE_URL + "/api/v3/auc/bigmodel/submit";
    private static final String QUERY_URL = BASE_URL + "/api/v3/auc/bigmodel/query";
    private static final String APP_ID = "";

    private static final OkHttpClient client = new Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static DoubaoResult getStrFmt(String fileUrl) throws IOException, InterruptedException {
        String responseBody = getStr(fileUrl);
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return new DoubaoResult();
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            if (!jsonObject.has("result") || !jsonObject.get("result").isJsonObject()) {
                return new DoubaoResult();
            }

            JsonObject result = jsonObject.getAsJsonObject("result");
            if (!result.has("text") || result.get("text").isJsonNull()) {
                return new DoubaoResult();
            }

            DoubaoResult doubaoResult = new DoubaoResult();
            doubaoResult.setResult(result.get("text").getAsString());

            // Extract words from utterances
            List<WordTime> wordsList = new ArrayList<>();
            if (result.has("utterances") && result.get("utterances").isJsonArray()) {
                JsonArray utterances = result.getAsJsonArray("utterances");
                for (int i = 0; i < utterances.size(); i++) {
                    JsonObject utterance = utterances.get(i).getAsJsonObject();
                    if (utterance.has("words") && utterance.get("words").isJsonArray()) {
                        JsonArray words = utterance.getAsJsonArray("words");
                        for (int j = 0; j < words.size(); j++) {
                            JsonObject word = words.get(j).getAsJsonObject();
                            WordTime wordTime = new WordTime();
                            if (word.has("text")) {
                                wordTime.setText(word.get("text").getAsString());
                            }
                            if (word.has("start_time")) {
                                wordTime.setStartTime(String.valueOf(word.get("start_time").getAsInt()));
                            }
                            if (word.has("end_time")) {
                                wordTime.setEndTime(String.valueOf(word.get("end_time").getAsInt()));
                            }
                            wordsList.add(wordTime);
                        }
                    }
                }
            }
            doubaoResult.setWords(wordsList);

            return doubaoResult;

        } catch (Exception e) {
            throw new IOException("Failed to parse result.text from doubao response", e);
        }
    }


    public static String getStr(String fileUrl) throws IOException, InterruptedException {
        String[] result = submitTask(fileUrl, APP_ID);
        String taskId = result[0];
        String xTtLogid = result[1];

        while (true) {
            try (Response queryResponse = queryTask(taskId, xTtLogid, APP_ID)) {
                String code = queryResponse.header("X-Api-Status-Code", "");
                if ("20000000".equals(code)) {
                    return queryResponse.body() != null ? queryResponse.body().string() : "";
                }
                if (!"20000001".equals(code) && !"20000002".equals(code)) {
                    throw new IOException("Query doubao task failed, code=" + code);
                }
            }
            Thread.sleep(1000);
        }
    }

    private static String[] submitTask(String fileUrl, String appId) throws IOException {
        String taskId = UUID.randomUUID().toString();
        Map<String, String> user = new HashMap<>();
        user.put("uid", "豆包语音");

        Map<String, Object> audio = new HashMap<>();
        audio.put("url", fileUrl);

        Map<String, String> corpus = new HashMap<>();
        corpus.put("correct_table_name", "");
        corpus.put("context", "");

        Map<String, Object> innerRequest = new HashMap<>();
        innerRequest.put("model_name", "bigmodel");
        innerRequest.put("enable_channel_split", true);
        innerRequest.put("enable_ddc", true);
        innerRequest.put("enable_speaker_info", true);
        innerRequest.put("enable_punc", true);
        innerRequest.put("enable_itn", true);
        innerRequest.put("corpus", corpus);

        Map<String, Object> mainRequest = new HashMap<>();
        mainRequest.put("user", user);
        mainRequest.put("audio", audio);
        mainRequest.put("request", innerRequest);

        Gson gson = new Gson();
        String jsonString = gson.toJson(mainRequest);
        RequestBody body = RequestBody.create(jsonString, JSON);

        Request request = new Request.Builder()
                .url(SUBMIT_URL)
                .header("x-api-key", appId)
                .header("X-Api-Resource-Id", "volc.bigasr.auc")
                .header("X-Api-Request-Id", taskId)
                .header("X-Api-Sequence", "-1")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            taskId = extractTaskId(responseBody, taskId);

            if ("20000000".equals(response.header("X-Api-Status-Code"))) {
                String xTtLogid = response.header("X-Tt-Logid", "");
                return new String[]{taskId, xTtLogid};
            }
            throw new UtilException("Submit doubao task failed, code=" + response.header("X-Api-Status-Code", "") + ", body=" + responseBody);
        }
    }

    private static Response queryTask(String taskId, String xTtLogid, String appId) throws IOException {
        RequestBody body = RequestBody.create("{}", JSON);

        Request request = new Request.Builder()
                .url(QUERY_URL)
                .header("x-api-key", appId)
                .header("X-Api-Resource-Id", "volc.bigasr.auc")
                .header("X-Api-Request-Id", taskId)
                .header("X-Tt-Logid", xTtLogid)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        if (response.header("X-Api-Status-Code") == null) {
            response.close();
            throw new IOException("Query doubao task failed, missing X-Api-Status-Code header");
        }
        return response;
    }

    private static String extractTaskId(String responseBody, String defaultTaskId) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return defaultTaskId;
        }
        try {
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            if (!jsonResponse.has("Data") || !jsonResponse.get("Data").isJsonObject()) {
                return defaultTaskId;
            }
            JsonObject data = jsonResponse.getAsJsonObject("Data");
            if (!data.has("TaskID") || data.get("TaskID").isJsonNull()) {
                return defaultTaskId;
            }
            return data.get("TaskID").getAsString();
        } catch (Exception e) {
            return defaultTaskId;
        }
    }

}
