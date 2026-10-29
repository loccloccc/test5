
package com.example.server;

import com.example.dao.LoaiMonDAO;
import com.example.dao.MonAnDAO;
import com.example.model.LoaiMon;
import com.example.model.MonAn;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * UDP Server của ứng dụng quản lý thực đơn.
 * Nhận yêu cầu từ Client, gọi DAO truy vấn MySQL và gửi kết quả trở lại Client.
 */
public class MenuServer {

    // Cổng UDP Server lắng nghe; phải trùng với cổng cấu hình trong MenuClient.
    private static final int PORT = 9876;

    // DAO thực hiện truy vấn loại món và món ăn trong database.
    private final LoaiMonDAO loaiMonDAO = new LoaiMonDAO();
    private final MonAnDAO monAnDAO = new MonAnDAO();

    // Điểm khởi chạy server khi chọn Run class này trong IntelliJ.
    public static void main(String[] args) {
        new MenuServer().start();
    }

    // Mở socket UDP và liên tục chờ các gói yêu cầu từ client.
    public void start() {
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("UDP Server dang chay tai port " + PORT);

            byte[] buffer = new byte[4096];

            // Vòng lặp giữ server hoạt động để phục vụ nhiều yêu cầu liên tiếp.
            while (true) {
                DatagramPacket request = new DatagramPacket(
                        buffer, buffer.length
                );

                // Chặn tại đây cho đến khi nhận được một gói UDP.
                serverSocket.receive(request);

                // Chuyển dữ liệu gói tin thành chuỗi UTF-8 để phân tích yêu cầu.
                String message = new String(
                        request.getData(),
                        request.getOffset(),
                        request.getLength(),
                        StandardCharsets.UTF_8
                );

                // Chuyển yêu cầu cho hàm xử lý và lấy chuỗi phản hồi.
                String response = xuLyYeuCau(message);

                byte[] responseData = response.getBytes(
                        StandardCharsets.UTF_8
                );

                DatagramPacket reply = new DatagramPacket(
                        responseData,
                        responseData.length,
                        request.getAddress(),
                        request.getPort()
                );

                // Gửi phản hồi về đúng địa chỉ IP và cổng của client đã yêu cầu.
                serverSocket.send(reply);
            }

        } catch (IOException e) {
            System.err.println("Loi UDP Server: " + e.getMessage());
        }
    }

    // Phân tích giao thức dạng ACTION|tham_so và điều phối đến chức năng tương ứng.
    private String xuLyYeuCau(String message) {
        try {
            String[] parts = message.split("\\|", -1);

            // Phần đầu chuỗi xác định thao tác: TIM_LOAI hoặc TIM_MON.
            switch (parts[0]) {
                case "TIM_LOAI":
                    if (parts.length != 2) {
                        return "ERROR|Sai dinh dang yeu cau";
                    }
                    return timLoai(parts[1]);

                case "TIM_MON":
                    if (parts.length != 5) {
                        return "ERROR|Sai dinh dang yeu cau";
                    }
                    return timMon(parts);

                default:
                    return "ERROR|Chuc nang khong hop le";
            }

        } catch (SQLException | NumberFormatException e) {
            return "ERROR|Loi xu ly yeu cau";
        }
    }

    // Xử lý yêu cầu tìm loại món theo tên gần đúng.
    private String timLoai(String tenLoai) throws SQLException {
        List<LoaiMon> danhSach =
                loaiMonDAO.timTheoTenGanDung(tenLoai);

        // "OK" là tiền tố báo truy vấn thành công; dữ liệu từng dòng nối sau dấu |.
        StringBuilder result = new StringBuilder("OK");

        for (LoaiMon loai : danhSach) {
            result.append("|")
                    .append(loai.getId()).append(";")
                    .append(lamSach(loai.getTenLoai())).append(";")
                    .append(lamSach(loai.getMoTa()));
        }

        return result.toString();
    }

    // Đọc điều kiện tìm món từ yêu cầu, kiểm tra khoảng giá rồi gọi DAO.
    private String timMon(String[] parts) throws SQLException {
        String tenMon = parts[1];
        BigDecimal giaTu = parseGia(parts[2]);
        BigDecimal giaDen = parseGia(parts[3]);
        String thoiGian = parts[4];

        // Từ chối điều kiện giá không hợp lệ trước khi truy vấn database.
        if (giaTu != null && giaDen != null
                && giaTu.compareTo(giaDen) > 0) {
            return "ERROR|Gia tu khong duoc lon hon gia den";
        }

        List<MonAn> danhSach = monAnDAO.timKiem(
                tenMon, giaTu, giaDen, thoiGian
        );

        StringBuilder result = new StringBuilder("OK");

        for (MonAn mon : danhSach) {
            result.append("|")
                    .append(mon.getId()).append(";")
                    .append(lamSach(mon.getTenMon())).append(";")
                    .append(mon.getGia()).append(";")
                    .append(lamSach(mon.getThoiGianPhucVu())).append(";")
                    .append(mon.getLoaiMonId() == null
                            ? "" : mon.getLoaiMonId()).append(";")
                    .append(lamSach(mon.getHinhAnh()));
        }

        return result.toString();
    }

    // Chuyển chuỗi giá thành BigDecimal; chuỗi rỗng nghĩa là không lọc theo giá đó.
    private BigDecimal parseGia(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        BigDecimal gia = new BigDecimal(value.trim());

        if (gia.signum() < 0) {
            throw new NumberFormatException("Gia khong hop le");
        }

        return gia;
    }

    // Làm sạch dữ liệu trước khi ghép vào giao thức phân tách bằng | và ;.
    // Thay các ký tự phân cách trong nội dung để không làm hỏng định dạng phản hồi.
    private String lamSach(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("|", "/")
                .replace(";", ",")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
