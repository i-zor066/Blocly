package io.bloc.android.blocly.api.network;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by igor on 27/8/15.
 */
public class GetFeedsNetworkRequest extends NetworkRequest<List<GetFeedsNetworkRequest.FeedResponse>> {

    public static final int ERROR_PARSING = 3;

    private static final String XML_TAG_TITLE = "title";
    private static final String XML_TAG_DESCRIPTION = "description";
    private static final String XML_TAG_LINK = "link";
    private static final String XML_TAG_ITEM = "item";
    private static final String XML_TAG_PUB_DATE = "pubDate";
    private static final String XML_TAG_GUID = "guid";
    private static final String XML_TAG_ENCLOSURE = "enclosure";
    private static final String XML_TAG_CONTENT_ENCODED = "content:encoded";
    private static final String XML_TAG_MEDIA_CONTENT = "media:content";
    private static final String XML_ATTRIBUTE_URL = "url";
    private static final String XML_ATTRIBUTE_TYPE = "type";

    String [] feedUrls;

    public GetFeedsNetworkRequest(String... feedUrls) {
        this.feedUrls = feedUrls;
    }

    @Override
    public List<FeedResponse> performRequest() {
        List<FeedResponse> responseFeeds = new ArrayList<FeedResponse>(feedUrls.length);
        for (String feedUrlString : feedUrls) {
            InputStream inputStream = openStream(feedUrlString);
            if (inputStream == null) {
                return null;
            }
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document xmlDocument = documentBuilder.parse(inputStream);
                String channelTitle = optFirstTagFromDocument(xmlDocument, XML_TAG_TITLE);
                String channelDescription = optFirstTagFromDocument(xmlDocument, XML_TAG_DESCRIPTION);
                String channelURL = optFirstTagFromDocument(xmlDocument, XML_TAG_LINK);

                NodeList allItemNodes = xmlDocument.getElementsByTagName(XML_TAG_ITEM);
                List<ItemResponse> responseItems = new ArrayList<ItemResponse>(allItemNodes.getLength());
                for (int itemIndex = 0; itemIndex < allItemNodes.getLength(); itemIndex++) {
                    // #7
                    String itemURL = null;
                    String itemTitle = null;
                    String itemImageURL = null;
                    String itemIframeURL = null;
                    String yThumbURL = null;
                    String itemContentEncodedText = null;
                    String itemMediaURL = null;
                    String itemMediaMIMEType = null;
                    String itemDescription = null;
                    String itemGUID = null;
                    String itemPubDate = null;
                    String itemEnclosureURL = null;
                    String itemEnclosureMIMEType = null;
                    String imageHeight = null;
                    Node itemNode = allItemNodes.item(itemIndex);
                    NodeList tagNodes = itemNode.getChildNodes();
                    for (int tagIndex = 0; tagIndex < tagNodes.getLength(); tagIndex++) {
                        Node tagNode = tagNodes.item(tagIndex);
                        String tag = tagNode.getNodeName();
                        // #9
                        if (XML_TAG_LINK.equalsIgnoreCase(tag)) {
                            itemURL = tagNode.getTextContent();
                        } else if (XML_TAG_TITLE.equalsIgnoreCase(tag)) {
                            itemTitle = tagNode.getTextContent();
                        } else if (XML_TAG_DESCRIPTION.equalsIgnoreCase(tag)) {
                            String descriptionText = tagNode.getTextContent();
                            itemImageURL = parseImageFromHTML(descriptionText);
                            imageHeight = parseImageHeightFromHTML(descriptionText);
                            if (imageHeight.contentEquals("1")) {
                                itemImageURL = null;
                            }
                            itemIframeURL = parseIframeLinkFromHTML(descriptionText);
                            yThumbURL = getThumbFromLink(itemIframeURL);
                            itemDescription = parseTextFromHTML(descriptionText);
                           // Log.v("imageHeight001", imageHeight + " Blah " + itemImageURL);
                        } else if (XML_TAG_ENCLOSURE.equalsIgnoreCase(tag)) {
                            // #10
                            NamedNodeMap enclosureAttributes = tagNode.getAttributes();
                            itemEnclosureURL = enclosureAttributes.getNamedItem(XML_ATTRIBUTE_URL).getTextContent();
                            itemEnclosureMIMEType = enclosureAttributes.getNamedItem(XML_ATTRIBUTE_TYPE).getTextContent();
                        } else if (XML_TAG_PUB_DATE.equalsIgnoreCase(tag)) {
                            itemPubDate = tagNode.getTextContent();
                        } else if (XML_TAG_GUID.equalsIgnoreCase(tag)) {
                            itemGUID = tagNode.getTextContent();
                        } else if (XML_TAG_CONTENT_ENCODED.equalsIgnoreCase(tag)) {
                            String contentEncoded = tagNode.getTextContent();
                            itemImageURL = parseImageFromHTML(contentEncoded);
                            imageHeight = parseImageHeightFromHTML(contentEncoded);
                            if (imageHeight.contentEquals("1")) {
                                itemImageURL = null;
                            }
                            itemIframeURL = parseIframeLinkFromHTML(contentEncoded);
                            yThumbURL = getThumbFromLink(itemIframeURL);
                            itemContentEncodedText = parseTextFromHTML(contentEncoded);
                            // #8
                        } else if (XML_TAG_MEDIA_CONTENT.equalsIgnoreCase(tag)) {
                            NamedNodeMap mediaAttributes = tagNode.getAttributes();
                            itemMediaURL = mediaAttributes.getNamedItem(XML_ATTRIBUTE_URL).getTextContent();
                            itemMediaMIMEType = mediaAttributes.getNamedItem(XML_ATTRIBUTE_TYPE).getTextContent();
                        }
                    }
                    if (itemEnclosureURL == null) {
                        itemEnclosureURL = itemImageURL;
                    }
                    if (itemEnclosureURL == null) {
                        itemEnclosureURL = yThumbURL;
                    }
                    if (itemEnclosureURL == null) {
                        itemEnclosureURL = itemMediaURL;
                        itemEnclosureMIMEType = itemMediaMIMEType;
                    }
                    if (itemMediaURL != null) {
                        itemEnclosureURL = itemMediaURL;
                    }
                    if (yThumbURL != null) {
                        itemEnclosureURL = yThumbURL;
                    }
                    if (itemContentEncodedText != null) {
                        itemDescription = itemContentEncodedText;
                    }

                    responseItems.add(new ItemResponse(itemURL, itemTitle, itemDescription,
                            itemGUID, itemPubDate, itemEnclosureURL, itemEnclosureMIMEType));
                } responseFeeds.add(new FeedResponse(feedUrlString, channelTitle, channelURL, channelDescription, responseItems));
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                setErrorCode(ERROR_IO);
                return null;
            } catch (SAXException e) {
                e.printStackTrace();
                setErrorCode(ERROR_PARSING);
                return null;
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                setErrorCode(ERROR_PARSING);
                return null;
            }
        }
        return responseFeeds;
    }



    private String optFirstTagFromDocument(Document document, String tagName) {
        NodeList elementsByTagName = document.getElementsByTagName(tagName);
        if (elementsByTagName.getLength() > 0) {
            return elementsByTagName.item(0).getTextContent();
        }
        return null;
    }

    static String parseTextFromHTML(String htmlString) {
        org.jsoup.nodes.Document document = Jsoup.parse(htmlString);
        return document.body().text();
    }

    static String parseImageFromHTML(String htmlString) {
        org.jsoup.nodes.Document document = Jsoup.parse(htmlString);
        Elements imgElements = document.select("img");
        if (imgElements.isEmpty()) {
            return null;
        }
        return imgElements.attr("src");
    }

    static String parseImageHeightFromHTML(String htmlString) {
        org.jsoup.nodes.Document document = Jsoup.parse(htmlString);
        Elements imgElements = document.select("img");
        if (imgElements.isEmpty()) {
            return null;
        }
        return imgElements.attr("height");
    }

    static String parseIframeLinkFromHTML(String htmlString) {
        org.jsoup.nodes.Document document = Jsoup.parse(htmlString);
        Elements iframeElements = document.select("iframe");
        if (iframeElements.isEmpty()) {
            return null;
        }
        return iframeElements.attr("src");
    }

    String getThumbFromLink(String link) {

        if (link == null ) {
            return null;
        } else {

            String pattern = "https:\\/\\/www\\.youtube\\.com\\/embed\\/(\\w+)";

            Pattern compiledPattern = Pattern.compile(pattern);
            Matcher matcher = compiledPattern.matcher(link);

            String id = null;
            if (matcher.find()) {
                id = matcher.group(1);
            }
            Log.v("id", id + " Blah");

            String thumb = null;
            if (id != null) {
                thumb = "http://img.youtube.com/vi/" + id + "/hqdefault.jpg";
            }

            return thumb;
        }
    }




    public static class FeedResponse {
        public final String channelFeedURL;
        public final String channelTitle;
        public final String channelURL;
        public final String channelDescription;
        public final List<ItemResponse> channelItems;

        FeedResponse(String channelFeedURL, String channelTitle, String channelURL,
                     String channelDescription, List<ItemResponse> channelItems) {
            this.channelFeedURL = channelFeedURL;
            this.channelTitle = channelTitle;
            this.channelURL = channelURL;
            this.channelDescription = channelDescription;
            this.channelItems = channelItems;
        }


    }

    public static class ItemResponse {
        public final String itemURL;
        public final String itemTitle;
        public final String itemDescription;
        public final String itemGUID;
        public final String itemPubDate;
        public final String itemEnclosureURL;
        public final String itemEnclosureMIMEType;

        ItemResponse(String itemURL, String itemTitle, String itemDescription,
                     String itemGUID, String itemPubDate, String itemEnclosureURL,
                     String itemEnclosureMIMEType) {
            this.itemURL = itemURL;
            this.itemTitle = itemTitle;
            this.itemDescription = itemDescription;
            this.itemGUID = itemGUID;
            this.itemPubDate = itemPubDate;
            this.itemEnclosureURL = itemEnclosureURL;
            this.itemEnclosureMIMEType = itemEnclosureMIMEType;
        }

    }

}
