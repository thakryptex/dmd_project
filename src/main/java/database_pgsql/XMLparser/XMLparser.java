package database_pgsql.XMLparser;

import database_pgsql.DBWorker;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class XMLparser {

    static int countPubs = 0;
    static Publication currPub = null;
    static String tagContent = "";
    static StringBuilder query = null;
    static DBWorker dbWorker = new DBWorker();

    public static void main(String[] args) throws IOException, XMLStreamException, SQLException, ClassNotFoundException {
        long start = System.currentTimeMillis();

        dbWorker.connectToDB();

        PubLibTables.deleteTables(dbWorker.statement());

        PubLibTables.createTables(dbWorker.statement());

        FileInputStream fis = new FileInputStream("dblp_part.xml");
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        XMLStreamReader reader = factory.createXMLStreamReader(fis);

        query = new StringBuilder();

        while(reader.hasNext()){
            if (countPubs > 1000000) break;
            int event = reader.next();
            switch(event){
                case XMLStreamConstants.START_ELEMENT:
                    String startElem = reader.getLocalName();
                    if (startElem.equals("article") || startElem.equals("inproceedings") || startElem.equals("book") || startElem.equals("incollection") || startElem.equals("proceedings") || startElem.equals("phdthesis") || startElem.equals("masterthesis")) {
                        if (!reader.getAttributeValue(1).contains("dblpnote")) {
                            currPub = new Publication();
                            currPub.mDate = reader.getAttributeValue(0);
                        }
                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                    if (tagContent != null && currPub != null)
                        tagContent += reader.getText().trim();
                    else
                        tagContent = reader.getText().trim();
                    if (tagContent.contains("'"))
                        tagContent = tagContent.replaceAll("'", "''");
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (currPub != null)
                        switcher(reader.getLocalName());
                    break;
            }
        }
        long finish = System.currentTimeMillis();
        System.out.println("\nTime: " + TimeUnit.MILLISECONDS.toSeconds(finish-start));

        dbWorker.disconnectFromDB();
    }

    private static void switcher(String endElement) throws SQLException {
        if (endElement.equals("article") || endElement.equals("inproceedings") || endElement.equals("book") || endElement.equals("incollection") || endElement.equals("proceedings") || endElement.equals("phdthesis") || endElement.equals("masterthesis")) {
            currPub.type = endElement;
            countPubs++;
//            if (countPubs > 82000)
                insertPubIntoDB(currPub);
            currPub = null;
            tagContent = null;
        }

        switch (endElement) {
            case "author":
                if (currPub.person == null) {
                    currPub.person = new ArrayList<>();
                }
                currPub.person.add(tagContent);
                tagContent = null;
                break;
            case "editor":
                if (currPub.editors == null) {
                    currPub.editors = new ArrayList<>();
                }
                currPub.editors.add(tagContent);
                tagContent = null;
                break;
            case "title":
                currPub.title = tagContent;
                tagContent = null;
                break;
            case "booktitle":
                currPub.booktitle = tagContent;
                tagContent = null;
                break;
            case "journal":
                currPub.journal = tagContent;
                tagContent = null;
                break;
            case "month":
                currPub.month = tagContent;
                tagContent = null;
                break;
            case "year":
                currPub.year = Integer.parseInt(tagContent);
                tagContent = null;
                break;
            case "volume":
                currPub.volume = tagContent;
                tagContent = null;
                break;
            case "pages":
                currPub.pages = tagContent;
                tagContent = null;
                break;
            case "series":
                currPub.series = tagContent;
                tagContent = null;
                break;
            case "publisher":
                currPub.publisher = tagContent;
                tagContent = null;
                break;
            case "isbn":
                currPub.isbn = tagContent;
                tagContent = null;
                break;
            case "note":
                currPub.note = tagContent;
                tagContent = null;
                break;
            case "url":
                currPub.url = tagContent;
                tagContent = null;
                break;
            case "ee":
                currPub.ee = tagContent;
                tagContent = null;
                break;
            case "school":
                currPub.school = tagContent;
                tagContent = null;
                break;
            case "crossref":
                tagContent = null;
                break;
            case "number":
                tagContent = null;
                break;
            case "cdrom":
                tagContent = null;
                break;
        }
    }

    private static void insertPubIntoDB(Publication pub) throws SQLException {
        //TODO concatenation here is bad
        //TODO concatenation here is bad (because inside StringBuilder I do String concatenations)
        //TODO concatenation here is bad
        query.append("insert into publication values (" + countPubs + ", '" + pub.title + "', " + pub.year + ", '" + pub.mDate + "'");
        if (pub.ee != null) {
            query.append(", '" + pub.ee + "'");
        } else query.append(", null");
        if (pub.note != null) {
            query.append(", '" + pub.note + "'");
        } else query.append(", null");
        if (pub.url != null) {
            query.append(", '" + pub.url + "'");
        } else query.append(", null");
        query.append(");\n");

        switch (pub.type) {
            case "article":
                query.append("insert into journal values ('" + pub.journal + "');\n");
                query.append("insert into article values (" + countPubs + ", ");
                if (pub.month != null) {
                    query.append("'" + pub.month + "', '" + pub.journal +"', ");
                } else query.append("null, '" + pub.journal +"', ");
                if (pub.volume != null) {
                    query.append("'" + pub.volume + "');\n");
                } else query.append("null);\n");
                break;
            case "inproceedings":
                query.append("insert into inproceeding values (" + countPubs + ", ");
                if (pub.month != null) {
                    query.append("'" + pub.month + "', ");
                } else query.append("null, ");
                if (pub.booktitle != null) {
                    query.append("'" + pub.booktitle + "');\n");
                } else query.append("null);\n");
                break;
            case "book":
                query.append("insert into publisher values ('" + pub.publisher + "');\n");
                query.append("insert into book values (" + countPubs + ", '" + pub.publisher + "', '" + pub.isbn + "', ");
                if (pub.series != null) {
                    query.append("'" + pub.series + "');\n");
                } else query.append("null);\n");
                break;
            case "incollection":
                query.append("insert into incollection values (" + countPubs + ", '" + pub.booktitle + "', ");
                if (pub.pages != null) {
                    query.append("'" + pub.pages + "');\n");
                } else query.append("null);");
                break;
            case "proceedings":
                query.append("insert into publisher values ('" + pub.publisher + "');\n");
                query.append("insert into proceedings values (" + countPubs + ", '" + pub.booktitle + "', '" + pub.publisher + "', '" + pub.isbn + "', ");
                if (pub.series != null) {
                    query.append("'" + pub.series + "', ");
                } else query.append("null, ");
                if (pub.series != null) {
                    query.append("'" + pub.series + "');\n");
                } else query.append("null);\n");
                break;
            case "phdthesis":
                query.append("insert into phdthesis values (" + countPubs + ", ");
                if (pub.school != null) {
                    query.append("'" + pub.pages + "', ");
                } else query.append("null, ");
                if (pub.pages != null) {
                    query.append("'" + pub.pages + "', ");
                } else query.append("null, ");
                if (pub.series != null) {
                    query.append("'" + pub.series + "', ");
                } else query.append("null, ");
                if (pub.volume != null) {
                    query.append("'" + pub.volume + "');\n");
                } else query.append("null);\n");
                break;
            case "masterthesis":
                query.append("insert into masterthesis values (" + countPubs + ", ");
                if (pub.school != null) {
                    query.append("'" + pub.pages + "', ");
                } else query.append("null, ");
                if (pub.pages != null) {
                    query.append("'" + pub.pages + "', ");
                } else query.append("null, ");
                if (pub.series != null) {
                    query.append("'" + pub.series + "', ");
                } else query.append("null, ");
                if (pub.volume != null) {
                    query.append("'" + pub.volume + "');\n");
                } else query.append("null);\n");
                break;
        }
        if (pub.person != null) {
            for (int i = 0; i < pub.person.size(); i++) {
                query.append("insert into person values ('" + pub.person.get(i) + "', null);\n");
                query.append("insert into written values (" + countPubs + ", '" + pub.person.get(i) + "');\n");
            }
        }

        if (pub.editors != null) {
            for (int i = 0; i < pub.editors.size(); i++) {
                query.append("insert into person values ('" + pub.editors.get(i) + "', null);\n");
                query.append("insert into edited values (" + countPubs + ", '" + pub.editors.get(i) + "');\n");
            }
        }

        System.out.println(countPubs);
//        System.out.println(query.toString());
        if (countPubs % 100 == 0) {
            dbWorker.executeUpdate(query.toString());
            query.setLength(0);
        }
    }
/**
    ?? ????? ??????????  - 35 sec    - 58,3 mins
    ?? 100 ??????????    - 14 sec    - 23,3 mins - 2561 sec (real)
    ?? 300 ??????????    - 13 sec    - 21,6 mins
    ?? 500 ??????????    - 13 sec    - 21,6 mins
    ?? 1000 ??????????   - 8-12 sec  - 13,3 - 20 mins
 */

}
