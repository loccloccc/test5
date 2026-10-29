
package com.example.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * UDP Client: gửi yêu cầu tìm kiếm đến MenuServer và nhận phản hồi.
 * Client dùng UDP để giao tiếp mạng, thay vì truy vấn database trực tiếp.
 */
public class MenuClient {

    // Địa chỉ máy chạy Server; localhost phù hợp khi Client và Server cùng máy.
    private static final String SERVER_HOST = "localhost";
    // Phải trùng với cổng mà MenuServer đang lắng nghe.
    private static final int SERVER_PORT = 9876;
    // Thời gian chờ phản hồi tối đa: 5000 mili giây = 5 giây.
    private static final int TIMEOUT = 5000;

    // Gửi chuỗi yêu cầu theo giao thức đã thống nhất và trả về chuỗi phản hồi.
    // IOException được báo cho giao diện nếu gửi/nhận mạng gặp sự cố.
    public String guiYeuCau(String message) throws IOException {
        InetAddress address = InetAddress.getByName(SERVER_HOST);

        // DatagramSocket là điểm gửi/nhận gói UDP; try-with-resources tự đóng socket.
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT);

            // Mã hóa nội dung thành UTF-8 để truyền được tiếng Việt.
            byte[] requestData = message.getBytes(StandardCharsets.UTF_8);

            DatagramPacket request = new DatagramPacket(
                    requestData,
                    requestData.length,
                    address,
                    SERVER_PORT
            );

            // Gửi gói yêu cầu đến địa chỉ và cổng của Server.
            socket.send(request);

            // Bộ đệm để chứa dữ liệu phản hồi nhận từ Server.
            byte[] buffer = new byte[8192];

            DatagramPacket response = new DatagramPacket(
                    buffer,
                    buffer.length
            );

            // Chờ nhận gói phản hồi; nếu quá 5 giây sẽ ném SocketTimeoutException.
            try {
                socket.receive(response);
            } catch (SocketTimeoutException e) {
                throw new SocketTimeoutException(
                        "Server khong phan hoi sau 5 giay."
                );
            }

            // Chuyển dữ liệu byte nhận được thành chuỗi UTF-8 để GUI phân tích.
            return new String(
                    response.getData(),
                    response.getOffset(),
                    response.getLength(),
                    StandardCharsets.UTF_8
            );
        }
    }
}
