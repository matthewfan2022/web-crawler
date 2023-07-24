package assignment;

import java.util.*;
import java.net.*;
import org.attoparser.simple.*;

/**
 * A markup handler which is called by the Attoparser markup parser as it parses the input;
 * responsible for building the actual web index.
 *
 *
 */
public class CrawlingMarkupHandler extends AbstractSimpleMarkupHandler {

    private WebIndex index;
    private URL currentURL;
    private Set<URL> urls;
    private Set<URL> urlsToAdd;
    private static Set<URL> allURLs;
    private int count;

    // Initializes variables by creating new objects
    public CrawlingMarkupHandler() {
        index = new WebIndex();
        urls = new HashSet<>();
        allURLs = new HashSet<>();
        urlsToAdd = new HashSet<>();
    }

    // Helper method to set the html page that's currently being crawled
    public void setCurrentURL(URL url) {
        currentURL = url;
    }

    // Returns the complete index that has been crawled thus far when called.
    public Index getIndex() {
        return index;
    }

    // Returns any new URLs found to the Crawler
    public List<URL> newURLs() {
        // For the first page visited
        if(allURLs.size() == 0) {
            allURLs.add(currentURL);
        }

        // Removes duplicate URLs (i.e. pages already visited) as well as invalid URLs
        for (URL i : urls) {
            if(!allURLs.contains(i)) {
                if(i.toString().startsWith("html",i.toString().length()-4) || i.toString().startsWith("htm",i.toString().length()-4) || i.toString().startsWith("txt",i.toString().length()-4)) {
                    if(!i.toString().contains("external.html")) {
                        urlsToAdd.add(i);
                        allURLs.add(i);
                    }
                }
            }
        }

        // Converts the set to a list and returns
        return new LinkedList<>(urlsToAdd);
    }

    /**
     * These are some of the methods from AbstractSimpleMarkupHandler.
     * All of its method implementations are NoOps, so we've added some things
     * to do; please remove all the extra printing before you turn in your code.
     *
     * Note: each of these methods defines a line and col param, but you probably
     * don't need those values. You can look at the documentation for the
     * superclass to see all of the handler methods.
     */

    /**
     * Called when the parser first starts reading a document.
     * @param startTimeNanos  the current time (in nanoseconds) when parsing starts
     * @param line            the line of the document where parsing starts
     * @param col             the column of the document where parsing starts
     */
    public void handleDocumentStart(long startTimeNanos, int line, int col) {
        // Resets sets
        urlsToAdd = new HashSet<>();
        urls = new HashSet<>();
    }

    /**
     * Called when the parser finishes reading a document.
     * @param endTimeNanos    the current time (in nanoseconds) when parsing ends
     * @param totalTimeNanos  the difference between current times at the start
     *                        and end of parsing
     * @param line            the line of the document where parsing ends
     * @param col             the column of the document where the parsing ends
     */
    // No implementation needed
    public void handleDocumentEnd(long endTimeNanos, long totalTimeNanos, int line, int col) {

    }

    /**
     * Called at the start of any tag.
     * @param elementName the element name (such as "div")
     * @param attributes  the element attributes map, or null if it has no attributes
     * @param line        the line in the document where this elements appears
     * @param col         the column in the document where this element appears
     */
    public void handleOpenElement(String elementName, Map<String, String> attributes, int line, int col) {

        // Detects hyperlinks and adds it to the list of hyperlinks on this html page
        if(attributes != null && attributes.containsKey("HREF")) {
            try {
                urls.add(new URL(currentURL, attributes.get("HREF")));
            } catch (MalformedURLException e) {

            }
        }
        if(attributes != null && attributes.containsKey("href")) {
            try {
                urls.add(new URL(currentURL, attributes.get("href")));
            } catch (MalformedURLException e) {

            }
        }
    }

    /**
     * Called at the end of any tag.
     * @param elementName the element name (such as "div").
     * @param line        the line in the document where this elements appears.
     * @param col         the column in the document where this element appears.
     */
    // No implementation needed
    public void handleCloseElement(String elementName, int line, int col) {

    }

    /**
     * Called whenever characters are found inside a tag. Note that the parser is not
     * required to return all characters in the tag in a single chunk. Whitespace is
     * also returned as characters.
     * @param ch      buffer containint characters; do not modify this buffer
     * @param start   location of 1st character in ch
     * @param length  number of characters in ch
     */
    public void handleText(char ch[], int start, int length, int line, int col) {
        String currentWord = "";
        // Loop through characters inside the tag
        for(int i = start; i < start + length; i++) {
            // Instead of printing raw whitespace, we're escaping it
            switch(ch[i]) {
                case '\\':
                    break;
                case '"':
                    break;
                case '\n':
                case '\r':
                    break;
                case '\t':
                    break;
                default:
                    // Separates word Strings based off of spaces, checks for valid punctuation, and adds to the index
                    if(ch[i] == ' ') {
                        currentWord = checkPunctuation(currentWord);
                        count++;
                        index.addWord(currentWord, currentURL, count);
                        currentWord = "";
                    } else {
                        currentWord += ch[i];
                    }
                    break;
            }
        }
        // End of the characters inside a tag; checks if there are still characters even though the tag ends
        if(!currentWord.equals("")) {
            currentWord = checkPunctuation(currentWord);
            count++;
            index.addWord(currentWord, currentURL, count);
        }
    }

    // Helper method that removes all non-letter characters from the outsides of a word String
    private String checkPunctuation(String currentWord) {
        // First, set all letters to lowercase
        currentWord = currentWord.toLowerCase();

        // Finds errand characters at the end of the String using ASCII values
        for(int j = currentWord.length() - 1; j >= 0; j--) {
            if((int) currentWord.charAt(j) >= 97 && (int) currentWord.charAt(j) <= 122) {
                break;
            } else {
                currentWord = currentWord.substring(0, j);
            }
        }

        // Finds errand characters at the beginning of the String
        for(int j = 0; j < currentWord.length(); j++) {
            if((int) currentWord.charAt(j) >= 97 && (int) currentWord.charAt(j) <= 122) {
                break;
            } else {
                currentWord = currentWord.substring(j+1);
                j--;
            }
        }

        return currentWord;
    }
}
