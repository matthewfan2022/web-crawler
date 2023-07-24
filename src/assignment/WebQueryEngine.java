package assignment;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;

/**
 * A query engine which holds an underlying web index and can answer textual queries with a
 * collection of relevant pages.
 *
 * TODO: Implement this!
 */
public class WebQueryEngine {
    public int index = 0;
    public String stream;
    public WebIndex webIndex;

    /**
     * Returns a WebQueryEngine that uses the given Index to constructe answers to queries.
     *
     * @param index The WebIndex this WebQueryEngine should use.
     * @return A WebQueryEngine ready to be queried.
     */
    public static WebQueryEngine fromIndex(WebIndex index) {
        //creates new web query engine and assigns web index
        WebQueryEngine engine = new WebQueryEngine();
        engine.webIndex = index;
        return engine;
    }

    /**
     * Returns a Collection of URLs (as Strings) of web pages satisfying the query expression.
     *
     * @param query A query expression.
     * @return A collection of web pages satisfying the query.
     */
    public Collection<Page> query(String query) {
        //reformats query by removing white space
        index = 0;
        stream = parseStream(query);

        //creates parse tree and returns urls
        Node parseTree = parseQuery();
        //printString(parseTree, 0);
        ArrayList urls = aggregateTree(parseTree);

        //creates pages from urls
        ArrayList<Page> queriedPages = new ArrayList<>();
        for (Object url : urls) {
            queriedPages.add(new Page((URL) url));
        }
        return queriedPages;
    }


    //gets next token from query based on index
    String getToken()
    {
        if (index >= stream.length()) {
            return "";
        }
        String subs = stream.substring(index, index+1);
        return subs;
    }

    //returns urls based on parse tree
    ArrayList aggregateTree(Node node) {
        //if not an operator
        if (!node.value.equals("|") && !node.value.equals("&")) {
            //if not operator
            if (node.value.charAt(0) == '!') {
                return listSubtraction(webIndex.allURLS, webIndex.phraseSearch(node.value.substring(1)));
            //if phrase with quotations
            } else if (node.value.charAt(0) == '\"') {
                return webIndex.phraseSearch(node.value.substring(1, node.value.length()-1));
            //if phrase with implicit and
            } else if (node.value.contains(" ")) {
                String[] words = node.value.split(" ");
                ArrayList array = webIndex.phraseSearch(words[0]);
                for (int i = 1; i < words.length; i++) {
                    array = listIntersection(array, webIndex.phraseSearch(words[i]));
                }
                return array;
            }
            //if regular word
            return webIndex.phraseSearch(node.value);
        }

        //if operator, aggregate left and right, and then combine based on operator
        ArrayList left = aggregateTree(node.left);
        ArrayList right = aggregateTree(node.right);
        if (node.value.equals("|")) {
            return listUnion(left, right);
        } else {
            return listIntersection(left, right);
        }
    }

    //remove uneeded white space and wraps entire query in parenthesis if needed
    String parseStream(String query) {
        //remove uneeded white space
        String newQuery = "";
        boolean inQuotation = false;
        for (int i = 0; i < query.length(); i++) {
            String substr = query.substring(i, i+1);
            if (substr.equals("\"")) {
                if (inQuotation) {
                    inQuotation = false;
                } else {
                    inQuotation = true;
                }
                newQuery += substr;
            } else if (inQuotation) {
                newQuery += substr;
            } else if (substr.equals(" ")) {
                if (i == 0 || i == query.length()-1) {
                    continue;
                }
                String prev = query.substring(i-1, i);
                String next = query.substring(i+1, i+2);
                if (!prev.equals("(") && !prev.equals(")") && !prev.equals("|") && !prev.equals("&")) {
                    if (!next.equals("(") && !next.equals(")") && !next.equals("|") && !next.equals("&")) {
                        newQuery += " ";
                    }
                }
            } else {
                newQuery += substr;
            }

        }
        query = newQuery;

        //check if need to add parenthesis
        int cnt = 0;
        boolean reachedZero = false;
        boolean hasOperator = false;
        for (int i = 0; i < query.length()-1; i++) {
            if (query.charAt(i) == '(') {
                cnt++;
            } else if (query.charAt(i) == ')') {
                cnt--;
            } else if (query.charAt(i) == '|' || query.charAt(i) == '&') {
                hasOperator = true;
            }
            if (cnt == 0) {
                reachedZero = true;
            }
        }
        if (reachedZero && hasOperator) {
            return "(" + query + ")";
        }
        return query;
    }

    //returns root node of parse tree
    Node parseQuery()
    {
        String t = getToken();
        index++;
        if (t.equals("(")) {
            Node left = parseQuery(); // recursively build the left subtree
            String op = getToken(); // get the binary operator: AND or OR
            index++;
            while (op.equals(")")) { //get next token if not operator
                op = getToken();
                index++;
            }
            Node right = parseQuery(); // recursively build the right subtree

            return new Node(op, left, right);
        } else {
            //parsing word until reaches operator or parenthesis
            String word = t;
            t = getToken();
            while(!t.equals(")") && !t.equals("|") && !t.equals("&") && !t.equals("")) {
                index++;
                word += t;
                t = getToken();
            }
            if (t.equals(")")) {
                index++;
            }
            return new Node(word, null, null);
        }
    }

    //prints parse tree with indentations
    //check if parse tree is correct
    public void printString(Node currNode, Integer depth) {
        if (currNode != null) {
            for (int i = 0; i < depth; i++) {
                System.out.print("   ");
            }
            System.out.println(currNode.value);
            printString(currNode.left, depth+1);
            printString(currNode.right, depth+1);
        }
    }

    //returns intersection of list
    public ArrayList listIntersection(ArrayList<URL> url1, ArrayList<URL> url2) {
        ArrayList<URL> intersection = new ArrayList<>();
        for (URL url : url1) {
            for (URL comparedURL: url2) {
                if (url == comparedURL) {
                    intersection.add(url);
                    break;
                }
            }
        }
        return intersection;
    }

    //returns union of list
    public ArrayList listUnion(ArrayList<URL> url1, ArrayList<URL> url2) {
        ArrayList<URL> union = url1;
        for (URL url : url2) {
            boolean shouldAdd = true;
            for (URL comparedURL : url1) {
                if (url == comparedURL) {
                    shouldAdd = false;
                    break;
                }
            }
            if (shouldAdd) {
                union.add(url);
            }
        }
        return union;
    }

    //returns difference of lists
    public ArrayList listSubtraction(ArrayList<URL> initialURL, ArrayList<URL> subtractedURL) {
        ArrayList<URL> difference = new ArrayList<>();
        for (URL url : initialURL) {
            boolean possible = true;
            for (URL comparedURL: subtractedURL) {
                if (url == comparedURL) {
                    possible = false;
                    break;
                }
            }
            if (possible) {
                difference.add(url);
            }
        }
        return difference;
    }

    //testing for web query
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        WebIndex index = new WebIndex();
        index = (WebIndex) index.load("index.db");
        WebQueryEngine engine = fromIndex(index);
        ArrayList urls = (ArrayList) engine.query("(an & (a & the))");

        System.out.println(urls.size());
    }
}
