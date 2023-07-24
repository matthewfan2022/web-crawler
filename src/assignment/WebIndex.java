package assignment;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A web-index which efficiently stores information about pages. Serialization is done automatically
 * via the superclass "Index" and Java's Serializable interface.
 *
 * TODO: Implement this!
 */
public class WebIndex extends Index {
    /**
     * Needed for Serialization (provided by Index) - don't remove this!
     */
    private static final long serialVersionUID = 1L;
    public HashMap<String, HashMap<URL, ArrayList<Integer>>> webData = new HashMap<String, HashMap<URL, ArrayList<Integer>>>();
    public ArrayList allURLS = new ArrayList();

    // TODO: Implement all of this! You may choose your own data structures an internal APIs.
    // You should not need to worry about serialization (just make any other data structures you use
    // here also serializable - the Java standard library data structures already are, for example).


    //adds word to the webData hashmap based on url and location of word in webpage
    public void addWord(String word, URL url, Integer index) {
        HashMap<URL, ArrayList<Integer>> wordDict;

        //array list with all urls
        if (!allURLS.contains(url)) {
            allURLS.add(url);
        }
        if (webData.containsKey(word)) {
            wordDict = webData.get(word);

            //update url and indices
            ArrayList<Integer> indices;
            if (wordDict.containsKey(url)) {
                indices = wordDict.get(url);
            } else {
                indices = new ArrayList<>();
            }
            indices.add(index);
            wordDict.put(url, indices);

        } else {
            //create new pair in hashmap
            wordDict = new HashMap<>();
            ArrayList<Integer> indices = new ArrayList<>();
            indices.add(index);
            wordDict.put(url, indices);
        }
        webData.put(word, wordDict);
    }

    //returns an array of urls with the search phrase

    public ArrayList<URL> phraseSearch(String phrase) {
        //converts to lower case and splits if multiple words
        phrase = phrase.toLowerCase();
        ArrayList<URL> urls = new ArrayList<>();
        String[] words = phrase.split(" ");
        HashMap<URL, ArrayList<Integer>> newWordDict;
        HashMap<URL, ArrayList<Integer>> wordDict;
        HashMap<URL, ArrayList<Integer>> placeholder;

        if (webData.containsKey(words[0])) {
            wordDict = webData.get(words[0]);
        } else {
            return urls;
        }

        //uses index and location of words to check if words are next to each other
        for (int i = 1; i < words.length; i++) {
            placeholder = new HashMap<>();
            newWordDict = webData.get(words[i]);

            for ( URL url : wordDict.keySet() ) {
                ArrayList<Integer> placeholderIndices = new ArrayList();
                if (newWordDict.containsKey(url)) {
                    ArrayList<Integer> newIndices = newWordDict.get(url);
                    ArrayList<Integer> indices = wordDict.get(url);
                    for (Integer ix : indices) {
                        for (Integer n_ix : newIndices) {
                            if (n_ix == ix + 1) {
                                placeholderIndices.add(n_ix);
                            }
                            if (n_ix > ix) {
                                break;
                            }
                        }
                    }
                }
                if (placeholderIndices.size() != 0) {
                    placeholder.put(url, placeholderIndices);
                }
            }

            wordDict = placeholder;
        }

        //after filtering, add urls to return array list
        for ( URL url : wordDict.keySet() ) {
            urls.add(url);
        }

        return urls;
    }


    //testing for index adding and searching
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        WebIndex index = new WebIndex();

        URL url = new URL("http://google.com");
        index.addWord("dog", url, 7);
        index.addWord("animal", url, 9);

        url = new URL("http://yahoo.com");
        index.addWord("cat", url, 4);
        index.addWord("dog", url, 5);
        index.addWord("animal", url, 6);

        url = new URL("http://bing.com");
        index.addWord("cat", url, 4);
        index.addWord("dog", url, 5);
        index.addWord("nimal", url, 6);

        ArrayList<URL> urls = index.phraseSearch("dog");
        for (int i = 0; i < urls.size(); i++) {
            System.out.println(urls.get(i));
        }

        index.save("index.db");

    }

}
