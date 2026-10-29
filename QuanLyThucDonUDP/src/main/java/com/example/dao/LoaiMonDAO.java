
package com.example.dao;

import com.example.model.LoaiMon;
import com.example.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) truy cập bảng loai_mon.
 * Lớp này gom câu lệnh SQL tìm loại món ra khỏi phần giao diện và xử lý UDP.
 */
public class LoaiMonDAO {

    // Tìm loại món có tên chứa từ khóa, không yêu cầu khớp toàn bộ tên.
    // Trả về danh sách rỗng nếu không có bản ghi phù hợp.
    public List<LoaiMon> timTheoTenGanDung(String tenLoai)
            throws SQLException {

        // Danh sách nhận các loại món tìm thấy trong database.
        List<LoaiMon> danhSach = new ArrayList<>();

        // LIKE kết hợp dấu % ở hai đầu để tìm kiếm gần đúng theo tên.
        // Dấu ? được truyền giá trị riêng bằng PreparedStatement để tránh ghép trực tiếp input vào SQL.
        String sql = """
                SELECT id, ten_loai, mo_ta
                FROM loai_mon
                WHERE ten_loai LIKE ?
                ORDER BY ten_loai
                """;

        // Mở kết nối và chuẩn bị câu SQL; tài nguyên tự đóng sau khi ra khỏi try.
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bỏ khoảng trắng đầu/cuối rồi thêm % để tìm chuỗi con.
            ps.setString(1, "%" + tenLoai.trim() + "%");

            // ResultSet chứa các dòng kết quả trả về từ database.
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Chuyển mỗi dòng kết quả SQL thành một đối tượng LoaiMon.
                    LoaiMon loaiMon = new LoaiMon(
                            rs.getInt("id"),
                            rs.getString("ten_loai"),
                            rs.getString("mo_ta")
                    );

                    // Lưu đối tượng vào danh sách kết quả.
                    danhSach.add(loaiMon);
                }
            }
        }

        // Trả kết quả về cho lớp gọi; nếu không có dữ liệu, danh sách vẫn rỗng.
        return danhSach;
    }
}
