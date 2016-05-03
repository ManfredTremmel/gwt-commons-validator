package java.net;

import java.io.Serializable;

public final class URI implements Comparable<URI>, Serializable {

    static final long serialVersionUID = -6052424284110960213L;

    private String path;

    public URI(String scheme, String host, String path, String fragment)
        throws URISyntaxException {
        this.path = path;
    }

    public URI normalize() {
        return this;
    }

    public String getPath() {
        return this.path;
    }

    @Override
    public int compareTo(URI that) {
        if (that == null || that.getPath() == null) {
            return -1;
        }
        if (this.getPath() == null) {
            return 1;
        }
        return this.getPath().compareTo(that.getPath());
    }
}
