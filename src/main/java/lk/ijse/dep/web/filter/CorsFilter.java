package lk.ijse.dep.web.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class CorsFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        System.out.println("incoming");
        res.setHeader("Access-Control-Allow-Origin","http://localhost:4200");
        res.setHeader("Access-Control-Allow-Methods","GET, PUT, POST, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers","Content-Type");
        chain.doFilter(req,res);
    }
}
