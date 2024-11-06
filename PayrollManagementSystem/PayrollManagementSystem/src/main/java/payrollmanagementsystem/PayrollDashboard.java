package payrollmanagementsystem;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/*
 Payroll Dashboard class represents the main dashboard of the Payroll Management System.
 It includes functionality for loading, saving, and processing payroll data.
 */

public class PayrollDashboard extends javax.swing.JFrame { // class 1

    // Debug mode flag
    public boolean debug = false;

    // Default table models for rates, payroll, and processed data
    private final DefaultTableModel RatesModel;
    private final DefaultTableModel PayrollModel;
    private final DefaultTableModel ProcessedModel;

    // Default CSV file paths
    final private String defaultCSVPathRate = "Department_Rates_File.csv";
    final private String defaultCSVPathPayroll = "Employee_Payroll_File.csv";
    final private String defaultCSVPathProcessed = "Processed_Payroll_File.csv";

    // File names for saving and loading
    private String fileName;
    private String fileName2;

    /**
     * Creates a new instance of PayrollDashboard.
     * Initializes default table models, checks and creates necessary CSV files, and
     * loads default data.
     */
    public PayrollDashboard() {
        RatesModel = new DefaultTableModel();
        PayrollModel = new DefaultTableModel();
        ProcessedModel = new DefaultTableModel();
        checkAndCreateFiles(); // Check and create necessary CSV files
        loadDefaultTable(); // Load default data into tables
        initComponents(); // Initialize GUI components
    }

    /**
     * Checks and creates necessary CSV files if they do not exist.
     */
    private void checkAndCreateFiles() {
        checkAndCreateFile(defaultCSVPathRate, "Dept. Code,Dept. Name,Regular Rate $,Overtime Rate $");
        checkAndCreateFile(defaultCSVPathPayroll, "ID. No,First Name,Last Name,Dept. Code,Position,Hours Worked");
    }

    /**
     * Checks if a file exists; if not, creates the file with the given header.
     */
    private static void checkAndCreateFile(String filePath, String header) {
        File file = new File(filePath);

        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(header);
                System.out.println("File created: " + filePath);
            } catch (IOException e) {
                System.err.println("Error creating file: " + filePath);
            }
        } else {
            System.out.println("File already exists: " + filePath);
        }
    }

    /**
     * Loads default data from CSV files into default table models.
     */
    private void loadDefaultTable() {
        File defaultCSVRates = new File(defaultCSVPathRate);
        File defaultCSVPayroll = new File(defaultCSVPathPayroll);
        File defaultCSVProcessed = new File(defaultCSVPathProcessed);
        loadCSVFile(defaultCSVRates, RatesModel);
        loadCSVFile(defaultCSVPayroll, PayrollModel);
    }

    /**
     * Loads data from a CSV file into the specified DefaultTableModel.
     */
    private void loadCSVFile(File file, DefaultTableModel DefRatesModel) {
        DefRatesModel.setColumnCount(0); // Clear previous columns
        DefRatesModel.setRowCount(0); // Clear previous data rows

        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextLine()) {
                String[] tableNames = scanner.nextLine().split(",");
                for (String tableName : tableNames) {
                    DefRatesModel.addColumn(tableName);
                    if (debug)
                        System.out.println(tableName);
                }
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split(",");
                DefRatesModel.addRow(data);
                if (debug)
                    System.out.println("->" + line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading the CSV file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Saves data from the specified DefaultTableModel into a CSV file.
     */
    private void saveCSVFile(File file, DefaultTableModel tableModel) {
        try (PrintWriter writer = new PrintWriter(file)) {
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                writer.print(tableModel.getColumnName(i));
                if (i < tableModel.getColumnCount() - 1) {
                    writer.print(",");
                }
            }
            writer.println();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    if (tableModel.getValueAt(i, j) != null) {
                        writer.print(tableModel.getValueAt(i, j));
                    } else {
                        writer.print("");
                    }
                    if (j < tableModel.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
            JOptionPane.showMessageDialog(this, "Updated Sucessfully!");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving the CSV file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generates a new CSV file containing processed payroll data.
     */
    public void generateCSV(DefaultTableModel tableModel1, DefaultTableModel tableModel2) {
        try {
            // Define the headers for the new CSV file
            String[] newHeaders = { "ID. No", "First Name", "Last Name", "Dept. Code", "Position", "Hours Worked",
                    "Regular Pay", "Overtime Pay", "Gross Pay" };

            // Create a new CSV file
            File processedFile = new File("Processed_Payroll_File.csv");
            try (FileWriter writer = new FileWriter(processedFile)) {
                writeHeaders(writer, newHeaders);

                // Iterate through the rows of tableModel2 to calculate and write the values
                for (int row = 0; row < tableModel2.getRowCount(); row++) {
                    writeRow(writer, tableModel2, row);

                    // Calculate Regular Pay, Overtime Pay, and Gross Pay
                    calculateAndWritePay(writer, tableModel1, tableModel2, row);
                }
            }

            displayConfirmationMessage(processedFile);

            if (debug)
                System.out.println("CSV file generated successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes headers to the CSV file.
     */
    private void writeHeaders(FileWriter writer, String[] headers) throws IOException {
        for (int i = 0; i < headers.length; i++) {
            writer.append(headers[i]);
            if (i < headers.length - 1) {
                writer.append(",");
            }
        }
        writer.append("\n");
    }

    /**
     * Writes a row of data to the CSV file.
     */
    private void writeRow(FileWriter writer, DefaultTableModel tableModel, int row) throws IOException {
        for (int col = 0; col < tableModel.getColumnCount(); col++) {
            Object cellValue = tableModel.getValueAt(row, col);

            if (cellValue == null || cellValue.toString().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Missing or empty value at:\n"
                        + "Row: " + row + "\nColumn: " + col);
                break;
            }

            writer.append(cellValue.toString());

            if (col < tableModel.getColumnCount() - 1) {
                writer.append(",");
            }
        }
    }

    /**
     * Calculates and writes Regular Pay, Overtime Pay, and Gross Pay to the CSV
     * file.
     */
    private void calculateAndWritePay(FileWriter writer, DefaultTableModel tableModel1, DefaultTableModel tableModel2,
            int row) throws IOException {
        try {
            String departmentCode = tableModel2.getValueAt(row, 3).toString();
            int departmentRowIndex = findDepartmentRowIndex(tableModel1, departmentCode);

            if (departmentRowIndex != -1) {
                // Calculate Regular Pay
                double regularPay = Double.parseDouble(tableModel2.getValueAt(row, 5).toString())
                        * Double.parseDouble(tableModel1.getValueAt(departmentRowIndex, 2).toString());
                writer.append(",").append(String.valueOf(regularPay));

                // Calculate Overtime Pay
                double hoursWorked = Double.parseDouble(tableModel2.getValueAt(row, 5).toString());
                double overtimeRate = Double.parseDouble(tableModel1.getValueAt(departmentRowIndex, 3).toString());
                double overtimePay = (hoursWorked > 40) ? (hoursWorked - 40) * overtimeRate : 0;
                writer.append(",").append(String.valueOf(overtimePay));

                // Calculate Gross Pay
                double grossPay = regularPay + overtimePay;

                writer.append(",").append(String.valueOf(grossPay));
                writer.append("\n");
            } else {
                System.err.println("Department code not found in tableModel1: " + departmentCode);
            }
        } catch (NullPointerException | NumberFormatException e) {
            // Handle exceptions if needed
            e.printStackTrace();
        }
    }

    /**
     * Finds the row index of a department in the given DefaultTableModel based on
     * the department code.
     * 
     * @param model          The DefaultTableModel to search.
     * @param departmentCode The department code to find.
     * @return The index of the row if found, otherwise -1.
     */
    private int findDepartmentRowIndex(DefaultTableModel model, String departmentCode) {
        for (int row = 0; row < model.getRowCount(); row++) {
            if (departmentCode.equals(model.getValueAt(row, 0).toString())) {
                return row; // Found the row
            }
        }
        return -1; // Not found
    }

    /**
     * Displays a confirmation message after successfully generating a CSV file.
     * Allows the user to choose whether to open the folder containing the file.
     * 
     * @param processedFile The generated CSV file.
     */
    private void displayConfirmationMessage(File processedFile) {
        Object[] options = { "OK", "Show Folder" };
        int choice = JOptionPane.showOptionDialog(
                null, // Use null to center the dialog on the screen
                "CSV file generated successfully.\n"
                        + processedFile.getAbsolutePath(),
                "Want to open folder?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0] // Default button (OK)
        );
        switch (choice) {
            case JOptionPane.YES_OPTION:
                break;
            case JOptionPane.NO_OPTION:
                openFolder(processedFile.getAbsolutePath());
                break;
            default:
                break;
        }
    }

    /**
     * Adds a new empty row to the specified DefaultTableModel.
     * 
     * @param Model The DefaultTableModel to which a new row will be added.
     */
    private void addNewRow(DefaultTableModel Model) {
        Object[] emptyRow = new Object[Model.getColumnCount()];
        Model.addRow(emptyRow);
    }

    /**
     * Opens the folder containing the specified file using the system's default
     * file manager.
     * 
     * @param filePath The path of the file whose folder will be opened.
     */
    private void openFolder(String filePath) {
        if (Desktop.isDesktopSupported()) {
            try {
                File file = new File(filePath);
                File folder = file.getParentFile();
                if (folder != null && folder.isDirectory()) {
                    Desktop.getDesktop().open(folder);
                } else {
                    if (debug)
                        System.out.println("Folder not found or is not a directory.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (debug)
                System.out.println("Desktop is not supported on this platform.");
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        DashTitle = new javax.swing.JLabel();
        SSNPanel = new javax.swing.JTabbedPane();
        DeptRatesPanel = new javax.swing.JPanel();
        DeptRatesPanelTable = new javax.swing.JPanel();
        ScrollPaneDeptRates = new javax.swing.JScrollPane();
        RatesTable = new javax.swing.JTable();
        DeptRatesButtonPanel = new javax.swing.JPanel();
        fileChooseButton = new javax.swing.JButton();
        ButtonAddRow = new javax.swing.JButton();
        ButtonDeleteRecord = new javax.swing.JButton();
        ButtonUpdateRecord = new javax.swing.JButton();
        selectedFileLabel = new javax.swing.JLabel();
        ButtonSaveAs1 = new javax.swing.JButton();
        PanelEmpData = new javax.swing.JPanel();
        EmpDataPanelTable = new javax.swing.JPanel();
        ScrollPaneEmpData = new javax.swing.JScrollPane();
        EmpDataTable = new javax.swing.JTable();
        EmpDataButtonPanel = new javax.swing.JPanel();
        fileChooseButton2 = new javax.swing.JButton();
        ButtonAddRow2 = new javax.swing.JButton();
        ButtonDeleteRecord2 = new javax.swing.JButton();
        ButtonUpdateRecord2 = new javax.swing.JButton();
        selectedFileLabel2 = new javax.swing.JLabel();
        ButtonDuplicate = new javax.swing.JButton();
        ProcessedDataPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        ButtonGenerate = new javax.swing.JButton();
        ProcessedScrollPane = new javax.swing.JScrollPane();
        ProcessedTable = new javax.swing.JTable();
        ExitButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        DashTitle.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 24)); // NOI18N
        DashTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        DashTitle.setText("SSN Payroll Management System");

        SSNPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        RatesTable.setModel(RatesModel);
        ScrollPaneDeptRates.setViewportView(RatesTable);

        javax.swing.GroupLayout DeptRatesPanelTableLayout = new javax.swing.GroupLayout(DeptRatesPanelTable);
        DeptRatesPanelTable.setLayout(DeptRatesPanelTableLayout);
        DeptRatesPanelTableLayout.setHorizontalGroup(
                DeptRatesPanelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(ScrollPaneDeptRates, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE));
        DeptRatesPanelTableLayout.setVerticalGroup(
                DeptRatesPanelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(DeptRatesPanelTableLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(ScrollPaneDeptRates, javax.swing.GroupLayout.DEFAULT_SIZE, 386,
                                        Short.MAX_VALUE)
                                .addContainerGap()));

        fileChooseButton.setText("Choose File");
        fileChooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooseButtonActionPerformed(evt);
            }
        });

        ButtonAddRow.setFont(new java.awt.Font("Segoe UI", Font.PLAIN, 11)); // NOI18N
        ButtonAddRow.setText("Add Row");
        ButtonAddRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonAddRowActionPerformed(evt);
            }
        });

        ButtonDeleteRecord.setFont(new java.awt.Font("Segoe UI", Font.PLAIN, 11)); // NOI18N
        ButtonDeleteRecord.setText("Delete");
        ButtonDeleteRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonDeleteRecordActionPerformed(evt);
            }
        });

        ButtonUpdateRecord.setFont(new java.awt.Font("Segoe UI", Font.PLAIN, 11)); // NOI18N
        ButtonUpdateRecord.setText("Update");
        ButtonUpdateRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonUpdateRecordActionPerformed(evt);
            }
        });

        selectedFileLabel.setText("Loaded: " + fileName);

        ButtonSaveAs1.setFont(new java.awt.Font("Segoe UI", Font.PLAIN, 11)); // NOI18N
        ButtonSaveAs1.setText("Save As");
        ButtonSaveAs1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonSaveAs1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout DeptRatesButtonPanelLayout = new javax.swing.GroupLayout(DeptRatesButtonPanel);
        DeptRatesButtonPanel.setLayout(DeptRatesButtonPanelLayout);
        DeptRatesButtonPanelLayout.setHorizontalGroup(
                DeptRatesButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(DeptRatesButtonPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(DeptRatesButtonPanelLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(DeptRatesButtonPanelLayout.createSequentialGroup()
                                                .addComponent(selectedFileLabel, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        330, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(DeptRatesButtonPanelLayout.createSequentialGroup()
                                                .addComponent(fileChooseButton, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(ButtonAddRow, javax.swing.GroupLayout.PREFERRED_SIZE, 80,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(ButtonSaveAs1, javax.swing.GroupLayout.PREFERRED_SIZE, 80,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(ButtonUpdateRecord,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 80,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(ButtonDeleteRecord,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 80,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))));
        DeptRatesButtonPanelLayout.setVerticalGroup(
                DeptRatesButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, DeptRatesButtonPanelLayout
                                .createSequentialGroup()
                                .addComponent(selectedFileLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(DeptRatesButtonPanelLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(fileChooseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ButtonDeleteRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ButtonUpdateRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ButtonSaveAs1, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ButtonAddRow, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(14, 14, 14)));

        javax.swing.GroupLayout DeptRatesPanelLayout = new javax.swing.GroupLayout(DeptRatesPanel);
        DeptRatesPanel.setLayout(DeptRatesPanelLayout);
        DeptRatesPanelLayout.setHorizontalGroup(
                DeptRatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(DeptRatesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(DeptRatesPanelLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(DeptRatesPanelTable, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(DeptRatesButtonPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap()));
        DeptRatesPanelLayout.setVerticalGroup(
                DeptRatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(DeptRatesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(DeptRatesPanelTable, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(DeptRatesButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));

        SSNPanel.addTab("Department Rates", DeptRatesPanel);

        EmpDataTable.setModel(PayrollModel);
        ScrollPaneEmpData.setViewportView(EmpDataTable);

        javax.swing.GroupLayout EmpDataPanelTableLayout = new javax.swing.GroupLayout(EmpDataPanelTable);
        EmpDataPanelTable.setLayout(EmpDataPanelTableLayout);
        EmpDataPanelTableLayout.setHorizontalGroup(
                EmpDataPanelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(ScrollPaneEmpData, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE));
        EmpDataPanelTableLayout.setVerticalGroup(
                EmpDataPanelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(EmpDataPanelTableLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(ScrollPaneEmpData, javax.swing.GroupLayout.DEFAULT_SIZE, 386,
                                        Short.MAX_VALUE)
                                .addContainerGap()));

        fileChooseButton2.setText("Choose File");
        fileChooseButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooseButton2ActionPerformed(evt);
            }
        });

        ButtonAddRow2.setFont(new java.awt.Font("Segoe UI", Font.PLAIN, 11)); // NOI18N
        ButtonAddRow2.setText("Add Row");
        ButtonAddRow2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonAddRow2ActionPerformed(evt);
            }
        });

        ButtonDeleteRecord2.setFont(new java.awt.Font("Segoe UI", Font.PLAIN, 11)); // NOI18N
        ButtonDeleteRecord2.setText("Delete");
        ButtonDeleteRecord2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonDeleteRecord2ActionPerformed(evt);
            }
        });

        ButtonUpdateRecord2.setFont(new java.awt.Font("Segoe UI", Font.PLAIN, 11)); // NOI18N
        ButtonUpdateRecord2.setText("Update");
        ButtonUpdateRecord2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonUpdateRecord2ActionPerformed(evt);
            }
        });

        selectedFileLabel2.setText("Loaded: " + fileName);

        ButtonDuplicate.setFont(new java.awt.Font("Segoe UI", Font.PLAIN, 11)); // NOI18N
        ButtonDuplicate.setText("Duplicate");
        ButtonDuplicate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonDuplicateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout EmpDataButtonPanelLayout = new javax.swing.GroupLayout(EmpDataButtonPanel);
        EmpDataButtonPanel.setLayout(EmpDataButtonPanelLayout);
        EmpDataButtonPanelLayout.setHorizontalGroup(
                EmpDataButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(EmpDataButtonPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(EmpDataButtonPanelLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(EmpDataButtonPanelLayout.createSequentialGroup()
                                                .addComponent(selectedFileLabel2,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 330,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(EmpDataButtonPanelLayout.createSequentialGroup()
                                                .addComponent(fileChooseButton2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(ButtonAddRow2, javax.swing.GroupLayout.PREFERRED_SIZE, 80,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(ButtonDuplicate, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(ButtonUpdateRecord2,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 80,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(ButtonDeleteRecord2,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 80,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))));
        EmpDataButtonPanelLayout.setVerticalGroup(
                EmpDataButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EmpDataButtonPanelLayout
                                .createSequentialGroup()
                                .addComponent(selectedFileLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(EmpDataButtonPanelLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(fileChooseButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ButtonDeleteRecord2, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ButtonUpdateRecord2, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ButtonDuplicate, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ButtonAddRow2, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(14, 14, 14)));

        javax.swing.GroupLayout PanelEmpDataLayout = new javax.swing.GroupLayout(PanelEmpData);
        PanelEmpData.setLayout(PanelEmpDataLayout);
        PanelEmpDataLayout.setHorizontalGroup(
                PanelEmpDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(PanelEmpDataLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(PanelEmpDataLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(EmpDataPanelTable, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(EmpDataButtonPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap()));
        PanelEmpDataLayout.setVerticalGroup(
                PanelEmpDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(PanelEmpDataLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(EmpDataPanelTable, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(EmpDataButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));

        SSNPanel.addTab("Employee Data", PanelEmpData);

        jLabel1.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Generate Processed File");

        ButtonGenerate.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 18)); // NOI18N
        ButtonGenerate.setText("Generate");
        ButtonGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonGenerateActionPerformed(evt);
            }
        });

        // Set the model for the ProcessedTable to the ProcessedModel
        ProcessedTable.setModel(ProcessedModel);
        // Make the ProcessedTable scrollable within the ProcessedScrollPane
        ProcessedScrollPane.setViewportView(ProcessedTable);



        // Create and configure the layout for the ProcessedDataPanel
        javax.swing.GroupLayout ProcessedDataPanelLayout = new javax.swing.GroupLayout(ProcessedDataPanel);
        ProcessedDataPanel.setLayout(ProcessedDataPanelLayout);
        ProcessedDataPanelLayout.setHorizontalGroup(
                ProcessedDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                ProcessedDataPanelLayout.createSequentialGroup()
                                        .addContainerGap()
                                        // Align components horizontally within the ProcessedDataPanel
                                        .addGroup(ProcessedDataPanelLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                // Make the ProcessedScrollPane take the available width
                                                .addComponent(ProcessedScrollPane)
                                                // Set the jLabel1 alignment and size
                                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                                                // Set the ButtonGenerate size and position
                                                .addComponent(ButtonGenerate, javax.swing.GroupLayout.Alignment.LEADING,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addContainerGap()));
        ProcessedDataPanelLayout.setVerticalGroup(
                ProcessedDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ProcessedDataPanelLayout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                // Set the jLabel1 position and size
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                // Set the ButtonGenerate position and size
                                .addComponent(ButtonGenerate)
                                .addGap(18, 18, 18)
                                // Make the ProcessedScrollPane take the remaining vertical space
                                .addComponent(ProcessedScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 365,
                                        Short.MAX_VALUE)
                                .addContainerGap()));

        // Add the ProcessedDataPanel as a tab in the SSNPanel
        SSNPanel.addTab("Processed Data", ProcessedDataPanel);

        // Create and configure the ExitButton
        ExitButton.setBackground(new java.awt.Color(196, 43, 28));
        ExitButton.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 14)); // NOI18N
        ExitButton.setText("Exit");
        // Set the action listener for the ExitButton
        ExitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitButtonActionPerformed(evt);
            }
        });

        // Create and configure the layout for the jPanel1
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                // Set up components within jPanel1
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(SSNPanel)
                                        .addComponent(DashTitle, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                jPanel1Layout.createSequentialGroup()
                                                        .addGap(0, 0, Short.MAX_VALUE)
                                                        // Set up ExitButton with preferred size
                                                        .addComponent(ExitButton,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 77,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                // Set up DashTitle with preferred size
                                .addComponent(DashTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 39,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                // Set up ExitButton
                                .addComponent(ExitButton)
                                .addGap(2, 2, 2)
                                // Set up SSNPanel
                                .addComponent(SSNPanel)
                                .addContainerGap()));

        // Create and configure the layout for the overall frame
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        // Set up jPanel1 within the frame
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        // Set up jPanel1 within the frame
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        pack();
        // Center the frame on the screen
        setLocationRelativeTo(null);
    }

    // Duplicate button action performed when clicked
    private void ButtonDuplicateActionPerformed(java.awt.event.ActionEvent evt) {
        // Get the selected row in EmpDataTable
        int selectedRow = EmpDataTable.getSelectedRow();
        if (selectedRow != -1) { // Check if a row is selected
            Object[] rowData = new Object[PayrollModel.getColumnCount()];

            // Copy data from selected row to rowData
            for (int i = 0; i < PayrollModel.getColumnCount(); i++) {
                rowData[i] = PayrollModel.getValueAt(selectedRow, i);
            }

            // Add a new row with duplicated data to PayrollModel
            PayrollModel.addRow(rowData);
        } else {
            // Show a warning if no row is selected
            JOptionPane.showMessageDialog(this, "Please select a row to duplicate.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // Update button action performed when clicked in EmpDataTable
    private void ButtonUpdateRecord2ActionPerformed(java.awt.event.ActionEvent evt) {
        // Check if a custom file name is provided; if not, use the default file path
        if (fileName2 == null) {
            if (debug)
                System.out.println("Using defaultFilePath for Payroll");
            fileName2 = defaultCSVPathPayroll;
        }

        // Check if the table is not in an editing state
        if (!EmpDataTable.isEditing()) {
            try {
                File selectedFile = new File(fileName2);
                // Save the content of the table to the selected CSV file
                saveCSVFile(selectedFile, PayrollModel);
            } catch (Exception e) {
                // Display an error message if no file was selected
                JOptionPane.showMessageDialog(this, "No file was selected!");
            }
        } else {
            // Display a warning if a cell is still being edited
            JOptionPane.showMessageDialog(this, "Cell is still being edited. Please unselect to update!");
        }
    }

    // Delete button action performed when clicked in EmpDataTable
    private void ButtonDeleteRecord2ActionPerformed(java.awt.event.ActionEvent evt) {
        // Get the selected rows in EmpDataTable
        int[] selectedRows = EmpDataTable.getSelectedRows();

        // Delete rows from PayrollModel
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            PayrollModel.removeRow(selectedRows[i]);
        }
    }

    // Add Row button action performed when clicked in EmpDataTable
    private void ButtonAddRow2ActionPerformed(java.awt.event.ActionEvent evt) {
        // Add a new row to PayrollModel
        addNewRow(PayrollModel);
    }

    // Choose File button action performed when clicked in EmpDataTable
    private void fileChooseButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        // Create a file chooser dialog
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        // Process the selected file
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            fileName2 = selectedFile.getAbsolutePath();
            if (debug)
                System.out.println("PATH: " + fileName2);
            selectedFileLabel2.setText(selectedFile.getName());
            // Load the content of the selected CSV file into PayrollModel
            loadCSVFile(selectedFile, PayrollModel);
        }
    }

    // Save As button action performed when clicked in RatesTable
    private void ButtonSaveAs1ActionPerformed(java.awt.event.ActionEvent evt) {
        // Check if a CSV file is open; if yes, prompt the user to save with a new name
        if (fileName != null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(fileName)); // Set the default file name
            int returnValue = fileChooser.showSaveDialog(this);

            // Process the selected file for saving
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                // Save the content of the table to the selected CSV file
                saveCSVFile(selectedFile, RatesModel);
            }
        } else {
            // Display an error message if no CSV file is open
            JOptionPane.showMessageDialog(this, "Please open a CSV file first.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Update button action performed when clicked in RatesTable
    private void ButtonUpdateRecordActionPerformed(java.awt.event.ActionEvent evt) {
        // Check if a custom file name is provided; if not, use the default file path
        if (fileName == null) {
            if (debug)
                System.out.println("Using defaultFilePath for Rates");
            fileName = defaultCSVPathRate;
        }

        // Check if the table is not in an editing state
        if (!RatesTable.isEditing()) {
            try {
                File selectedFile = new File(fileName);
                // Save the content of the table to the selected CSV file
                saveCSVFile(selectedFile, RatesModel);
            } catch (Exception e) {
                // Display an error message if no file was selected
                JOptionPane.showMessageDialog(this, "No file was selected!");
            }
        } else {
            // Display a warning if a cell is still being edited
            JOptionPane.showMessageDialog(this, "Cell is still being edited. Please unselect to update!");
        }
    }

    // Delete button action performed when clicked in RatesTable
    private void ButtonDeleteRecordActionPerformed(java.awt.event.ActionEvent evt) {
        // Get the selected rows in RatesTable
        int[] selectedRows = RatesTable.getSelectedRows();

        // Delete rows from RatesModel
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            RatesModel.removeRow(selectedRows[i]);
        }
    }

    // Add Row button action performed when clicked in RatesTable
    private void ButtonAddRowActionPerformed(java.awt.event.ActionEvent evt) {
        // Add a new row to RatesModel
        addNewRow(RatesModel);
    }

    // Choose File button action performed when clicked in RatesTable
    private void fileChooseButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // Create a file chooser dialog
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        // Process the selected file
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            fileName = selectedFile.getAbsolutePath();
            if (debug)
                System.out.println("PATH: " + fileName);
            selectedFileLabel.setText(selectedFile.getName());
            // Load the content of the selected CSV file into RatesModel
            loadCSVFile(selectedFile, RatesModel);
        }
    }

    // Generate button action performed when clicked
    private void ButtonGenerateActionPerformed(java.awt.event.ActionEvent evt) {
        // Generate a CSV file using data from RatesModel and PayrollModel
        generateCSV(RatesModel, PayrollModel);
        // Load the processed CSV file into ProcessedModel
        File processedCSV = new File(defaultCSVPathProcessed);
        loadCSVFile(processedCSV, ProcessedModel);
    }

    // Exit button action performed when clicked
    private void ExitButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // Exit the application
        System.exit(0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PayrollDashboard.class.getName()).log(java.util.logging.Level.SEVERE,
                    null, ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PayrollDashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ButtonAddRow;
    private javax.swing.JButton ButtonAddRow2;
    private javax.swing.JButton ButtonDeleteRecord;
    private javax.swing.JButton ButtonDeleteRecord2;
    private javax.swing.JButton ButtonDuplicate;
    private javax.swing.JButton ButtonGenerate;
    private javax.swing.JButton ButtonSaveAs1;
    private javax.swing.JButton ButtonUpdateRecord;
    private javax.swing.JButton ButtonUpdateRecord2;
    private javax.swing.JLabel DashTitle;
    private javax.swing.JPanel DeptRatesButtonPanel;
    private javax.swing.JPanel DeptRatesPanel;
    private javax.swing.JPanel DeptRatesPanelTable;
    private javax.swing.JPanel EmpDataButtonPanel;
    private javax.swing.JPanel EmpDataPanelTable;
    private javax.swing.JTable EmpDataTable;
    private javax.swing.JButton ExitButton;
    private javax.swing.JPanel PanelEmpData;
    private javax.swing.JPanel ProcessedDataPanel;
    private javax.swing.JScrollPane ProcessedScrollPane;
    private javax.swing.JTable ProcessedTable;
    private javax.swing.JTable RatesTable;
    private javax.swing.JTabbedPane SSNPanel;
    private javax.swing.JScrollPane ScrollPaneDeptRates;
    private javax.swing.JScrollPane ScrollPaneEmpData;
    private javax.swing.JButton fileChooseButton;
    private javax.swing.JButton fileChooseButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel selectedFileLabel;
    private javax.swing.JLabel selectedFileLabel2;
    // End of variables declaration//GEN-END:variables
}
