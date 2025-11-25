
package cz.vut.fit.pdbe.xnuriey00.ui;

import cz.vut.fit.pdbe.xnuriey00.Main;
import cz.vut.fit.pdbe.xnuriey00.util.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainWindow extends JFrame {




    public MainWindow() {
        setTitle("Rental Property IS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.LIGHT_GRAY);

        // 1. Header
        add(createHeader(), BorderLayout.NORTH);

        // 2. Main Content Container
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        

        // Add the three main columns
        contentPanel.add(LeftControlPanel(), BorderLayout.WEST);
        contentPanel.add(JMap(), BorderLayout.CENTER);
        contentPanel.add(RightDetailPanel(), BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);
        
        setLocationRelativeTo(null);
    }


    private JPanel createHeader() {
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Top, left, bottom, right margin (like CSS margin)
        

        JLabel title = new JLabel("Rental Property IS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setIcon(new ImageIcon(getClass().getClassLoader().getResource("logo.png")));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);


        JButton refreshBtn = new JButton("REFRESH");
        JButton helpBtn = new JButton("HELP");

        JButton logoutBtn = new JButton("LOGOUT");

        // HEADER ACTION LISTENERS
        refreshBtn.addActionListener(e -> System.out.println("Header: Refresh clicked"));
        helpBtn.addActionListener(e -> System.out.println("Header: Help clicked"));
        logoutBtn.addActionListener(e -> {
            UserSession.getInstance().logout();
            this.dispose();
            Main.launch();
        });

        if (UserSession.getInstance().isAdmin()) {
            JButton manageUsers = new JButton("MANAGE USERS");
            manageUsers.addActionListener(e -> new UserManagementWindow().setVisible(true));
            buttonPanel.add(manageUsers);

        }
        buttonPanel.add(refreshBtn);
        buttonPanel.add(helpBtn);
        buttonPanel.add(logoutBtn);

        header.add(title, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);
        return header;
    }

  

    // LEFT PANEL: Search, Slider, Table, Analysis

    private JPanel LeftControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Search Bar
        JTextField searchField = new JTextField("Search");
        searchField.setMaximumSize(new Dimension(220, 26));
        
        // SEARCH ACTION LISTENER
        searchField.addActionListener(e -> System.out.println("Search: " + searchField.getText()));

        // Price Range Slider
        JLabel priceLabel = new JLabel("Price Range");
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSlider priceSlider = new JSlider(0, 5000, 1500);

        // SLIDER ACTION LISTENER
        priceSlider.addChangeListener(e -> {
            if (!priceSlider.getValueIsAdjusting()) {
                System.out.println("Price Adjusted: " + priceSlider.getValue());
            }
        });

        // Buttons
        JButton drawAreaBtn = new JButton("Draw Area");
        JButton uploadImgBtn = new JButton("Upload Image");

        // BUTTON ACTION LISTENERS


        drawAreaBtn.setText("\uD83D\uDDFA️ Draw Area"); // 🗺️
        drawAreaBtn.setBackground(new Color(173, 216, 230)); // Light blue
        drawAreaBtn.setOpaque(true);
        drawAreaBtn.setBorderPainted(false);

        uploadImgBtn.setText("\uD83D\uDDBC️ Upload Image"); // 🖼️
        uploadImgBtn.setBackground(new Color(173, 216, 230)); // Light blue
        uploadImgBtn.setOpaque(true);
        uploadImgBtn.setBorderPainted(false);

        drawAreaBtn.addActionListener(e -> System.out.println("Action: Draw Area"));
        uploadImgBtn.addActionListener(e -> System.out.println("Action: Upload Image"));

        // Spatial Search Section (Titled Border)
        JPanel spatialPanel = new JPanel();
        spatialPanel.setLayout(new BoxLayout(spatialPanel, BoxLayout.Y_AXIS));
        spatialPanel.setBorder(BorderFactory.createTitledBorder("Spatial Search"));
        spatialPanel.add(drawAreaBtn);

        // Multimedia Search Section (Titled Border)
        JPanel multimediaPanel = new JPanel();
        multimediaPanel.setLayout(new BoxLayout(multimediaPanel, BoxLayout.Y_AXIS));
        multimediaPanel.setBorder(BorderFactory.createTitledBorder("Multimedia Search"));
        multimediaPanel.add(uploadImgBtn);

        JPanel searchControlsWrapper = new JPanel(new GridLayout(2, 1)); // 2 rows, 1 column
        searchControlsWrapper.setOpaque(false); // Make it transparent so parent background shows
        searchControlsWrapper.add(spatialPanel);
        searchControlsWrapper.add(multimediaPanel);

        // Results Table
        String[] columns = {"Name", "Price", "Area", "Beds"};
        Object[][] data = {
            {"Sunset Hts", "$1,500", "358m²", "2"},
            {"Maple View", "$1,500", "358m²", "2"},
            {"City Loft", "$1,500", "358m²", "2"},
            {"Bay Area", "$1,500", "358m²", "2"}
        };
        JTable table = new JTable(new DefaultTableModel(data, columns));
        table.setFillsViewportHeight(true);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(280, 150));

        // TABLE ROW ACTION LISTENER
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                System.out.println("Table Row Selected: " + table.getSelectedRow());
            }
        });

        // Analysis Section
        JPanel analysisPanel = new JPanel(new GridLayout(3, 1));
        analysisPanel.setBorder(BorderFactory.createTitledBorder("Analysis"));
        analysisPanel.add(new JLabel("Ave. Price: $1,500"));
        analysisPanel.add(new JLabel("Density: High"));

        // Adding Components with spacing
        panel.add(searchField);
        panel.add(priceLabel);
        panel.add(priceSlider);
        panel.add(searchControlsWrapper);
        panel.add(new JLabel("Results"));
        panel.add(tableScroll);
        panel.add(analysisPanel);
        return panel;
    }



    // CENTER PANEL: Custom JMap Class

    private JPanel JMap() {
        // Custom JPanel with paintComponent and needed methods
        JPanel mapPanel = new JPanel();

        mapPanel.setBackground(new Color(220, 235, 220)); // Light greenish/map color
        mapPanel.setBorder(new LineBorder(new Color(200, 200, 200), 1));

        // Zoom Controls
        JPanel zoomPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        zoomPanel.setBounds(10, 10, 40, 80); // Top Left
        JButton zoomIn = new JButton("+");
        JButton zoomOut = new JButton("-");
        zoomPanel.add(zoomIn);
        zoomPanel.add(zoomOut);
        mapPanel.add(zoomPanel);

        // Add Button (Bottom Center)
        JButton addBtn = new JButton("Add");
        addBtn.setForeground(Color.WHITE);
        addBtn.setBounds(250, 650, 100, 40); // Approximate position
        mapPanel.add(addBtn);

        // MAP ACTION LISTENERS
        zoomIn.addActionListener(e -> System.out.println("Map: Zoom In"));
        zoomOut.addActionListener(e -> System.out.println("Map: Zoom Out"));
        addBtn.addActionListener(e -> System.out.println("Map: Add Property Clicked"));

        // Mock Mouse Listener for clicking map
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Map Clicked at: " + e.getX() + ", " + e.getY());
            }
        });

        return mapPanel;
    }

 
    // RIGHT PANEL: Details, Images, Calc

    private JPanel RightDetailPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Image Carousel
        JPanel imgPanel = new JPanel(new BorderLayout());
        imgPanel.setBackground(Color.LIGHT_GRAY);
        imgPanel.setPreferredSize(new Dimension(290, 180));
        imgPanel.setMaximumSize(new Dimension(290, 180));
        JLabel imgLabel = new JLabel("Image Placeholder", SwingConstants.CENTER);
        imgPanel.add(imgLabel, BorderLayout.CENTER);

        // Carousel Controls
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        imgPanel.add(prev, BorderLayout.WEST);
        imgPanel.add(next, BorderLayout.EAST);

        // CAROUSEL ACTION LISTENERS
        prev.addActionListener(e -> System.out.println("Carousel: Previous"));
        next.addActionListener(e -> System.out.println("Carousel: Next"));

        // Details Text
        JLabel buildingName = new JLabel("Sunset Heights - 4B");
        buildingName.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextArea detailsArea = new JTextArea(
            "Price: $2,200/mo\n" +
            "Address: 123 Maple Ave, Cityville\n" +
            "Area: 358m²\n" +
            "Neighborhood: Downtown\n" +
            "No. of Bed: 2\n\n" +
            "Description: Spacious unit with great views, modern amenities."
        );
        detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setEditable(false);
        

        // --- MODIFIED SECTION: Nearby Calculator ---
        JPanel calcPanel = new JPanel(new GridBagLayout());
        calcPanel.setBorder(BorderFactory.createTitledBorder("Nearby Analysis"));
        

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);

        gbc.gridx = 0; gbc.gridy = 0;
        calcPanel.add(new JLabel("Target Point:"), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JTextField targetField = new JTextField("Metro Station");
        calcPanel.add(targetField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JButton calculateBtn = new JButton("Calculate Distances");
        calculateBtn.setBackground(new Color(220, 220, 220));
        calcPanel.add(calculateBtn, gbc);

        // CALCULATE ACTION LISTENER
        calculateBtn.addActionListener(e -> {
            String target = targetField.getText();
            System.out.println("Calculating distance to: " + target);
            JOptionPane.showMessageDialog(panel, "Distance to " + target + ": 350m");
        });
        // -------------------------------------------

        // Footer Buttons (Edit/Delete)
        JPanel footer = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");

        editBtn.setForeground(Color.WHITE);
        deleteBtn.setBackground(new Color(255, 200, 200)); // Light red
        deleteBtn.setForeground(Color.RED);

        footer.add(editBtn);
        footer.add(deleteBtn);

        // FOOTER ACTION LISTENERS
        editBtn.addActionListener(e -> System.out.println("Action: Edit Property"));
        deleteBtn.addActionListener(e -> System.out.println("Action: Delete Property"));

        // Assembly
        panel.add(imgPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(buildingName);
        panel.add(Box.createVerticalStrut(10));
        panel.add(detailsArea);
        panel.add(Box.createVerticalStrut(15));
        panel.add(calcPanel);
        panel.add(Box.createVerticalGlue()); // Push footer to bottom
        panel.add(footer);

        return panel;
    }
}