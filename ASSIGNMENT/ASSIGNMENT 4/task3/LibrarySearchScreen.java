package task3;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LibrarySearchScreen extends Frame {
    private final TextField queryField = new TextField(22);
    private final Choice categoryChoice = new Choice();
    private final Button searchButton = new Button("Search");
    private final TextArea resultsArea = new TextArea("", 12, 60, TextArea.SCROLLBARS_VERTICAL_ONLY);
    private final Button prevButton = new Button("Prev");
    private final Button nextButton = new Button("Next");
    private final Button reserveButton = new Button("Reserve");
    private final Label statusLabel = new Label("Ready - Showing 1-3 of 5 books");

    private final List<String> catalog = Arrays.asList(
            "[CS-101] Operating System Concepts | Silberschatz | Computer Science | Available",
            "[CS-104] Introduction to Algorithms | Cormen et al. | Computer Science | Available",
            "[EC-202] Digital Signal Processing | Proakis | Electronics | Checked Out",
            "[MA-301] Linear Algebra and Its Applications | Gilbert Strang | Mathematics | Available",
            "[CS-209] Computer Networking: A Top-Down Approach | Kurose & Ross | Computer Science | Available"
    );
    private List<String> filtered = new ArrayList<>(catalog);
    private int page = 0;
    private static final int PAGE_SIZE = 3;

    public LibrarySearchScreen() {
        super("Campus Library Search Screen");
        setLayout(new BorderLayout(8, 8));

        // 1. NORTH: Title bar + Search row (fixed height on resize)
        Panel topPanel = new Panel(new GridLayout(2, 1, 0, 4));
        Label titleBar = new Label("  CAMPUS CENTRAL LIBRARY - CATALOG SEARCH", Label.LEFT);
        titleBar.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleBar.setBackground(new Color(230, 235, 242));

        Panel searchRow = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        categoryChoice.add("All Categories");
        categoryChoice.add("Computer Science");
        categoryChoice.add("Electronics");
        categoryChoice.add("Mathematics");
        searchRow.add(new Label("Keyword:"));
        searchRow.add(queryField);
        searchRow.add(new Label("Category:"));
        searchRow.add(categoryChoice);
        searchRow.add(searchButton);

        topPanel.add(titleBar);
        topPanel.add(searchRow);
        add(topPanel, BorderLayout.NORTH);

        // 2. CENTER: Results area (absorbs all horizontal and vertical resizing)
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(resultsArea, BorderLayout.CENTER);

        // 3. SOUTH: Action buttons row + Status bar (fixed height on resize)
        Panel bottomPanel = new Panel(new GridLayout(2, 1, 0, 2));
        Panel buttonRow = new Panel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        buttonRow.add(prevButton);
        buttonRow.add(nextButton);
        buttonRow.add(reserveButton);

        statusLabel.setBackground(new Color(240, 240, 240));
        bottomPanel.add(buttonRow);
        bottomPanel.add(statusLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        // Wire event handlers
        searchButton.addActionListener(e -> executeSearch());
        prevButton.addActionListener(e -> {
            if (page > 0) {
                page--;
                refreshResults();
            }
        });
        nextButton.addActionListener(e -> {
            if ((page + 1) * PAGE_SIZE < filtered.size()) {
                page++;
                refreshResults();
            }
        });
        reserveButton.addActionListener(e -> statusLabel.setText("Reservation request logged for selected title on Page " + (page + 1) + "."));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        refreshResults();
        setSize(650, 420);
        setLocationRelativeTo(null);
    }

    private void executeSearch() {
        String kw = queryField.getText().trim().toLowerCase();
        String cat = categoryChoice.getSelectedItem();
        filtered = catalog.stream()
                .filter(book -> kw.isEmpty() || book.toLowerCase().contains(kw))
                .filter(book -> "All Categories".equals(cat) || book.contains("| " + cat + " |"))
                .collect(Collectors.toList());
        page = 0;
        refreshResults();
    }

    private void refreshResults() {
        if (filtered.isEmpty()) {
            resultsArea.setText("No matching books found.");
            statusLabel.setText("0 results found.");
            return;
        }
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filtered.size());
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            sb.append((i + 1)).append(". ").append(filtered.get(i)).append("\n\n");
        }
        resultsArea.setText(sb.toString());
        statusLabel.setText(String.format("Showing %d-%d of %d matching books (Page %d)",
                start + 1, end, filtered.size(), page + 1));
    }

    public static void main(String[] args) {
        new LibrarySearchScreen().setVisible(true);
    }
}
