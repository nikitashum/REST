
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import java.util.HashMap;

/**
 * An example showing how to send HTTP GET and read the response from the server
 */
public class Main {

    public static void main(String[] args) {
        Main Run = new Main("104.248.47.74", 80);
        Run.authorize();
        for (int j = 1; j < 5; j++) {
            Run.getTask(j);
            Run.solveTask(j);
        }
        Run.getSecret();
        Run.solveSecret();
        Run.getResult();
    }

    private String BASE_URL; // Base URL (address) of the server
    private int ID;
    private String IPSecret;
    private JSONArray Task;

    /**
     * Create an HTTP GET example
     *
     * @param host Will send request to this host: IP address or domain
     * @param port Will use this port
     */
    public Main(String host, int port) {
        BASE_URL = "http://" + host + ":" + port + "/";
    }

    /**
     * Authorizaton
     */
    public void authorize() {
        String mail = "nikitasu@stud.ntnu.no";
        String phone = "46237765";

        JSONObject json = new JSONObject();
        json.put("email", mail);
        json.put("phone", phone);
        System.out.println("Posting this JSON data to server");
        System.out.println(json.toString());
        sendPost("dkrest/auth", json, 0);
    }

    /**
     * Send an HTTP GET to get first task
     */
    public void getTask(int j) {
        sendGet("dkrest/gettask/" + j + "?sessionId=" + ID);
    }

    /**
     * Send an HTTP GET to get secret task
     */
    public void getSecret() {
        double a = Math.sqrt(4064256);
        sendGet("dkrest/gettask/" + a + "?sessionId=" + ID);
    }

    /**
     * Send an HTTP GET to get Result
     */
    public void getResult() {
        sendGet("dkrest/results/" + ID);
    }

    /**
     * Solve Secret
     */
    public void solveSecret() {
        String ip = Task.getString(0);
        ip = ip.substring(0, ip.length() - 1);
        ip = ip.concat("1");
        JSONObject json = new JSONObject();
        json.put("sessionId", ID);
        json.put("ip", ip);
        System.out.println("Posting this JSON data to server");
        System.out.println(json.toString());
        sendPost("dkrest/solve", json, 5);

    }

    /**
     * Solve tasks
     */
    public void solveTask(int j) {
        if (j == 1) {
            String msg = "Hello";
            JSONObject json = new JSONObject();
            json.put("sessionId", ID);
            json.put("msg", msg);
            System.out.println("Posting this JSON data to server");
            System.out.println(json.toString());
            sendPost("dkrest/solve", json, 0);
        }
        if (j == 2) {
            String text = Task.toString();
            int x = text.length() - 2;
            String msg = text.substring(2, x);
            JSONObject json = new JSONObject();
            json.put("sessionId", ID);
            json.put("msg", msg);
            System.out.println("Posting this JSON data to server");
            System.out.println(json.toString());
            sendPost("dkrest/solve", json, 0);
        }
        if (j == 3) {
            List<Integer> list = new ArrayList();

            int pro = 1;
            for (int i = 0; i < Task.length(); i++) {
                list.add(i, Task.getInt(i));
            }
            for (int i = 0; i < Task.length(); i++) {
                pro = pro * list.get(i);
            }
            JSONObject json = new JSONObject();
            json.put("sessionId", ID);
            json.put("result", pro);
            System.out.println("Posting this JSON data to server");
            System.out.println(pro);
            sendPost("dkrest/solve", json, 0);
        }

        if (j == 4) {
            HashMap<String, Integer> list = new HashMap<String, Integer>();
            for (int i = 0; i < 9999; i++) {
                String password = String.valueOf(i);
                list.put(DigestUtils.md5Hex(password), i);
            }
            int pin = list.get(Task.getString(0));
            JSONObject json = new JSONObject();
            json.put("sessionId", ID);
            json.put("pin", pin);
            System.out.println("Posting this JSON data to server");
            System.out.println();
            sendPost("dkrest/solve", json, 0);
        }
    }

    /**
     * Send HTTP GET
     *
     * @param path Relative path in the API.
     */
    private void sendGet(String path) {
        try {
            String url = BASE_URL + path;
            URL urlObj = new URL(url);
            System.out.println("Sending HTTP GET to " + url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                System.out.println("Server reached");
                // Response was OK, read the body (data)
                InputStream stream = con.getInputStream();
                String jsonObjectString = convertStreamToString(stream);
                stream.close();
                System.out.println("Response from the server:");
                System.out.println(jsonObjectString);

                JSONObject jsonObject = new JSONObject(jsonObjectString);

                if (jsonObject.has("arguments")) {
                    Task = jsonObject.getJSONArray("arguments");
                }

            } else {
                String responseDescription = con.getResponseMessage();
                System.out.println("Request failed, response code: " + responseCode + " (" + responseDescription + ")");
            }
        } catch (ProtocolException e) {
            System.out.println("Protocol not supported by the server");
        } catch (IOException e) {
            System.out.println("Something went wrong: " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Send HTTP POST
     *
     * @param path Relative path in the API.
     * @param jsonData The data in JSON format that will be posted to the server
     */
    private void sendPost(String path, JSONObject jsonData, int i) {
        try {
            String url = BASE_URL + path;
            URL urlObj = new URL(url);
            System.out.println("Sending HTTP POST to " + url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            os.write(jsonData.toString().getBytes());
            os.flush();

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                System.out.println("Server reached");

                // Response was OK, read the body (data)
                InputStream stream = con.getInputStream();
                String responseBody = convertStreamToString(stream);
                stream.close();
                System.out.println("Response from the server:");
                System.out.println(responseBody);

                JSONObject jsonObject = new JSONObject(responseBody);

                if (jsonObject.has("sessionId")) {
                    ID = jsonObject.getInt("sessionId");
                }

                if (i == 5 && jsonObject.has("arguments")) {
                    IPSecret = jsonObject.getString("arguments");
                    System.out.println(IPSecret);
                }

            } else {
                String responseDescription = con.getResponseMessage();
                System.out.println("Request failed, response code: " + responseCode + " (" + responseDescription + ")");
            }
        } catch (ProtocolException e) {
            System.out.println("Protocol nto supported by the server");
        } catch (IOException e) {
            System.out.println("Something went wrong: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Read the whole content from an InputStream, return it as a string
     *
     * @param is Inputstream to read the body from
     * @return The whole body as a string
     */
    private String convertStreamToString(InputStream is) {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                response.append('\n');
            }
        } catch (IOException ex) {
            System.out.println("Could not read the data from HTTP response: " + ex.getMessage());
        }
        return response.toString();
    }

}
