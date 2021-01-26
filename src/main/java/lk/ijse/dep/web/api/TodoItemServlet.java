package lk.ijse.dep.web.api;

import lk.ijse.dep.web.dto.TodoItemDTO;
import org.apache.commons.dbcp2.BasicDataSource;
import lk.ijse.dep.web.util.Priority;
import lk.ijse.dep.web.util.Status;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "TodoItemServlet", value = "/api/v1/items/*")
public class TodoItemServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        Jsonb jsonb = JsonbBuilder.create();
        if (request.getPathInfo() == null || request.getPathInfo().equals("/")) {
            try (Connection connection = cp.getConnection()) {
//                request.setAttribute("user", "admin");
                PreparedStatement pstm = connection.
                        prepareStatement("SELECT * FROM todo_item WHERE username = ?");
                pstm.setObject(1, request.getAttribute("user"));
                ResultSet rst = pstm.executeQuery();
                List<TodoItemDTO> items = new ArrayList<>();
                while (rst.next()) {
                    items.add(new TodoItemDTO(rst.getInt("id"),
                            rst.getString("text"),
                            Priority.valueOf(rst.getString("priority")),
                            Status.valueOf(rst.getString("status")),
                            rst.getString("username")));
                }
                response.setContentType("application/json");
                response.getWriter().println(jsonb.toJson(items));
            } catch (SQLException | JsonbException throwables) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                throwables.printStackTrace();
            }
        } else {
            try (Connection connection = cp.getConnection() ) {
                int id = Integer.parseInt(request.getPathInfo().replace("/", ""));
//                request.setAttribute("user","admin");
                PreparedStatement pstm = connection.
                        prepareStatement("SELECT * FROM todo_item WHERE id=? AND username=?");
                pstm.setObject(1, id);
                pstm.setObject(2, request.getAttribute("user"));
                ResultSet rst = pstm.executeQuery();
                if (!rst.next()){
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }else{
                    response.setContentType("application/json");
                    TodoItemDTO item = new TodoItemDTO(rst.getInt("id"),
                            rst.getString("text"),
                            Priority.valueOf(rst.getString("priority")),
                            Status.valueOf(rst.getString("status")),
                            rst.getString("username"));
                    response.getWriter().println(jsonb.toJson(item));
                }
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (SQLException throwables) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                throwables.printStackTrace();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        try {
            TodoItemDTO item = jsonb.fromJson(request.getReader(), TodoItemDTO.class);
            if (item.getId() != null || item.getText() == null || item.getUsername() == null ||
                    item.getText().trim().isEmpty() || item.getUsername().trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
            try (Connection connection = cp.getConnection()) {
                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM `user` WHERE username = ?");
                pstm.setObject(1, item.getUsername());
                if (!pstm.executeQuery().next()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("text/plain");
                    response.getWriter().println("Invalid user");
                    return;
                }
                pstm = connection.prepareStatement("INSERT INTO todo_item (`text`, `priority`, `status`, `username`) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                pstm.setObject(1, item.getText());
                pstm.setObject(2, item.getPriority().toString());
                pstm.setObject(3, item.getStatus().toString());
                pstm.setObject(4, item.getUsername());
                if (pstm.executeUpdate() > 0) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    ResultSet generatedKeys = pstm.getGeneratedKeys();
                    generatedKeys.next();
                    int generatedId = generatedKeys.getInt(1);
                    item.setId(generatedId);
                    response.setContentType("application/json");
                    response.getWriter().println(jsonb.toJson(item));
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        } catch (JsonbException | SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        try {
            TodoItemDTO item = jsonb.fromJson(req.getReader(), TodoItemDTO.class);
            if (item.getId() == null || item.getText() == null || item.getUsername() == null ||
                    item.getText().trim().isEmpty() || item.getUsername().trim().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
            try (Connection connection = cp.getConnection()) {
                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM `user` WHERE username = ?");
                pstm.setObject(1, item.getUsername());
                if (!pstm.executeQuery().next()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.setContentType("text/plain");
                    resp.getWriter().println("Invalid user");
                    return;
                }
                pstm = connection.prepareStatement("UPDATE todo_item SET `text`=?, `priority`=?, `status`=? WHERE id=? AND username=?");
                pstm.setObject(1, item.getText());
                pstm.setObject(2, item.getPriority().toString());
                pstm.setObject(3, item.getStatus().toString());
                pstm.setObject(4, item.getId());
                pstm.setObject(5, item.getUsername());
                if (pstm.executeUpdate() > 0) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    resp.setContentType("application/json");
                    resp.getWriter().println(jsonb.toJson(true));
                } else {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        } catch (JsonbException | SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        Jsonb jsonb = JsonbBuilder.create();
        String id = req.getHeader("Id");
        try(Connection connection = cp.getConnection()){
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM todo_item WHERE id=?");
            pstm.setObject(1,id);
            if(pstm.executeUpdate()>0){
                resp.setContentType("application/json");
                resp.getWriter().println(jsonb.toJson(true));
            }else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (SQLException e){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
