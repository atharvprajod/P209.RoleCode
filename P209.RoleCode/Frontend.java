import java.util.List;
import java.util.NoSuchElementException;

public class Frontend implements FrontendInterface {

    private final BackendInterface backend;

    public Frontend(BackendInterface backend) {
        this.backend = backend;
    }

    @Override
    public String generateShortestPathPromptHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<section class=\"shortest-path\">");
        sb.append("<h2>Find a Shortest Path</h2>");
        sb.append("<label for=\"start\">Start location:</label>");
        sb.append("<input id=\"start\" type=\"text\" name=\"start\" />");
        sb.append("<label for=\"end\">Destination:</label>");
        sb.append("<input id=\"end\" type=\"text\" name=\"end\" />");
        sb.append("<button id=\"find-shortest\" type=\"button\">Find Shortest Path</button>");
        sb.append("</section>");
        return sb.toString();
    }

    @Override
    public String generateShortestPathResponseHTML(String start, String end) {
        if (start == null || start.trim().isEmpty() || end == null || end.trim().isEmpty()) {
            return "<p>Error: start and end locations must be provided.</p>";
        }

        try {
            List<String> path = backend.findLocationsOnShortestPath(start, end);
            if (path == null || path.isEmpty()) {
                return "<p>No path found from \"" + escape(start) + "\" to \"" + escape(end) + "\".</p>";
            }

            StringBuilder list = new StringBuilder("<ol>");
            for (String loc : path) list.append("<li>").append(escape(loc)).append("</li>");
            list.append("</ol>");

            List<Double> segTimes = backend.findTimesOnShortestPath(start, end);
            double total = 0.0;
            if (segTimes != null) {
                for (Double d : segTimes) if (d != null) total += d;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<section class=\"shortest-path-result\">");
            sb.append("<p>Shortest path from <strong>")
              .append(escape(start)).append("</strong> to <strong>")
              .append(escape(end)).append("</strong>:</p>");
            sb.append(list);
            sb.append("<p>Total travel time: ").append(formatSeconds(total)).append("</p>");
            sb.append("</section>");
            return sb.toString();

        } catch (NoSuchElementException nse) {
            return "<p>Error: one or both locations are unknown, or not connected.</p>";
        } catch (Exception e) {
            return "<p>Unexpected error computing path.</p>";
        }
    }

    @Override
    public String generateFurthestDestinationFromPromptHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<section class=\"furthest-dest\">");
        sb.append("<h2>Find Furthest Destination</h2>");
        sb.append("<label for=\"from\">Start location:</label>");
        sb.append("<input id=\"from\" type=\"text\" name=\"from\" />");
        sb.append("<button id=\"furthest-btn\" type=\"button\">Furthest Destination From</button>");
        sb.append("</section>");
        return sb.toString();
    }

    @Override
    public String generateFurthestDestinationFromResponseHTML(String start) {
        if (start == null || start.trim().isEmpty()) {
            return "<p>Error: start location must be provided.</p>";
        }

        try {
            List<String> all = backend.getListOfAllLocations();
            if (all == null || !all.contains(start)) {
                return "<p>Error: start location is unknown or has no reachable destinations.</p>";
            }
        } catch (Exception e) {
            return "<p>Error: unable to verify start location.</p>";
        }

        try {
            String dest = backend.getFurthestDestinationFrom(start);
            if (dest == null || dest.trim().isEmpty()) {
                return "<p>No reachable destinations from \"" + escape(start) + "\".</p>";
            }

            List<String> path = backend.findLocationsOnShortestPath(start, dest);

            StringBuilder sb = new StringBuilder();
            sb.append("<section class=\"furthest-dest-result\">");
            sb.append("<p>Starting from <strong>").append(escape(start))
              .append("</strong>, the furthest destination is <strong>")
              .append(escape(dest)).append("</strong>.</p>");

            if (path != null && !path.isEmpty()) {
                sb.append("<p>One shortest path to this destination:</p><ol>");
                for (String loc : path) sb.append("<li>").append(escape(loc)).append("</li>");
                sb.append("</ol>");
            } else {
                sb.append("<p>No path data available.</p>");
            }

            sb.append("</section>");
            return sb.toString();

        } catch (NoSuchElementException nse) {
            return "<p>Error: start location is unknown or has no reachable destinations.</p>";
        } catch (Exception e) {
            return "<p>Unexpected error computing furthest destination.</p>";
        }
    }

    // --- helpers ---

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String formatSeconds(double secs) {
        if (Math.abs(secs - Math.rint(secs)) < 1e-9) {
            return ((long) Math.rint(secs)) + " seconds";
        } else {
            return String.format("%.1f seconds", secs);
        }
    }
}
