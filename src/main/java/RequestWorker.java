import database_mapDB.MappedDB;
import database_pgsql.DBWorker;
import database_pgsql.UsersTable;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class RequestWorker {

    public DBWorker dbsql = new DBWorker();
    public MappedDB dbWorker = new MappedDB();
    public HashMap<String, String> emptyMap = new HashMap<>();
    public String layout = "public/layout.vtl";

    public RequestWorker() {
    }

    public ModelAndView index(Request req, Response res) {
        HashMap<String, Object> model = new HashMap<>();
        model.put("template", "public/index.vtl");
        model.put("styles", "public/styles/index.vtl");
        model.put("login", req.session().attribute("login"));
        return new ModelAndView(model, layout);
    }

    public ModelAndView login(Request req, Response res) {
        HashMap<String, Object> model = new HashMap<>();

        req.session(true).maxInactiveInterval(60 * 60);
        if (!req.session().attributes().isEmpty())
            model.put("error", req.session().attribute("error"));

        model.put("template", "public/login.vtl");
        model.put("styles", "public/styles/login.vtl");
        return new ModelAndView(model, layout);
    }

    public String authorize(Request req, Response res) {
        req.session().removeAttribute("error");

        String[] args = req.body().split("&");
        String login = args[0].split("=")[1];
        String password = args[1].split("=")[1];

        try {
            UsersTable.findUser(login, password, dbsql.statement());
//            database_mapDB.UsersTable.findUser(login, password, dbWorker);
        } catch (NoSuchElementException e) {
            System.out.println(req.body());
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
        model.put("styles", "public/styles/register.vtl");
        return new ModelAndView(model, layout);
    }

    public String registration(Request req, Response res) {
        String[] args = req.body().split("&");
        String login = args[0].split("=")[1];
        String password = args[1].split("=")[1];

        try {
            UsersTable.addUser(login, password, dbsql.statement());
//            database_mapDB.UsersTable.addUser(login, password, dbWorker);
        } catch (Exception e) {
            req.session().attribute("error", "User with this login already exist.");
            res.redirect("/register");
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

        if (req.session().attribute("error") != null || req.session().attributes().isEmpty()) {
            req.session().attribute("error", "Firstly, you have to log in before use PubLib.");
            res.redirect("/login");
        } else {
            model.put("pubs", req.session().attribute("pubs"));
        }

        model.put("template", "public/search.vtl");
        model.put("styles", "public/styles/search.vtl");
        model.put("login", req.session().attribute("login"));
        return new ModelAndView(model, layout);
    }


    public String searching(Request req, Response res) {
        HashMap<String, Object> model = new HashMap<>();

        System.out.println(req.body());

        List<String> list = Arrays.asList(req.body().split("&"));

        list.forEach(p -> {
            String s = p.replace("+", " ");
            String[] attr = s.split("\\=");
            if (attr.length > 1)
                model.put(attr[0], attr[1]);
        });

        List<HashMap> results = null;
        try {
            results = dbWorker.search(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        req.session().attribute("pubs", results);
        res.redirect("/search");

        return "";
    }

    public ModelAndView moreinfo(Request req, Response res) {
        HashMap<String, Object> model = new HashMap<>();

        HashMap results = null;
        try {
            results = dbsql.getPubInfo(Integer.parseInt(req.params("pubid")));
//            results = dbWorker.getPubInfo(Integer.parseInt(req.params("pubid")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.put("pub", results);
        model.put("template", "public/publication.vtl");
        model.put("styles", "public/styles/publication.vtl");
        return new ModelAndView(model, layout);
    }

}
