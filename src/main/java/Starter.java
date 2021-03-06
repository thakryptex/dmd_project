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
        request.dbsql.connectToDB();
        request.dbWorker.connectToDB("publib.db");

        get("/", (req, res) -> {
            res.redirect("/index");
            return "Redirect to Main Page";
        });

        get("/index"     , request::index , new VelocityTemplateEngine());
        get("/search"    , request::search, new VelocityTemplateEngine());
        post("/searching", request::searching);

        get("/login"     , request::login   , new VelocityTemplateEngine());
        get("/logout"    , request::logout);
        post("/authorize", request::authorize);

        get("/register"     , request::register, new VelocityTemplateEngine());
        post("/registration", request::registration);

        get("/publication/:pubid", request::moreinfo, new VelocityTemplateEngine());

    }

}
