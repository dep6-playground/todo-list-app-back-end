package lk.ijse.dep.web.api;

import dto.UserDTO;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet(name = "UserServlet", urlPatterns = "/api/v1/users/*")
public class UserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        try {
            UserDTO userDTO = jsonb.fromJson(request.getReader(), UserDTO.class);
            if (userDTO.getUsername() == null || userDTO.getPassword() == null || userDTO.getUsername().trim().isEmpty() || userDTO.getPassword().trim().isEmpty()){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
            try (Connection connection = cp.getConnection()) {
                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM `user` WHERE username=?");
                pstm.setObject(1, userDTO.getUsername());
                if (pstm.executeQuery().next()){
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("User already exists");
                    return;
                }
                pstm = connection.prepareStatement("INSERT INTO `user` VALUES (?,?)");
                pstm.setObject(1, userDTO.getUsername());
                String sha256Hex = DigestUtils.sha256Hex(userDTO.getPassword());
                pstm.setObject(2, sha256Hex);
                if (pstm.executeUpdate()> 0){
                    response.setStatus(HttpServletResponse.SC_CREATED);
                }else{
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } catch (SQLException throwables) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                throwables.printStackTrace();
            }

        }catch (JsonbException exp){
            exp.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
