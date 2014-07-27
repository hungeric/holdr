package me.tatarka.socket.compile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class SocketViewParser {
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    private static final String APP_NS = "http://schemas.android.com/apk/res-auto";
    private static final String ID = "id";
    private static final String IGNORE = "socket_ignore";
    private static final String FIELD_NAME = "socket_field_name";

    public List<View> parse(Reader res) throws IOException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(res);

            List<View> views = new ArrayList<View>();
            View.Builder currentView = null;

            int tag;
            while ((tag = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (tag == XmlPullParser.START_TAG) {
                    String type = parseType(parser.getName());
                    String id = parseId(parser.getAttributeValue(ANDROID_NS, ID));
                    boolean ignore = parseIgnore(parser.getAttributeValue(APP_NS, IGNORE));
                    String fieldName = parser.getAttributeValue(APP_NS, FIELD_NAME);

                    if (id != null && !ignore) {
                        View.Builder view = View.of(type, id);
                        if (fieldName != null) {
                            view.fieldName(fieldName);
                        }

                        if (currentView != null) {
                            currentView.child(view);
                        }
                        currentView = view;
                    }
                } else if (tag == XmlPullParser.END_TAG) {
                    if (currentView != null) {
                        View.Builder parent = currentView.parent();
                        if (parent == null) {
                            views.add(currentView.build());
                        }
                        currentView = parent;
                    }
                }
            }
            return views;
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }

    private static String parseType(String type) {
        if (type == null) return null;
        if (type.contains(".")) return type;
        return "android.widget." + type;
    }

    private static String parseId(String id) {
        if (id == null) return null;
        int sep = id.indexOf('/');
        if (sep == -1) return id;
        return id.substring(sep + 1);
    }

    private static boolean parseIgnore(String ignore) {
        return "true".equals(ignore);
    }
}