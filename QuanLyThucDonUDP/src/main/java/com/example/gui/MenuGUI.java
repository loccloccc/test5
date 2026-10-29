
package com.example.gui;

import com.example.client.MenuClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Giao diện chính thức (Java Swing) để tìm kiếm thực đơn.
 * Người dùng nhập điều kiện, GUI gửi yêu cầu qua MenuClient và hiển thị kết quả trong bảng.
 */
public class MenuGUI extends JFrame {

    // Các ô nhập tiêu chí tìm kiếm; người dùng có thể bỏ trống tiêu chí không cần lọc.
    private final JTextField txtTenMon = new JTextField(15);
    private final JTextField txtGiaTu = new JTextField(10);
    private final JTextField txtGiaDen = new JTextField(10);
    private final JTextField txtThoiGian = new JTextField(10);

    // Model lưu các dòng dữ liệu hiển thị trong JTable.
    // Tắt sửa trực tiếp để người dùng không vô tình thay đổi kết quả truy vấn.
    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                            "Mã món", "Tên món", "Giá (VNĐ)",
                            "Thời gian phục vụ"
                    }, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    // Bảng trình bày danh sách món ăn server trả về.
    private final JTable table = new JTable(tableModel);
    private final JButton btnTimKiem = new JButton("Tìm kiếm");

    // Dùng UDP Client để giao tiếp với server, không truy vấn database trực tiếp từ GUI.
    private final MenuClient client = new MenuClient();

    // Thiết lập cửa sổ, kích thước, hành vi đóng và các thành phần giao diện.
    public MenuGUI() {
        setTitle("HỆ THỐNG QUẢN LÝ THỰC ĐƠN");
        setSize(850, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        khoiTaoGiaoDien();
        btnTimKiem.addActionListener(e -> timKiem());
    }

    // Tạo bố cục gồm thông tin sinh viên, các ô tìm kiếm và bảng kết quả.
    private void khoiTaoGiaoDien() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                "Điều kiện tìm kiếm"
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        themTruong(form, gbc, 0, 0, "Tên món ăn:", txtTenMon);
        themTruong(form, gbc, 0, 1, "Giá từ:", txtGiaTu);
        themTruong(form, gbc, 2, 1, "Giá đến:", txtGiaDen);
        themTruong(form, gbc, 0, 2,
                "Thời gian phục vụ:", txtThoiGian);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        form.add(btnTimKiem, gbc);

        JLabel lblSinhVien = new JLabel(
                "Sinh viên thực hiện: [MSSV] - [Họ và tên]"
        );
        lblSinhVien.setBorder(
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );

        JPanel north = new JPanel(new BorderLayout());
        north.add(lblSinhVien, BorderLayout.NORTH);
        north.add(form, BorderLayout.CENTER);

        table.setRowHeight(25);
        table.setAutoCreateRowSorter(true);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    // Hàm dùng lại để đặt nhãn và ô nhập vào đúng vị trí trong form.
    private void themTruong(
            JPanel panel,
            GridBagConstraints gbc,
            int x,
            int y,
            String label,
            JTextField field
    ) {
        gbc.gridwidth = 1;
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = x + 1;
        panel.add(field, gbc);
    }

    // Đọc điều kiện, kiểm tra dữ liệu rồi gửi yêu cầu tìm món sang server.
    private void timKiem() {
        String tenMon = txtTenMon.getText().trim();
        String giaTu = txtGiaTu.getText().trim();
        String giaDen = txtGiaDen.getText().trim();
        String thoiGian = txtThoiGian.getText().trim();

        // Không gửi yêu cầu rỗng hoàn toàn vì không có tiêu chí để tìm kiếm.
        if (tenMon.isEmpty()
                && giaTu.isEmpty()
                && giaDen.isEmpty()
                && thoiGian.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập ít nhất một điều kiện tìm kiếm.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        BigDecimal min = null;
        BigDecimal max = null;

        // Kiểm tra định dạng và tính hợp lệ của khoảng giá trước khi gửi yêu cầu.
        try {
            if (!giaTu.isEmpty()) {
                min = new BigDecimal(giaTu);
                if (min.signum() < 0) {
                    throw new NumberFormatException();
                }
            }

            if (!giaDen.isEmpty()) {
                max = new BigDecimal(giaDen);
                if (max.signum() < 0) {
                    throw new NumberFormatException();
                }
            }

            if (min != null && max != null
                    && min.compareTo(max) > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Giá từ không được lớn hơn giá đến.",
                        "Dữ liệu không hợp lệ",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Giá phải là số không âm hợp lệ.",
                    "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Tạm khóa nút để tránh gửi nhiều yêu cầu cùng lúc; xóa kết quả cũ.
        btnTimKiem.setEnabled(false);
        tableModel.setRowCount(0);

        // Chạy yêu cầu mạng ở luồng nền để giao diện không bị treo khi chờ server.
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            // Tạo thông điệp theo giao thức TIM_MON|tên|giá_từ|giá_đến|thời_gian.
            protected String doInBackground() throws IOException {
                String request = "TIM_MON|"
                        + tenMon + "|"
                        + giaTu + "|"
                        + giaDen + "|"
                        + thoiGian;

                return client.guiYeuCau(request);
            }

            @Override
            // Chạy lại trên luồng giao diện sau khi yêu cầu mạng kết thúc.
            protected void done() {
                btnTimKiem.setEnabled(true);

                try {
                    hienThiKetQua(get());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            MenuGUI.this,
                            "Không thể tìm kiếm. Hãy kiểm tra "
                                    + "UDP Server đang chạy.\n"
                                    + e.getMessage(),
                            "Lỗi kết nối",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    // Đọc tiền tố phản hồi từ server: NOT_FOUND, ERROR hoặc OK và cập nhật GUI.
    private void hienThiKetQua(String response) {
        if (response.startsWith("NOT_FOUND|")) {
            JOptionPane.showMessageDialog(
                    this,
                    response.substring("NOT_FOUND|".length()),
                    "Không tìm thấy kết quả",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        if (response.startsWith("ERROR|")) {
            JOptionPane.showMessageDialog(
                    this,
                    response.substring("ERROR|".length()),
                    "Lỗi yêu cầu",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (response.equals("OK")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không tìm thấy món ăn phù hợp.",
                    "Kết quả tìm kiếm",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        if (!response.startsWith("OK|")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Phản hồi từ server không hợp lệ.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Bỏ tiền tố "OK|" rồi tách từng bản ghi và từng trường theo giao thức.
        String data = response.substring(3);
        String[] records = data.split("\\|");

        for (String record : records) {
            String[] fields = record.split(";", -1);

            if (fields.length >= 4) {
                tableModel.addRow(new Object[]{
                        fields[0],
                        fields[1],
                        fields[2],
                        fields[3]
                });
            }
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không tìm thấy món ăn phù hợp.",
                    "Kết quả tìm kiếm",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    // Khởi tạo giao diện trên Event Dispatch Thread theo khuyến nghị của Swing.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MenuGUI gui = new MenuGUI();
            gui.setVisible(true);
        });
    }
}
