package database.XMLparser;

import java.util.ArrayList;

public class Publication {

    String pubType, mDate;
    ArrayList<String> authors, editors;
    String title, booktitle;
    String month;
    int year;
    String journal, volume, pages, series, isbn, publisher;
    String note;
    String url, ee;
    String school;

    @Override
    public String toString(){
        String author = null;
        if (authors != null) author = authors.get(0);
        if (pubType == "article")
            return "==========" + "\nType: " + pubType + "\nAuthor: " + author + "\nTitle: " + title +
                    "\nJournal: " + journal + "\nYear: " + year + "\n==========";

        if (pubType == "inproceedings")
            return "==========" + "\nType: " + pubType + "\nAuthor: " + author + "\nTitle: " + title +
                    "\nPages: " + pages + "\nBook title: " + booktitle + "\nYear: " + year + "\n==========";

        if (pubType == "book")
            return "==========" + "\nType: " + pubType + "\nAuthor: " + author + "\nEditor: " + editors + "\nTitle: " + title +
                    "\nSeries: " + series + "\nPublisher: " + publisher + "\nYear: " + year + "\nISBN: " + isbn + "\n==========";

        if (pubType == "incollection")
            return "==========" + "\nType: " + pubType + "\nAuthor: " + author + "\nTitle: " + title +
                    "\nBook title: " + booktitle + "\nYear: " + year + "\n==========";


        return "==========" + "Unregistered type" + pubType + "\n==========";
    }


}
