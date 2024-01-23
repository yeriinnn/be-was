package http;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Uri {
    private String path;
    private Map<String, String> query = new HashMap<>();

    private Uri(String uri) {
        String[] tokens = uri.split("\\?");
        this.path = tokens[0];
        if(hasQuery(uri)){
            setQuery(this.query, tokens[1]);
        }
    }

    public static Uri from(String uri) {
        return new Uri(uri);
    }

    private boolean hasQuery(String uri){
        return uri.contains("?");
    }

    static void setQuery(Map<String, String> query, String queryString) {
        StringTokenizer st = new StringTokenizer(queryString, "&");
        while (st.hasMoreTokens()){
            String[] tokens = st.nextToken().split("=");
            query.put(tokens[0], tokens[1]);
        }
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    private String getQueryString(){
        if(query.size() == 0){
            return "";
        }
        StringBuilder sb = new StringBuilder("?");

        for(String key : query.keySet()){
            sb.append(key).append("=").append(query.get(key)).append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public String toString() {
        return path + getQueryString();
    }
}