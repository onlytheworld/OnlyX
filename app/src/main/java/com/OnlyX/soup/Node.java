package com.OnlyX.soup;

import com.OnlyX.utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/9/11.
 */
public class Node {

    private final Element element;

    public Node(String html) {
        this.element = Jsoup.parse(html).body();
    }

    public Node(Element element) {
        this.element = element;
    }

    public Node id(String id) {
        return new Node(element.getElementById(id));
    }

    public Node getChild(String cssQuery) {
        return new Node(get().select(cssQuery).first());
    }

    public Node getLastChild(String cssQuery) {
        return new Node(get().select(cssQuery).last());
    }

    public List<Node> list(String cssQuery) {
        List<Node> list = new LinkedList<>();
        Elements elements = element.select(cssQuery);
        for (Element e : elements) {
            list.add(new Node(e));
        }
        return list;
    }

    public Element get() {
        return element;
    }

    public String text() {
        try {
            return element.text().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public String text(String cssQuery) {
        try {
            return element.select(cssQuery).first().text().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public String textWithSubstring(String cssQuery, int start, int end) {
        return StringUtils.substring(text(cssQuery), start, end);
    }

    public String textWithSubstring(String cssQuery, int start) {
        return textWithSubstring(cssQuery, start, -1);
    }

    public String textWithSplit(String cssQuery, String regex, int index) {
        return StringUtils.split(text(cssQuery), regex, index);
    }

    public String attr(String attr) {
        try {
            return element.attr(attr).trim();
        } catch (Exception e) {
            return null;
        }
    }

    public String attr(String cssQuery, String attr) {
        try {
            return element.select(cssQuery).first().attr(attr).trim();
        } catch (Exception e) {
            return null;
        }
    }

    public String attrWithSubString(String attr, int start, int end) {
        return StringUtils.substring(attr(attr), start, end);
    }

    public String attrWithSubString(String cssQuery, String attr, int start, int end) {
        return StringUtils.substring(attr(cssQuery, attr), start, end);
    }

    public String attrWithSplit(String cssQuery, String attr, String regex, int index) {
        return StringUtils.split(attr(cssQuery, attr), regex, index);
    }

    public String src() {
        return attr("src");
    }

    public String src(String cssQuery) {
        return attr(cssQuery, "src");
    }

    public String href() {
        return attr("href");
    }

    public String href(String cssQuery) {
        return attr(cssQuery, "href");
    }

    public String hrefWithSubString(int start, int end) {
        return attrWithSubString("href", start, end);
    }

    public String hrefWithSubString(int start) {
        return hrefWithSubString(start, -1);
    }

    public String hrefWithSubString(String cssQuery, int start, int end) {
        return attrWithSubString(cssQuery, "href", start, end);
    }

    public String hrefWithSplit(int index) {
        return splitHref(href(), index);
    }

    public String hrefWithSplit(String cssQuery, int index) {
        return splitHref(href(cssQuery), index);
    }

    static public String splitHref(String str, int index) {
        if (str == null) {
            return null;
        }
        str = str.replaceFirst(".*\\..*?/", "");
        str = str.replaceAll("[/\\.=\\?]", " ");
        str = str.trim();
        return StringUtils.split(str, "\\s+", index);
    }

}
