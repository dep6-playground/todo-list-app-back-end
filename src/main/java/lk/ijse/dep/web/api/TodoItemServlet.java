package lk.ijse.dep.web.api;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "TodoItemServlet", value = "/api/v1/items/*")
public class TodoItemServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try {
            Connection connection = cp.getConnection();
            System.out.println(connection);
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
