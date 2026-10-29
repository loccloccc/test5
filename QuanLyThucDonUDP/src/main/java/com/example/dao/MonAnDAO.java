
package com.example.dao;

import com.example.config.DBConnection;
import com.example.model.MonAn;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO truy vấn bảng mon_an.
 * Xây dựng điều kiện SQL theo những tiêu chí người dùng đã nhập.
 */
public class MonAnDAO {

    // Tìm món theo tên gần đúng, giá tối thiểu/tối đa và thời gian phục vụ.
    // Tiêu chí nào để trống sẽ không được thêm vào điều kiện truy vấn.
    public List<MonAn> timKiem(
            String tenMon,
            BigDecimal giaTu,
            BigDecimal giaDen,
            String thoiGianPhucVu
    ) throws SQLException {

        // Danh sách các món ăn thỏa mãn tất cả điều kiện được cung cấp.
        List<MonAn> danhSach = new ArrayList<>();

        // StringBuilder cho phép thêm điều kiện SQL một cách linh hoạt.
        // WHERE 1=1 giúp các điều kiện AND có thể nối thêm thuận tiện.
        StringBuilder sql = new StringBuilder("""
                SELECT id, ten_mon, hinh_anh, gia,
                       thoi_gian_phuc_vu, loai_mon_id
                FROM mon_an
                WHERE 1 = 1
                """);

        // Chỉ lọc theo tên khi người dùng có nhập tên món.
        if (tenMon != null && !tenMon.trim().isEmpty()) {
            sql.append(" AND ten_mon LIKE ?");
        }

        // Giá tối thiểu: chỉ lấy món có giá lớn hơn hoặc bằng giá này.
        if (giaTu != null) {
            sql.append(" AND gia >= ?");
        }

        // Giá tối đa: chỉ lấy món có giá nhỏ hơn hoặc bằng giá này.
        if (giaDen != null) {
            sql.append(" AND gia <= ?");
        }

        // Tìm chuỗi thời gian người dùng nhập bên trong trường thời gian phục vụ.
        if (thoiGianPhucVu != null
                && !thoiGianPhucVu.trim().isEmpty()) {
            sql.append(" AND thoi_gian_phuc_vu LIKE ?");
        }

        // Sắp xếp kết quả theo tên món để danh sách dễ theo dõi.
        sql.append(" ORDER BY ten_mon");

        // Kết nối database và tạo PreparedStatement từ câu SQL đã hoàn thiện.
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql.toString())) {

            // Chỉ số tham số bắt đầu từ 1 trong PreparedStatement.
            int index = 1;

            if (tenMon != null && !tenMon.trim().isEmpty()) {
                ps.setString(index++, "%" + tenMon.trim() + "%");
            }

            if (giaTu != null) {
                ps.setBigDecimal(index++, giaTu);
            }

            if (giaDen != null) {
                ps.setBigDecimal(index++, giaDen);
            }

            if (thoiGianPhucVu != null
                    && !thoiGianPhucVu.trim().isEmpty()) {
                ps.setString(
                        index++,
                        "%" + thoiGianPhucVu.trim() + "%"
                );
            }

            // Chạy truy vấn và đọc từng dòng kết quả.
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Chuyển dữ liệu mỗi dòng thành đối tượng MonAn.
                    MonAn monAn = new MonAn(
                            rs.getInt("id"),
                            rs.getString("ten_mon"),
                            rs.getString("hinh_anh"),
                            rs.getBigDecimal("gia"),
                            rs.getString("thoi_gian_phuc_vu"),
                            (Integer) rs.getObject("loai_mon_id")
                    );

                    // Thêm món vào danh sách kết quả.
                    danhSach.add(monAn);
                }
            }
        }

        // Trả danh sách về cho Server; không có kết quả thì trả danh sách rỗng.
        return danhSach;
    }
}
