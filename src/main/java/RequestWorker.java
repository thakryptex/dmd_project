import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.DefaultHelperRegistry;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.js.HandlebarsJs;
import database.DBWorker;
import database.UsersTable;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class RequestWorker {

    public DBWorker dbWorker = new DBWorker();
    public HashMap<String, String> emptyMap = new HashMap<>();
    public String layout = "public/layout.vtl";

    public RequestWorker() {
    }

    public ModelAndView index(Request req, Response res) {
        HashMap<String, Object> model = new HashMap<>();
        model.put("template", "public/index.vtl");
        model.put("login", req.session().attribute("login"));
        return new ModelAndView(model, layout);
    }

    public ModelAndView login(Request req, Response res) {
        HashMap<String, Object> model = new HashMap<>();

        req.session(true).maxInactiveInterval(60*60);
        if (!req.session().attributes().isEmpty())
            model.put("error", req.session().attribute("error"));

        model.put("template", "public/login.vtl");
        return new ModelAndView(model, layout);
    }

    public String authorize(Request req, Response res) {

        String[] args = req.body().split("&");
        String login = args[0].split("=")[1];
        String password = args[1].split("=")[1];

        try {
            UsersTable.findUser(login, password, dbWorker.statement());
        } catch (NoSuchElementException e) {
            req.session().attribute("error", "Wrong login or password. Try again...");
            res.redirect("/login");
        } catch (Exception e) {
            e.printStackTrace();
        }

        req.session().attribute("login", login);
        req.session().attribute("password", password);
        res.redirect("/index");

        return "";
    }

    public ModelAndView register(Request req, Response res) {
        HashMap<String, Object> model = new HashMap<>();
        req.session(true);

        if (!req.session().attributes().isEmpty())
            model.put("error", req.session().attribute("error"));

        model.put("template", "public/register.vtl");
        return new ModelAndView(model, layout);
    }

    public String registration(Request req, Response res) {
        String[] args = req.body().split("&");
        String login = args[0].split("=")[1];
        String password = args[1].split("=")[1];

        try {
            UsersTable.addUser(login, password, dbWorker.statement());
        } catch (SQLException e) {
            req.session().attribute("error", "User with this login already exist.");
            res.redirect("/login");
        }

        req.session().attribute("login", login);
        req.session().attribute("password", password);
        res.redirect("/index");

        return "";
    }

    public String logout(Request req, Response res) {
        req.session().invalidate();
        res.redirect("/index");
        return "";
    }

    public ModelAndView search(Request req, Response res) {
        HashMap<String, Object> model = new HashMap<>();

        if (req.session().attributes().isEmpty()) {
            req.session().attribute("error", "Firstly, you have to log in before use PubLib.");
            res.redirect("/login");
        }

        model.put("template", "public/search.vtl");
        model.put("login", req.session().attribute("login"));
        return new ModelAndView(model, layout);
    }


}
