import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class FrontendTests {

    private FrontendInterface makeFE() throws IOException {
        GraphADT<String, Double> g = new Graph_Placeholder();
        Backend_Placeholder b = new Backend_Placeholder(g);
        b.loadGraphData("campus.dot");
        return new Frontend(b);
    }

    /** roleTest1: prompt HTML for shortest-path form has required IDs, labels, and button text. */
    @Test
    public void roleTest1_promptForShortestPath_hasInputsAndButton() throws IOException {
        FrontendInterface fe = makeFE();
        String html = fe.generateShortestPathPromptHTML();

        assertTrue(html.contains("id=\"start\""), "Expected input with id=\"start\"");
        assertTrue(html.contains("id=\"end\""), "Expected input with id=\"end\"");
        assertTrue(html.contains("Find Shortest Path"), "Expected 'Find Shortest Path' button label");
        assertTrue(html.contains("<label"), "Expected labels for inputs");
        assertTrue(html.contains("<section"), "Expected a wrapping section");
    }

    /**
     * roleTest2: with the placeholder graph, the linear path is:
     * Union South -> Computer Sciences and Statistics -> Weeks Hall for Geological Sciences
     * Placeholder backend returns times [1.0, 2.0, 3.0] for 3 locations => total 6 seconds.
     */
    @Test
    public void roleTest2_responseForKnownPath_listsNodesAndShowsTotalTime() throws IOException {
        FrontendInterface fe = makeFE();
        String html = fe.generateShortestPathResponseHTML(
                "Union South", "Weeks Hall for Geological Sciences");

        assertTrue(html.contains("Shortest path from"), "Should describe the path");
        assertTrue(html.contains("<ol>") && html.contains("</ol>"), "Should include an ordered list");
        assertTrue(html.contains("<li>Union South</li>"), "Should list Union South");
        assertTrue(html.contains("<li>Computer Sciences and Statistics</li>"), "Should list CSS");
        assertTrue(html.contains("<li>Weeks Hall for Geological Sciences</li>"), "Should list Weeks Hall");
        // total time from placeholder times is 6 seconds
        assertTrue(html.contains("6"), "Total time should include 6");
        assertTrue(html.contains("seconds"), "Total time should specify seconds");
    }

    /**
     * roleTest3: furthest-destination flow.
     * After loadGraphData("campus.dot"), placeholder backend appends "Mosse Humanities Building" and
     * returns the last location as the furthest. We expect that name in the response.
     */
    @Test
    public void roleTest3_furthestDestination_promptAndResponses() throws IOException {
        FrontendInterface fe = makeFE();

        String prompt = fe.generateFurthestDestinationFromPromptHTML();
        assertTrue(prompt.contains("id=\"from\""), "Expected input with id=\"from\"");
        assertTrue(prompt.contains("Furthest Destination From"), "Expected button label");

        String ok = fe.generateFurthestDestinationFromResponseHTML("Union South");
        assertTrue(ok.contains("Union South"), "Should mention the start");
        assertTrue(ok.contains("Mosse Humanities Building"),
                "Should mention the furthest destination appended by loadGraphData");

        assertTrue(ok.contains("<ol>") && ok.contains("</ol>"), "Should include an ordered list of the path");

        String err = fe.generateFurthestDestinationFromResponseHTML("NotARealPlace");
        assertTrue(err.toLowerCase().contains("error") || err.toLowerCase().contains("no reachable"),
                "Should communicate an error for unknown start");
    }
}
