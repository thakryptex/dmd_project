package database.XMLparser;

import java.util.ArrayList;

public class Publication {

    String type, mDate;
    ArrayList<String> person, editors;
    String title, booktitle;
    String month;
    int year, id;
    String journal, volume, pages, series, isbn, publisher;
    String note;
    String url, ee;
    String school;

    @Override
    public String toString(){
        String author = null;
        if (person != null) author = person.get(0);
        if (type == "article")
            return "==========" + "\nType: " + type + "\nAuthor: " + author + "\nTitle: " + title +
                    "\nJournal: " + journal + "\nYear: " + year + "\n==========";

        if (type == "inproceedings")
            return "==========" + "\nType: " + type + "\nAuthor: " + author + "\nTitle: " + title +
                    "\nPages: " + pages + "\nBook title: " + booktitle + "\nYear: " + year + "\n==========";

        if (type == "book")
            return "==========" + "\nType: " + type + "\nAuthor: " + author + "\nEditor: " + editors + "\nTitle: " + title +
                    "\nSeries: " + series + "\nPublisher: " + publisher + "\nYear: " + year + "\nISBN: " + isbn + "\n==========";

        if (type == "incollection")
            return "==========" + "\nType: " + type + "\nAuthor: " + author + "\nTitle: " + title +
                    "\nBook title: " + booktitle + "\nYear: " + year + "\n==========";


        return "==========" + "Unregistered type" + type + "\n==========";
    }


}
