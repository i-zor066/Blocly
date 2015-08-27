package io.bloc.android.blocly.api.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by igor on 27/8/15.
 */
public class GetFeedsNetworkRequest extends NetworkRequest {

    String [] feedUrls;

    public GetFeedsNetworkRequest(String... feedUrls) {
        this.feedUrls = feedUrls;
    }

    @Override
    public Object performRequest() {
        for (String feedUrlString : feedUrls) {
            InputStream inputStream = openStream(feedUrlString);
            //int counter = 0;
            if (inputStream == null) {
                return null;
            }
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = bufferedReader.readLine();
                while (line != null) {
                    Log.v(getClass().getSimpleName(), "Line: " + line);
                    line = bufferedReader.readLine();
                    if (line != null) {
                        /*counter = counter + numberOfOccurrences(line, "<item>");
                        Log.v(getClass().getSimpleName(), "Counter: " + counter);*/
                        List<String> items = getTagValues(line);
                        int counter2 = items.size();
                        Log.v(getClass().getSimpleName(), "Number of titles: " + counter2);
                        Log.v(getClass().getSimpleName(), "List of titles: " + items);
                    }
                }
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
                setErrorCode(ERROR_IO);
                return null;
            }
        }
        return null;
    }

   /* private static int numberOfOccurrences(String source, String sentence) {
        int occurrences = 0;

        if (source.contains(sentence)) {
            int withSentenceLength    = source.length();
            int withoutSentenceLength = source.replace(sentence, "").length();
            occurrences = (withSentenceLength - withoutSentenceLength) / sentence.length();
        }

        return occurrences;
    }*/

    private static final Pattern TAG_REGEX = Pattern.compile("<item><title>(.+?)</title>");

    private static List<String> getTagValues(final String str) {
        final List<String> tagValues = new ArrayList<String>();
        final Matcher matcher = TAG_REGEX.matcher(str);
        while (matcher.find()) {
            tagValues.add(matcher.group(1));
        }
        return tagValues;
    }

}
