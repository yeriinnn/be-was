package http;

import java.util.Arrays;

public enum ContentType {
    HTML("html", "/templates", "text/html"),
    CSS("css", "/static", "text/css"),
    JS("js", "/static", "text/javascript"),
    ICO("ico", "/static", "image/x-icon"),
    PNG("png", "/static", "image/png"),
    JPG("jpg", "/static", "image/jpeg"),
    WOFF("woff", "/static", "font/woff"),
    TTF("ttf", "/static", "font/ttf"),
    NONE("", "", "INVALID"),
    ;

    private String value;
    private String path;
    private String mime;

    ContentType(String value, String path, String mime) {
        this.value = value;
        this.path = path;
        this.mime = mime;
    }

    public static ContentType findBy(String file){
        System.out.println("file = " + file);

        if (file == null) {
            return NONE;
        }

        return Arrays.stream(values())
                .filter(type -> file.endsWith(type.value))
                .findFirst()
                .orElse(NONE);
    }

    public String getLocation() {
        return path;
    }

    private boolean isRedirect(String file){
        return "redirect:".equals(file);
    }

    public String getPath() {
        return path;
    }

    public String getMime() {
        return mime;
    }
}
