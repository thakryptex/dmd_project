import spark.template.velocity.VelocityTemplateEngine;

import java.sql.SQLException;

import static spark.Spark.*;

public class Starter {

//      get("/publications/:type/:number", pm::getPublication);

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        new Starter().run();

    }

    public void run() throws SQLException, ClassNotFoundException {

        staticFileLocation("/public");

        RequestWorker request = new RequestWorker();
        request.dbWorker.connectToDB();

        get("/", (req, res) -> {
            res.redirect("/index");
            return "Redirect to Main Page";
        });

        get("/index" , request::index , new VelocityTemplateEngine());
        get("/search", request::search, new VelocityTemplateEngine());

        get("/login", request::login   , new VelocityTemplateEngine());
        get("/register", request::register, new VelocityTemplateEngine());

        post("/authorize"   , request::authorize);
        post("/registration", request::registration);

        get("/logout", request::logout);
    }

}
