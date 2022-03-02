package io.github.qzcsfchh.deeplinkgenerator.annotation;

public class DeepLinkEntity {
    private String fullClass;
    private String scheme;
    private String host;
    private String action;
    private String path;
    private boolean exported;

    public String getFullClass() {
        return fullClass;
    }

    public void setFullClass(String fullClass) {
        this.fullClass = fullClass;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "DeepLinkEntity{" +
                "fullClass='" + fullClass + '\'' +
                ", scheme='" + scheme + '\'' +
                ", host='" + host + '\'' +
                ", action='" + action + '\'' +
                ", path='" + path + '\'' +
                ", exported=" + exported +
                '}';
    }
}
